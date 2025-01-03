package miphi.project.interfaces;

import miphi.project.model.User;

import java.util.UUID;

public interface IUserService {
    boolean registerUser(String username, String password);
    UUID loginUser(String username, String password);
    User getUser(UUID userUuid);
}
