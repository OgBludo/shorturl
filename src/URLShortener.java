import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class URLShortener {
    private static String userUUID;
    private static int numOfClicks;
    private static int timeToLive;

    public static void main(String[] args) {
        // Генерация уникального UUID для пользователя при первом запуске (сохраняется в файл userUUID +- как localstorage)
        userUUID = getUserUUID();

        // Чтение настроек (едактируются в файле config.txt)
        GetConfig.SetConfig();
        int numOfClicks = GetConfig.GetNumOfClicks();
        int timeToLive = GetConfig.GetTTL();

        // Чтение существующих записей (links.json - там хранятся все ссылки)
        LinksEditor.GetLinks();

        // Вывод меню
        showMenu();
    }

    private static String getUserUUID() {
        String filePath = "src/userUUID.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            if ((line = br.readLine()) != null) {
                return line.trim();
            }
        } catch (IOException e) {
            //Для проверки, в случае первого запуска данный файл не существует, поэтому отлавливается ошибка
            //e.printStackTrace();
        }
        // Генерация нового UUID, если файл не существует или пуст
        String newUUID = UUID.randomUUID().toString();
        try {
            Files.write(Paths.get(filePath), newUUID.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newUUID;
    }

    //Меню для взаимодействия
    private static void showMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            LinksEditor.removeExpiredLinks(userUUID);
            System.out.println("Меню:");
            System.out.println("1. Сократить ссылку");
            System.out.println("2. Перейти по короткой ссылке");
            System.out.println("3. Удалить короткую ссылку");
            System.out.println("4. Изменить короткую ссылку");
            System.out.println("5. Отобразить все ссылки");
            System.out.println("6. Изменить время жизни ссылки(можно в файле)");
            System.out.println("7. Изменить количество переходов ссылки(можно в файле)");
            System.out.println("8. Выйти");
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
                    LinksEditor.updateShortLink(oldShortURL, userUUID);
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
                    System.out.println("Выход из программы.");
                    LinksEditor.save();
                    return;
                default:
                    System.out.println("\n Неверный выбор. Попробуйте снова.\n");
            }
        }
    }
}
