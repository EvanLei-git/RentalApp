document.addEventListener('DOMContentLoaded', function () {
    // Function to check all checkboxes in a container
    function checkAllFilters(container) {
        if (container) {
            container.querySelectorAll('input[type="checkbox"]').forEach(checkbox => {
                checkbox.checked = true;
            });
        }
    }

    // Function to show all filters and check all checkboxes
    function showAndCheckAllFilters() {
        applicationFilter.style.display = 'block';
        visitFilter.style.display = 'block';
        checkAllFilters(applicationFilter);
        checkAllFilters(visitFilter);
    }

    const filterButtons = document.querySelectorAll('input[name="filterType"]');
    const applicationFilter = document.getElementById('applicationStatusFilter');
    const visitFilter = document.getElementById('visitStatusFilter');

    // Add event listeners
    filterButtons.forEach(button => {
        button.addEventListener('change', function () {
            const selectedFilter = this.id;

            if (selectedFilter === 'all') {
                showAndCheckAllFilters();
            } else if (selectedFilter === 'applications') {
                applicationFilter.style.display = 'block';
                visitFilter.style.display = 'none';
                checkAllFilters(applicationFilter);
            } else if (selectedFilter === 'visits') {
                applicationFilter.style.display = 'none';
                visitFilter.style.display = 'block';
                checkAllFilters(visitFilter);
            }

            updateDashboard();
        });
    });

    // Add event listeners to filters and date inputs
    [applicationFilter, visitFilter].forEach(filter => {
        if (filter) {
            filter.querySelectorAll('input[type="checkbox"]').forEach(checkbox => {
                checkbox.addEventListener('change', updateDashboard);
            });
        }
    });


    async function updateDashboard() {
        const dashboardContent = document.getElementById('dashboardContent');
        dashboardContent.innerHTML = `
                        <div class="loading-spinner">
                            <div class="spinner-border text-primary" role="status">
                                <span class="visually-hidden">Loading...</span>
                            </div>
                        </div>`;

        const selectedFilter = 'all';
        const applicationStatuses = Array.from(document.querySelectorAll('#applicationStatusFilter input:checked')).map(cb => cb.value);
        const visitStatuses = Array.from(document.querySelectorAll('#visitStatusFilter input:checked')).map(cb => cb.value);

        try {
            let endpoint = '/api/tenant/dashboard';
            if (selectedFilter === 'applications') {
                endpoint = '/api/tenant/applications';
            } else if (selectedFilter === 'visits') {
                endpoint = '/api/tenant/visits';
            }
            const params = new URLSearchParams({
                applicationStatuses: applicationStatuses.length > 0 ? applicationStatuses.join(',') : 'NONE',
                visitStatuses: visitStatuses.length > 0 ? visitStatuses.join(',') : 'NONE'
            });

            const response = await fetch(endpoint + '?' + params.toString(), {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });

            const responseData = await response.text();
            let data;
            try {
                data = JSON.parse(responseData);
            } catch (e) {
                throw new Error(responseData || 'Invalid response format');
            }

            if (!response.ok) {
                throw new Error(data.message || data || `HTTP error! status: ${response.status}`);
            }

            let html = '';

            if ((!data.applications || data.applications.length === 0) &&
                (!data.visits || data.visits.length === 0)) {
                html = `
                                <div class="alert alert-info" role="alert">
                                    <i class="fas fa-info-circle"></i> No items found.
                                </div>`;
            } else {
                if (selectedFilter === 'all') {
                    // Combine and sort applications and visits by date
                    const timeline = [];
                    if (data.applications) {
                        timeline.push(...data.applications.map(app => ({
                            type: 'application',
                            date: new Date(app.applicationDate),
                            data: app
                        })));
                    }
                    if (data.visits) {
                        timeline.push(...data.visits.map(visit => ({
                            type: 'visit',
                            date: new Date(visit.visitDate),
                            data: visit
                        })));
                    }

                    // Sort by date, most recent first
                    timeline.sort((a, b) => b.date - a.date);

                    html = '<div class="timeline">';
                    timeline.forEach(item => {
                        if (item.type === 'application') {
                            html += generateApplicationHtml(item.data);
                        } else {
                            html += generateVisitHtml(item.data);
                        }
                    });
                    html += '</div>';
                } else if (selectedFilter === 'applications') {
                    if (data.applications && data.applications.length > 0) {
                        html = '<div class="applications-list">';
                        data.applications.forEach(app => {
                            html += generateApplicationHtml(app);
                        });
                        html += '</div>';
                    } else {
                        html = `
                                        <div class="alert alert-info" role="alert">
                                            <i class="fas fa-info-circle"></i> No applications found.
                                        </div>`;
                    }
                } else if (selectedFilter === 'visits') {
                    if (data.visits && data.visits.length > 0) {
                        html = '<div class="visits-list">';
                        data.visits.forEach(visit => {
                            html += generateVisitHtml(visit);
                        });
                        html += '</div>';
                    } else {
                        html = `
                                        <div class="alert alert-info" role="alert">
                                            <i class="fas fa-info-circle"></i> No visits found.
                                        </div>`;
                    }
                }
            }

            dashboardContent.innerHTML = html;
        } catch (error) {
            console.error('Error:', error);
            dashboardContent.innerHTML = `
                            <div class="alert alert-danger" role="alert">
                                <i class="fas fa-exclamation-circle"></i> ${error.message}
                            </div>`;
            throw error; // Re-throw to handle in the refresh button
        }
    }

    function generateApplicationHtml(application) {
        const statusColor = getStatusColor(application.status);
        return `
                        <div class="card dashboard-item mb-3">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div class="d-flex align-items-center">
                                        <h5 class="card-title mb-0">${application.property.address}</h5>
                                    </div>
                                    <span class="badge ${statusColor}">${application.status}</span>
                                </div>
                                <div class="property-details mt-2">
                                    <p class="mb-1">
                                        <i class="fas fa-file-alt alert-icon"></i> Rental Application Applied on ${formatDate(application.applicationDate)}
                                    </p>
                                    <p class="mb-1">
                                        <i class="fas fa-home"></i> ${application.property.type}, 
                                        ${application.property.bedrooms} bed, ${application.property.bathrooms} bath
                                    </p>
                                    <p class="mb-0">
                                        <i class="fas fa-map-marker-alt"></i> ${application.property.city}, ${application.property.country}
                                    </p>
                                </div>
                            </div>
                        </div>`;
    }

    function generateVisitHtml(visit) {
        const statusColor = getStatusColor(visit.visitStatus);
        return `
                        <div class="card dashboard-item mb-3">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div class="d-flex align-items-center">
                                        <h5 class="card-title mb-0">${visit.property.address}</h5>
                                    </div>
                                    <span class="badge ${statusColor}">${visit.visitStatus}</span>
                                </div>
                                <div class="property-details mt-2">
                                    <p class="mb-1">
                                        <i class="fas fa-calendar-check alert-icon"></i> Visit scheduled for ${formatDate(visit.visitDate)}
                                    </p>
                                    <p class="mb-1">
                                        <i class="fas fa-home"></i> ${visit.property.type}, 
                                        ${visit.property.bedrooms} bed, ${visit.property.bathrooms} bath
                                    </p>
                                    <p class="mb-0">
                                        <i class="fas fa-map-marker-alt"></i> ${visit.property.city}, ${visit.property.country}
                                    </p>
                                </div>
                            </div>
                        </div>`;
    }

    function getStatusColor(status) {
        switch (status) {
            case 'PENDING':
                return 'bg-warning text-dark';
            case 'REQUESTED':
                return 'bg-primary';
            case 'APPROVED':
            case 'ACCEPTED':
                return 'bg-success';
            case 'REJECTED':
            case 'CANCELLED':
                return 'bg-danger';
            case 'COMPLETED':
                return 'bg-info';
            case 'SCHEDULED':
                return 'bg-info';
            default:
                return 'bg-secondary';
        }
    }

    function formatDate(dateString) {
        const options = {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        };
        return new Date(dateString).toLocaleDateString('en-US', options);
    }



    document.querySelectorAll('#applicationStatusFilter input, #visitStatusFilter input').forEach(checkbox => {
        checkbox.addEventListener('change', updateDashboard);
    });



    // Initial dashboard update
    window.addEventListener('load', () => {
        setTimeout(updateDashboard, 100); // Small delay to ensure DOM is ready
    });
});