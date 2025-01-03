package miphi.project;

import java.net.URI;
import java.awt.Desktop;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

// Interfaces
interface IUserService {
    boolean registerUser(String username, String password);
    UUID loginUser(String username, String password);
    User getUser(UUID userUuid);
}

interface ILinkService {
    String createShortLink(String originalUrl, UUID userUuid, int limit);
    void accessLink(String shortUrl);
    List<Link> getUserLinks(UUID userUuid);
}

// User class
class User {
    public UUID userUuid;
    public String username;
    public String passwordHash;

    User(UUID userUuid, String username, String passwordHash) {
        this.userUuid = userUuid;
        this.username = username;
        this.passwordHash = passwordHash;
    }
}

// Link class
class Link {
    public String originalUrl;
    public String shortUrl;
    public UUID userUuid;
    public int limit;
    public long expiryTime;
    public int accessCount;

    Link(String originalUrl, String shortUrl, UUID userUuid, int limit, long expiryTime) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.userUuid = userUuid;
        this.limit = limit;
        this.expiryTime = expiryTime;
        this.accessCount = 0;
    }
}

// Implementation of UserService
class UserService implements IUserService {
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

// Implementation of LinkService
class LinkService implements ILinkService {
    private static final String BASE_URL = "https://short.ly/";
    private static final long LINK_LIFETIME_MS = 24 * 60 * 60 * 1000; // 24 часа
    private final Map<String, Link> linkMap = new ConcurrentHashMap<>();
    private final Map<UUID, List<Link>> userLinks = new ConcurrentHashMap<>();
    protected final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public LinkService() {
        executor.scheduleAtFixedRate(this::cleanupExpiredLinks, 1, 1, TimeUnit.HOURS);
    }

    @Override
    public String createShortLink(String originalUrl, UUID userUuid, int limit) {
        String uniqueKey = UUID.randomUUID().toString().substring(0, 8);
        String shortUrl = BASE_URL + uniqueKey;
        long expiryTime = System.currentTimeMillis() + LINK_LIFETIME_MS;

        Link link = new Link(originalUrl, shortUrl, userUuid, limit, expiryTime);
        linkMap.put(shortUrl, link);
        userLinks.computeIfAbsent(userUuid, k -> new ArrayList<>()).add(link);

        return shortUrl;
    }

    @Override
    public void accessLink(String shortUrl) {
        Link link = linkMap.get(shortUrl);

        if (link == null) {
            System.out.println("Ссылка не найдена или её срок истёк.");
            return;
        }

        if (link.accessCount >= link.limit) {
            System.out.println("Лимит переходов по ссылке исчерпан.");
            return;
        }

        if (System.currentTimeMillis() > link.expiryTime) {
            System.out.println("Срок действия ссылки истёк.");
            linkMap.remove(shortUrl);
            return;
        }

        link.accessCount++;
        System.out.println("Переход по ссылке...");
        try {
            Desktop.getDesktop().browse(new URI(link.originalUrl));
        } catch (Exception e) {
            System.err.println("Ошибка при открытии ссылки: " + e.getMessage());
        }
    }

    @Override
    public List<Link> getUserLinks(UUID userUuid) {
        return userLinks.getOrDefault(userUuid, Collections.emptyList());
    }

    private void cleanupExpiredLinks() {
        long now = System.currentTimeMillis();
        linkMap.values().removeIf(link -> now > link.expiryTime);
        System.out.println("Очистка устаревших ссылок завершена.");
    }
}

// Main service coordinating user and link operations
public class UrlShortenerService {
    private final IUserService userService;
    private final ILinkService linkService;

    public UrlShortenerService() {
        this.userService = new UserService();
        this.linkService = new LinkService();
    }

    public static void main(String[] args) {
        UrlShortenerService app = new UrlShortenerService();
        Scanner scanner = new Scanner(System.in);
        UUID currentUserUuid = null;

        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Регистрация");
            System.out.println("2. Авторизация");
            System.out.println("3. Создать короткую ссылку");
            System.out.println("4. Перейти по короткой ссылке");
            System.out.println("5. Просмотреть мои ссылки");
            System.out.println("6. Показать информацию о пользователе");
            System.out.println("7. Выход");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> {
                    System.out.println("Введите имя пользователя:");
                    String username = scanner.nextLine();
                    System.out.println("Введите пароль:");
                    String password = scanner.nextLine();
                    boolean isRegistered = app.userService.registerUser(username, password);
                    if (!isRegistered) {
                        System.out.println("Не удалось зарегистрировать пользователя. Попробуйте снова.");
                    } else {
                        System.out.println("Регистрация успешна.");
                    }
                }
                case 2 -> {
                    System.out.println("Введите имя пользователя:");
                    String username = scanner.nextLine();
                    System.out.println("Введите пароль:");
                    String password = scanner.nextLine();
                    currentUserUuid = app.userService.loginUser(username, password);
                }
                case 3 -> {
                    if (currentUserUuid == null) {
                        System.out.println("Сначала выполните авторизацию.");
                        break;
                    }
                    System.out.println("Введите длинный URL:");
                    String originalUrl = scanner.nextLine();
                    System.out.println("Введите лимит переходов:");
                    int limit = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    String shortUrl = app.linkService.createShortLink(originalUrl, currentUserUuid, limit);
                    System.out.println("Короткая ссылка: " + shortUrl);
                }
                case 4 -> {
                    System.out.println("Введите короткую ссылку:");
                    String shortUrl = scanner.nextLine();
                    app.linkService.accessLink(shortUrl);
                }
                case 5 -> {
                    if (currentUserUuid == null) {
                        System.out.println("Сначала выполните авторизацию.");
                        break;
                    }
                    System.out.println("Ваши ссылки:");
                    List<Link> links = app.linkService.getUserLinks(currentUserUuid);
                    for (Link link : links) {
                        System.out.println(link.shortUrl + " -> " + link.originalUrl);
                        System.out.println("Лимит: " + link.limit + ", Переходы: " + link.accessCount);
                    }
                }
                case 6 -> {
                    if (currentUserUuid == null) {
                        System.out.println("Сначала выполните авторизацию.");
                        break;
                    }
                    User user = app.userService.getUser(currentUserUuid);
                    if (user != null) {
                        System.out.println("Информация о пользователе:");
                        System.out.println("UUID: " + user.userUuid);
                        System.out.println("Имя пользователя: " + user.username);
                    } else {
                        System.out.println("Пользователь не найден.");
                    }
                }
                case 7 -> {
                    System.out.println("Выход из системы. До свидания!");
                    ((LinkService) app.linkService).executor.shutdown();
                    return;
                }
                default -> System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }
}
