document.addEventListener('DOMContentLoaded', function () {

    // Add view type change handler
    document.querySelectorAll('input[name="viewType"]').forEach(radio => {
        radio.addEventListener('change', function () {
            if (this.id === 'propertiesView') {
                // Hide all filters when properties view is selected
                document.getElementById('propertyStatusFilter').style.display = 'none';
                document.getElementById('applicationStatusFilter').style.display = 'none';
                document.getElementById('visitStatusFilter').style.display = 'none';
            } else {
                document.getElementById('applicationStatusFilter').style.display = 'block';
                document.getElementById('visitStatusFilter').style.display = 'block';
                document.getElementById('propertyStatusFilter').style.display = 'none';
            }
            updateDashboard();
        });
    });

    document.querySelectorAll('#applicationStatusFilter input[type="checkbox"]').forEach(cb => {
        cb.checked = true;
    });
    document.querySelectorAll('#visitStatusFilter input[type="checkbox"]').forEach(cb => {
        cb.checked = true;
    });

    document.querySelectorAll('.filter-section input[type="checkbox"]').forEach(checkbox => {
        checkbox.addEventListener('change', updateDashboard);
    });
    const urlParams = new URLSearchParams(window.location.search);
    const view = urlParams.get('view');
    if (view === 'properties') {
        const propRadio = document.getElementById('propertiesView');
        if (propRadio) {
            propRadio.checked = true;
            propRadio.dispatchEvent(new Event('change'));
        }
    }
    updateDashboard();
});

