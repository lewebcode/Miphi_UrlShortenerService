package miphi.project.model;

import java.util.UUID;

public class User {
    public UUID userUuid;
    public String username;
    public String passwordHash;

    public User(UUID userUuid, String username, String passwordHash) {
        this.userUuid = userUuid;
        this.username = username;
        this.passwordHash = passwordHash;
    }
}
