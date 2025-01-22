function updateDashboard() {
    const view = document.querySelector('input[name="viewType"]:checked').id;

    // Hide all filter sections and content
    document.getElementById('usersFilterSection').style.display = 'none';
    document.getElementById('propertiesFilterSection').style.display = 'none';
    document.getElementById('reportsFilterSection').style.display = 'none';
    document.getElementById('usersTable').style.display = 'none';
    document.getElementById('propertiesTable').style.display = 'none';
    document.getElementById('reportsContent').style.display = 'none';

    if (view === 'usersView') {
        document.getElementById('usersFilterSection').style.display = 'block';
        document.getElementById('usersTable').style.display = 'block';
        fetchUsers();
    } else if (view === 'propertiesView') {
        document.getElementById('propertiesFilterSection').style.display = 'block';
        document.getElementById('propertiesTable').style.display = 'block';
        fetchProperties();
    } else if (view === 'reportsView') {
        document.getElementById('reportsFilterSection').style.display = 'block';
        document.getElementById('reportsContent').style.display = 'block';
        loadReports();
    }
}

function fetchUsers() {
    console.log('Fetching users...');
    const tableBody = document.getElementById('usersTableBody');

    // Get filter values
    const username = document.getElementById('userSearch').value;
    const verifiedChecked = document.getElementById('statusVerified').checked;
    const unverifiedChecked = document.getElementById('statusUnverified').checked;
    const landlordChecked = document.getElementById('typeLandlord').checked;
    const tenantChecked = document.getElementById('typeTenant').checked;

    // Show loading state
    tableBody.innerHTML = `
                    <tr>
                        <td colspan="6" class="text-center">
                            <div class="spinner-border text-primary" role="status">
                                <span class="visually-hidden">Loading...</span>
                            </div>
                        </td>
                    </tr>
                `;

    // Build query parameters
    const params = new URLSearchParams();
    if (username) params.append('username', username);

    // Handle role filter
    if (landlordChecked && !tenantChecked) {
        params.append('role', 'LANDLORD');
    } else if (tenantChecked && !landlordChecked) {
        params.append('role', 'TENANT');
    }

    // Handle verification filter
    if (verifiedChecked && !unverifiedChecked) {
        params.append('verified', true);
    } else if (unverifiedChecked && !verifiedChecked) {
        params.append('verified', false);
    }

    fetch(`/api/administrators/users?${params.toString()}`)
        .then(response => response.json())
        .then(users => {
            console.log('Received users:', users);
            tableBody.innerHTML = '';

            users.forEach(user => {
                const roleValue = user.roles && user.roles[0] ? user.roles[0] : '';
                const verifiedStatus = user.verified ?
                    '<span class="badge bg-success"><i class="fas fa-check-circle"></i> Verified</span>' :
                    '<span class="badge bg-warning"><i class="fas fa-clock"></i> Pending</span>';

                const row = document.createElement('tr');
                row.innerHTML = `
                                <td>${user.id || ''}</td>
                                <td>${user.username || ''}</td>
                                <td>${user.firstName || ''} ${user.lastName || ''}</td>
                                <td>${user.email || ''}</td>
                                <td>
                                    <span class="badge ${getBadgeClass(roleValue)}">${roleValue}</span>
                                    ${verifiedStatus}
                                </td>
                                <td>
                                    <button class="btn btn-sm btn-outline-primary" onclick="viewUser(${user.id})" title="View User">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                    ${roleValue !== 'ADMINISTRATOR' ?
                    `<button class="btn btn-sm btn-outline-danger ms-1" onclick="deleteUser(${user.id})" title="Delete User">
                                            <i class="fas fa-trash"></i>
                                        </button>` : ''
                }
                                    ${!user.verified && (roleValue === 'TENANT' || roleValue === 'LANDLORD') ?
                    `<button class="btn btn-sm btn-outline-success ms-1" onclick="verifyUser(${user.id})" title="Verify User">
                                            <i class="fas fa-check"></i>
                                        </button>` : ''
                }
                                </td>
                            `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            tableBody.innerHTML = `
                            <tr>
                                <td colspan="6" class="text-center text-danger">
                                    <i class="fas fa-exclamation-circle"></i> Error loading users: ${error.message}
                                </td>
                            </tr>
                        `;
        });
}

// Add event listeners for filters
document.getElementById('userSearch').addEventListener('input', debounce(fetchUsers, 300));
document.getElementById('statusVerified').addEventListener('change', fetchUsers);
document.getElementById('statusUnverified').addEventListener('change', fetchUsers);
document.getElementById('typeLandlord').addEventListener('change', fetchUsers);
document.getElementById('typeTenant').addEventListener('change', fetchUsers);

// Debounce function to prevent too many API calls
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function getBadgeClass(role) {
    switch (role) {
        case 'TENANT':
            return 'bg-info';
        case 'LANDLORD':
            return 'bg-success';
        case 'ADMINISTRATOR':
            return 'bg-primary';
        default:
            return 'bg-secondary';
    }
}

function viewUser(userId) {
    fetch(`/api/administrators/users/${userId}/details`)
        .then(response => response.json())
        .then(user => {
            // Create modal if it doesn't exist
            let modalHtml = `
                            <div class="modal fade" id="userDetailsModal" tabindex="-1">
                                <div class="modal-dialog modal-lg">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h5 class="modal-title">User Details</h5>
                                        </div>
                                        <div class="modal-body">
                                            <div class="row">
                                                <div class="col-md-6">
                                                    <h6>Basic Information</h6>
                                                    <table class="table">
                                                        <tr>
                                                            <th>Username:</th>
                                                            <td>${user.username || 'N/A'}</td>
                                                        </tr>
                                                        <tr>
                                                            <th>Full Name:</th>
                                                            <td>${user.firstName || ''} ${user.lastName || ''}</td>
                                                        </tr>
                                                        <tr>
                                                            <th>Email:</th>
                                                            <td>${user.email || 'N/A'}</td>
                                                        </tr>
                                                        <tr>
                                                            <th>Role:</th>
                                                            <td>${user.roles ? user.roles.join(', ') : 'N/A'}</td>
                                                        </tr>
                                                    </table>
                                                </div>
                                                ${user.monthlyIncome ? `
                                                <div class="col-md-6">
                                                    <h6>Tenant Information</h6>
                                                    <table class="table">
                                                        <tr>
                                                            <th>Monthly Income:</th>
                                                            <td>€${user.monthlyIncome}</td>
                                                        </tr>
                                                        <tr>
                                                            <th>Employment Status:</th>
                                                            <td>${user.employmentStatus || 'N/A'}</td>
                                                        </tr>
                                                    </table>
                                                </div>` : ''}
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        `;

            // Remove existing modal if any
            let existingModal = document.getElementById('userDetailsModal');
            if (existingModal) {
                existingModal.remove();
            }

            // Add new modal to body
            document.body.insertAdjacentHTML('beforeend', modalHtml);

            // Show modal
            let modal = new bootstrap.Modal(document.getElementById('userDetailsModal'));
            modal.show();

            // Load ID images if tenant
            if (user.monthlyIncome && (user.idPhotoFront || user.idPhotoBack)) {
                setTimeout(() => {
                    if (user.idPhotoFront) {
                        document.getElementById('frontId').src = `data:image/jpeg;base64,${user.idPhotoFront}`;
                    }
                }, 500);

                setTimeout(() => {
                    if (user.idPhotoBack) {
                        document.getElementById('backId').src = `data:image/jpeg;base64,${user.idPhotoBack}`;
                    }
                }, 1000);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Failed to load user details'
            });
        });
}

async function approveProperty(propertyId) {
    const result = await Swal.fire({
        title: 'Verification Confirmation',
        text: 'Are you sure you want to verify this property?',
        showCancelButton: true,
        confirmButtonText: '<i class="fas fa-check"></i> Verify',
        cancelButtonText: '<i class="fas fa-times"></i> Cancel',
        confirmButtonColor: '#28a745',
        cancelButtonColor: '#6c757d'
    });

    if (result.isConfirmed) {
        try {
            console.log('Making API call to approve property'); // Debug log
            const response = await fetch(`/api/administrators/properties/${propertyId}/approve`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            console.log('API response status:', response.status); // Debug log

            if (response.ok) {
                Swal.fire(
                    'Verified!',
                    'Property verified successfully',
                    'success'
                );
                fetchProperties();
            } else {
                const errorData = await response.text();
                console.error('API error:', errorData); // Debug log
                Swal.fire(
                    'Error!',
                    `Failed to verify property: ${errorData}`,
                    'error'
                );
            }
        } catch (error) {
            console.error('Error:', error);
            Swal.fire(
                'Error!',
                'An error occurred while verifying the property',
                'error'
            );
        }
    } else {
        console.log('Verification cancelled by user'); // Debug log
    }
}

function fetchProperties() {
    console.log('Fetching properties...');
    const tableBody = document.getElementById('propertiesTableBody');

    // Get filter values
    const ownerSearch = document.getElementById('ownerSearch').value;
    const rentedChecked = document.getElementById('statusRented').checked;
    const unrentedChecked = document.getElementById('statusUnrented').checked;

    // Show loading state
    tableBody.innerHTML = `
                    <tr>
                        <td colspan="8" class="text-center">
                            <div class="spinner-border text-primary" role="status">
                                <span class="visually-hidden">Loading...</span>
                            </div>
                        </td>
                    </tr>
                `;

    // Build query parameters
    const params = new URLSearchParams();
    if (ownerSearch) {
        params.append('ownerUsername', ownerSearch);
    }

    // Handle rented status filter
    if (rentedChecked && !unrentedChecked) {
        params.append('isRented', true);
    } else if (unrentedChecked && !rentedChecked) {
        params.append('isRented', false);
    }

    fetch(`/api/administrators/properties?${params.toString()}`)
        .then(response => response.json())
        .then(properties => {
            console.log('Received properties:', properties);
            tableBody.innerHTML = '';

            properties.forEach(property => {
                const ownerName = property.owner ?
                    `${property.owner.firstName} ${property.owner.lastName} (${property.owner.username})` :
                    'Unknown';

                const row = document.createElement('tr');
                row.innerHTML = `
                                <td>${property.propertyId || ''}</td>
                                <td>${property.address || ''}, ${property.city || ''}</td>
                                <td>${ownerName}</td>
                                <td>${property.type || ''}</td>
                                <td>€${property.rentAmount || '0'}</td>
                                <td>
                                    <span class="badge ${getBadgeClassForProperty(property.rented)}">
                                        ${property.rented ? 'Rented' : 'Available'}
                                    </span>
                                    <span class="badge ${property.approved ? 'bg-success' : 'bg-warning'}">
                                        ${property.approved ? 'Approved' : 'Pending'}
                                    </span>
                                </td>
                                <td>
                                    <button class="btn btn-sm btn-outline-primary" onclick="viewProperty(${property.propertyId})" title="View Property">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                    <button class="btn btn-sm btn-outline-danger ms-1" onclick="deleteProperty(${property.propertyId})" title="Delete Property">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                    ${!property.approved ?
                    `<button class="btn btn-sm btn-outline-success ms-1" onclick="approveProperty(${property.propertyId})" title="Approve Property">
                                            <i class="fas fa-check"></i>
                                        </button>` : ''
                }
                                </td>
                            `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            tableBody.innerHTML = `
                            <tr>
                                <td colspan="8" class="text-center text-danger">
                                    <i class="fas fa-exclamation-circle"></i> Error loading properties: ${error.message}
                                </td>
                            </tr>
                        `;
        });
}

// Add event listeners for property filters
document.getElementById('ownerSearch').addEventListener('input', debounce(fetchProperties, 300));
document.getElementById('statusRented').addEventListener('change', fetchProperties);
document.getElementById('statusUnrented').addEventListener('change', fetchProperties);

function getBadgeClassForProperty(isRented) {
    return isRented ? 'bg-success' : 'bg-warning';
}

function viewProperty(propertyId) {
    window.location.href = `/property/${propertyId}/details`;
}

function deleteUser(userId) {
    Swal.fire({
        title: 'Are you sure?',
        text: "This will permanently delete this user and all their associated data!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!',
        cancelButtonText: 'Cancel'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch(`/api/administrators/users/${userId}`, {
                method: 'DELETE',
                headers: {
                    'Accept': 'application/json'
                }
            })
                .then(async response => {
                    const data = await response.json();
                    if (!response.ok) {
                        throw new Error(data.message || 'Failed to delete user');
                    }
                    return data;
                })
                .then(data => {
                    Swal.fire({
                        icon: 'success',
                        title: 'Deleted!',
                        text: data.message || 'User has been deleted successfully.',
                        timer: 2000
                    });
                    fetchUsers(); // Refresh the users list
                })
                .catch(error => {
                    console.error('Error:', error);
                    Swal.fire({
                        icon: 'error',
                        title: 'Error',
                        text: error.message || 'Failed to delete user. Please try again.'
                    });
                });
        }
    });
}

function verifyUser(userId) {
    Swal.fire({
        title: 'Verify User',
        text: "Are you sure you want to verify this user?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#28a745',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Yes, verify!',
        cancelButtonText: 'Cancel'
    }).then((result) => {
        if (result.isConfirmed) {
            // Show loading state
            Swal.fire({
                title: 'Verifying...',
                text: 'Please wait while we verify the user.',
                allowOutsideClick: false,
                allowEscapeKey: false,
                showConfirmButton: false,
                //willOpen: () => {
                //    Swal.showLoading();
                //}
            });

            fetch(`/api/administrators/users/${userId}/verify`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            })
                .then(async response => {
                    const text = await response.text();
                    if (!response.ok) {
                        throw new Error(text || 'Network response was not ok');
                    }
                    return text;
                })
                .then(message => {
                    Swal.fire({
                        icon: 'success',
                        title: 'Verified!',
                        text: message || 'User has been verified successfully.',
                        timer: 2000
                    });
                    fetchUsers(); // Refresh the users list
                })
                .catch(error => {
                    console.error('Error:', error);
                    Swal.fire({
                        icon: 'error',
                        title: 'Error',
                        text: error.message || 'Failed to verify user. Please try again.'
                    });
                });
        }
    });
}

function deleteProperty(propertyId) {
    Swal.fire({
        title: 'Are you sure?',
        text: "This will permanently delete this property and all its associated data!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!',
        cancelButtonText: 'Cancel'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch(`/api/administrators/properties/${propertyId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.text();
                })
                .then(message => {
                    Swal.fire(
                        'Deleted!',
                        'Property has been deleted successfully.',
                        'success'
                    );
                    fetchProperties(); // Refresh the properties list
                })
                .catch(error => {
                    console.error('Error:', error);
                    Swal.fire(
                        'Error!',
                        'Failed to delete property. Please try again.',
                        'error'
                    );
                });
        }
    });
}

// function approveProperty(propertyId) {
//     Swal.fire({
//         title: 'Approve Property',
//         text: "Are you sure you want to approve this property?",
//         icon: 'question',
//         showCancelButton: true,
//         confirmButtonColor: '#28a745',
//         cancelButtonColor: '#6c757d',
//         confirmButtonText: 'Yes, approve!',
//         cancelButtonText: 'Cancel'
//     }).then((result) => {
//         if (result.isConfirmed) {
//             fetch(`/api/administrators/properties/${propertyId}/approve`, {
//                 method: 'POST'
//             })
//             .then(response => {
//                 if (!response.ok) {
//                     throw new Error('Network response was not ok');
//                 }
//                 return response.text();
//             })
//             .then(message => {
//                 Swal.fire(
//                     'Approved!',
//                     'Property has been approved successfully.',
//                     'success'
//                 );
//                 fetchProperties(); // Refresh the properties list
//             })
//             .catch(error => {
//                 console.error('Error:', error);
//                 Swal.fire(
//                     'Error!',
//                     'Failed to approve property. Please try again.',
//                     'error'
//                 );
//             });
//         }
//     });
// }

function viewReport(reportId) {
    fetch(`/api/reports/details/${reportId}`)
        .then(response => response.json())
        .then(report => {
            // Create modal if it doesn't exist
            let modalHtml = `
                <div class="modal fade" id="reportDetailsModal" tabindex="-1">
                    <div class="modal-dialog modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title">Report Details</h5>
                            </div>
                            <div class="modal-body">
                                <div class="row">
                                    <div class="col-md-6">
                                        <h6>Basic Information</h6>
                                        <table class="table">
                                            <tr>
                                                <th>Title:</th>
                                                <td>${report.title || 'N/A'}</td>
                                            </tr>
                                            <tr>
                                                <th>Reported By:</th>
                                                <td>${report.user.username || 'N/A'}</td>
                                            </tr>
                                            <tr>
                                                <th>User Role:</th>
                                                <td>${report.userRole || 'N/A'}</td>
                                            </tr>
                                            <tr>
                                                <th>Status:</th>
                                                <td>
                                                    <span class="badge ${report.resolved ? 'bg-success' : 'bg-warning'}">
                                                        ${report.resolved ? 'Resolved' : 'Pending'}
                                                    </span>
                                                </td>
                                            </tr>
                                            <tr>
                                                <th>Date Submitted:</th>
                                                <td>${new Date(report.createDate).toLocaleString() || 'N/A'}</td>
                                            </tr>
                                        </table>
                                    </div>
                                    <div class="col-md-6">
                                        <h6>Report Description</h6>
                                        <div class="p-3 bg-light rounded">
                                            ${report.description || 'No description provided'}
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                                ${!report.resolved ?
                `<button type="button" class="btn btn-success" onclick="toggleReportStatus(${report.id})">
                                        Mark as Resolved
                                    </button>` : ''
            }
                            </div>
                        </div>
                    </div>
                </div>
            `;

            // Remove existing modal if any
            let existingModal = document.getElementById('reportDetailsModal');
            if (existingModal) {
                existingModal.remove();
            }

            // Add new modal to body
            document.body.insertAdjacentHTML('beforeend', modalHtml);

            // Show modal
            let modal = new bootstrap.Modal(document.getElementById('reportDetailsModal'));
            modal.show();
        })
        .catch(error => {
            console.error('Error:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Failed to load report details'
            });
        });
}

function loadReports() {
    const resolvedChecked = document.getElementById('resolvedFilter').checked;
    const unresolvedChecked = document.getElementById('unresolvedFilter').checked;

    fetch('/api/reports/all')
        .then(response => response.json())
        .then(reports => {
            const filteredReports = reports.filter(report =>
                (report.resolved && resolvedChecked) || (!report.resolved && unresolvedChecked)
            );

            const tableBody = document.getElementById('reportsTableBody');
            tableBody.innerHTML = '';

            filteredReports.forEach(report => {
                const row = document.createElement('tr');
                const createDate = new Date(report.createDate).toLocaleString();
                row.innerHTML = `
                                <td>${report.id}</td>
                                <td>${report.user.username}</td>
                                <td>${report.userRole}</td>
                                <td>${report.title}</td>
                                <td>${createDate}</td>
                                <td>
                                    <button class="btn btn-sm btn-outline-primary" onclick="viewReport(${report.id})">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                    <button class="btn btn-sm btn-outline-danger ms-1" onclick="deleteReport(${report.id})">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                    <button class="btn btn-sm ${report.resolved ? 'btn-success' : 'btn-warning'}" 
                                            onclick="toggleReportStatus(${report.id})">
                                        ${report.resolved ? 'Resolved' : 'Pending'}
                                    </button>
                                </td>
                            `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error loading reports:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Failed to load reports'
            });
        });
}

function deleteReport(reportId) {
    Swal.fire({
        title: 'Are you sure?',
        text: "This action cannot be undone!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch(`/api/reports/${reportId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        Swal.fire(
                            'Deleted!',
                            'Report deleted successfully',
                            'success'
                        );
                        loadReports();
                    } else {
                        throw new Error('Failed to delete report');
                    }
                })
                .catch(error => {
                    console.error('Error deleting report:', error);
                    Swal.fire(
                        'Error!',
                        'Failed to delete report. Please try again.',
                        'error'
                    );
                });
        }
    });
}

