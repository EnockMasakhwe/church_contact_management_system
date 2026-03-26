package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/contact")
    public User saveUser(@RequestBody User user){
        System.out.println("Received: " + user.getName());
        return userService.saveUser(user);
    }
}
