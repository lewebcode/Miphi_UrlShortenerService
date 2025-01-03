package miphi.project.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import miphi.project.interfaces.IUserService;
import miphi.project.model.User;

/**
 * Сервис для управления пользователями.
 * Обрабатывает регистрацию, авторизацию и извлечение данных пользователя.
 */
public class UserService implements IUserService {
    private final Map<String, User> userMap = new ConcurrentHashMap<>();

    /**
     * Регистрация нового пользователя.
     *
     * @param username Имя пользователя.
     * @param password Пароль пользователя.
     * @return True, если пользователь успешно зарегистрирован; false, если имя уже занято.
     */
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

    /**
     * Авторизация пользователя.
     *
     * @param username Имя пользователя.
     * @param password Пароль пользователя.
     * @return UUID пользователя, если авторизация успешна, или null, если ошибка.
     */
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

    /**
     * Получает данные пользователя по UUID.
     *
     * @param userUuid UUID пользователя.
     * @return Пользователь, если найден, иначе null.
     */
    @Override
    public User getUser(UUID userUuid) {
        return userMap.values().stream()
                .filter(user -> user.userUuid.equals(userUuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Хеширует пароль пользователя с использованием алгоритма MD5.
     *
     * @param password Пароль, который необходимо захешировать.
     * @return Хеш пароля.
     */
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

