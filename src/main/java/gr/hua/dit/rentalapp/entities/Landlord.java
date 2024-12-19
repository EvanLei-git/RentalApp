package gr.hua.dit.rentalapp.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "landlords")
public class Landlord extends User {

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Property> properties = new ArrayList<>();

    @OneToMany(mappedBy = "property.owner", cascade = CascadeType.ALL)
    private List<RentalApplication> receivedApplications = new ArrayList<>();

    // Constructors
    public Landlord() {
        super();
    }

    public Landlord(String username, String email, String password) {
        super(username, email, password);
    }

    // Getters and Setters
    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<RentalApplication> getReceivedApplications() {
        return receivedApplications;
    }

    public void setReceivedApplications(List<RentalApplication> receivedApplications) {
        this.receivedApplications = receivedApplications;
    }
}