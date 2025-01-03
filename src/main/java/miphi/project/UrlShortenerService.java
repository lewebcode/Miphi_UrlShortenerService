package miphi.project;

import miphi.project.interfaces.ILinkService;
import miphi.project.interfaces.IUserService;
import miphi.project.service.LinkService;
import miphi.project.service.UserService;
import miphi.project.model.Link;
import miphi.project.model.User;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

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
            System.out.println("3. Показать информацию о пользователе");
            System.out.println("4. Создать короткую ссылку");
            System.out.println("5. Перейти по короткой ссылке");
            System.out.println("6. Просмотреть мои ссылки");
            System.out.println("7. Удалить ссылку");
            System.out.println("8. Изменить лимит переходов по ссылке");
            System.out.println("9. Выход");

            int choice = scanner.nextInt();
            scanner.nextLine();

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
                    User user = app.userService.getUser(currentUserUuid);
                    if (user != null) {
                        System.out.println("Информация о пользователе:");
                        System.out.println("UUID: " + user.userUuid);
                        System.out.println("Имя пользователя: " + user.username);
                    } else {
                        System.out.println("Пользователь не найден.");
                    }
                }
                case 4 -> {
                    if (currentUserUuid == null) {
                        System.out.println("Сначала выполните авторизацию.");
                        break;
                    }
                    System.out.println("Введите длинный URL:");
                    String originalUrl = scanner.nextLine();
                    System.out.println("Введите лимит переходов:");
                    int limit = scanner.nextInt();
                    System.out.println("Введите время жизни ссылки (в миллисекундах):");
                    long lifetimeMs = scanner.nextLong();
                    scanner.nextLine(); // Consume newline
                    String shortUrl = app.linkService.createShortLink(originalUrl, currentUserUuid, limit, lifetimeMs);
                    System.out.println("Короткая ссылка: " + shortUrl);
                }
                case 5 -> {
                    System.out.println("Введите короткую ссылку:");
                    String shortUrl = scanner.nextLine();
                    app.linkService.accessLink(shortUrl);
                }
                case 6 -> {
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
                case 7 -> {
                    if (currentUserUuid == null) {
                        System.out.println("Сначала выполните авторизацию.");
                        break;
                    }
                    System.out.println("Введите короткую ссылку для удаления:");
                    String shortUrlToDelete = scanner.nextLine();
                    boolean isDeleted = app.linkService.deleteUserLink(shortUrlToDelete, currentUserUuid);
                    if (isDeleted) {
                        System.out.println("Ссылка удалена успешно.");
                    } else {
                        System.out.println("Не удалось удалить ссылку. Проверьте правильность данных.");
                    }
                }
                case 8 -> {
                    if (currentUserUuid == null) {
                        System.out.println("Сначала выполните авторизацию.");
                        break;
                    }
                    System.out.println("Введите короткую ссылку для изменения лимита:");
                    String shortUrlToUpdate = scanner.nextLine();
                    System.out.println("Введите новый лимит переходов:");
                    int newLimit = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    boolean isUpdated = app.linkService.updateLinkLimit(shortUrlToUpdate, currentUserUuid, newLimit);
                    if (isUpdated) {
                        System.out.println("Лимит переходов успешно обновлён.");
                    } else {
                        System.out.println("Не удалось обновить лимит переходов. Проверьте правильность данных.");
                    }
                }
                case 9 -> {
                    System.out.println("Выход из системы. До свидания!");
                    ((LinkService) app.linkService).executor.shutdown();
                    return;
                }
                default -> System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }
}
