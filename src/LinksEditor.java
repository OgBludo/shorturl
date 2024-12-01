import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class LinksEditor {
    static JSONArray data;

    //Считываем все существующие ссылки и информацию о них
    public static void GetLinks() {
        String filePath = "src/links.json";
        try {
            // Проверка существования файла
            if (!Files.exists(Paths.get(filePath))) {
                data = new JSONArray();
            }
            // Чтение данных из файла links.json
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            data = new JSONArray(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String GetShortLink(String UUID, String longURL, int noc, int ttl) {
        String shortURL = null;
        // Проверяем, существует ли короткая ссылка для данного UUID и longURL
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.getString("UUID").equals(UUID) && jsonObject.getString("longURL").equals(longURL)) {
                shortURL = jsonObject.getString("shortURL");
                updateShortLink(shortURL, UUID, ttl, noc);
                shortURL = jsonObject.getString("shortURL");
                break;
            }
        }
        // Если короткая ссылка не найдена, генерируем новую
        if (shortURL == null) {
            shortURL = generateUniqueURL();
            JSONObject newLink = new JSONObject();
            newLink.put("UUID", UUID);
            newLink.put("shortURL", shortURL);
            newLink.put("longURL", longURL);
            newLink.put("num_of_clicks", noc);
            newLink.put("time_to_live", ttl);
            newLink.put("created_at", System.currentTimeMillis());
            data.put(newLink);

            save();

        }
        return shortURL;
    }

    //Функция генерации уникального url, абсолютно все будут уникальны так как мы проверям, чтобы новая не соответствовала ни одной из уже существующих
    private static String generateUniqueURL() {
        Set<String> existingShortURLs = new HashSet<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            existingShortURLs.add(jsonObject.getString("shortURL"));
        }
        String shortURL;
        do {
            shortURL = "http://short.url/" + UUID.randomUUID().toString().substring(0, 8);
        } while (existingShortURLs.contains(shortURL));

        return shortURL;
    }

    //Функция удаления всех ссылок у которых истекло время существования
    public static void removeExpiredLinks(String UUID) {
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            long createdAt = jsonObject.getLong("created_at");
            int ttl = jsonObject.getInt("time_to_live");
            if (currentTime - createdAt > ttl) {
                if (jsonObject.getString("UUID").equals(UUID)) {
                    System.out.println("Время жизни ссылки: " + jsonObject.getString("shortURL") + " истекло.\nСсылка удалена!\n");
                }
                data.remove(i);
                i--;
            }
        }
    }

    //Функция перехода по короткой ссылке
    public static void followTheLink(String shortURL, String UUID) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.getString("UUID").equals(UUID) && jsonObject.getInt("num_of_clicks") > 0 && jsonObject.getString("shortURL").equals(shortURL)) {
                String url = jsonObject.getString("longURL");
                int currentClicks = jsonObject.getInt("num_of_clicks");
                jsonObject.put("num_of_clicks", currentClicks - 1);
                data.put(i, jsonObject);
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
                return;
            } else if (jsonObject.getString("UUID").equals(UUID) && jsonObject.getInt("num_of_clicks") == 0 && jsonObject.getString("shortURL").equals(shortURL)) {
                System.out.println("Кол-во переходов по ссылке закончилось - ссылка будет удалена!");
                data.remove(i);
                return;
            }
        }
        System.out.println("\nТакая ссылка отсутствует!\n");
        save();
    }

    //Функция сохранения всех данных в файл
    public static void save() {
        try {
            Files.write(Paths.get("src/links.json"), data.toString(4).getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Функция удаления короткой ссылки
    public static void deleteShortLink(String shortURL, String UUID) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.getString("UUID").equals(UUID) && jsonObject.getString("shortURL").equals(shortURL)) {
                data.remove(i);
                System.out.println("Ссылка " + shortURL + " удалена!");
                save();
                return;
            }
        }
        System.out.println("Ссылка " + shortURL + " не найдена!\n");
    }

    // Функция изменения короткой ссылки
    public static void updateShortLink(String oldShortURL, String UUID, int ttl, int noc) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.getString("UUID").equals(UUID) && jsonObject.getString("shortURL").equals(oldShortURL)) {
                String newShortURL = generateUniqueURL();
                jsonObject.put("shortURL", newShortURL);
                jsonObject.put("num_of_clicks", noc);
                jsonObject.put("time_to_live", ttl);
                jsonObject.put("created_at", System.currentTimeMillis());
                data.put(i, jsonObject);
                System.out.println("Ссылка " + oldShortURL + " изменена на " + newShortURL + "\n");
                save();
                return;
            }
        }
        System.out.println("Ссылка " + oldShortURL + " не найдена!\n");
    }

    // Функция отображения всех коротких ссылок вместе с длинными для определенного пользователя
    public static void displayAllLinks(String UUID) {
        System.out.println("\nВаши ссылки: ");
        System.out.println("-----------------------------------");
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.getString("UUID").equals(UUID)) {
                System.out.println("Короткая ссылка: " + jsonObject.getString("shortURL"));
                System.out.println("Длинная ссылка: " + jsonObject.getString("longURL"));
                System.out.println("Количество кликов: " + jsonObject.getInt("num_of_clicks"));
                System.out.println("-----------------------------------\n");
            }
        }
    }

}
