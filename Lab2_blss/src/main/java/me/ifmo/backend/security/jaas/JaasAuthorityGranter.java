package me.ifmo.backend.security.jaas;

import org.springframework.security.authentication.jaas.AuthorityGranter;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JaasAuthorityGranter implements AuthorityGranter {

    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Set<String> grant(Principal principal) {
        if (principal == null) {
            return Collections.emptySet();
        }

        String name = null;
        if (principal.getName() != null) {
            String trimmed = principal.getName().trim();
            if (!trimmed.isEmpty()) {
                name = trimmed;
            }
        }


        if (name == null) {
            return Collections.emptySet();
        }

        Set<String> authorities = new HashSet<>();

        if (principal instanceof RolePrincipal) {
            authorities.add(ROLE_PREFIX + name);
        } else if (principal instanceof PrivilegePrincipal) {
            authorities.add(name);
        } else if (principal instanceof UserPrincipal) {
            return Collections.emptySet();
        }

        return authorities;
    }
}