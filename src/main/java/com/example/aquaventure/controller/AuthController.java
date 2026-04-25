package com.example.aquaventure.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.entity.User;
import com.example.aquaventure.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService service;

    @PostMapping("/register")
    public String register(@RequestBody User user){
        return service.register(user);
    }

    @GetMapping("/register")
    public String registerInfo() {
        return "Use POST /auth/register with JSON body: firstname, lastname, email, password";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user){
        return service.login(
            user.getEmail(),
             user.getPassword());  
    }

    @GetMapping("/login")
    public String loginInfo() {
        return "Use POST /auth/login with JSON body: email, password";
    }
}