function updateDashboard() {
    const dashboardContent = document.getElementById('dashboardContent');
    if (!dashboardContent) {
        console.error('Dashboard content element not found');
        return;
    }

    // Show loading state
    dashboardContent.innerHTML = `
        <div class="d-flex justify-content-center p-4">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
        </div>
    `;

    // Get selected view type
    const selectedView = document.querySelector('input[name="viewType"]:checked');
    if (!selectedView) {
        console.error('No view type selected');
        return;
    }
    const requestedView = selectedView.id;
    const filterType = 'all';

    let filters = {
        viewType: requestedView === 'alertsView' ? 'alerts' : 'properties'
    };

    if (requestedView === 'alertsView') {
        const applicationStatuses = Array.from(document.querySelectorAll('#applicationStatusFilter input:checked'))
            .map(cb => cb.value.toUpperCase());
        const visitStatuses = Array.from(document.querySelectorAll('#visitStatusFilter input:checked'))
            .map(cb => cb.value.toUpperCase());
        if (applicationStatuses.length > 0) {
            filters.applicationStatus = applicationStatuses;
        }
        if (visitStatuses.length > 0) {
            filters.visitStatus = visitStatuses;
        }
        filters.filterType = filterType;
    }

    console.log('Sending filters:', filters);

    // Make API call to fetch dashboard data
    fetch('/api/landlords/dashboard', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(filters)
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Failed to load dashboard data');
                });
            }
            return response.json();
        })
        .then(data => {
            if (selectedView.id === 'alertsView') {
                const alerts = data.alerts || [];
                let alertsHtml = '<div class="alerts-container">';
                if (alerts.length === 0) {
                    alertsHtml = `
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle"></i> No alerts found.
                        </div>
                    `;
                } else {
                    // Sort alerts by timestamp descending
                    const sortedAlerts = [...alerts].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
                    const filteredAlerts = sortedAlerts.filter(alert => {
                        if (alert.type === 'application' && filters.applicationStatus) {
                            return filters.applicationStatus.includes(alert.status);
                        }
                        if (alert.type === 'visit' && filters.visitStatus) {
                            return filters.visitStatus.includes(alert.status);
                        }
                        return !filters.applicationStatus && !filters.visitStatus;
                    });
                    filteredAlerts.forEach(alert => {
                        const status = alert.status.toLowerCase();
                        const icon = alert.type === 'application' ? 'file-alt' : 'calendar-check';

                        alertsHtml += `
                            <div class="alert alert-${status} d-flex align-items-start">
                                <div class="flex-shrink-0">
                                    <i class="fas fa-${icon} alert-icon"></i>
                                </div>
                                <div class="flex-grow-1">
                                    <h5 class="alert-heading">${alert.message}</h5>
                                    <p class="property-address mb-1">
                                        <i class="fas fa-building"></i> ${alert.propertyAddress}
                                    </p>
                                    <small class="timestamp d-flex align-items-center">
                                        <i class="fas fa-clock"></i> ${new Date(alert.timestamp).toLocaleDateString()}
                                    </small>
                                    ${alert.tenantName ? `
                                    <small class="tenant-info d-flex align-items-center mt-1">
                                        <i class="fas fa-${alert.isVerified ? 'check-circle' : 'times-circle'} me-2"></i>
                                        <span class="tenant-badge">
                                            ${alert.tenantName}
                                        </span>
                                    </small>` : ''}
                                    ${alert.type === 'application' && alert.monthlyIncome ? `
                                    <small class="monthly-income d-flex align-items-center mt-1">
                                        <i class="fas fa-euro-sign me-1"></i> Monthly Income: ${alert.monthlyIncome.toLocaleString()}
                                    </small>` : ''}
                                </div>
                                <div class="ms-3 d-flex flex-column align-items-end">
                                    <span class="badge bg-${getStatusColor(alert.status)} mb-2">${alert.status}</span>
                                    ${(alert.status === 'PENDING' || (alert.type === 'visit' && alert.status === 'REQUESTED')) ? `
                                        <div class="btn-group btn-group-sm" role="group">
                                            ${alert.type === 'application' ? `
                                                <button type="button" class="btn btn-success" onclick="handleApplicationAction(${alert.id}, 'APPROVED', this)">
                                                    <i class="fas fa-check"></i> Approve
                                                </button>
                                                <button type="button" class="btn btn-danger" onclick="handleApplicationAction(${alert.id}, 'REJECTED', this)">
                                                    <i class="fas fa-times"></i> Reject
                                                </button>
                                            ` : alert.status === 'REQUESTED' ? `
                                                <button type="button" class="btn btn-success" onclick="handleVisitAction(${alert.id}, 'APPROVE', this)">
                                                    <i class="fas fa-calendar-check"></i> Schedule
                                                </button>
                                                <button type="button" class="btn btn-danger" onclick="handleVisitAction(${alert.id}, 'CANCEL', this)">
                                                    <i class="fas fa-times"></i> Decline
                                                </button>
                                            ` : `
                                                <button type="button" class="btn btn-success" onclick="handleVisitAction(${alert.id}, 'APPROVE', this)">
                                                    <i class="fas fa-check"></i> Approve
                                                </button>
                                                <button type="button" class="btn btn-danger" onclick="handleVisitAction(${alert.id}, 'CANCEL', this)">
                                                    <i class="fas fa-times"></i> Cancel
                                                </button>
                                            `}
                                        </div>
                                    ` : ''}
                                </div>
                            </div>
                        `;
                    });
                }
                alertsHtml += '</div>';
                dashboardContent.innerHTML = alertsHtml;
            } else {
                // Display properties view
                const properties = data.properties || [];
                if (properties.length === 0) {
                    dashboardContent.innerHTML = `
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle"></i> You haven't added any properties yet.
                            Click the "Add Property" button to get started.
                        </div>
                    `;
                } else {
                    let propertiesHtml = '<div class="row">';
                    properties.forEach(property => {
                        const statusClass = property.status === 'RENTED' ? 'success' : 'warning';
                        const statusBadge = `<span class="badge bg-${statusClass} me-1">${property.status}</span>`;

                        propertiesHtml += `
                            <div class="col-md-4 mb-4">
                                <div class="card property-card">
                                    <div class="card-body">
                                        <div class="d-flex justify-content-between align-items-start mb-2">
                                            <h5 class="card-title">${property.address}</h5>
                                            <div>${statusBadge}</div>
                                        </div>
                                        <p class="card-text">
                                            <i class="fas fa-home"></i> ${property.type}<br>
                                            <i class="fas fa-euro-sign"></i> ${property.rentAmount}/month<br>
                                            <i class="fas fa-bed"></i> ${property.bedrooms} bedrooms<br>
                                            <i class="fas fa-bath"></i> ${property.bathrooms} bathrooms<br>
                                            <i class="fas fa-map-marker-alt"></i> ${property.country}
                                        </p>
                                        <div class="mt-3">
                                            <button class="btn btn-outline-primary btn-sm me-2" onclick="editProperty(${property.id})">
                                                <i class="fas fa-edit"></i> Edit
                                            </button>
                                            <button class="btn btn-outline-danger btn-sm" onclick="deleteProperty(${property.id})">
                                                <i class="fas fa-trash"></i> Delete
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        `;
                    });
                    propertiesHtml += '</div>';
                    dashboardContent.innerHTML = propertiesHtml;
                }
            }
        })
        .catch(error => {
            console.error('Error:', error);
            dashboardContent.innerHTML = `
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-circle"></i> ${error.message}. Please try again.
                </div>
            `;
        });
}

