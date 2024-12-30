package gr.hua.dit.rentalapp.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping
    public String welcome() {
        return "Welcome Homies, check out our Rentals!";
    }

    // If you want a health-check or version info
    @GetMapping("/test")
    public String healthCheck() {
        return "OK - the service is running";
    }
}
