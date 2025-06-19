import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

public class sending_query {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String x = input.nextLine();
        File[] roots = File.listRoots();
        search2 visitor = new search2(x);
        try {
            for (File Drive : roots) {
                Files.walkFileTree(Drive.toPath(), visitor);
            }
            TreeMap<Integer, List<Path>> ScoreBoard = visitor.getScore();
            System.out.println(ScoreBoard.get(ScoreBoard.lastKey()));
        } catch (Exception e) {
            System.err.print(e);
        }
        input.close();
    }
}
