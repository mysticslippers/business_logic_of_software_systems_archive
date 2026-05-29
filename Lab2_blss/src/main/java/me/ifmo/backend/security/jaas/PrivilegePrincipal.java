package me.ifmo.backend.security.jaas;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

public class PrivilegePrincipal implements Principal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;

    public PrivilegePrincipal(String name) {
        this.name = Objects.requireNonNull(name, "Privilege principal name must not be null").trim();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PrivilegePrincipal that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "PrivilegePrincipal{name='%s'}".formatted(name);
    }
}