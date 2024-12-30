package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.services.LandlordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/landlords")
public class LandlordController {

    private final LandlordService landlordService;

    @Autowired
    public LandlordController(LandlordService landlordService) {
        this.landlordService = landlordService;
    }

    // GET all landlords
    @GetMapping
    public List<Landlord> getAllLandlords() {
        return landlordService.getAllLandlords();
    }

    // GET landlord by ID
    @GetMapping("/{id}")
    public Landlord getLandlordById(@PathVariable Long id) {
        return landlordService.getLandlordById(id);
    }

    // PUT: update landlord profile
    @PutMapping("/{id}")
    public ResponseEntity<String> updateLandlord(@PathVariable Long id, @RequestBody Landlord updatedLandlord) {
        landlordService.updateLandlord(id, updatedLandlord);
        return ResponseEntity.ok("Landlord updated successfully!");
    }

    // GET: properties belonging to a landlord
    @GetMapping("/{id}/properties")
    public List<Property> getLandlordProperties(@PathVariable Long id) {
        return landlordService.getPropertiesByLandlord(id);
    }

    // todo: landlord manage - properties or respond to rental applications....
}
