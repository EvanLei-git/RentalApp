package gr.hua.dit.rentalapp.entities;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "administrators")
public class Administrator extends User {


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "verifiedBy")
    private List<Property> pendingProperties = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "verifiedBy")
    private List<Tenant> unverifiedTenants = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "verifiedBy")
    private List<Landlord> unverifiedLandlords = new ArrayList<>();

    // Constructors
    public Administrator() {
        super();
    }

    public Administrator(String username, String email, String password) {
        super(username, email, password);
    }

    // Getters and Setters

    public List<Property> getPendingProperties() {
        return pendingProperties;
    }

    public void setPendingProperties(List<Property> pendingProperties) {
        this.pendingProperties = pendingProperties;
    }

    public List<Tenant> getUnverifiedTenants() {
        return unverifiedTenants;
    }

    public void setUnverifiedTenants(List<Tenant> unverifiedTenants) {
        this.unverifiedTenants = unverifiedTenants;
    }
}