function submitProperty(form) {
    const formData = new FormData(form);
    const propertyData = {
        address: formData.get('address'),
        city: formData.get('city'),
        postalCode: formData.get('postalCode'),
        country: formData.get('country'),
        bedrooms: parseInt(formData.get('bedrooms')),
        bathrooms: parseInt(formData.get('bathrooms')),
        rentAmount: parseFloat(formData.get('rentAmount')),
        sizeInSquareMeters: parseFloat(formData.get('sizeInSquareMeters')),
        type: formData.get('type'),
        description: formData.get('description'),
        hasParking: formData.get('hasParking') === 'on',
        allowsPets: formData.get('allowsPets') === 'on',
        hasGarden: formData.get('hasGarden') === 'on',
        hasBalcony: formData.get('hasBalcony') === 'on',
        isRented: false,
        isApproved: false,
        creationDate: new Date().toISOString()
    };

    // Prepare headers
    const headers = {
        'Content-Type': 'application/json'
    };

    // Add CSRF token if available
    const token = document.head.querySelector('meta[name="_csrf"]');
    const header = document.head.querySelector('meta[name="_csrf_header"]');
    if (token && header) {
        headers[header.content] = token.content;
    }

    fetch('/property/create', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(propertyData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to add property');
            }
            // Close modal and reset form
            $('#addPropertyModal').modal('hide');
            form.reset();
            // Refresh dashboard to show new property
            refreshDashboard();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to add property: ' + error.message);
        });

    return false;
}

