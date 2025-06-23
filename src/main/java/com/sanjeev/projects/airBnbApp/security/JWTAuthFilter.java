package com.sanjeev.projects.airBnbApp.security;

import com.sanjeev.projects.airBnbApp.entity.User;
import com.sanjeev.projects.airBnbApp.service.UserService;
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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
@RequiredArgsConstructor
@Configuration
public class JWTAuthFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserService userService;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String requestHeaderToken = request.getHeader("Authorization");
            if(requestHeaderToken == null || !requestHeaderToken.startsWith("Bearer") ){
                filterChain.doFilter(request,response);
                return;
            }

            String token = requestHeaderToken.split("Bearer ")[1];
            Long userId = jwtService.getUserIdFromToken(token);
            if(userId != null&& SecurityContextHolder.getContext().getAuthentication() == null){
                User user=userService.getUserById(userId);
                // check if the user should be allowed
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request,response);
        }
        catch (JwtException e){
                handlerExceptionResolver.resolveException(request,response,null,e);
        }
    }
}
