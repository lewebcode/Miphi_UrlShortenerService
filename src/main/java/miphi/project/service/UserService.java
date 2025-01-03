package miphi.project.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import miphi.project.interfaces.IUserService;
import miphi.project.model.User;

public class UserService implements IUserService {
    private final Map<String, User> userMap = new ConcurrentHashMap<>();

    @Override
    public boolean registerUser(String username, String password) {
        if (userMap.containsKey(username)) {
            System.out.println("Пользователь с таким именем уже существует.");
            return false;
        }
        UUID userUuid = UUID.randomUUID();
        String passwordHash = hashPassword(password);
        userMap.put(username, new User(userUuid, username, passwordHash));
        System.out.println("Пользователь успешно зарегистрирован. UUID: " + userUuid);
        return true;
    }

    @Override
    public UUID loginUser(String username, String password) {
        User user = userMap.get(username);
        if (user == null) {
            System.out.println("Пользователь не найден.");
            return null;
        }
        String passwordHash = hashPassword(password);
        if (passwordHash.equals(user.passwordHash)) {
            System.out.println("Авторизация успешна.");
            return user.userUuid;
        } else {
            System.out.println("Неверный пароль.");
            return null;
        }
    }

    @Override
    public User getUser(UUID userUuid) {
        return userMap.values().stream()
                .filter(user -> user.userUuid.equals(userUuid))
                .findFirst()
                .orElse(null);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хеширования пароля: MD5 не поддерживается", e);
        }
    }
}

