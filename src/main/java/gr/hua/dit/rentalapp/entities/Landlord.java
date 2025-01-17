package gr.hua.dit.rentalapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "landlords")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Landlord extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "verifiedLandlords", "roles"})
    private Administrator verifiedBy;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner", "visits", "applications"})
    private List<Property> properties = new ArrayList<>();

    @Column(name = "phone_number")
    private String phoneNumber;

    private boolean isVerified;

    // Constructors
    public Landlord() {
        super();
    }

    public Landlord(String username, String email, String password) {
        super(username, email, password);
    }

    public Landlord(String username, String email, String password, String phoneNumber) {
        super(username, email, password);
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public Administrator getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Administrator verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}
