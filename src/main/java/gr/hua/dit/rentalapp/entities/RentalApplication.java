package gr.hua.dit.rentalapp.entities;


import jakarta.persistence.*;
import java.util.Date;
import gr.hua.dit.rentalapp.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;




@Entity
@Table(name = "rental_applications")
public class RentalApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ApplicationStatus status;

    @Temporal(TemporalType.DATE)
    private Date applicationDate;

    // Constructors
    public RentalApplication() {
        this.status = ApplicationStatus.PENDING;
        this.applicationDate = new Date();
    }

    public RentalApplication(Tenant applicant, Property property) {
        this.applicant = applicant;
        this.property = property;
        this.status = ApplicationStatus.PENDING;
        this.applicationDate = new Date();
    }

    // Getters and Setters
    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Tenant getApplicant() {
        return applicant;
    }

    public void setApplicant(Tenant applicant) {
        this.applicant = applicant;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }
}
