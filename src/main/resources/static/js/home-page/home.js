// Function to load property data
async function loadPropertyData() {
    try {
        const response = await fetch('/property/homeData');
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();

// Update the property grid
        const propertyGrid = document.getElementById('propertyGrid');
        propertyGrid.innerHTML = '';

        if (!data.properties || data.properties.length === 0) {
            propertyGrid.innerHTML = '<div class="col-12"><div class="alert alert-info">No properties found.</div></div>';
            return;
        }

        data.properties.forEach(property => {
            const col = document.createElement('div');
            col.className = 'col';
            col.innerHTML = `
                    <div class="card property-card" style="cursor: pointer; position: relative;" onclick="window.location.href='/property/${property.id}/details'">
                        <div class="card-body">
                            <h5 class="card-title">${property.address || 'Property'}</h5>
                            <p class="card-text">
                                <strong>Location:</strong> ${property.city || ''}, ${property.country || ''}<br>
                                <strong>Price:</strong> €${property.rentAmount || 0}<br>
                                <strong>Size:</strong> ${property.sizeInSquareMeters || 0} m²<br>
                                <strong>Bedrooms:</strong> ${property.bedrooms || 0}<br>
                                <strong>Bathrooms:</strong> ${property.bathrooms || 0}<br>
                                <strong>Amenities:</strong><br>
                                ${property.hasParking ? '✓ Parking<br>' : ''}
                                ${property.allowsPets ? '✓ Pets Allowed<br>' : ''}
                                ${property.hasGarden ? '✓ Garden<br>' : ''}
                                ${property.hasBalcony ? '✓ Balcony' : ''}
                            </p>
                        </div>
                        <div class="landlord-username">
                            ${property.landlordUsername}
                        </div>
                    </div>
                `;
            propertyGrid.appendChild(col);
        });

// Update filter dropdowns
        if (data.cities) {
            const cityFilter = document.getElementById('cityFilter');
            cityFilter.innerHTML = '<option value="">All Cities</option>';
            data.cities.forEach(city => {
                cityFilter.innerHTML += `<option value="${city}">${city}</option>`;
            });
        }

        if (data.countries) {
            const countryFilter = document.getElementById('countryFilter');
            countryFilter.innerHTML = '<option value="">All Countries</option>';
            data.countries.forEach(country => {
                countryFilter.innerHTML += `<option value="${country}">${country}</option>`;
            });
        }

// Update price range inputs
        if (data.minPrice !== undefined) {
            document.getElementById('minPrice').value = data.minPrice;
        }
        if (data.maxPrice !== undefined) {
            document.getElementById('maxPrice').value = data.maxPrice;
        }
    } catch (error) {
        console.error('Error loading property data:', error);
        const propertyGrid = document.getElementById('propertyGrid');
        propertyGrid.innerHTML = `
                <div class="col-12">
                    <div class="alert alert-danger">
                        Error loading properties: ${error.message}
                    </div>
                </div>
            `;
    }
}

// Function to update properties based on filters
async function updateProperties() {
    try {
        const city = document.getElementById('cityFilter').value;
        const country = document.getElementById('countryFilter').value;
        const minPrice = document.getElementById('minPrice').value;
        const maxPrice = document.getElementById('maxPrice').value;
        const hasParking = document.getElementById('parkingFilter').checked;
        const allowsPets = document.getElementById('petsFilter').checked;
        const hasGarden = document.getElementById('gardenFilter').checked;
        const hasBalcony = document.getElementById('balconyFilter').checked;

// Build query parameters
        const params = new URLSearchParams();
        if (city) params.append('city', city);
        if (country) params.append('country', country);
        if (minPrice) params.append('minPrice', minPrice);
        if (maxPrice) params.append('maxPrice', maxPrice);
        if (hasParking) params.append('hasParking', hasParking);
        if (allowsPets) params.append('allowsPets', allowsPets);
        if (hasGarden) params.append('hasGarden', hasGarden);
        if (hasBalcony) params.append('hasBalcony', hasBalcony);

        const response = await fetch(`/property/filter?${params.toString()}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const properties = await response.json();

// Update the property grid
        const propertyGrid = document.getElementById('propertyGrid');
        propertyGrid.innerHTML = '';

        if (!properties || properties.length === 0) {
            propertyGrid.innerHTML = '<div class="col-12"><div class="alert alert-info">No properties found matching your criteria.</div></div>';
            return;
        }

        properties.forEach(property => {
            const col = document.createElement('div');
            col.className = 'col';
            col.innerHTML = `
                    <div class="card property-card" style="cursor: pointer; position: relative;" onclick="window.location.href='/property/${property.id}/details'">
                        <div class="card-body">
                            <h5 class="card-title">${property.address || 'Property'}</h5>
                            <p class="card-text">
                                <strong>Location:</strong> ${property.city || ''}, ${property.country || ''}<br>
                                <strong>Price:</strong> €${property.rentAmount || 0}<br>
                                <strong>Size:</strong> ${property.sizeInSquareMeters || 0} m²<br>
                                <strong>Bedrooms:</strong> ${property.bedrooms || 0}<br>
                                <strong>Bathrooms:</strong> ${property.bathrooms || 0}<br>
                                <strong>Amenities:</strong><br>
                                ${property.hasParking ? '✓ Parking<br>' : ''}
                                ${property.allowsPets ? '✓ Pets Allowed<br>' : ''}
                                ${property.hasGarden ? '✓ Garden<br>' : ''}
                                ${property.hasBalcony ? '✓ Balcony' : ''}
                            </p>
                        </div>
                        <div class="landlord-username">
                            ${property.landlordUsername}
                        </div>
                    </div>
                `;
            propertyGrid.appendChild(col);
        });
    } catch (error) {
        console.error('Error updating properties:', error);
        const propertyGrid = document.getElementById('propertyGrid');
        propertyGrid.innerHTML = `
                <div class="col-12">
                    <div class="alert alert-danger">
                        Error updating properties: ${error.message}
                    </div>
                </div>
            `;
    }
}

// Load property data when page loads
document.addEventListener('DOMContentLoaded', loadPropertyData);

// Add event listeners to filters
document.getElementById('cityFilter').addEventListener('change', updateProperties);
document.getElementById('countryFilter').addEventListener('change', updateProperties);
document.getElementById('minPrice').addEventListener('input', updateProperties);
document.getElementById('maxPrice').addEventListener('input', updateProperties);
document.getElementById('parkingFilter').addEventListener('change', updateProperties);
document.getElementById('petsFilter').addEventListener('change', updateProperties);
document.getElementById('gardenFilter').addEventListener('change', updateProperties);
document.getElementById('balconyFilter').addEventListener('change', updateProperties);
