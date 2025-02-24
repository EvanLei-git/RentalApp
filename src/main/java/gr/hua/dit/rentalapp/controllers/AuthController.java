package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.services.UserAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserAuthService userAuthService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class)
    @ResponseBody
    public ResponseEntity<?> registerUser(@RequestParam String username,
                                          @RequestParam String password,
                                          @RequestParam String email,
                                          @RequestParam String firstName,
                                          @RequestParam String lastName,
                                          @RequestParam String role,
                                          @RequestParam(required = false) Double monthlyIncome,
                                          @RequestParam(required = false) String employmentStatus) {
        try {
            User user = userAuthService.register(username, password, email, firstName, lastName, role,
                    monthlyIncome, employmentStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful! Please login.");
            response.put("userId", user.getUserId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public String logout() {
        return "redirect:/login?logout";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/dashboard";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home() {
        return "/home-page/home";
    }
}
