package miphi.project.interfaces;

import miphi.project.model.Link;

import java.util.List;
import java.util.UUID;

public interface ILinkService {
    String createShortLink(String originalUrl, UUID userUuid, int limit);
    void accessLink(String shortUrl);
    List<Link> getUserLinks(UUID userUuid);
}