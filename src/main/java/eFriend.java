import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                new FileReader("DATABASEKABASIC.txt"))) {
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

    private static void desktopSupport(String appli) {
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
                    List<Path> searchResult = sending_query.file_search(appli);
                    if (searchResult == null || searchResult.isEmpty())
                        throw new Exception("no related file found");

                    if (searchResult.size() == 1) {
                        Path file = searchResult.get(0);
                        try {
                            Desktop.getDesktop().open(file.toFile());
                            worked = true;
                        } catch (Exception e) {
                            ProcessBuilder pb = new ProcessBuilder("explorer.exe", file.toString());
                            Process p = pb.start();
                            p.waitFor();
                            worked = true;
                        }
                    } else {
                        voicemsg("List of related files shown");
                        error = -1;
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

    // there are some cases where its misfiring like 'open allen act papers'
    // this is somehow instead opens ssc X papers pdf
    // also in case of multiple outputs at times this the last one have more garbage
    // values
    // and its shown even when there even if they are outnumbered
    public static void main(String[] args) {
        myWhisper = new VoiceToText_Whisper();
        loadApps();
        AccessData.loadData();
        Scanner input = new Scanner(System.in);
        // voicemsg("i am ur Friend");
        voicemsg("Welcome");
        boolean _continue = true;
        // if required then for finding any random app use explorer.exe ->
        // shell:AppsFolder
        // in this window use eFriend's ability to see and percive and search for the
        // required app
        // near 100% chance for all launchable apps
        // need to define like queries asking for "apps" explicitly only use this
        while (_continue) {
            // String app = input.nextLine().strip().toLowerCase();
            String aq = input.nextLine();
            String app;
            if (aq.equals("VOICE_MODE")) {
                app = myWhisper.GetVoiceInput().strip().toLowerCase().replaceAll("[^a-zA-Z0-9]$", "");
                System.out.println(app);
            } else
                app = aq.strip().toLowerCase().replaceAll("[^a-zA-Z0-9]$", "");
            ;

            String appspace = " " + app + " ";
            String Qword;
            if (app.contains("open ")) {
                String[] arrOfStr = app.split("open ", 2);
                String appli = arrOfStr[1].strip();
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
                            desktopSupport(appli);
                            continue;
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
                    desktopSupport(appli);
                }
                // <>
            } else if (appspace.contains(" learn ")
                    || appspace.contains(" remember ")) {
                String[] arrOfStr;
                if (app.contains("learn that"))
                    arrOfStr = app.split("learn that", 2);
                else if (app.contains("learn this"))
                    arrOfStr = app.split("learn this", 2);
                else if (app.contains("remember that"))
                    arrOfStr = app.split("remember that", 2);
                else if (app.contains("remember this"))
                    arrOfStr = app.split("remember this", 2);
                else if (app.contains("learn"))
                    arrOfStr = app.split("learn", 2);
                else
                    arrOfStr = app.split("remember", 2);
                for (String appli : arrOfStr) {
                    if (appli != null && appli.strip() != "") {
                        boolean matchFoundis = appli.contains(" is ");
                        boolean matchFoundist = appli.contains(" is the ");
                        boolean matchFoundas = appli.contains(" as ");
                        boolean matchFoundast = appli.contains(" as the ");
                        if (matchFoundis || matchFoundas || matchFoundist || matchFoundast) {
                            String[] applix;
                            if (matchFoundist) {
                                applix = appli.split(" is the ", 2);
                            } else if (matchFoundis) {
                                applix = appli.split(" is ", 2);
                            } else if (matchFoundast) {
                                applix = appli.split(" as the ", 2);
                            } else {
                                applix = appli.split(" as ", 2);
                            }
                            for (String a : applix) {
                                a = a.strip();
                            }
                            ArrayList<ArrayList<AccessData>> previous = new ArrayList<ArrayList<AccessData>>();
                            // this needs to be a arraylist as Array of ArrayList is not allowed; the size
                            // is
                            // strictly 2.
                            boolean[] exists = new boolean[2];
                            boolean match = false;
                            for (int i = 0; i < 2; i++) {
                                previous.add(AccessData.getData(applix[i]));
                                exists[i] = (previous.get(i).size() != 0);
                            }
                            if (exists[0] && exists[1]) {
                                for (AccessData access : previous.get(0)) {
                                    if (access.data.equalsIgnoreCase(applix[1])) {
                                        match = true;
                                        break;
                                    }
                                }
                            }
                            if (match)
                                voicemsg("Ya! you once told me about that");
                            else {
                                voicemsg("This contradicts with some previous info, you gave");
                                if (exists[0] ^ exists[1]) {
                                    int k;
                                    if (exists[0])
                                        k = 0;
                                    else
                                        k = 1;
                                    ArrayList<AccessData> accessArr = previous.get(k);
                                    if (accessArr.size() == 1) {
                                        System.out.println(applix[k]
                                                + " is " + previous.get(k).get(0).data);
                                        voicemsg("Should I change or add this as new?");
                                        String reply = " " + input.nextLine().toLowerCase() + " ";
                                        // reply.contains(" yes ") || reply.contains(" ys ")
                                        // || reply.contains(" ya ")
                                        // || reply.contains(" sure ") ||
                                        if (((reply.contains(" change "))
                                                && !(reply.contains(" no ") || (reply.contains(" not ")
                                                        || reply.contains(" donot ") || reply.contains(" don't ")))
                                        // || (reply.equals("why not"))
                                        )) {
                                            try {
                                                AccessData.replaceWith(previous.get(k).get(0).index, applix);
                                                voicemsg("Change made!");
                                            } catch (Exception e) {
                                                voicemsg("Sorry changes failed");
                                            }
                                        } else if ((reply.contains(" new ") || reply.contains(" add "))
                                                && !(reply.contains(" no ") || reply.contains(" not ")
                                                        || reply.contains(" donot ") || reply.contains(" don't "))) {
                                            try {
                                                AccessData.addData(applix);
                                                voicemsg("New data added");
                                            } catch (Exception e) {
                                                voicemsg("Sorry addition failed");
                                            }
                                        } else {
                                            voicemsg("No change made!");
                                        }
                                    } else {
                                        ArrayList<Integer> intList = new ArrayList<Integer>();
                                        for (int i = 0; i < accessArr.size(); i++) {
                                            System.out.println(
                                                    previous.get(k).get(i).index + " = = " + applix[k]
                                                            + " is " + previous.get(k).get(i).data);
                                            intList.add(previous.get(k).get(i).index);
                                        }
                                        voicemsg("Which one should or just add this as new?");
                                        String change = input.nextLine().toLowerCase();
                                        try {
                                            int line = Integer.parseInt(change);
                                            if (intList.contains(line)) {
                                                AccessData.replaceWith(line, applix);
                                                voicemsg("Changes made");
                                            } else {
                                                voicemsg("Sorry the value entered is not the expected one");
                                                voicemsg("No change made");
                                            }
                                        } catch (Exception e) {
                                            if (change.contains(" add ") || change.contains(" new ")) {
                                                AccessData.addData(applix);
                                                voicemsg("New data added");
                                            } else {
                                                voicemsg("No change made");
                                            }
                                        }
                                    }
                                } else if (exists[0] && exists[1] && !match) {
                                    // well which one to etdit?<obviously this will happen rarely better ask user>
                                    // (1)remove 1 or 2 (2)add new keep old<again rare>
                                    ArrayList<Integer> intList = new ArrayList<Integer>();
                                    for (int i = 0; i < 2; i++) {
                                        for (AccessData access : previous.get(i)) {
                                            System.out.println(access.index + " = = " + applix[i].replace("|", "")
                                                    + " is " +
                                                    access.data.replace("|", ""));
                                            intList.add(access.index);
                                        }
                                    }
                                    voicemsg("Which one should or just add this as new?");
                                    String change = input.nextLine().toLowerCase();
                                    try {
                                        int line = Integer.parseInt(change);
                                        if (intList.contains(line)) {
                                            AccessData.replaceWith(line, applix);
                                            voicemsg("Replacement made");
                                        } else {
                                            voicemsg("Sorry the value entered is not the expected one");
                                            voicemsg("No change made");
                                        }
                                    } catch (Exception e) {
                                        if (change.contains(" add ") || change.contains(" new ")) {
                                            AccessData.addData(applix);
                                            voicemsg("New data added");
                                        } else {
                                            voicemsg("No change made");
                                        }
                                    }

                                } else {
                                    AccessData.addData(applix);
                                }
                            }
                        }
                    }
                }
            } else if (appspace.contains(" what ") || appspace.contains(" who ") || appspace.contains(" where ")
                    || appspace.contains(" when ")) {
                if (appspace.contains(" what "))
                    Qword = "what";
                else if (appspace.contains(" who "))
                    Qword = "who";
                else if (appspace.contains(" where "))
                    Qword = "where";
                else
                    Qword = "when";
                boolean getLost = false;
                app = app.replace(Qword, "");
                for (String[] topic : AccessData.DataTable) {
                    if (getLost) {
                        break;
                    }
                    for (int i = 0; i < 2; i++) {
                        if (appspace.contains(" " + topic[i] + " ")) {
                            if (app.replace(topic[i], "").split(" ").length < 3) {
                                System.out.println(topic[i] + " is " + topic[i == 0 ? 1 : 0]);
                                getLost = true;
                                break;
                            }
                        }
                    }
                }
                if (!getLost) {
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                .isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(
                                    new URI("https://www.google.com/search?q="
                                            + app.replaceAll(" ", "+")));
                        }
                    } catch (Exception EEEE) {
                        voicemsg("Sorry! can't get info regarding " + app);
                    }
                }

            } else if (app.equalsIgnoreCase("end") || appspace.contains(" end ")
                    || appspace.contains(" bye ") || appspace.contains(" see you ")
                    || appspace.contains(" good night ")) {
                voicemsg("bye");
                _continue = false;
            } else {
                String appplus = app.replaceAll(" ", "+");
                Runtime rt = Runtime.getRuntime();
                boolean worked = false;

                // 1. Direct exec
                try {
                    rt.exec(app.split(" "));
                    worked = true;
                } catch (Exception ignored) {
                }

                // 2. Browse as URI
                if (!worked) {
                    try {
                        if (Desktop.isDesktopSupported()
                                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI(app));
                            worked = true;
                        }
                    } catch (Exception ignored) {
                    }
                }

                // 3. Check if it looks like a domain and browse http://
                if (!worked) {
                    try {
                        if (app.contains(".") && !app.contains(" ")) {
                            InetAddress.getByName(app);
                            if (Desktop.isDesktopSupported()
                                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(new URI("http://" + app));
                                worked = true;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                // 4. Try mapped app (cmd Start)
                if (!worked) {
                    try {
                        String appAction = getApp(app);
                        if (appAction == null)
                            throw new NullPointerException("App not in the list");

                        ProcessBuilder processBuilder = new ProcessBuilder(
                                "cmd.exe", "/c", "Start", appAction);
                        processBuilder.start();
                        worked = true;
                    } catch (Exception ignored) {
                    }
                }

                // 5. Try mapped app via browser
                if (!worked) {
                    try {
                        String appAction = getApp(app);
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

                // 6. Try mapped website
                if (!worked) {
                    try {
                        String appAction = getApp(app + " website");
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

                // 7. Google fallback
                if (!worked) {
                    try {
                        if (Desktop.isDesktopSupported()
                                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(
                                    new URI("https://www.google.com/search?q=" + appplus));
                            worked = true;
                        }
                    } catch (Exception e) {
                        voicemsg("Sorry! can't get info regarding " + app);
                    }
                }
            }

        }
        input.close();
    }
}
