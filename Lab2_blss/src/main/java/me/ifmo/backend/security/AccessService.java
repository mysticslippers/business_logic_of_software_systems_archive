package me.ifmo.backend.security;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.entities.Enrollment;
import me.ifmo.backend.entities.Payment;
import me.ifmo.backend.entities.User;
import me.ifmo.backend.repositories.EnrollmentRepository;
import me.ifmo.backend.repositories.PaymentRepository;
import me.ifmo.backend.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("accessService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public boolean canAccessEnrollment(Long enrollmentId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        String currentUserEmail = getCurrentUserEmail(authentication);
        if (currentUserEmail == null) {
            return false;
        }

        return enrollmentRepository.findById(enrollmentId)
                .map(Enrollment::getUser)
                .map(User::getEmail)
                .filter(currentUserEmail::equals)
                .isPresent();
    }

    public boolean canAccessUserEnrollments(Long userId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        String currentUserEmail = getCurrentUserEmail(authentication);
        if (currentUserEmail == null) {
            return false;
        }

        return userRepository.findByEmail(currentUserEmail)
                .map(User::getId)
                .filter(userId::equals)
                .isPresent();
    }

    public boolean canCreatePaymentForEnrollment(Long enrollmentId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        String currentUserEmail = getCurrentUserEmail(authentication);
        if (currentUserEmail == null) {
            return false;
        }

        return enrollmentRepository.findById(enrollmentId)
                .map(Enrollment::getUser)
                .map(User::getEmail)
                .filter(currentUserEmail::equals)
                .isPresent();
    }

    public boolean canAccessPayment(Long paymentId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        String currentUserEmail = getCurrentUserEmail(authentication);
        if (currentUserEmail == null) {
            return false;
        }

        return paymentRepository.findById(paymentId)
                .map(Payment::getEnrollment)
                .map(Enrollment::getUser)
                .map(User::getEmail)
                .filter(currentUserEmail::equals)
                .isPresent();
    }

    public boolean canAccessPaymentByEnrollment(Long enrollmentId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        String currentUserEmail = getCurrentUserEmail(authentication);
        if (currentUserEmail == null) {
            return false;
        }

        return enrollmentRepository.findById(enrollmentId)
                .map(Enrollment::getUser)
                .map(User::getEmail)
                .filter(currentUserEmail::equals)
                .isPresent();
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_ADMIN::equals);
    }

    private String getCurrentUserEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        String name = authentication.getName();
        if (name == null || name.isBlank()) {
            return null;
        }

        return name.trim();
    }
}