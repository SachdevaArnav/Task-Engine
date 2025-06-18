import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class search2 extends SimpleFileVisitor<Path> {

    String query;

    public search2(String query) {
        this.query = query;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        int score = FuzzyScoring(file.toString(), query);
        if (score > 0) {
            System.out.println(file.toString());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
        String[] blacklist = {
                "AppData", "Roaming", "bin", "build", "cache", "logs",
                ".git", ".svn", ".config", ".terraform", ".npm", "temp",
                "tmp", "gradle", ".vscode", "release", "node_modules", "venv",
                "__pycache__"
        };
        for (String a : blacklist) {
            if (dir.getFileName() != null) {
                if (dir.getFileName().toString().equalsIgnoreCase(a)) {
                    return FileVisitResult.SKIP_SUBTREE;
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
        if (txt.toLowerCase().contains(query.toLowerCase())) {
            score += 1;
        }
        return score;
    }
}
