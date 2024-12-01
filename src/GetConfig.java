import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GetConfig {
    static int numOfClicks = 0;
    static int timeToLive = 0;

    //Считываем настройки из файла
    public static void SetConfig() {
        String filePath = "src/config.txt";
        Map<String, String> properties = readPropertiesFromFile(filePath);
        if (properties != null) {
            numOfClicks = Integer.parseInt(properties.get("num_of_clicks"));
            timeToLive = Integer.parseInt(properties.get("time_to_live"));
        } else {
            System.out.println("Failed to read properties from file.");
        }
    }

    public static int GetNumOfClicks() {
        return numOfClicks;
    }

    public static int GetTTL() {
        return timeToLive;
    }

    public static Map<String, String> readPropertiesFromFile(String filePath) {
        Map<String, String> properties = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    properties.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return properties;
    }
}
