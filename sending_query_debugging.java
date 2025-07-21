
// import java.io.File;
// import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
// import java.util.Date;
// import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class sending_query_debugging {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Related text");
        String x = referenceTime.simplify(input.nextLine()).replaceAll("[^A-Za-z0-9 ]", " ");
        DateTimeQueryPraser.ParsedDateTime datetime = DateTimeQueryPraser.parse(x);
        // File[] roots = File.listRoots();
        search2 visitor = new search2(
                DateTimeQueryPraser.getCleanInput(), datetime);// "newest", "old" };
        try {
            // for (File Drive : roots) {
            String Drive = "D:\\OneDrive\\Desktop\\clg\\hostel .jpg";
            Files.walkFileTree(Paths.get(Drive), visitor);
            // }
            TreeMap<Integer, List<Path>> ScoreBoard = visitor.getScore();
            // List<Path> ScoreList = ScoreBoard.get(ScoreBoard.lastKey());
            int count = 0;
            for (Map.Entry<Integer, List<Path>> entry : ScoreBoard.descendingMap().entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
                count++;
                if (count == 2)
                    break;
            }
            // System.out.println(ScoreList);
            // System.out.println(ScoreBoard.lastKey());
        } catch (Exception e) {
            System.err.print(e);
        }
        input.close();
    }
}
