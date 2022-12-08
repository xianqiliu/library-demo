package com.library.controller;

import com.library.model.Role;
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
        ResponseEntity<User> re = null;

        if (userRepository.countByUsername(username) == 0) {
            try {
                User user = new User();
                user.setUsername(username);
                user.setPassword(new BCryptPasswordEncoder().encode(password));
                user.setEnabled(true);
                user.getRoles().add(roleRepository.getById(2));

                User _user = userRepository
                        .save(user);

                re = new ResponseEntity<>(_user, HttpStatus.CREATED);
                LOGGER.info(re.toString());
                return re;
            } catch (Exception e) {
                re = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                LOGGER.error(re.toString());
                return re;
            }
        } else {
            LOGGER.error("This username has been taken, please try to sign up with a new one");
            re = new ResponseEntity<>(null, HttpStatus.CONFLICT);
            LOGGER.error(re.toString());
            return re;
        }
    }

    // API - Get all users
    @GetMapping("/users")
    @SecurityRequirement(name = "admin")
    public ResponseEntity<List<User>> getAllUsers() {
        ResponseEntity<List<User>> re = new ResponseEntity<>(HttpStatus.NO_CONTENT);

        try {
            List<User> users = new ArrayList<>();

            users.addAll((Collection<? extends User>) userRepository.findAll());

            if (users.isEmpty()) {
                LOGGER.info(re.toString());
                return re;
            }

            re = new ResponseEntity<>(users, HttpStatus.OK);
            LOGGER.info(re.toString());
            return re;
        } catch (Exception e) {
            re = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error(re.toString());
            return re;
        }
    }
}
