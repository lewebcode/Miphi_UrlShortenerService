package miphi.project.model;

import java.util.UUID;

public class Link {
    public String originalUrl;
    public String shortUrl;
    public UUID userUuid;
    public int limit;
    public long expiryTime;
    public int accessCount;

    public Link(String originalUrl, String shortUrl, UUID userUuid, int limit, long expiryTime) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.userUuid = userUuid;
        this.limit = limit;
        this.expiryTime = expiryTime;
        this.accessCount = 0;
    }
}

