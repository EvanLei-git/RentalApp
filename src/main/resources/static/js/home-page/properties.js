let currentProperties = [];
const propertyModal = new bootstrap.Modal(document.getElementById('propertyModal'));
let selectedPropertyId = null;

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function () {
    const token = localStorage.getItem('token');
    const authButtons = document.getElementById('authButtons');
    const landlordElements = document.querySelectorAll('.landlord-only');

    if (token) {
        // User is logged in
        authButtons.innerHTML = '<button onclick="logout()" class="btn btn-outline-light">Logout</button>';

        // Check if user is a landlord
        const userRole = localStorage.getItem('userRole');
        if (userRole === 'LANDLORD') {
            landlordElements.forEach(el => el.style.display = '');
        }
    }

    loadProperties();
});

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    window.location.href = '/login';
}

function checkAuth(event) {
    const token = localStorage.getItem('token');
    if (!token) {
        event.preventDefault();
        window.location.href = '/login';
        return false;
    }
    return true;
}

async function loadProperties() {
    try {
        const response = await fetch('/api/properties');
        if (!response.ok) {
            throw new Error('Failed to load properties');
        }

        currentProperties = await response.json();
        displayProperties(currentProperties);

    } catch (error) {
        console.error('Error loading properties:', error);
        const propertiesList = document.getElementById('propertiesList');
        propertiesList.innerHTML = '<div class="col-12"><div class="alert alert-danger">Failed to load properties</div></div>';
    }
}

function displayProperties(properties) {
    const propertiesList = document.getElementById('propertiesList');
    propertiesList.innerHTML = '';

    if (properties.length === 0) {
        propertiesList.innerHTML = '<div class="col-12"><div class="alert alert-info">No properties found matching your criteria</div></div>';
        return;
    }

    properties.forEach(property => {
        if (!property.approved) return; // Only show approved properties

        const card = document.createElement('div');
        card.className = 'col-md-6 col-lg-4';
        card.innerHTML = `
                    <div class="card property-card">
                        <img src="https://via.placeholder.com/400x200?text=Property+Image" class="property-image" alt="Property">
                        <div class="card-body">
                            <h5 class="card-title">${property.address}</h5>
                            <p class="card-text">
                                <strong>Type:</strong> ${property.type}<br>
                                <strong>Rent:</strong> €${property.rentAmount}/month<br>
                                <strong>Bedrooms:</strong> ${property.bedrooms} | 
                                <strong>Bathrooms:</strong> ${property.bathrooms}
                            </p>
                            <div class="mb-3">
                                ${property.amenities.map(amenity =>
            `<span class="badge bg-secondary amenity-badge">${amenity}</span>`
        ).join('')}
                            </div>
                            <button class="btn btn-primary" onclick="showPropertyDetails(${property.propertyId})">
                                View Details
                            </button>
                        </div>
                    </div>
                `;
        propertiesList.appendChild(card);
    });
}

function showPropertyDetails(propertyId) {
    const property = currentProperties.find(p => p.propertyId === propertyId);
    if (!property) return;

    selectedPropertyId = propertyId;
    const modalContent = document.getElementById('propertyModalContent');

    modalContent.innerHTML = `
                <div class="row">
                    <div class="col-md-6">
                        <img src="https://via.placeholder.com/400x300?text=Property+Image" class="img-fluid" alt="Property">
                    </div>
                    <div class="col-md-6">
                        <h4>${property.address}</h4>
                        <p><strong>Type:</strong> ${property.type}</p>
                        <p><strong>Rent:</strong> €${property.rentAmount}/month</p>
                        <p><strong>Bedrooms:</strong> ${property.bedrooms}</p>
                        <p><strong>Bathrooms:</strong> ${property.bathrooms}</p>
                        <h5>Amenities:</h5>
                        <div>
                            ${property.amenities.map(amenity =>
        `<span class="badge bg-secondary amenity-badge">${amenity}</span>`
    ).join('')}
                        </div>
                    </div>
                </div>
            `;

    // Show/hide viewing button based on authentication
    const viewingButton = document.getElementById('viewingButton');
    viewingButton.style.display = localStorage.getItem('token') ? '' : 'none';

    propertyModal.show();
}

async function requestViewing() {
    if (!selectedPropertyId) return;

    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login';
        return;
    }

    try {
        const response = await fetch('/api/viewings/request', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                propertyId: selectedPropertyId
            })
        });

        if (!response.ok) {
            throw new Error('Failed to request viewing');
        }

        alert('Viewing request sent successfully!');
        propertyModal.hide();

    } catch (error) {
        console.error('Error requesting viewing:', error);
        alert('Failed to request viewing. Please try again later.');
    }
}

function applyFilters() {
    const type = document.getElementById('propertyType').value;
    const minPrice = document.getElementById('minPrice').value;
    const maxPrice = document.getElementById('maxPrice').value;
    const bedrooms = document.getElementById('bedrooms').value;

    let filteredProperties = currentProperties;

    if (type) {
        filteredProperties = filteredProperties.filter(p => p.type === type);
    }
    if (minPrice) {
        filteredProperties = filteredProperties.filter(p => p.rentAmount >= minPrice);
    }
    if (maxPrice) {
        filteredProperties = filteredProperties.filter(p => p.rentAmount <= maxPrice);
    }
    if (bedrooms) {
        filteredProperties = filteredProperties.filter(p => p.bedrooms >= parseInt(bedrooms));
    }

    displayProperties(filteredProperties);
}

function resetFilters() {
    document.getElementById('propertyType').value = '';
    document.getElementById('minPrice').value = '';
    document.getElementById('maxPrice').value = '';
    document.getElementById('bedrooms').value = '';
    displayProperties(currentProperties);
}
