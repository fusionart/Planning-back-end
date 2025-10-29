package com.monbat.planning.controllers;

import com.monbat.planning.models.dto.user_session.LoginRequest;
import com.monbat.planning.models.dto.user_session.LoginResponse;
import com.monbat.planning.models.dto.user_session.SessionInfo;
import com.monbat.planning.services.AuthService;
import com.monbat.planning.utils.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Validate input
            if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LoginResponse(false, "Username is required", null));
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new LoginResponse(false, "Password is required", null));
            }

            int requestResponse = this.authService.getRequestResponse(loginRequest.getUsername(), loginRequest.getPassword());

            if (requestResponse == 200) {
                // Update UserSession
                UserSession.getInstance().setUser(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                );

                // Return success response
                return ResponseEntity.ok(
                        new LoginResponse(true, "Login successful", loginRequest.getUsername())
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, "Invalid credentials", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, "Login failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout() {
        UserSession.getInstance().clearSession();
        return ResponseEntity.ok(
                new LoginResponse(true, "Logout successful", null)
        );
    }

    @GetMapping("/session")
    public ResponseEntity<SessionInfo> getSessionInfo() {
        UserSession session = UserSession.getInstance();

        if (session.isLoggedIn()) {
            return ResponseEntity.ok(
                    new SessionInfo(true, session.getUsername())
            );
        } else {
            return ResponseEntity.ok(
                    new SessionInfo(false, null)
            );
        }
    }
}
