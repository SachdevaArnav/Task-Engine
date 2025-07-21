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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class search2 extends SimpleFileVisitor<Path> {

    String query;
    DateTimeQueryPraser.ParsedDateTime datetime;
    TreeMap<Integer, List<Path>> ScoreBoard = new TreeMap<>();
    // int totalLength = 0;
    Pattern UUID = Pattern.compile("[a-fA-F0-9]{8}-([a-fA-F0-9]{4}-){3}[a-fA-F0-9]{12}");
    // ppt and pptx and like r not connected together and that thing is not added
    // for checking file extension correct that
    private static final List<Set<String>> extensionGroups = List.of(
            // Documents (merged: word, text, openoffice, latex, log, document)
            Set.of("doc", "docx", "odt", "rtf", "txt", "pages", "wpd", "pdf", "log", "tex", "latex", "text", "notes",
                    "notepad", "openoffice", "word", "document"),

            // Presentations
            Set.of("ppt", "pptx", "key", "presentation", "slides"),

            // Spreadsheets (merged: excel + spreadsheet)
            Set.of("xls", "xlsx", "csv", "dif", "excel", "spreadsheet"),

            // Images (merged: screenshot + photo + image)
            Set.of("jpg", "jpeg", "png", "gif", "bmp", "psd", "tiff", "ico", "image", "img", "photo", "screenshot"),

            // Audio
            Set.of("mp3", "m4a", "aac", "wav", "flac", "aiff", "mid", "midi", "audio"),

            // Video
            Set.of("mp4", "mov", "avi", "flv", "mpeg", "mkv", "video"),

            // Archive / Compressed
            Set.of("zip", "rar", "tar", "7z", "gz", "archive", "compressed"),

            // Code / Programming (merged: code, java, python, web)
            Set.of("java", "class", "jar", "py", "python", "cpp", "c", "js", "html", "css", "xml", "json", "sql",
                    "code", "web"),

            // Email
            Set.of("eml", "msg", "email"));

    public static final Set<String> genericNames = new HashSet<>();
    static {
        for (Set<String> s : extensionGroups) {
            genericNames.addAll(s);
        }
    }

    public search2(
            String query, DateTimeQueryPraser.ParsedDateTime dateTime) {
        this.query = query;
        this.datetime = dateTime;
    }

    String[] goodextList = {
            "doc", "docx", "odt", "rtf", "txt", "pages", "wpd", "pdf", "ppt", "pptx", "key",
            "xls", "xlsx", "csv", "dif", "jpg", "jpeg", "png", "gif", "bmp", "psd", "tiff", "ico",
            "mp3", "m4a", "aac", "wav", "flac", "aiff", "mid", "midi",
            "mp4", "mov", "avi", "flv", "mpeg", "mkv"
    };
    String[] blacklist = {
            "AppData", "Roaming", "bin", "build", "cache", "logs",
            ".git", ".svn", ".config", ".terraform", ".npm", "temp",
            "tmp", ".gradle", ".vscode", "release", "node_modules", "venv",
            "__pycache__", "$RECYCLE.BIN", "platforms", "res", "layout", "build-tools", "ndk", "emulator",
            "gen", "obj", "plugins", "tzdata"
    };

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        if (!query.matches("(?i).*folder.*") && !query.matches("(?i).*directory.*")
                && (file.getFileName().toString().replaceAll("[^A-Za-z0-9]", "").length() <= 35)
                && (Files.isRegularFile(file))) {
            Matcher UUmatch = UUID.matcher(file.getFileName().toString());
            if (UUmatch.find()) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            int score = FuzzyScoring(file);
            if (score > 0) {
                addRequired(score, file);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
        Pattern[] uselessFolders = {
                Pattern.compile("android-\\d+"),
                Pattern.compile("drawable-.*"),
                Pattern.compile("^(?i)(java|jdk|python|gcc|node|openjdk|dotnet)[-+][\\d\\.]+.*")
        };
        for (int i = 0; i < uselessFolders.length; i++) {
            if (dir.getFileName() != null) {
                if (uselessFolders[i].matcher(dir.getFileName().toString()).find()) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }
        }
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
        if (dir.getFileName() != null && dir.getFileName().toString().replaceAll("[^A-Za-z0-9]", "").length() <= 30) {
            if (query.matches("(?i).*folder.*") || query.matches("(?i).*dir.*")) {
                int score = FuzzyScoring(dir);
                if (score > 0) {
                    addRequired(score, dir);
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }

    // A possible flaw here can be that queries like 'start restart' would give a
    // double rating to a file with 'restart' words in name
    // interestingly if query was 'restart start' then it will be rated correctly
    // ->So using sorted array (sorted based on number of characters NOT
    // alphabetical order) and remving the token from the name/preName once used
    String quickName;

    public int FuzzyScoring(Path file) {
        int score = 0;
        String name = " " + file.getFileName().toString().replaceAll("[^a-zA-Z0-9. ]", " ") + " ";
        String ext = "";
        boolean extMatched = true;
        boolean AtleastOneExact = false;
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
        boolean MultiWord = false;
        String bareString = quickName.strip();
        String[] alphaQuickName = quickName.split("[^A-Za-z]");
        for (int i = 0; i < bareString.length() - 1; i++) {
            if (bareString.charAt(i) == ' ') {
                MultiWord = true;
                bareString = null;
                break;
            }
        }
        String preName = " " + file.getParent().toString().replaceAll("[^a-zA-Z0-9 ]", " ") + " ";
        String[] Tokens1 = query.replaceAll("[^a-zA-Z0-9 ]", " ").split(" ");
        Arrays.sort(Tokens1, Comparator.comparing(s -> s.length()));
        String[] Tokens = new String[Tokens1.length];
        for (int i = 0; i < Tokens1.length; i++) {
            Tokens[i] = Tokens1[Tokens1.length - 1 - i];
        }
        Tokens1 = null;
        boolean safeExtUsed = extMatched;
        for (String token : Tokens) {
            if (token != null && (!token.equals(""))) {
                if (!safeExtUsed) {
                    if (token.equalsIgnoreCase(ext)) {
                        score += 70;
                        extMatched = true;
                        safeExtUsed = true;
                    } else {
                        for (Set<String> value : extensionGroups) {
                            if (!extMatched && value.contains(token.toLowerCase())) {
                                if (value.contains(ext.toLowerCase())) {
                                    score += 60;
                                    extMatched = true;
                                    safeExtUsed = true;
                                    break;
                                }
                                for (String a : alphaQuickName) {
                                    if (value.contains(a.toLowerCase())) {
                                        score += 60;
                                        safeExtUsed = true;
                                        alphaQuickName = null;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                String quickNameClean = quickName.replaceAll("[0-9 ]", "");
                String tokenClean = token.replaceAll("[0-9 ]", "");
                search2.what result = null;
                if (quickName.replaceAll(" ", "").length() > 2) {
                    if (quickName.matches("(?i).*\\b" + token + "\\b.*")) {
                        score += 70;
                        quickName = " " + quickName.replaceFirst("(?i)\\b" + Pattern.quote(token) + "\\b", "")
                                + " ";
                        if (!genericNames.contains(token)) {
                            AtleastOneExact = true;
                        }
                    } else if (quickNameClean.length() > 2 && tokenClean.length() > 2
                            && quickNameClean.equalsIgnoreCase(tokenClean)) {
                        score += 70;
                        if (!genericNames.contains(token)) {
                            AtleastOneExact = true;
                        }
                    } else if (quickName.toLowerCase().contains(token.toLowerCase())) {
                        score += 55;
                        quickName = " " + quickName.replaceFirst("(?i)" + Pattern.quote(token), "") + " ";
                        if (!genericNames.contains(token)) {
                            AtleastOneExact = true;
                        }
                    } else if ((result = DL_light(quickName.toLowerCase(), token.toLowerCase())).match) {
                        score += 55;
                        quickName = " " + quickName.replaceFirst("(?i)" + result.replacement, "") + " ";
                    } else if (preName.matches("(?i).*\\b" + token + "\\b.*")) {
                        score += 60;
                        preName = " " + preName.replaceFirst("(?i)\\b" + Pattern.quote(token) + "\\b", "") + " ";

                    } else if (preName.toLowerCase().contains(token.toLowerCase())) {
                        score += 45;
                        preName = " " + preName.replaceFirst("(?i)" + Pattern.quote(token), "") + " ";

                    } else if ((result = DL_light(preName.toLowerCase(), token.toLowerCase())).match) {
                        score += 45;
                        preName = " " + preName.replaceFirst("(?i)" + result.replacement, "") + " ";
                    } else {
                        score -= 20;// penalty for useless tokens
                    }
                    if (preName.replaceAll(" ", "").length() <= 2) {
                        score += 200;
                    }
                } else if ((MultiWord || extMatched)) {
                    score += 100;
                } else {
                    score -= 20;// penalty for useless tokens
                }
            }
        }
        if (AtleastOneExact) {
            if (score > 0 && !safeExtUsed)

            {
                for (String e : goodextList) {
                    if (e.equalsIgnoreCase(ext)) {
                        score += 55;
                        break;
                    }
                }
            }
            if (score > 0 && datetime != null && datetime.start != null) {
                if (datetime.end != null) {
                    for (LocalDateTime dt : TimeInLocal(file)) {
                        if (dt != null
                                && (((truncate(dt, datetime.grain)).isAfter(truncate(datetime.start, datetime.grain))
                                        && (truncate(dt, datetime.grain))
                                                .isBefore(truncate(datetime.end, datetime.grain)))
                                        || (truncate(dt, datetime.grain))
                                                .equals(truncate(datetime.start, datetime.grain))
                                        || (truncate(dt, datetime.grain))
                                                .equals(truncate(datetime.end, datetime.grain)))) {
                            score += 70;
                            break;
                        }
                    }
                } else {
                    for (LocalDateTime dt : TimeInLocal(file)) {
                        if (dt != null
                                && (truncate(dt, datetime.grain)).equals(truncate(datetime.start, datetime.grain))) {
                            score += 70;
                            break;
                        }
                    }
                }
            }
            String[] pieces = file.toString().split(Pattern.quote(File.separator));
            int parts = pieces.length;
            if (parts > 8) {
                score -= (parts - 8) * 25;
            }
            return score;
        } else
            return 0;
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

    public static search2.what DL_light(String quickname, String b) {
        b = b.replaceAll("[^A-Za-z]", "");
        boolean match = false;
        String[] quickarr;
        if (!b.equals("")) {
            if (Math.abs(quickname.length() - b.length()) <= 2) {
                quickarr = new String[] { quickname.strip().replaceAll("[^A-Za-z]", "") };
            } else {
                quickarr = quickname.strip().split("[^A-Za-z]+");
            }
            for (String a : quickarr) {
                int i = 0, j = 0, edits = 0;
                int threshold;
                if (!a.equals("")) {
                    int diff = Math.abs(b.length() - a.length());
                    if (diff > 2) {
                        continue;
                    } else {
                        if (a.length() <= 3) {
                            threshold = 0;
                        } else if (a.length() <= 5) {
                            threshold = 1;
                        } else if (a.length() <= 8) {
                            threshold = 2;
                        } else if (a.length() <= 15) {
                            threshold = 4;
                        } else {
                            threshold = 6;
                        }
                        if (diff > threshold) {
                            continue;
                        } else {
                            while (i < a.length() && j < b.length()) {
                                if (a.charAt(i) == b.charAt(j)) {
                                    i++;
                                    j++;
                                } else {
                                    edits++;
                                    if (edits > threshold) {
                                        break;
                                    }
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
                            if (edits <= threshold) {
                                if (i < a.length() || j < b.length())
                                    edits += (a.length() - i) + (b.length() - j);
                            }
                            match = edits <= threshold;
                            if (match) {
                                return new search2.what(match, a);
                            }
                        }
                    }
                }
            }
        }
        return new search2.what(match, null);
    }

    public static class what {

        public boolean match;
        public String replacement;

        public what(boolean match, String replacement) {
            this.match = match;
            this.replacement = replacement;
        }
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
                return dt;
        }
    }

    public void addRequired(int Score, Path file) {
        if (ScoreBoard.size() != 0) {
            int key = ScoreBoard.firstKey();
            if (ScoreBoard.size() < 6) {
                ScoreBoard.computeIfAbsent(Score, k -> new ArrayList<>()).add(file);
                // totalLength += 1;
            } else if (key <= Score) {
                if (ScoreBoard.containsKey(Score)) {
                    ScoreBoard.get(Score).add(file);
                    // totalLength += 1;
                } else {
                    // totalLength = totalLength + 1 - ScoreBoard.get(key).size();
                    ScoreBoard.remove(key);
                    ScoreBoard.put(Score, new ArrayList<>(List.of(file)));
                }
            }
        } else {
            ScoreBoard.computeIfAbsent(Score, k -> new ArrayList<>()).add(file);
            // totalLength += 1;
        }
    }

    public TreeMap<Integer, List<Path>> getScore() {
        return ScoreBoard;
    }
}
