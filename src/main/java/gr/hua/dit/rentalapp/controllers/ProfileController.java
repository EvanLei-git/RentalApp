package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.services.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserAuthService userService;

    @Autowired
    public ProfileController(UserAuthService userService) {
        this.userService = userService;
    }

    @GetMapping("/edit")
    public ModelAndView showEditProfileForm(Authentication authentication) {
        if (authentication == null) {
            return new ModelAndView("redirect:/login");
        }

        String username;
        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getName();
        }

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        ModelAndView modelAndView = new ModelAndView("profile/edit");
        modelAndView.addObject("user", user);
        modelAndView.addObject("isTenant", user instanceof Tenant);
        if (user instanceof Tenant) {
            modelAndView.addObject("tenant", (Tenant) user);
        }
        
        return modelAndView;
    }

    @PostMapping("/update")
    public ModelAndView updateProfile(
            @ModelAttribute User updatedUser,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (authentication == null) {
            return new ModelAndView("redirect:/login");
        }

        String currentUsername;
        if (authentication.getPrincipal() instanceof UserDetails) {
            currentUsername = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            currentUsername = authentication.getName();
        }

        User currentUser = userService.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        // Check if username is being changed and if it's available
        if (!currentUsername.equals(updatedUser.getUsername())) {
            if (userService.findByUsername(updatedUser.getUsername()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Username already taken!");
                return new ModelAndView("redirect:/profile/edit");
            }
            currentUser.setUsername(updatedUser.getUsername());
        }

        // Update basic user information
        currentUser.setFirstName(updatedUser.getFirstName());
        currentUser.setLastName(updatedUser.getLastName());
        currentUser.setEmail(updatedUser.getEmail());

        userService.updateUser(currentUser.getUserId(), currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        
        return new ModelAndView("redirect:/profile/edit");
    }
}
