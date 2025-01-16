# ***Итоговый проект «Сервис коротких ссылок»***
___
## ***Как пользоваться сервисом***
___
#### При первом запуске программы, проверьте что в файле userUUID.txt отсутствует информация, или отсутствует данный файл, так как при первом запуске программы туда будет записан ваш UUID.
#### Далее в консоли отобразиться меню с выбором команд:
![image](https://github.com/user-attachments/assets/656400b2-85b6-4ea7-ad28-e40ac6a692c7)
#### Здесь вам необходимо выбрать интересующую вас команду, название каждой говорит само за себя, для того чтобы выбрать команду необходимо ввести в консоль цифру и нажать клавишу Enter, далее следуйте указаниям в зависимости от выбранной операции, для завершения работы введите 8 и нажмите Enter
#### ***Если вы хотите удалить все ссылки или изменить конфигурацию это можно сделать путем редактирования файлов links.json и config.txt соответственно***
## ***Какие команды поддерживаются***
## Данный проект предусматривает меню при помощи котрого можно выбрать одно из следующих действий:
+ ## 1. Сократить ссылку
#### Данный функционал является основным, каждая ссылка является уникальной по скольку, перед тем как создать короткую ссылку производится проверка на совпадение с уже существующими ссылками:
```Java
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
```
#### При этом в соответствии с условие поставленной задачи в случае, если пользователь повторно вводит одну и ту же длинную ссылку, генерируется новая уникальная короткая ссылка
```Java
 // Проверяем, существует ли короткая ссылка для данного UUID и longURL
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.getString("UUID").equals(UUID) && jsonObject.getString("longURL").equals(longURL)) {
                shortURL = jsonObject.getString("shortURL");
 // Генерируем новую ссылку
                updateShortLink(shortURL, UUID, ttl, noc);
                shortURL = jsonObject.getString("shortURL");
                break;
            }
        }
```
+ ## 2. Перейти по короткой ссылке
#### При выборе данной опции происходит переход на прямой портал по длинной ссылке:
```Java

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
```
+ ## 3. Удалить короткую ссылку
#### Данная опция была добавлена поскольку в задании неясно, может ли пользвоатель редактировать или изменять ссылку, поэтому на всякий случай я ее добавил
```Java
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
```
+ ## 4. Изменить короткую ссылку
#### Идентично с предыдущим
```Java
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
```
+ ## 5. Отобразить все ссылки
#### Идентично с предыдущим, при этом хочу отметить, все ссылки отображаются по UUID пользователя, то есть он может видеть и редактировать только те ссылки, которые сам создал
```Java
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
```
+ ## 6. Изменить время жизни ссылки(можно в конфигурационном файле)
#### Опять же из задания не особо было понятно, поэтому я решил добавить отдельно
+ ## 7. Изменить количество переходов ссылки(можно в конфигурационном файле)
#### Идентично с предыдущим
+ ## 8. Выйти
#### Функция завершения работы с программой

____

## ***Для того чтобы протестировать проект можно использовать любую ссылку, если необходимо протестировать возможность использования несколькими пользователями, удалите файл userUUID.txt (убедитесь перед этим что в файле links.json присутствуют записи другого пользователя, также эти записи можно добавить вручную, пример приведен ниже), запустите программу заново, будет сгенерирован новый UUID для пользователя и продолжайте тестирование в соответствии с описанным выше***
Пример данных:
```json
[{
    "time_to_live": 86400000,
    "shortURL": "http://short.url/d1607ab6",
    "created_at": 1733057205213,
    "longURL": "https://github.com/OgBludo/shorturl/new/master?filename=README.md",
    "UUID": "a8d0f27b-5ba4-45aa-bad6-2f28343c1e51",
    "num_of_clicks": 5
}]
``` 

*Важное замечание, время жизни ссылки указано в миллисекундах,проверка производиться каждый раз когда отображается меню, в случае если ссылка текущего пользователя должна быть удалена, ему выводится сообщение об этом.
