
package com.example.moviebooking;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5175")
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to Movie Ticket Booking System!";
    }
}