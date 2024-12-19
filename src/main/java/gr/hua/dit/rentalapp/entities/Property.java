package gr.hua.dit.rentalapp.entities;

import jakarta.persistence.*;
import java.util.List;

import gr.hua.dit.rentalapp.enums.PropertyType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Landlord owner;

    @NotBlank
    private String address;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PropertyType type;

    @NotNull
    private double rentAmount;

    @NotNull
    private int bedrooms;

    @NotNull
    private int bathrooms;

    private boolean isApproved;

    @ElementCollection
    @CollectionTable(name = "property_amenities", joinColumns = @JoinColumn(name = "property_id"))
    private List<String> amenities = new ArrayList<>();

    // Constructors
    public Property() {
    }

    public Property(Landlord owner, String address, PropertyType type, double rentAmount, int bedrooms, int bathrooms) {
        this.owner = owner;
        this.address = address;
        this.type = type;
        this.rentAmount = rentAmount;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.isApproved = false; // Default value
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

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }
}