function editProperty(propertyId) {
    fetch(`/property/${propertyId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load property details');
            }
            return response.json();
        })
        .then(property => {
            // Fill in all form fields
            document.getElementById('editAddress').value = property.address;
            document.getElementById('editCity').value = property.city;
            document.getElementById('editPostalCode').value = property.postalCode;
            document.getElementById('editCountry').value = property.country;
            document.getElementById('editBedrooms').value = property.bedrooms;
            document.getElementById('editBathrooms').value = property.bathrooms;
            document.getElementById('editRentAmount').value = property.rentAmount;
            document.getElementById('editSize').value = property.sizeInSquareMeters;
            document.getElementById('editType').value = property.type;
            document.getElementById('editDescription').value = property.description;
            document.getElementById('editHasParking').checked = property.hasParking;
            document.getElementById('editAllowsPets').checked = property.allowsPets;
            document.getElementById('editHasGarden').checked = property.hasGarden;
            document.getElementById('editHasBalcony').checked = property.hasBalcony;
            document.getElementById('editPropertyId').value = propertyId;

            // Show modal
            new bootstrap.Modal(document.getElementById('editPropertyModal')).show();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to load property details: ' + error.message);
        });
}

function updateProperty(form) {
    const formData = new FormData(form);
    const propertyId = document.getElementById('editPropertyId').value;
    const propertyData = {
        address: formData.get('address'),
        city: formData.get('city'),
        postalCode: formData.get('postalCode'),
        country: formData.get('country'),
        bedrooms: parseInt(formData.get('bedrooms')),
        bathrooms: parseInt(formData.get('bathrooms')),
        rentAmount: parseFloat(formData.get('rentAmount')),
        sizeInSquareMeters: parseFloat(formData.get('sizeInSquareMeters')),
        type: formData.get('type'),
        description: formData.get('description'),
        hasParking: formData.get('hasParking') === 'on',
        allowsPets: formData.get('allowsPets') === 'on',
        hasGarden: formData.get('hasGarden') === 'on',
        hasBalcony: formData.get('hasBalcony') === 'on'
    };

    const headers = {
        'Content-Type': 'application/json'
    };

    const token = document.head.querySelector('meta[name="_csrf"]');
    const header = document.head.querySelector('meta[name="_csrf_header"]');
    if (token && header) {
        headers[header.content] = token.content;
    }

    fetch(`/property/${propertyId}`, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify(propertyData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to update property');
            }
            // Close modal and refresh dashboard
            bootstrap.Modal.getInstance(document.getElementById('editPropertyModal')).hide();
            updateDashboard();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to update property: ' + error.message);
        });

    return false;
}

function deleteProperty(propertyId) {
    if (confirm('Are you sure you want to delete this property?')) {
        const headers = {
            'Content-Type': 'application/json'
        };

        const token = document.head.querySelector('meta[name="_csrf"]');
        const header = document.head.querySelector('meta[name="_csrf_header"]');
        if (token && header) {
            headers[header.content] = token.content;
        }

        fetch(`/property/${propertyId}`, {
            method: 'DELETE',
            headers: headers
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text || 'Failed to delete property');
                    });
                }
                updateDashboard();
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Failed to delete property: ' + error.message);
            });
    }
}

function refreshDashboard() {
    location.reload();
}

function getStatusColor(status) {
    switch (status.toUpperCase()) {
        case 'APPROVED':
        case 'COMPLETED':
            return 'success';
        case 'REJECTED':
        case 'DECLINED':
        case 'CANCELED':
            return 'danger';
        case 'PENDING':
            return 'warning';
        case 'REQUESTED':
            return 'info';
        case 'SCHEDULED':
            return 'primary';
        default:
            return 'secondary';
    }
}

function handleApplicationAction(applicationId, action) {
    const token = document.head.querySelector('meta[name="_csrf"]');
    const header = document.head.querySelector('meta[name="_csrf_header"]');

    const headers = {
        'Content-Type': 'application/json'
    };

    if (token && header) {
        headers[header.content] = token.content;
    }

    const status = action.toUpperCase();

    fetch(`/api/rental-applications/${applicationId}/status`, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify(status)
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Failed to process application action');
                });
            }
            updateDashboard();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to process application: ' + error.message);
        });
}

function handleVisitAction(visitId, action) {
    const token = document.head.querySelector('meta[name="_csrf"]');
    const header = document.head.querySelector('meta[name="_csrf_header"]');

    const headers = {
        'Content-Type': 'application/json'
    };

    if (token && header) {
        headers[header.content] = token.content;
    }

    fetch(`/api/visits/${visitId}/update-status`, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify({ action: action })
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    let error;
                    try {
                        const jsonError = JSON.parse(text);
                        error = jsonError.error || 'Failed to process visit action';
                    } catch {
                        error = text || 'Failed to process visit action';
                    }
                    throw new Error(error);
                });
            }
            updateDashboard();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to process visit: ' + error.message);
        });
}

