import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class sending_query {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String x = input.nextLine();
        try {
            Files.walkFileTree(Paths.get("D:\\"), new search2(x));
        } catch (Exception e) {
            System.err.print(e);
        }
        input.close();
    }
}
