package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.services.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    @Autowired
    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    // GET all properties
    @GetMapping
    public List<Property> getAllProperties() {
        return propertyService.getAllProperties();
    }

    // GET single property
    @GetMapping("/{propertyId}")
    public Property getPropertyById(@PathVariable Long propertyId) {
        return propertyService.getPropertyById(propertyId);
    }

    // POST: create a new property (Landlord usage)
    @PostMapping
    public ResponseEntity<String> createProperty(@RequestBody Property property) {
        propertyService.createProperty(property);
        return ResponseEntity.ok("Property created successfully!");
    }

    // PUT: update property details
    @PutMapping("/{propertyId}")
    public ResponseEntity<String> updateProperty(@PathVariable Long propertyId, @RequestBody Property updatedProperty) {
        propertyService.updateProperty(propertyId, updatedProperty);
        return ResponseEntity.ok("Property updated successfully!");
    }

    // DELETE: remove property
    @DeleteMapping("/{propertyId}")
    public ResponseEntity<String> deleteProperty(@PathVariable Long propertyId) {
        propertyService.deleteProperty(propertyId);
        return ResponseEntity.ok("Property deleted successfully!");
    }
}
