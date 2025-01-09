package gr.hua.dit.rentalapp.config;

import gr.hua.dit.rentalapp.entities.Administrator;
import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.enums.PropertyType;
import gr.hua.dit.rentalapp.repositories.AdministratorRepository;
import gr.hua.dit.rentalapp.repositories.LandlordRepository;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.repositories.TenantRepository;
import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.enums.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private LandlordRepository landlordRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        setupDefaultRoles();

        // Create test users if database is empty
        if (propertyRepository.count() == 0) {
            // Create test tenant
            Tenant testTenant = tenantRepository.findByUsername("testtenant")
                    .orElseGet(() -> {
                        Tenant tenant = new Tenant();
                        tenant.setUsername("testtenant");
                        tenant.setEmail("testtenant@example.com");
                        tenant.setPassword(passwordEncoder.encode("test"));
                        tenant.setFirstName("Test");
                        tenant.setLastName("Tenant");
                        tenant.setEmploymentStatus("Employed");
                        tenant.setMonthlyIncome(2000.0);
                        Set<Role> roles = new HashSet<>();
                        roles.add(roleRepository.findByName(RoleType.TENANT).orElseThrow()); // Use the persisted role
                        tenant.setRoles(roles);
                        return tenantRepository.save(tenant);
                    });

            // Create test landlord
            Landlord testLandlord = landlordRepository.findByUsername("testlandlord")
                    .orElseGet(() -> {
                        Landlord landlord = new Landlord();
                        landlord.setUsername("testlandlord");
                        landlord.setEmail("testlandlord@example.com");
                        landlord.setPassword(passwordEncoder.encode("test"));
                        landlord.setFirstName("Test");
                        landlord.setLastName("Landlord");
                        Set<Role> roles = new HashSet<>();
                        roles.add(roleRepository.findByName(RoleType.LANDLORD).orElseThrow()); // Use the persisted role
                        landlord.setRoles(roles);
                        return landlordRepository.save(landlord);
                    });

            // Create test admin
            Administrator testAdmin = administratorRepository.findByUsername("testadmin")
                    .orElseGet(() -> {
                        Administrator admin = new Administrator();
                        admin.setUsername("testadmin");
                        admin.setEmail("testadmin@example.com");
                        admin.setPassword(passwordEncoder.encode("test"));
                        admin.setFirstName("Test");
                        admin.setLastName("Admin");
                        Set<Role> roles = new HashSet<>();
                        roles.add(roleRepository.findByName(RoleType.ADMINISTRATOR).orElseThrow()); // Use the persisted role
                        admin.setRoles(roles);
                        return administratorRepository.save(admin);
                    });

            // Create test properties
            createTestProperty(testLandlord, "123 Main St", "Athens", "Greece", 
                    PropertyType.APARTMENT, 800.0, 2, 1, 75.0, true, true, false, true,
                    "Modern apartment in the heart of Athens");

            createTestProperty(testLandlord, "456 Beach Road", "Thessaloniki", "Greece", 
                    PropertyType.HOUSE, 1200.0, 3, 2, 120.0, true, false, true, true,
                    "Spacious house with garden near the sea");

            createTestProperty(testLandlord, "789 Mountain View", "Patras", "Greece", 
                    PropertyType.DUPLEX, 2000.0, 4, 3, 200.0, true, true, true, true,
                    "Luxury duplex with panoramic views");

            createTestProperty(testLandlord, "321 City Center", "Athens", "Greece", 
                    PropertyType.STUDIO, 500.0, 1, 1, 45.0, false, true, false, true,
                    "Cozy studio in downtown Athens");

            createTestProperty(testLandlord, "654 Suburban Lane", "Heraklion", "Greece", 
                    PropertyType.HOUSE, 1500.0, 3, 2, 150.0, true, true, true, false,
                    "Family house in quiet neighborhood");

            createTestProperty(testLandlord, "987 Coastal Drive", "Volos", "Greece", 
                    PropertyType.APARTMENT, 900.0, 2, 1, 80.0, true, false, false, true,
                    "Sea view apartment with modern amenities");

            createTestProperty(testLandlord, "147 Park Avenue", "Larissa", "Greece", 
                    PropertyType.HOUSE, 1100.0, 3, 2, 130.0, true, true, true, true,
                    "Beautiful house near central park");
        }
    }

    private void setupDefaultRoles() {
        // Administrator
        if (roleRepository.findByName(RoleType.ADMINISTRATOR).isEmpty()) {
            roleRepository.save(new Role(RoleType.ADMINISTRATOR));
        }
        // Landlord
        if (roleRepository.findByName(RoleType.LANDLORD).isEmpty()) {
            roleRepository.save(new Role(RoleType.LANDLORD));
        }
        // Tenant
        if (roleRepository.findByName(RoleType.TENANT).isEmpty()) {
            roleRepository.save(new Role(RoleType.TENANT));
        }
    }

    private void createTestProperty(Landlord landlord, String address, String city, String country,
                                  PropertyType type, double rentAmount, int bedrooms, int bathrooms,
                                  double size, boolean hasParking, boolean allowsPets,
                                  boolean hasGarden, boolean hasBalcony, String description) {
        Property property = new Property();
        property.setOwner(landlord);
        property.setAddress(address);
        property.setType(type);
        property.setRentAmount(rentAmount);
        property.setBedrooms(bedrooms);
        property.setBathrooms(bathrooms);
        property.setCity(city);
        property.setCountry(country);
        property.setPostalCode(getPostalCodeForCity(city)); 
        property.setSizeInSquareMeters(size);
        property.setHasParking(hasParking);
        property.setAllowsPets(allowsPets);
        property.setHasGarden(hasGarden);
        property.setHasBalcony(hasBalcony);
        property.setDescription(description);
        property.setApproved(true); 
        propertyRepository.save(property);
    }

    private String getPostalCodeForCity(String city) {
        switch (city.toLowerCase()) {
            case "athens":
                return "10431";
            case "thessaloniki":
                return "54624";
            case "patras":
                return "26221";
            case "heraklion":
                return "71201";
            case "volos":
                return "38221";
            case "larissa":
                return "41221";
            default:
                return "10000"; 
        }
    }
}
