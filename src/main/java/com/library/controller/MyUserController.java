package com.library.controller;

import com.library.exception.BadRequestException;
import com.library.exception.ConflictException;
import com.library.exception.InternalServerException;
import com.library.exception.NoContentException;
import com.library.model.User;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MyUserController {

    // 使用 slf4j 作为日志框架
    private static final Logger LOGGER = LoggerFactory.getLogger(MyUserController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    // API - (Register) Create a new user
    @PostMapping("/users")
    public ResponseEntity<User> registerUser(@RequestParam String username, @RequestParam String password) {
        if(username == null || password == null)
            throw new BadRequestException("Username and Password could not be null");

        if (userRepository.countByUsername(username) != 0) {
            LOGGER.error("This username has been taken, please try to sign up with a new one");
            throw new ConflictException("This username has been taken, please try to sign up");
        }

        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(new BCryptPasswordEncoder().encode(password));
            user.setEnabled(true);
            user.getRoles().add(roleRepository.getById(2));

            User _user = userRepository
                    .save(user);

            ResponseEntity<User> re = new ResponseEntity<>(_user, HttpStatus.CREATED);
            LOGGER.info(re.toString());
            return re;
        } catch (Exception e) {
            throw new InternalServerException("Unknown error");
        }

    }

    // API - Get all users
    @GetMapping("/users")
    @SecurityRequirement(name = "admin")
    public ResponseEntity<List<User>> getAllUsers() {
        ResponseEntity<List<User>> re;

        List<User> users = new ArrayList<>();

        users.addAll((Collection<? extends User>) userRepository.findAll());

        if (users.isEmpty()) {
            throw new NoContentException("No content");
        }

        re = new ResponseEntity<>(users, HttpStatus.OK);
        LOGGER.info(re.toString());
        return re;
    }
}