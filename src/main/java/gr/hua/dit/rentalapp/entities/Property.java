package gr.hua.dit.rentalapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.hua.dit.rentalapp.enums.PropertyType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "properties")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long propertyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "landlord_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "properties", "visits", "password", "roles", "authorities"})
    @JsonProperty("owner")
    private Landlord owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "pendingProperties", "password", "roles", "authorities"})
    @JsonProperty("verifiedBy")
    private Administrator verifiedBy;

    @NotBlank
    @JsonProperty("address")
    private String address;

    @Enumerated(EnumType.STRING)
    @NotNull
    @JsonProperty("type")
    private PropertyType type;

    @NotNull
    @JsonProperty("rentAmount")
    private double rentAmount;

    @NotNull
    @JsonProperty("bedrooms")
    private int bedrooms;

    @NotNull
    @JsonProperty("bathrooms")
    private int bathrooms;

    @NotBlank
    @JsonProperty("country")
    private String country;

    @NotBlank
    @JsonProperty("city")
    private String city;

    @NotBlank
    @JsonProperty("postalCode")
    private String postalCode;

    @Column(columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @NotNull
    @JsonProperty("sizeInSquareMeters")
    private double sizeInSquareMeters;

    @NotNull
    @JsonProperty("hasParking")
    private boolean hasParking;

    @NotNull
    @JsonProperty("allowsPets")
    private boolean allowsPets;

    @NotNull
    @JsonProperty("hasGarden")
    private boolean hasGarden;

    @NotNull
    @JsonProperty("hasBalcony")
    private boolean hasBalcony;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("creationDate")
    private Date creationDate;

    @Column(name = "is_rented", nullable = false, columnDefinition = "boolean default false")
    @JsonProperty("isRented")
    private boolean isRented;

    @Column(name = "is_approved", nullable = false, columnDefinition = "boolean default false")
    @JsonProperty("isApproved")
    private boolean isApproved;

    // Constructors
    public Property() {
        this.creationDate = new Date();
        this.isRented = false;
        this.isApproved = false;
    }

    public Property(Landlord owner, String address, PropertyType type, double rentAmount, int bedrooms, int bathrooms) {
        this();
        this.owner = owner;
        this.address = address;
        this.type = type;
        this.rentAmount = rentAmount;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
    }

    // Getters and Setters
    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public Landlord getOwner() {
        return owner;
    }

    public void setOwner(Landlord owner) {
        this.owner = owner;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public double getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(double rentAmount) {
        this.rentAmount = rentAmount;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(int bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getSizeInSquareMeters() {
        return sizeInSquareMeters;
    }

    public void setSizeInSquareMeters(double sizeInSquareMeters) {
        this.sizeInSquareMeters = sizeInSquareMeters;
    }

    public boolean isHasParking() {
        return hasParking;
    }

    public void setHasParking(boolean hasParking) {
        this.hasParking = hasParking;
    }

    public boolean isAllowsPets() {
        return allowsPets;
    }

    public void setAllowsPets(boolean allowsPets) {
        this.allowsPets = allowsPets;
    }

    public boolean isHasGarden() {
        return hasGarden;
    }

    public void setHasGarden(boolean hasGarden) {
        this.hasGarden = hasGarden;
    }

    public boolean isHasBalcony() {
        return hasBalcony;
    }

    public void setHasBalcony(boolean hasBalcony) {
        this.hasBalcony = hasBalcony;
    }

    public Administrator getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Administrator verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isRented() {
        return isRented;
    }

    public void setRented(boolean rented) {
        isRented = rented;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}