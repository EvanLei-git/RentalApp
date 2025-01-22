package gr.hua.dit.rentalapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tenants")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tenant extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id") // refering to the Administrator id that verified the tenant
    @JsonIgnore
    private Administrator verifiedBy;

    @PositiveOrZero
    @Column(name = "monthly_income")
    private Double monthlyIncome;

    @Column(name = "employment_status")
    private String employmentStatus;

    private boolean backgroundCheckCleared = false;

    @Column(name = "is_verified")
    private boolean verified = false;

    @Column(name = "pending_verification")
    private boolean pendingVerification = false;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RentalApplication> submittedApplications = new ArrayList<>();

    public Tenant() {
        super();
    }

    public Tenant(String username, String email, String password, String employmentStatus, Double monthlyIncome) {
        super(username, email, password);
        this.employmentStatus = employmentStatus;
        this.monthlyIncome = monthlyIncome;
    }

    // Getters and Setters
    public Administrator getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Administrator verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public Double getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(Double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public boolean isBackgroundCheckCleared() {
        return backgroundCheckCleared;
    }

    public void setBackgroundCheckCleared(boolean backgroundCheckCleared) {
        this.backgroundCheckCleared = backgroundCheckCleared;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isPendingVerification() {
        return pendingVerification;
    }

    public void setPendingVerification(boolean pendingVerification) {
        this.pendingVerification = pendingVerification;
    }

    public List<RentalApplication> getSubmittedApplications() {
        return submittedApplications;
    }

    public void setSubmittedApplications(List<RentalApplication> submittedApplications) {
        this.submittedApplications = submittedApplications;
    }
}
