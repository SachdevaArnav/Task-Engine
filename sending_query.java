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
        System.out.println("date-time (it should be like 'Jan 15, 2019 20:12')");
        // this is getting case sensitive tried all combinations
        DateTimeQueryPraser.ParsedDateTime datetime = DateTimeQueryPraser.parse(input.nextLine().strip());
        // DateTimeFormatter.ofPattern("MMM dd, yyyy HH", Locale.ENGLISH));
        // String date = input.nextLine();
        File[] roots = File.listRoots();
        search2 visitor = new search2(x, datetime);
        // String[] timeStrings = { "today", "yesterday", "week", "month", "year",
        // "recent", "recently", "older", "latest",
        // "newest", "old" };
        try {
            for (File Drive : roots) {
                // String Drive = "D:\\OneDrive\\Desktop\\Java\\ARS_Platform\\.vscode";
                Files.walkFileTree((Drive).toPath(), visitor);
            }
            TreeMap<Integer, List<Path>> ScoreBoard = visitor.getScore();
            List<Path> ScoreList = ScoreBoard.get(ScoreBoard.lastKey());
            System.out.println(ScoreList);
            // if (ScoreList.size() >= 1) {// >1
            // System.out.println("Tell more info for finding the exact file");
            // String info = input.nextLine().toLowerCase();
            // for (String timestr : timeStrings) {
            // if (info.contains(timestr)) {
            // for (int i = 0; i < ScoreList.size(); i++) {
            // Map<String, Object> attr = Files.readAttributes(ScoreList.get(i),
            // "lastModifiedTime,lastAccessTime");
            // ZonedDateTime zd =
            // ZonedDateTime.parse(attr.get("lastModifiedTime").toString(),
            // DateTimeFormatter.ISO_INSTANT);
            // Date date = Date.from(zd.toInstant());
            // }
            // }
            // }
            // }
            // System.out.println("");
        } catch (Exception e) {
            System.err.print(e);
        }
        input.close();
    }
}
