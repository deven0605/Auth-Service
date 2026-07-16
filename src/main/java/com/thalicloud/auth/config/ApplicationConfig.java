package com.thalicloud.auth.config;

import com.thalicloud.auth.entity.DeliveryPartner;
import com.thalicloud.auth.repository.CustomerRepository;
import com.thalicloud.auth.repository.DeliveryPartnerRepository;
import com.thalicloud.auth.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final VendorRepository vendorRepository;
    private final CustomerRepository customerRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        // Delivery partner JWTs use "PARTNER:+91XXXXXXXXXX" as subject (disambiguates
        // from a Customer's plain "+91XXXXXXXXXX", since the same phone could be both);
        // Customer JWTs use phone (starts with "+"); vendor JWTs use email.
        return username -> {
            if (username.startsWith(DeliveryPartner.USERNAME_PREFIX)) {
                String phone = username.substring(DeliveryPartner.USERNAME_PREFIX.length());
                return deliveryPartnerRepository.findByPhone(phone)
                        .orElseThrow(() -> new UsernameNotFoundException("Delivery partner not found: " + phone));
            }
            if (username.startsWith("+")) {
                return customerRepository.findByPhone(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + username));
            }
            return vendorRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Vendor not found: " + username));
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
