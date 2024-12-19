package gr.hua.dit.rentalapp.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tenants")
public class Tenant extends User {

    @NotBlank
    private String employmentStatus;

    private double monthlyIncome;

    private boolean backgroundCheckCleared;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalApplication> submittedApplications = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "tenant_viewed_properties",
            joinColumns = @JoinColumn(name = "tenant_id"),
            inverseJoinColumns = @JoinColumn(name = "property_id"))
    private List<Property> viewedProperties = new ArrayList<>();

    // Constructors
    public Tenant() {
        super();
    }

    public Tenant(String username, String email, String password, String employmentStatus, double monthlyIncome) {
        super(username, email, password);
        this.employmentStatus = employmentStatus;
        this.monthlyIncome = monthlyIncome;
        this.backgroundCheckCleared = false; // Default value
    }

    // Getters and Setters
    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public boolean isBackgroundCheckCleared() {
        return backgroundCheckCleared;
    }

    public void setBackgroundCheckCleared(boolean backgroundCheckCleared) {
        this.backgroundCheckCleared = backgroundCheckCleared;
    }

    public List<RentalApplication> getSubmittedApplications() {
        return submittedApplications;
    }

    public void setSubmittedApplications(List<RentalApplication> submittedApplications) {
        this.submittedApplications = submittedApplications;
    }

    public List<Property> getViewedProperties() {
        return viewedProperties;
    }

    public void setViewedProperties(List<Property> viewedProperties) {
        this.viewedProperties = viewedProperties;
    }
}