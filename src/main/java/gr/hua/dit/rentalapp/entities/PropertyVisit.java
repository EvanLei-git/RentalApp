package gr.hua.dit.rentalapp.entities;


import jakarta.persistence.*;
import java.util.Date;


import gr.hua.dit.rentalapp.enums.VisitStatus;

@Entity
@Table(name = "property_visits")
public class PropertyVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long visitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    private Landlord landlord;

    @Temporal(TemporalType.TIMESTAMP)
    private Date visitDate;

    @Enumerated(EnumType.STRING)
    private VisitStatus visitStatus;

    // Constructors
    public PropertyVisit() {
    }

    public PropertyVisit(Property property, Tenant tenant, Landlord landlord, Date visitDate, VisitStatus visitStatus) {
        this.property = property;
        this.tenant = tenant;
        this.landlord = landlord;
        this.visitDate = visitDate;
        this.visitStatus = visitStatus;
    }

    // Getters and Setters
    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Landlord getLandlord() {
        return landlord;
    }

    public void setLandlord(Landlord landlord) {
        this.landlord = landlord;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public VisitStatus getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(VisitStatus visitStatus) {
        this.visitStatus = visitStatus;
    }
}