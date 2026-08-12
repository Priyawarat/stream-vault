package com.priye.streamvault.common.config;

import com.priye.streamvault.user.service.JwtService;
import com.priye.streamvault.user.service.RegistrationService;
import com.priye.streamvault.user.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userDetailsService;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver exceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwtToken = authHeader.split("Bearer ")[1];
            UUID userId = jwtService.getUserIdFromToken(jwtToken);

            if(userId !=null || SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailsService.getUserById(userId);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            exceptionResolver.resolveException(request, response, null, e);
        }
    }
}
