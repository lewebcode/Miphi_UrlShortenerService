package miphi.project.model;

import java.util.UUID;

/**
 * Класс, представляющий пользователя системы.
 * Хранит информацию о пользователе, включая его уникальный идентификатор (UUID),
 * имя пользователя и хешированный пароль.
 */
public class User {
    public UUID userUuid;
    public String username;
    public String passwordHash;

    /**
     * Конструктор для создания объекта {@code User}.
     *
     * @param userUuid Уникальный идентификатор пользователя.
     * @param username Имя пользователя.
     * @param passwordHash Хеш пароля пользователя.
     */
    public User(UUID userUuid, String username, String passwordHash) {
        this.userUuid = userUuid;
        this.username = username;
        this.passwordHash = passwordHash;
    }
}
