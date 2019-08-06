package com.nexters.teambuilder.user.api;

import com.nexters.teambuilder.user.api.dto.UserResponse;
import com.nexters.teambuilder.user.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/apis")
public class AuthController {

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {

        return UserResponse.of(user);
    }
}