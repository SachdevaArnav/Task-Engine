import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class search2 extends SimpleFileVisitor<Path> {

    String query;
    DateTimeQueryPraser.ParsedDateTime datetime;
    TreeMap<Integer, List<Path>> ScoreBoard = new TreeMap<>();
    private static final Map<String, List<String>> extension_map = Map.ofEntries(

            // Documents
            Map.entry("pdf", List.of("pdf")),
            Map.entry("word", List.of("doc", "docx", "odt", "rtf", "txt", "pages", "wpd")),
            Map.entry("text", List.of("txt", "rtf")),
            Map.entry("openoffice", List.of("odt")),
            Map.entry("latex", List.of("tex")),
            Map.entry("notes", List.of("txt", "rtf")),
            Map.entry("log", List.of("log")),
            Map.entry("document", List.of("doc", "docx", "odt", "rtf", "txt", "pages", "wpd", "pdf")),
            Map.entry("notepad", List.of("txt", "rtf")),
            // Presentations
            Map.entry("presentation", List.of("ppt", "pptx", "key")),
            Map.entry("powerpoint", List.of("ppt", "pptx")),

            // Spreadsheets
            Map.entry("excel", List.of("xls", "xlsx", "csv")),
            Map.entry("spreadsheet", List.of("xls", "xlsx", "csv", "dif")),

            // Images
            Map.entry("image", List.of("jpg", "jpeg", "png", "gif", "bmp", "psd", "tiff", "ico")),
            Map.entry("photo", List.of("jpg", "jpeg", "png", "gif")),
            Map.entry("screenshot", List.of("png", "jpg")),

            // Audio
            Map.entry("audio", List.of("mp3", "m4a", "aac", "wav", "flac", "aiff", "mid", "midi")),

            // Video
            Map.entry("video", List.of("mp4", "mov", "avi", "flv", "mpeg", "mkv")),

            // Compressed
            Map.entry("archive", List.of("zip", "rar", "tar", "7z", "gz")),
            Map.entry("compressed", List.of("zip", "rar", "7z", "tar", "gz")),

            // Code / Programming
            Map.entry("code", List.of("java", "py", "cpp", "c", "js", "html", "css", "xml", "json", "sql")),
            Map.entry("java", List.of("java", "class", "jar")),
            Map.entry("python", List.of("py")),
            Map.entry("web", List.of("html", "css", "js")),

            // Email
            Map.entry("email", List.of("eml", "msg")));

    public search2(
            String query, DateTimeQueryPraser.ParsedDateTime dateTime) {
        this.query = query;
        this.datetime = dateTime;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        int score = FuzzyScoring(file);
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
            int score = FuzzyScoring(dir);
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
    public int FuzzyScoring(Path file) {
        int score = 0;
        String name = " " + file.getFileName().toString().replaceAll("[^a-zA-Z0-9. ]", " ") + " ";
        String quickName;
        String ext = "";
        boolean extMatched = true;
        if (Files.isRegularFile(file)) {
            if (name.contains(".")) {
                String[] nameq = name.split("\\.(?=[^\\.]*$)");
                quickName = nameq[0].replaceAll("\\.", " ");
                ext = nameq[1].replaceAll(" ", "");
                extMatched = false;
            } else {
                quickName = name;
            }
        } else {
            quickName = name.replaceAll("\\.", " ");
        }
        String preName = " " + file.getParent().toString().replaceAll("[^a-zA-Z0-9 ]", " ") + " ";
        String[] Tokens = query.replaceAll("[^a-zA-Z0-9 ]", " ").split(" ");
        Arrays.sort(Tokens, Comparator.comparing(s -> s.length()));
        for (String token : Tokens) {
            if ((!token.equals(""))) {
                if (!extMatched && extension_map.containsKey(token)) {
                    for (String e : extension_map.get(token)) {
                        if (e.equalsIgnoreCase(ext)) {
                            score += 70;
                            extMatched = true;
                        }
                    }
                } else if (!quickName.replaceAll(" ", "").equals("")) {
                    if (quickName.matches("(?i).*\\b" + token + "\\b.*")) {
                        score += 70;
                        quickName = " " + quickName.replaceFirst("(?i)\\b" + Pattern.quote(token) + "\\b", "")
                                + " ";
                        if (quickName.replaceAll(" ", "").equals("") && extMatched) {
                            score += 100;
                        }
                    } else if (quickName.toLowerCase().contains(token.toLowerCase())) {
                        score += 55;
                        quickName = " " + quickName.replaceFirst("(?i)" + Pattern.quote(token), "") + " ";
                    } else if (DL_light(quickName.toLowerCase(), token.toLowerCase())) {
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
        }
        if (score > 0) {
            for (LocalDateTime dt : TimeInLocal(file)) {
                if (dt != null && (truncate(dt, datetime.grain)).equals(truncate(datetime.start, datetime.grain))) {
                    score += 70;
                    break;
                }
            }
        }
        String[] pieces = file.toString().split(Pattern.quote(File.separator));
        int parts = pieces.length;
        if (parts > 8) {
            score -= (parts - 8) * 25;
        }
        return score;
    }

    public LocalDateTime[] TimeInLocal(Path file) {
        // Parse the string into an Instant (point in time in UTC)
        LocalDateTime[] TimeArray;
        try {
            Map<String, Object> attr = Files.readAttributes(file,
                    "lastModifiedTime,lastAccessTime,creationTime");

            Object[] IntialArray = attr.values().toArray();
            TimeArray = new LocalDateTime[IntialArray.length];
            for (int i = 0; i < IntialArray.length; i++) {
                try {
                    Instant instant = Instant.parse(IntialArray[i].toString());
                    // Convert Instant to LocalDateTime in the system default time zone
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    TimeArray[i] = localDateTime;
                } catch (Exception e) {
                    TimeArray[i] = LocalDateTime.MIN;
                    System.out.println(file.toString() + " falied for time array at point " + i);
                }
            }
        } catch (Exception e) {
            System.out.println("hello" + e);
            TimeArray = new LocalDateTime[] { LocalDateTime.MIN };
        }
        return TimeArray;
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

    public static LocalDateTime truncate(LocalDateTime dt, ChronoUnit unit) {
        switch (unit) {
            case YEARS:
                return LocalDateTime.of(dt.getYear(), 1, 1, 0, 0);
            case MONTHS:
                return LocalDateTime.of(dt.getYear(), dt.getMonth(), 1, 0, 0);
            case DAYS:
            case HOURS:
            case MINUTES:
            case SECONDS:
            case MILLIS:
            case MICROS:
            case NANOS:
                return dt.truncatedTo(unit);
            default:
                throw new UnsupportedOperationException("Unsupported truncation unit: " + unit);
        }
    }

    public TreeMap<Integer, List<Path>> getScore() {
        return ScoreBoard;
    }
}
