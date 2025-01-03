package miphi.project.model;

import java.util.UUID;

/**
 * Класс, представляющий ссылку.
 * Хранит информацию об оригинальной ссылке, короткой ссылке, пользователе, лимите переходов,
 * времени истечения срока действия и количестве переходов.
 */
public class Link {
    public final String originalUrl;
    public final String shortUrl;
    public final UUID userUuid;
    public int limit;
    public final long expiryTime;
    public int accessCount;

    /**
     * Конструктор для создания объекта {@code Link}.
     *
     * @param originalUrl URL, который необходимо сократить.
     * @param shortUrl Короткая версия оригинальной ссылки.
     * @param userUuid UUID пользователя, который создал эту ссылку.
     * @param limit Лимит переходов для ссылки (сколько раз её можно открыть).
     * @param expiryTime Время истечения срока действия ссылки в миллисекундах с момента её создания.
     */
    public Link(String originalUrl, String shortUrl, UUID userUuid, int limit, long expiryTime) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.userUuid = userUuid;
        this.limit = limit;
        this.expiryTime = expiryTime;
        this.accessCount = 0;
    }
}


