package com.davimarques.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.davimarques.todolist.user.IUserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCryptParser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
            var servletPath = request.getServletPath();

            if (servletPath.startsWith("/tasks/")) {

                var authorization = request.getHeader("Authorization");
                
                String authEnconded = authorization.substring("Basic".length()).trim();
                byte[] authDecode = Base64.getDecoder().decode(authEnconded);

                String authString = new String(authDecode);

                String[] authCredentials = authString.split(":");
                
                String username = authCredentials[0];
                String password = authCredentials[1];

                // User validation
                var user = this.userRepository.findByUsername(username);
                
                if (user == null) {
                    response.sendError(401);
                } else {
                    BCrypt.Result passwordIsValid = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                    if (passwordIsValid.verified) {
                        request.setAttribute("userId", user.getId());
                        filterChain.doFilter(request, response);
                    } else {
                        response.sendError(401);
                    }
                    
                }
            } else {
                filterChain.doFilter(request, response);
            }
    }
}
