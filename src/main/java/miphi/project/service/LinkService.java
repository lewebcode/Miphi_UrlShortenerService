package miphi.project.service;

import java.net.URI;
import java.awt.Desktop;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import miphi.project.interfaces.ILinkService;
import miphi.project.model.Link;
import miphi.project.util.ConfigService;

public class LinkService implements ILinkService {
    private final String baseUrl = new ConfigService().getConfigValue("base_url", "https://short.ly/");
    private final long defaultLifetimeMs = new ConfigService().getLongConfigValue("default_link_lifetime_ms", 86400000);
    private final int maxAccessLimit = new ConfigService().getIntConfigValue("max_link_access_limit", 100);

    private final Map<String, Link> linkMap = new ConcurrentHashMap<>();
    private final Map<UUID, List<Link>> userLinks = new ConcurrentHashMap<>();
    public final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public LinkService() {
        executor.scheduleAtFixedRate(this::cleanupExpiredLinks, 1, 1, TimeUnit.HOURS);
    }

    @Override
    public String createShortLink(String originalUrl, UUID userUuid, int userDefinedLimit, long userDefinedLifetimeMs) {
        String uniqueKey = UUID.randomUUID().toString().substring(0, 8);
        String shortUrl = baseUrl + uniqueKey;

        int finalLimit = Math.max(userDefinedLimit, maxAccessLimit);
        long expiryTime = System.currentTimeMillis() + Math.min(userDefinedLifetimeMs, defaultLifetimeMs);

        Link link = new Link(originalUrl, shortUrl, userUuid, finalLimit, expiryTime);
        linkMap.put(shortUrl, link);
        userLinks.computeIfAbsent(userUuid, k -> new ArrayList<>()).add(link);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String formattedDate = dateFormatter.format(new Date(expiryTime));
        System.out.println("Ссылка создана. Истекает: " + formattedDate);
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
            System.out.println("Ссылка недоступна, так как лимит переходов был исчерпан.");
            linkMap.remove(shortUrl);
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
        List<Link> userLinksList = userLinks.getOrDefault(userUuid, Collections.emptyList());

        long now = System.currentTimeMillis();
        userLinksList.removeIf(link -> {
            boolean isExpired = now > link.expiryTime;
            boolean isLimitExceeded = link.accessCount >= link.limit;

            if (isExpired || isLimitExceeded) {
                linkMap.remove(link.shortUrl);
                System.out.println((isExpired ? "Ссылка истекла: " : "Лимит переходов исчерпан для ссылки: ") + link.shortUrl);
                return true;
            }

            return false;
        });

        return userLinksList;
    }

    @Override
    public boolean updateLinkLimit(String shortUrl, UUID userUuid, int newLimit) {
        Link link = linkMap.get(shortUrl);

        if (link == null) {
            System.out.println("Ссылка не найдена.");
            return false;
        }

        if (!link.userUuid.equals(userUuid)) {
            System.out.println("У вас нет прав на изменение лимита этой ссылки.");
            return false;
        }

        link.limit = newLimit;
        return true;
    }

    @Override
    public boolean deleteUserLink(String shortUrl, UUID userUuid) {
        Link link = linkMap.get(shortUrl);

        if (link == null) {
            System.out.println("Ссылка не найдена.");
            return false;
        }

        if (!link.userUuid.equals(userUuid)) {
            System.out.println("У вас нет прав на удаление этой ссылки.");
            return false;
        }

        linkMap.remove(shortUrl);
        List<Link> userLinksList = userLinks.get(userUuid);
        if (userLinksList != null) {
            userLinksList.removeIf(l -> l.shortUrl.equals(shortUrl));
        }

        return true;
    }

    private void cleanupExpiredLinks() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<String, Link>> iterator = linkMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Link> entry = iterator.next();
            Link link = entry.getValue();

            boolean isExpired = now > link.expiryTime;
            boolean isLimitExceeded = link.accessCount >= link.limit;

            if (isExpired || isLimitExceeded) {
                iterator.remove();
                System.out.println((isExpired ? "Ссылка истекла: " : "Лимит переходов исчерпан для ссылки: ") + link.shortUrl);

                List<Link> userLinksList = userLinks.get(link.userUuid);
                if (userLinksList != null) {
                    userLinksList.removeIf(l -> l.shortUrl.equals(link.shortUrl));
                }
            }
        }

        System.out.println("Очистка устаревших или заблокированных ссылок завершена.");
    }
}