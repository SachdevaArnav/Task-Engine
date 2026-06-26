import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Write a description of class eFriend here.
 *
 * @author (Arnav Sachdeva)
 * @version (1)
 * @date start(18/03/2021)
 */
public class eFriend {
    static VoiceToText_Whisper myWhisper;

    static void voicemsg(String voice) {
        System.out.println(voice);
        try {
            File scriptFile = new File("avaaj.vbs");
            FileWriter myWriter = new FileWriter(scriptFile);
            myWriter.write("Dim message, sapi\n ");
            myWriter.write(" message = \"" + voice + "\"\n");
            myWriter.write(" Set sapi = CreateObject(\"sapi.spvoice\")\n");
            myWriter.write("sapi.Speak message");
            myWriter.close();
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("wscript.exe", scriptFile.getAbsolutePath());
                Process process = processBuilder.start();
                process.waitFor();
            }

            catch (Exception ev) {
                System.out.println("error in speaking");
            }
        } catch (Exception evv) {
            System.out.println("error in overall production of voice");
        }
    }

    static final HashMap<String, String> AppList = new HashMap<String, String>();

    static void loadApps() {
        try (BufferedReader myReader = new BufferedReader(
                new FileReader("AppList.txt"))) {
            String database;
            while (((database = myReader.readLine()) != null)) {
                if (database.contains(":") && database.contains("|")) {
                    String[] arrOfdata = database.split("\\|");
                    String value = null;
                    if ((value = arrOfdata[arrOfdata.length - 1]).contains(":")) {
                        value = value.replaceFirst(":", "");
                        for (String part : arrOfdata) {
                            if (part != null && (part = part.strip()) != "")
                                AppList.put(part.replaceAll("////s+", " ").toLowerCase(), value.toLowerCase());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("App List loading failed");
        }
    }

    static String getApp(String app) {
        return AppList.get(app);
    }

    private static void desktopSupport(Scanner input, String appli) {
        byte error = 0;
        boolean worked = false;
        Runtime rt = Runtime.getRuntime();

        try {
            // 1. Try direct exec
            try {
                rt.exec(appli.split(" "));
                worked = true;
            } catch (Exception ignored) {
            }

            // 2. Try .exe
            if (!worked) {
                try {
                    rt.exec((appli + ".exe").split(" "));
                    worked = true;
                } catch (Exception ignored) {
                }
            }

            // 3. Try browsing as URI
            if (!worked) {
                try {
                    if (Desktop.isDesktopSupported()
                            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(appli));
                        worked = true;
                    }
                } catch (Exception ignored) {
                }
            }

            // 4. Try browsing appli.exe as URI
            if (!worked) {
                try {
                    if (Desktop.isDesktopSupported()
                            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(appli + ".exe"));
                        worked = true;
                    }
                } catch (Exception ignored) {
                }
            }

            // 5. Try mapped app
            if (!worked) {
                try {
                    String appAction = getApp(appli);
                    if (appAction == null)
                        throw new NullPointerException("App not in the list");

                    ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "Start", appAction);
                    pb.start();
                    worked = true;
                } catch (Exception ignored) {
                }
            }

            // 6. Try mapped app via browser
            if (!worked) {
                try {
                    String appAction = getApp(appli);
                    if (appAction == null)
                        throw new NullPointerException("App not in the list");

                    if (Desktop.isDesktopSupported()
                            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(appAction));
                        worked = true;
                    }
                } catch (Exception ignored) {
                }
            }

            // 7. Try website mapping
            if (!worked) {
                try {
                    String appAction = getApp(appli + " website");
                    if (appAction == null)
                        throw new NullPointerException("Website not in the list");

                    if (Desktop.isDesktopSupported()
                            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(appAction));
                        worked = true;
                    }
                } catch (Exception ignored) {
                }
            }

            // 8. Try opening as file
            if (!worked) {
                try {
                    Desktop.getDesktop().open(new File(appli));
                    worked = true;
                } catch (Exception ignored) {
                }
            }

            // 9. Try explorer
            if (!worked) {
                try {
                    if (new File(appli).exists()) {
                        ProcessBuilder pb = new ProcessBuilder("explorer.exe", appli);
                        Process p = pb.start();
                        p.waitFor();
                        worked = true;
                    }
                } catch (Exception ignored) {
                }
            }

            // 10. File search fallback
            if (!worked) {
                try {
                    Path file = sending_query.file_search(input, appli);
                    if (file == null)
                        throw new Exception("no related file found");
                    try {
                        Desktop.getDesktop().open(file.toFile());
                        worked = true;
                    } catch (Exception e) {
                        ProcessBuilder pb = new ProcessBuilder("explorer.exe", file.toString());
                        Process p = pb.start();
                        p.waitFor();
                        worked = true;
                    }
                } catch (Exception ignored) {
                    error = 1;
                }
            }

            // Final fallback: Google
            if (!worked && error == 1) {
                try {
                    if (Desktop.isDesktopSupported()
                            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(
                                new URI("https://www.google.com/search?q="
                                        + appli.replaceAll(" ", "+")));
                    }
                } catch (Exception e) {
                    voicemsg("Sorry! can't get info regarding " + appli);
                }
            }

            if (worked && error == 0) {
                voicemsg("Opening " + appli);
            }

        } catch (Exception e) {
            voicemsg("Sorry! can't get info regarding " + appli);
        }
    }

    public static void openTarget(Scanner input, String appli) {
        appli = appli.strip();
        if (appli.contains("www.") && !(appli.contains("http"))) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://" + appli));
                }
            } catch (Exception ee) {
                voicemsg("Sorry!Can't reach this webpage.");
                voicemsg("Please check the URL");
            }
        } else if (appli.contains(".") && !(appli.contains(" "))) {
            if ((appli.contains("http"))) {
                String web;
                if (!appli.contains("://")) {
                    if (appli.contains("https")) {
                        web = "https://" + appli.split("https")[1].strip();
                    } else {
                        web = "http://" + appli.split("http")[1].strip();
                    }
                } else {
                    web = appli;
                }
                try {
                    if (Desktop.isDesktopSupported()
                            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(web));
                    }
                } catch (Exception ee) {
                    voicemsg("Sorry!Can't reach this webpage.");
                    voicemsg("Please check the URL");
                }
            } else {
                try {
                    InetAddress.getByName(appli);// this method tries to resolve given name into
                } catch (Exception p) {
                    desktopSupport(input, appli);
                    return;
                }
                try {
                    if (Desktop.isDesktopSupported()
                            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI("http://" + appli));
                    }
                } catch (Exception ee) {
                    voicemsg("Sorry!Can't reach this webpage.");
                    voicemsg("Please check the URL");
                }
            }
        } else {
            desktopSupport(input, appli);
        }
    }
}
