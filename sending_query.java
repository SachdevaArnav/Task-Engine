import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.Date;
// import java.time.ZonedDateTime;
import java.util.List;
// import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class sending_query {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Related text");
        String x = input.nextLine();
        DateTimeQueryPraser.ParsedDateTime datetime = DateTimeQueryPraser.parse(x);
        File[] roots = File.listRoots();
        search2 visitor = new search2(x, datetime);
        // String[] timeStrings = { "today", "yesterday", "week", "month", "year",
        // "recent", "recently", "older", "latest",
        // "newest", "old" };
        try {
            for (File Drive : roots) {
                // String Drive =
                // "A:\\Python\\Lib\\site-packages\\jsonschema_specifications-2025.4.1.dist-info";
                Files.walkFileTree((Drive).toPath(), visitor);
            }
            TreeMap<Integer, List<Path>> ScoreBoard = visitor.getScore();
            List<Path> ScoreList = ScoreBoard.get(ScoreBoard.lastKey());
            System.out.println(ScoreList);
            // System.out.println(ScoreBoard.lastKey());

        } catch (Exception e) {
            System.err.print(e);
        }
        input.close();
    }
}