function toggleReportStatus(reportId) {
    fetch(`/api/reports/${reportId}/toggle-status`, {
        method: 'PUT'
    })
        .then(response => {
            if (response.ok) {
                loadReports();
            } else {
                throw new Error('Failed to update report status');
            }
        })
        .catch(error => {
            console.error('Error updating report status:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Failed to update report status. Please try again.'
            });
        });
}

// Add event listeners for report filters
document.getElementById('resolvedFilter').addEventListener('change', loadReports);
document.getElementById('unresolvedFilter').addEventListener('change', loadReports);

function showAlert(type, message) {
    // Remove any existing alerts
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());

    // Create new alert
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                `;

    // Insert alert after the content header
    const contentHeader = document.querySelector('.content-header');
    contentHeader.insertAdjacentElement('afterend', alert);

    // Remove alert after 3 seconds
    setTimeout(() => {
        alert.remove();
    }, 3000);
}

// Add event listener for owner search
document.getElementById('ownerSearch').addEventListener('input', function () {
    fetchProperties();
});

// Initialize dashboard when the page loads
document.addEventListener('DOMContentLoaded', function () {
    console.log('Page loaded, initializing dashboard...');
    updateDashboard();

    // Add event listeners for view type changes
    document.querySelectorAll('input[name="viewType"]').forEach(radio => {
        radio.addEventListener('change', updateDashboard);
    });
});