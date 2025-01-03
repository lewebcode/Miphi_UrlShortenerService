package miphi.project.interfaces;

import miphi.project.model.Link;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса для работы с короткими ссылками.
 * Осуществляет создание, обновление, удаление и доступ к коротким ссылкам,
 * а также позволяет получать список коротких ссылок для конкретного пользователя.
 */
public interface ILinkService {

    /**
     * Создает короткую ссылку для указанного оригинального URL.
     * Возвращает сгенерированную короткую ссылку.
     *
     * @param originalUrl Исходный URL, для которого необходимо создать короткую ссылку.
     * @param userUuid Уникальный идентификатор пользователя, создавшего ссылку.
     * @param limit Лимит переходов по ссылке.
     * @param userDefinedLifetimeMs Время жизни ссылки в миллисекундах, по истечении которого ссылка станет недействительной.
     * @return Короткая ссылка.
     */
    String createShortLink(String originalUrl, UUID userUuid, int limit, long userDefinedLifetimeMs);

    /**
     * Пытается выполнить переход по короткой ссылке.
     * Если ссылка не существует или срок ее действия истек, пользователю будет сообщено об этом.
     *
     * @param shortUrl Короткая ссылка для перехода.
     */
    void accessLink(String shortUrl);

    /**
     * Получает все короткие ссылки, связанные с указанным пользователем.
     * Возвращает список ссылок, который может быть пустым, если у пользователя нет коротких ссылок.
     *
     * @param userUuid Уникальный идентификатор пользователя, для которого нужно получить список ссылок.
     * @return Список ссылок пользователя.
     */
    List<Link> getUserLinks(UUID userUuid);

    /**
     * Удаляет короткую ссылку, принадлежащую указанному пользователю.
     * Возвращает {@code true}, если удаление прошло успешно, {@code false} в случае ошибки.
     *
     * @param shortUrlToDelete Короткая ссылка, которую необходимо удалить.
     * @param currentUserUuid UUID текущего пользователя, пытающегося удалить ссылку.
     * @return {@code true} - если ссылка успешно удалена, {@code false} - если возникла ошибка (например, недостаточно прав).
     */
    boolean deleteUserLink(String shortUrlToDelete, UUID currentUserUuid);

    /**
     * Обновляет лимит переходов для указанной короткой ссылки.
     * Возвращает {@code true}, если обновление прошло успешно, {@code false} в случае ошибки.
     *
     * @param shortUrl Короткая ссылка, для которой необходимо изменить лимит.
     * @param userUuid UUID пользователя, который владеет ссылкой и имеет право ее обновить.
     * @param newLimit Новый лимит переходов.
     * @return {@code true} - если лимит успешно обновлен, {@code false} - если возникла ошибка (например, если пользователю не разрешено изменять ссылку).
     */
    boolean updateLinkLimit(String shortUrl, UUID userUuid, int newLimit);
}