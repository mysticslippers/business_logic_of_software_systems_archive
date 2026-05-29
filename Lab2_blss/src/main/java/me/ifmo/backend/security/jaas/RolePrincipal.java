package me.ifmo.backend.security.jaas;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

public class RolePrincipal implements Principal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;

    public RolePrincipal(String name) {
        this.name = Objects.requireNonNull(name, "Role principal name must not be null").trim();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof RolePrincipal that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "RolePrincipal{name='%s'}".formatted(name);
    }
}