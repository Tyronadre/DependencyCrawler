import java.io.BufferedReader;
import java.io.FileReader;

public class TxtToJsonInput {
    public static void main(String[] args) {
        try (var input = new BufferedReader(new FileReader(TxtToJsonInput.class.getClassLoader().getResource("photoprism.txt").getFile()))) {
            String line;
            while ((line = input.readLine()) != null) {
                var s = line.split(":");
                if (s.length != 3) {
                    continue;
                }
                System.out.println("{\"groupId\": \"" + s[0] + "\", \"name\": \"" + s[1] + "\", \"version\": \"" + s[2] + "\", \"type\": \"MAVEN\"},");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
