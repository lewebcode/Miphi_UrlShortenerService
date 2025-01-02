package miphi.project;

import java.net.URI;
import java.awt.Desktop;
import java.util.*;
import java.util.concurrent.*;

public class UrlShortenerService {

    public static class Link {
        String originalUrl;
        String shortUrl;
        UUID userUuid;
        int limit;
        long expiryTime;
        int accessCount;

        Link(String originalUrl, String shortUrl, UUID userUuid, int limit, long expiryTime) {
            this.originalUrl = originalUrl;
            this.shortUrl = shortUrl;
            this.userUuid = userUuid;
            this.limit = limit;
            this.expiryTime = expiryTime;
            this.accessCount = 0;
        }
    }

    private static final String BASE_URL = "https://short.ly/";
    private static final long LINK_LIFETIME_MS = 24 * 60 * 60 * 1000; // 24 часа
    private final Map<String, Link> linkMap = new ConcurrentHashMap<>();
    private final Map<UUID, List<Link>> userLinks = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public UrlShortenerService() {
        // Запуск задачи для удаления устаревших ссылок
        executor.scheduleAtFixedRate(this::cleanupExpiredLinks, 1, 1, TimeUnit.HOURS);
    }

    public String createShortLink(String originalUrl, UUID userUuid, int limit) {
        String uniqueKey = UUID.randomUUID().toString().substring(0, 8);
        String shortUrl = BASE_URL + uniqueKey;
        long expiryTime = System.currentTimeMillis() + LINK_LIFETIME_MS;

        Link link = new Link(originalUrl, shortUrl, userUuid, limit, expiryTime);
        linkMap.put(shortUrl, link);
        userLinks.computeIfAbsent(userUuid, k -> new ArrayList<>()).add(link);

        return shortUrl;
    }

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

    private void cleanupExpiredLinks() {
        long now = System.currentTimeMillis();
        linkMap.values().removeIf(link -> now > link.expiryTime);
        System.out.println("Очистка устаревших ссылок завершена.");
    }

    public List<Link> getUserLinks(UUID userUuid) {
        return userLinks.getOrDefault(userUuid, Collections.emptyList());
    }

    public static void main(String[] args) {
        UrlShortenerService service = new UrlShortenerService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в сервис сокращения ссылок!");
        System.out.println("Генерация UUID для пользователя...");
        UUID userUuid = UUID.randomUUID();
        System.out.println("Ваш UUID: " + userUuid);

        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Создать короткую ссылку");
            System.out.println("2. Перейти по короткой ссылке");
            System.out.println("3. Просмотреть мои ссылки");
            System.out.println("4. Выход");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> {
                    System.out.println("Введите длинный URL:");
                    String originalUrl = scanner.nextLine();
                    System.out.println("Введите лимит переходов:");
                    int limit = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    String shortUrl = service.createShortLink(originalUrl, userUuid, limit);
                    System.out.println("Короткая ссылка: " + shortUrl);
                }
                case 2 -> {
                    System.out.println("Введите короткую ссылку:");
                    String shortUrl = scanner.nextLine();
                    service.accessLink(shortUrl);
                }
                case 3 -> {
                    System.out.println("Ваши ссылки:");
                    List<Link> links = service.getUserLinks(userUuid);
                    for (Link link : links) {
                        System.out.println(link.shortUrl + " -> " + link.originalUrl);
                        System.out.println("Лимит: " + link.limit + ", Переходы: " + link.accessCount);
                    }
                }
                case 4 -> {
                    System.out.println("Выход из системы. До свидания!");
                    executor.shutdown();
                    return;
                }
                default -> System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }
}

