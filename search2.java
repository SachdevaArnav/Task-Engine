import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class search2 extends SimpleFileVisitor<Path> {

    String query;
    TreeMap<Integer, List<Path>> ScoreBoard = new TreeMap<>();

    public search2(
            String query) {
        this.query = query;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        int score = FuzzyScoring(file, query);
        if (score > 0) {
            ScoreBoard.computeIfAbsent(score, k -> new ArrayList<>()).add(file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
        String[] blacklist = {
                "AppData", "Roaming", "bin", "build", "cache", "logs",
                ".git", ".svn", ".config", ".terraform", ".npm", "temp",
                "tmp", ".gradle", ".vscode", "release", "node_modules", "venv",
                "__pycache__", "$RECYCLE.BIN"
        };
        for (String a : blacklist) {
            if (dir.getFileName() != null) {
                if (dir.getFileName().toString().equalsIgnoreCase(a)) {
                    return FileVisitResult.SKIP_SUBTREE;
                } else if (dir.getFileName().toString().startsWith(".")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                // this is windows specific:considering only 'Users'folder in C drive
                else if (dir.getRoot().toString().replaceAll("[\\\\/]+$", "").equalsIgnoreCase("C:")) {
                    if (dir.toString().contains("Users")) {
                        return FileVisitResult.CONTINUE;
                    } else {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        if (dir.getFileName() != null) {
            int score = FuzzyScoring(dir, query);
            if (score > 0) {
                ScoreBoard.computeIfAbsent(score, k -> new ArrayList<>()).add(dir);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    // A possible flaw here can be that queries like 'start restart' would give a
    // double rating to a file with 'restart' words in name
    // interestingly if query was 'restart start' then it will be rated correctly
    // ->So using sorted array (sorted based on number of characters NOT
    // alphabetical order) and remving the token from the name/preName once used
    public int FuzzyScoring(Path file, String query) {
        int score = 0;
        String name = " " + file.getFileName().toString().replaceAll("[^a-zA-Z0-9. ]", " ") + " ";
        String preName = " " + file.getParent().toString().replaceAll("[^a-zA-Z0-9 ]", " ") + " ";
        String[] Tokens = query.replaceAll("[^a-zA-Z0-9 ]", " ").split(" ");
        Arrays.sort(Tokens, Comparator.comparing(s -> s.length()));
        for (String token : Tokens) {
            if ((!token.equals("")) && (!name.replaceAll(" ", "").equals(""))) {
                if (name.matches("(?i).*" + token + ".*")) {
                    score += 70;
                    name = " " + name.replaceFirst("(?i)\\b" + Pattern.quote(token) + "\\b", "") + " ";
                    String quickName;
                    if (name.contains(".")) {
                        quickName = name.split(".(?=[^.]*$)")[0];
                    } else {
                        quickName = name;
                    }
                    if (quickName.replaceAll(" ", "").equals("")) {
                        score += 100;
                    }
                    // this regex is for removing the extension while checking for file name +100
                } else if (name.toLowerCase().contains(token.toLowerCase())) {
                    score += 55;
                    name = " " + name.replaceFirst("(?i)" + Pattern.quote(token), "") + " ";
                } else if (DL_light(name.toLowerCase(), token.toLowerCase())) {
                    score += 55;
                } else if (preName.matches("(?i).*" + token + ".*")) {
                    score += 60;
                    preName = " " + preName.replaceFirst("(?i)\\b" + Pattern.quote(token) + "\\b", "") + " ";
                    if (preName.replaceAll(" ", "").equals("")) {
                        score += 200;
                    }
                } else if (preName.toLowerCase().contains(token.toLowerCase())) {
                    score += 45;
                    preName = " " + preName.replaceFirst("(?i)" + Pattern.quote(token), "") + " ";
                } else if (DL_light(preName.toLowerCase(), token.toLowerCase())) {
                    score += 45;
                }
            }
        }
        String[] pieces = file.toString().split(Pattern.quote(File.separator));// ("[^a-zA-Z0-9]"
        int parts = pieces.length;
        if (parts > 8) {
            score -= (parts - 8) * 25;
        }
        return score;
    }

    public boolean DL_light(String a, String b) {
        int i = 0, j = 0, edits = 0;
        int threshold;
        if (a.length() <= 5) {
            threshold = 1;
        } else if (a.length() <= 8) {
            threshold = 2;
        } else if (a.length() <= 15) {
            threshold = 4;
        } else {
            threshold = 6;
        }
        while (i < a.length() && j < b.length()) {
            if (a.charAt(i) == b.charAt(j)) {
                i++;
                j++;
            } else {
                edits++;
                if (edits > threshold)
                    return false;
                // checking for transposition only adjacent swaps
                if (i + 1 < a.length() && j + 1 < b.length() && a.charAt(i) == b.charAt(j + 1)
                        && a.charAt(i + 1) == b.charAt(j)) {
                    i += 2;
                    j += 2;
                } else if (a.length() > b.length()) {
                    i++;
                } else if (a.length() < b.length()) {
                    j++;
                } else {
                    i++;
                    j++;
                }
            }
        }
        if (i < a.length() || j < b.length())
            edits++;
        return true;
    }

    public TreeMap<Integer, List<Path>> getScore() {
        return ScoreBoard;
    }
}
