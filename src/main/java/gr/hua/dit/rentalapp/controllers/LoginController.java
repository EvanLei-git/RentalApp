package gr.hua.dit.rentalapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class LoginController {

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard() {
        return new ModelAndView("dashboard");
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }
    // GET: home page
    @GetMapping("/home")
    public ModelAndView home() {
        return new ModelAndView("home");
    }


}
