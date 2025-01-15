package gr.hua.dit.rentalapp.entities;

import jakarta.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @Column(name = "visit_date")
    private Date visitDate;

    @Column(name = "visit_status")
    @Enumerated(EnumType.STRING)
    private VisitStatus visitStatus;

    @Column(name = "status_created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date statusCreatedAt;

    @Column(name = "status_updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date statusUpdatedAt;

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

    // Factory method for creating a visit request
    public static PropertyVisit createVisitRequest(Long propertyId, String visitDateStr) throws ParseException {
        PropertyVisit visit = new PropertyVisit();
        Property property = new Property();
        property.setPropertyId(propertyId);
        visit.setProperty(property);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        visit.setVisitDate(dateFormat.parse(visitDateStr));
        return visit;
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

    public Date getStatusCreatedAt() {
        return statusCreatedAt;
    }

    public Date getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(Date statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    @PrePersist
    protected void onCreate() {
        statusCreatedAt = new Date();
        statusUpdatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        statusUpdatedAt = new Date();
    }
}