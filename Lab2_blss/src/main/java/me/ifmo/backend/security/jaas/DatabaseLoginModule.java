package me.ifmo.backend.security.jaas;

import me.ifmo.backend.entities.Privilege;
import me.ifmo.backend.entities.Role;
import me.ifmo.backend.entities.User;
import me.ifmo.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DatabaseLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;

    private final Set<java.security.Principal> principalsToAdd = new HashSet<>();

    private boolean loginSucceeded;
    private boolean commitSucceeded;

    @Override
    public void initialize(
            Subject subject,
            CallbackHandler callbackHandler,
            Map<String, ?> sharedState,
            Map<String, ?> options
    ) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.principalsToAdd.clear();
        this.loginSucceeded = false;
        this.commitSucceeded = false;
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("CallbackHandler is required");
        }

        NameCallback nameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);

        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        } catch (Exception exception) {
            throw new LoginException("Failed to obtain authentication data: " + exception.getMessage());
        }

        String username = normalize(nameCallback.getName());
        char[] passwordChars = passwordCallback.getPassword();
        String password = passwordChars == null ? null : new String(passwordChars);

        clearPassword(passwordCallback, passwordChars);

        if (username == null || password == null || password.isBlank()) {
            throw new FailedLoginException("Username or password is blank");
        }

        UserRepository userRepository = SpringContextHolder.getBean(UserRepository.class);
        PasswordEncoder passwordEncoder = SpringContextHolder.getBean(PasswordEncoder.class);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new FailedLoginException("User not found"));

        validateUserState(user);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new FailedLoginException("Invalid username or password");
        }

        principalsToAdd.add(new UserPrincipal(user.getEmail()));

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                if (role == null) {
                    continue;
                }

                String roleName = normalize(role.getName());
                if (roleName != null) {
                    principalsToAdd.add(new RolePrincipal(roleName));
                }

                if (role.getPrivileges() != null) {
                    for (Privilege privilege : role.getPrivileges()) {
                        if (privilege == null) {
                            continue;
                        }

                        String privilegeName = normalize(privilege.getName());
                        if (privilegeName != null) {
                            principalsToAdd.add(new PrivilegePrincipal(privilegeName));
                        }
                    }
                }
            }
        }

        loginSucceeded = true;
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!loginSucceeded) {
            return false;
        }

        if (subject.isReadOnly()) {
            throw new LoginException("Subject is read-only");
        }

        subject.getPrincipals().addAll(principalsToAdd);
        commitSucceeded = true;
        return true;
    }

    @Override
    public boolean abort() {
        if (!loginSucceeded) {
            return false;
        }

        if (!commitSucceeded) {
            clearState();
        } else {
            logout();
        }

        return true;
    }

    @Override
    public boolean logout() {
        if (subject != null && !subject.isReadOnly()) {
            subject.getPrincipals().removeAll(principalsToAdd);
        }

        clearState();
        return true;
    }

    private void validateUserState(User user) throws LoginException {
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new FailedLoginException("User is disabled");
        }

        if (!Boolean.TRUE.equals(user.getAccountNonLocked())) {
            throw new AccountLockedException("User account is locked");
        }

        if (!Boolean.TRUE.equals(user.getCredentialsNonExpired())) {
            throw new CredentialExpiredException("User credentials are expired");
        }
    }

    private void clearPassword(PasswordCallback passwordCallback, char[] passwordChars) {
        passwordCallback.clearPassword();

        if (passwordChars != null) {
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }

    private void clearState() {
        principalsToAdd.clear();
        loginSucceeded = false;
        commitSucceeded = false;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}