package com.groupe.gestion_.de_.notes.security.Jwt;

import com.groupe.gestion_.de_.notes.services.ServiceImplementation.LoginRecordsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginRecordsServiceImpl loginRecordsServiceImpl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // to Retrieve the authenticated user's username
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Getting the client's IP address if there is
        String ipAddress = request.getRemoteAddr();

        // Call the service to record the login event
        loginRecordsServiceImpl.recordLogin(username, ipAddress);

        // Continue with the default authentication success behavior (e.g., returning the JWT token)
        // Note: Your main auth controller logic will handle the JWT response. This is for the audit side effect.
    }
}
