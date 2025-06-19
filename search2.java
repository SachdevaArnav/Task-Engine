import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class search2 extends SimpleFileVisitor<Path> {

    String query;
    TreeMap<Integer, List<Path>> ScoreBoard = new TreeMap<>();

    public search2(
            String query) {
        this.query = query;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        int score = FuzzyScoring(file.toString(), query);
        if (score > 1) {
            // System.out.println("Score:" + score + file.toString());
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

    public int FuzzyScoring(String txt, String query) {
        int score = 0;
        String[] Tokens = query.split(" ");
        for (String token : Tokens) {
            if (txt.toLowerCase().contains(token.toLowerCase()) && (!token.equals(""))) {
                score += 1;
            }
        }
        return score;
    }

    public TreeMap<Integer, List<Path>> getScore() {
        return ScoreBoard;
    }
}
