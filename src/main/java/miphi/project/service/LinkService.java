package miphi.project.service;

import java.net.URI;
import java.awt.Desktop;
import java.util.*;
import java.util.concurrent.*;

import miphi.project.interfaces.ILinkService;
import miphi.project.model.Link;

public class LinkService implements ILinkService {
    private static final String BASE_URL = "https://short.ly/";
    private static final long LINK_LIFETIME_MS = 24 * 60 * 60 * 1000; // 24 часа
    private final Map<String, Link> linkMap = new ConcurrentHashMap<>();
    private final Map<UUID, List<Link>> userLinks = new ConcurrentHashMap<>();
    public final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

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
        if (link == null || System.currentTimeMillis() > link.expiryTime) {
            System.out.println("Ссылка не найдена или её срок истёк.");
            linkMap.remove(shortUrl);
            return;
        }
        if (link.accessCount >= link.limit) {
            System.out.println("Лимит переходов по ссылке исчерпан.");
            return;
        }

        link.accessCount++;
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

