package com.petadoption.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.intuit.karate.junit5.Karate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("mysql")
public class AuthControllerIT {

    @Karate.Test
    Karate testAuthEndpoints() {
        return Karate.run("register", "login").relativeTo(getClass());
    }
}
