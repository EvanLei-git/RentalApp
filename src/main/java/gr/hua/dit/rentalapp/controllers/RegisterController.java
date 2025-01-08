package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.entities.Administrator;
import gr.hua.dit.rentalapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String email,
                             @RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String role,
                             RedirectAttributes redirectAttributes) {
        try {
            // Check if username already exists
            if (userService.findByUsername(username) != null) {
                redirectAttributes.addFlashAttribute("error", "Username already exists!");
                return "redirect:/register";
            }

            // Create appropriate user type based on role
            User user;
            switch (role.toUpperCase()) {
                case "TENANT":
                    user = new Tenant();
                    break;
                case "LANDLORD":
                    user = new Landlord();
                    break;
                case "ADMINISTRATOR":
                    user = new Administrator();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid role: " + role);
            }

            // Set basic user information
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(password);

            // Save user with role
            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid role selected!");
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }
}
