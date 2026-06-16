import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class sending_query {
    public static Path file_search(Scanner input, String inputText) {
        String x = referenceTime.simplify(inputText).replaceAll("[^A-Za-z0-9 ]", " ");
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
            if (ScoreList.size() == 1) {
                return ScoreList.get(0);
            } else if (ScoreList.size() > 1) {
                // instead of just printing the path of the files
                // this part will allow selecting the file for opening
                // so basically this function instead of returning List<Path> will simply pass
                // the final Path selected
                System.out.println("Found multiple similar files");
                System.out.println("Please select the appropriate file to proceed");
                int serial = 1;
                int intialSerial = 0;
                for (Map.Entry<Integer, List<Path>> entry : ScoreBoard.descendingMap().entrySet()) {
                    intialSerial = serial;
                    List<Path> entryList = entry.getValue();
                    for (Path path : entryList) {
                        System.out.println(serial + " = " + path);
                        serial++;
                    }
                    System.out.println();
                    System.out.println("enter '0' if none of these.....");
                    int y = input.nextInt();
                    if (y < 0 || y > serial) {
                        throw new Exception("Invalid indexed entered");
                    }
                    if (y > 0) {
                        if (y >= intialSerial) {
                            return entryList.get(y - intialSerial);
                        } else {
                            int n;
                            for (Map.Entry<Integer, List<Path>> entry2 : ScoreBoard.descendingMap().entrySet()) {
                                if ((n = entry2.getValue().size()) < y) {
                                    y -= n;
                                } else {
                                    return entry2.getValue().get(y - 1);
                                }
                            }
                            throw new Exception("Invalid indexed entered");
                        }
                    }
                }
            } else {
                throw new Exception("Cannot find any related file");
            }
            return null;
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }

    }
}
