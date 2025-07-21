import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class sending_query {
    public static List<Path> file_search(String input) {
        String x = referenceTime.simplify(input).replaceAll("[^A-Za-z0-9 ]", " ");
        DateTimeQueryPraser.ParsedDateTime datetime = DateTimeQueryPraser.parse(x);
        File[] roots = File.listRoots();
        search2 visitor = new search2(
                DateTimeQueryPraser.getCleanInput(), datetime);
        try {
            for (File Drive : roots) {
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
                }
            } else {
                System.out.println(ScoreList);
            }
            return ScoreList;
        } catch (Exception e) {
            System.err.print(e);
            return null;
        }

    }
}
