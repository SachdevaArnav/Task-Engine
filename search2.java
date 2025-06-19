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
        String name = " " + file.getFileName().toString() + " ";
        String preName = " " + file.getParent().toString().replaceAll(Pattern.quote(File.separator), " ") + " ";
        String[] Tokens = query.replaceAll("[^a-zA-Z0-9 ]+", " ").split(" ");
        Arrays.sort(Tokens, Comparator.comparing(s -> s.length()));
        for (String token : Tokens) {
            if ((!token.equals("")) && (!name.replaceAll("[^ ]", "").equals(""))) {
                if (name.toLowerCase().contains(" " + token.toLowerCase() + " ")) {
                    score += 70;
                    name = " " + name.replace("(?!)" + token, "") + " ";
                    if (name.replaceAll("[^ ]", "").equals("")) {
                        score += 100;
                    }
                } else if (name.toLowerCase().contains(token.toLowerCase())) {
                    score += 55;
                    name = " " + name.replace("(?!)" + token, "") + " ";
                } else if (preName.toLowerCase().contains(" " + token.toLowerCase() + " ")) {
                    score += 60;
                    preName = " " + preName.replace("(?!)" + token, "") + " ";
                    if (preName.replaceAll("[^ ]", "").equals("")) {
                        score += 200;
                    }
                } else if (preName.toLowerCase().contains(token.toLowerCase())) {
                    score += 45;
                    preName = " " + preName.replace("(?!)" + token, "") + " ";
                }
            }
        }
        String[] pieces = file.toString().split("[^a-zA-Z0-9]");
        int parts = pieces.length;
        if (parts > 8) {
            score -= (parts - 8) * 25;
        }
        return score;
    }

    public TreeMap<Integer, List<Path>> getScore() {
        return ScoreBoard;
    }
}
