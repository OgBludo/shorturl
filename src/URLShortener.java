import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.UUID;

public class URLShortener {
    private static String userUUID;
    private static int numOfClicks;
    private static int timeToLive;

    public static void main(String[] args) {
        // Чтение настроек (редактируются в файле config.txt)
        GetConfig.SetConfig();
        numOfClicks = GetConfig.GetNumOfClicks();
        timeToLive = GetConfig.GetTTL();

        // Чтение существующих записей (links.json - там хранятся все ссылки)
        LinksEditor.GetLinks();

        // Вывод меню
        showMenu();
    }

    //Функции описанные ниже сделаны для реализации входа и регистрации работают по UUID
    //Функция регистрации
    private static void register() {
        String newUUID = UUID.randomUUID().toString();
        String filePath = "src/userUUID.txt";
        try {
            Files.write(Paths.get(filePath), (newUUID + "\n").getBytes(), StandardOpenOption.APPEND);
            userUUID = newUUID;
            System.out.println("Регистрация успешна. Ваш UUID: " + userUUID +"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Функция входа по uuid
    private static void login() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ваш UUID: ");
        String inputUUID = scanner.nextLine();
        String filePath = "src/userUUID.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean found = false;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals(inputUUID)) {
                    userUUID = inputUUID;
                    System.out.println("Вы вошли.\n");
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("Неверный UUID. Попробуйте снова.\n");
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла. Попробуйте снова.\n");
        }
    }

    //Меню для взаимодействия
    private static void showMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (userUUID == null) {
                System.out.println("Меню:");
                System.out.println("1. Регистрация");
                System.out.println("2. Вход");
                System.out.println("3. Выйти");
                System.out.print("Выберите опцию: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        register();
                        break;
                    case 2:
                        login();
                        break;
                    case 3:
                        System.out.println("Выход из программы.");
                        return;
                    default:
                        System.out.println("\n Неверный выбор. Попробуйте снова.\n");
                }
            } else {
                LinksEditor.removeExpiredLinks(userUUID);
                System.out.println("Меню:");
                System.out.println("1. Сократить ссылку");
                System.out.println("2. Перейти по короткой ссылке");
                System.out.println("3. Удалить короткую ссылку");
                System.out.println("4. Изменить короткую ссылку");
                System.out.println("5. Отобразить все ссылки");
                System.out.println("6. Изменить время жизни ссылки(можно в файле)");
                System.out.println("7. Изменить количество переходов ссылки(можно в файле)");
                System.out.println("8. Выйти из аккаунта");
                System.out.println("9. Выйти из программы");
                System.out.print("Выберите опцию: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("\n Введите длинную ссылку: ");
                        String longURL = scanner.nextLine();
                        String shortURL = LinksEditor.GetShortLink(userUUID, longURL, numOfClicks, timeToLive);
                        System.out.println("Сокращенная ссылка: " + shortURL + "\n");
                        break;
                    case 2:
                        System.out.print("\n Введите короткую ссылку: ");
                        String inputShortURL = scanner.nextLine();
                        LinksEditor.followTheLink(inputShortURL, userUUID);
                        System.out.println();
                        break;
                    case 3:
                        System.out.print("\n Введите короткую ссылку для удаления: ");
                        String deleteShortURL = scanner.nextLine();
                        LinksEditor.deleteShortLink(deleteShortURL, userUUID);
                        break;
                    case 4:
                        System.out.print("\n Введите старую короткую ссылку: ");
                        String oldShortURL = scanner.nextLine();
                        LinksEditor.updateShortLink(oldShortURL, userUUID, timeToLive, numOfClicks);
                        break;
                    case 5:
                        LinksEditor.displayAllLinks(userUUID);
                        break;
                    case 6:
                        System.out.print("\n Введите новое время жизни в миллисекундах: ");
                        timeToLive = scanner.nextInt();
                        break;
                    case 7:
                        System.out.print("\n Введите новое количество переходов ссылки: ");
                        numOfClicks = scanner.nextInt();
                        break;
                    case 8:
                        userUUID = null;
                        System.out.println("Вы вышли из аккаунта.");
                        break;
                    case 9:
                        System.out.println("Выход из программы.");
                        LinksEditor.save();
                        return;
                    default:
                        System.out.println("\n Неверный выбор. Попробуйте снова.\n");
                }
            }
        }
    }
}