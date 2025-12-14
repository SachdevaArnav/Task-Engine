import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class settings {
    protected static boolean defaultweekStartSun = false;
    static {
        defaultweekStartSun = loadfromDisk();
    }

    public static void reload() {
        defaultweekStartSun = loadfromDisk();
    }

    private static boolean loadfromDisk() {
        try (Scanner SettingsReader = new Scanner(new File("settings.cfg"))) {
            String input;
            String[] parts;
            while (SettingsReader.hasNextLine()) {
                input = SettingsReader.nextLine();
                if (input.contains(":")) {
                    if ((parts = input.split(":"))[0].equalsIgnoreCase("StartWeekONsunday")) {
                        return Boolean.parseBoolean(parts[1]);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("User preference for week failed to load");
            System.err.println("Fallback to Monday start week executed");
            return false;
        }
        return false;
    }

    private static void changeSetting(String field, String to) {
        Path settings = Paths.get("Settings.cfg");
        Path temp = Paths.get("Settings.temp");
        try {
            Files.createFile(settings);

        } catch (Exception e) {
            System.err.println(e);
        }
        try {
            Files.createFile(temp);
        } catch (Exception e) {
            System.err.println(e);
        }
        String line;
        try (BufferedReader reader = Files.newBufferedReader(settings);
                BufferedWriter writer = Files.newBufferedWriter(temp)) {
            boolean changeMade = false;
            while (!changeMade && (line = reader.readLine()) != null) {
                if (line.startsWith(field + ":")) {
                    writer.write(field + ":" + to);
                    changeMade = true;
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }
            if (!changeMade) {
                writer.write(field + ":" + to);
                writer.newLine();
            }
            // Atomic replacement
            Files.move(temp, settings, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void setWeekStartDay(boolean sunday) {
        if (sunday != defaultweekStartSun) {
            changeSetting("StartWeekONsunday", Boolean.toString(sunday));
        }
    }

    public static boolean isWeekStartSun() {
        reload();
        return defaultweekStartSun;
    }
}
