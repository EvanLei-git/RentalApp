package gr.hua.dit.rentalapp.config;

import gr.hua.dit.rentalapp.entities.*;
import gr.hua.dit.rentalapp.enums.ApplicationStatus;
import gr.hua.dit.rentalapp.enums.PropertyType;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import gr.hua.dit.rentalapp.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    private RentalApplicationRepository rentalApplicationRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PropertyVisitRepository propertyVisitRepository;

    @Override
    public void run(String... args) {
        setupDefaultRoles();

        // Create test users if database is empty
        if (propertyRepository.count() == 0) {
            try {
                // Create test admin first since we need it to verify landlords
                Administrator admin = createAdmin("testadmin", "test_admin@example.com", "test", "Test", "Admin");

                // Create test tenants
                Tenant tenant1 = createTenant("testtenant1", "test_tenant1@example.com", "test", "Test", "Tenant1", "Employed", 2500.0);
                Tenant tenant2 = createTenant("testtenant2", "test_tenant2@example.com", "test", "Test", "Tenant2", "Self-Employed", 3000.0);
                
                // Create test landlords
                Landlord landlord1 = createLandlord("testlandlord1", "test_landlord1@example.com", "test", "Test", "Landlord1", "1234567890", admin);
                Landlord landlord2 = createLandlord("testlandlord2", "test_landlord2@example.com", "test", "Test", "Landlord2", "0987654321", admin);

                // Create properties for Test Landlord1
                Property property1 = createTestProperty(landlord1, "Test Address 1", "Athens", "Greece", 
                        PropertyType.APARTMENT, 800.0, 2, 1, 85.0, true, true, false, true,
                        "Test Property 1 - Unrented Apartment", false, true);

                Property property2 = createTestProperty(landlord1, "Test Address 2", "Thessaloniki", "Greece", 
                        PropertyType.HOUSE, 1200.0, 3, 2, 120.0, true, false, true, true,
                        "Test Property 2 - Unrented House", false, false);

                Property property3 = createTestProperty(landlord1, "Test Address 3", "Athens", "Greece", 
                        PropertyType.APARTMENT, 750.0, 2, 1, 75.0, true, false, false, true,
                        "Test Property 3 - Unrented Apartment", false, true);

                // Create properties for Test Landlord2
                Property property4 = createTestProperty(landlord2, "Test Address 4", "Patras", "Greece", 
                        PropertyType.STUDIO, 600.0, 1, 1, 65.0, false, true, false, true,
                        "Test Property 4 - Rented Studio", true, true);

                Property property5 = createTestProperty(landlord2, "Test Address 5", "Heraklion", "Greece", 
                        PropertyType.APARTMENT, 900.0, 2, 2, 95.0, true, true, false, true,
                        "Test Property 5 - Unrented Apartment", false, false);

                // Create initial rental applications
                createRentalApplication(property1, tenant1, ApplicationStatus.APPROVED);
                createRentalApplication(property2, tenant2, ApplicationStatus.PENDING);
                createRentalApplication(property5, tenant1, ApplicationStatus.PENDING);

                // Create more test rental applications
                createRentalApplication(property1, tenant1, ApplicationStatus.PENDING);
                createRentalApplication(property1, tenant2, ApplicationStatus.APPROVED);
                createRentalApplication(property2, tenant1, ApplicationStatus.REJECTED);

                // Create test property visits
                LocalDateTime today = LocalDate.now().atTime(9, 0); // Start with 9 AM today
                LocalDateTime yesterday = LocalDate.now().minusDays(1).atTime(14, 30); // 2:30 PM yesterday

                createPropertyVisit(property1, tenant1, landlord1,
                        Date.from(today.plusHours(2).atZone(ZoneId.systemDefault()).toInstant()), // 11:00 AM today
                        VisitStatus.SCHEDULED);

                createPropertyVisit(property1, tenant2, landlord1,
                        Date.from(yesterday.plusHours(3).atZone(ZoneId.systemDefault()).toInstant()), // 5:30 PM yesterday
                        VisitStatus.COMPLETED);

                createPropertyVisit(property2, tenant1, landlord1,
                        Date.from(today.plusHours(6).atZone(ZoneId.systemDefault()).toInstant()), // 3:00 PM today
                        VisitStatus.CANCELED);

                createPropertyVisit(property2, tenant2, landlord1,
                        Date.from(today.plusHours(8).atZone(ZoneId.systemDefault()).toInstant()), // 5:00 PM today
                        VisitStatus.REQUESTED);

                // Create some test reports
                Report report1 = new Report();
                report1.setTitle("Test Report 1");
                report1.setDescription("This is a test report from tenant");
                report1.setUser(tenant1);
                report1.setUserRole("TENANT");
                report1.setCreateDate(new Date());
                report1.setResolved(false);

                Report report2 = new Report();
                report2.setTitle("Test Report 2");
                report2.setDescription("This is a test report from landlord");
                report2.setUser(landlord1);
                report2.setUserRole("LANDLORD");
                report2.setCreateDate(new Date());
                report2.setResolved(true);

                reportRepository.saveAll(Arrays.asList(report1, report2));

            } catch (Exception e) {
                System.err.println("Error initializing data: " + e.getMessage());
                e.printStackTrace();
            }
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

    private Tenant createTenant(String username, String email, String password, String firstName, String lastName, 
                              String employmentStatus, double monthlyIncome) {
        Tenant tenant = new Tenant();
        tenant.setUsername(username);
        tenant.setEmail(email);
        tenant.setPassword(passwordEncoder.encode(password));
        tenant.setFirstName(firstName);
        tenant.setLastName(lastName);
        tenant.setEmploymentStatus(employmentStatus);
        tenant.setMonthlyIncome(monthlyIncome);
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(RoleType.TENANT).orElseThrow());
        tenant.setRoles(roles);
        return tenantRepository.save(tenant);
    }

    private Landlord createLandlord(String username, String email, String password, String firstName, String lastName, String phoneNumber, Administrator verifiedBy) {
        Landlord landlord = new Landlord();
        landlord.setUsername(username);
        landlord.setEmail(email);
        landlord.setPassword(passwordEncoder.encode(password));
        landlord.setFirstName(firstName);
        landlord.setLastName(lastName);
        landlord.setPhoneNumber(phoneNumber);
        landlord.setVerifiedBy(verifiedBy);
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(RoleType.LANDLORD).orElseThrow());
        landlord.setRoles(roles);
        return landlordRepository.save(landlord);
    }

    private Administrator createAdmin(String username, String email, String password, String firstName, String lastName) {
        Administrator admin = new Administrator();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(RoleType.ADMINISTRATOR).orElseThrow());
        admin.setRoles(roles);
        return administratorRepository.save(admin);
    }

    private Property createTestProperty(Landlord landlord, String address, String city, String country,
                                      PropertyType type, double rentAmount, int bedrooms, int bathrooms,
                                      double size, boolean hasParking, boolean allowsPets,
                                      boolean hasGarden, boolean hasBalcony, String description,
                                      boolean isRented, boolean isApproved) {
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
        property.setRented(isRented);
        property.setApproved(isApproved);

        LocalDateTime yesterday = LocalDate.now().minusDays(1).atTime(10, 0);
        property.setCreationDate(Date.from(yesterday.atZone(ZoneId.systemDefault()).toInstant()));

        return propertyRepository.save(property);
    }

    private RentalApplication createRentalApplication(Property property, Tenant tenant, ApplicationStatus status) {
        RentalApplication application = new RentalApplication(tenant, property);
        application.setStatus(status);
        return rentalApplicationRepository.save(application);
    }

    private PropertyVisit createPropertyVisit(Property property, Tenant tenant, Landlord landlord, Date visitDate, VisitStatus status) {
        PropertyVisit visit = new PropertyVisit(property, tenant, landlord, visitDate, status);
        return propertyVisitRepository.save(visit);
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
            default:
                return "00000";
        }
    }
}
