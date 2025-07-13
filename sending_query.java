import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class sending_query {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Related text");
        String x = referenceTime.simplify(input.nextLine());
        DateTimeQueryPraser.ParsedDateTime datetime = DateTimeQueryPraser.parse(x);
        File[] roots = File.listRoots();
        search2 visitor;

        visitor = new search2(
                DateTimeQueryPraser.getCleanInput(), datetime);

        // String[] timeStrings = { "today", "yesterday", "week", "month", "year",
        // "recent", "recently", "older", "latest",
        // "newest", "old" };
        try {
            for (File Drive : roots) {
                // String Drive = "D:\\sachd\\Desktop\\Rakendra\\Wallpapers\\spider man.jpg";
                Files.walkFileTree((Drive).toPath(), visitor);
            }
            TreeMap<Integer, List<Path>> ScoreBoard = visitor.getScore();
            List<Path> ScoreList = ScoreBoard.get(ScoreBoard.lastKey());
            if (ScoreList.size() > 1) {
                int count = 0;
                for (Map.Entry<Integer, List<Path>> entry : ScoreBoard.descendingMap().entrySet()) {
                    int size = entry.getValue().size();
                    if (count < 10) {
                        if (size + count <= 10) {
                            System.out.println(entry.getKey() + " = " + entry.getValue());
                            count += size;
                        } else {
                            System.out.println(entry.getKey() + " = " + entry.getValue());
                            break;
                        }
                    } else {
                        break;
                    }
                    // count++;
                    // if (count == 2)
                    // break;
                }
            } else {
                System.out.println(ScoreList);
            }
            // System.out.println(ScoreList);
            // System.out.println(ScoreBoard.lastKey());

        } catch (Exception e) {
            System.err.print(e);
        }
        input.close();
    }
}
