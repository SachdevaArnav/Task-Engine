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
                                AppList.put(part.replaceAll("////s+", " "), value);
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

    private static void nonwebSupport(String appli) {
        byte error = 0;
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec(appli.split(" "));
        } catch (Exception e) {
            try {
                rt.exec((appli + ".exe").split(" "));
            } catch (Exception erk) {
                try {
                    if (Desktop.isDesktopSupported()
                            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(appli));
                    }
                } catch (Exception ee) {
                    try {
                        if (Desktop.isDesktopSupported()
                                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI(appli + ".exe"));
                        }
                    } catch (Exception eq) {
                        String appAction;
                        appAction = getApp(appli);
                        try {
                            if (appAction == null)
                                throw new NullPointerException("App not in the list");
                            ProcessBuilder processBuilder = new ProcessBuilder(
                                    "cmd.exe",
                                    "/c",
                                    "Start", appAction);
                            processBuilder.start();
                        } catch (Exception ed) {
                            try {
                                if (appAction == null)
                                    throw new NullPointerException("App not in the list");
                                if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                        .isSupported(Desktop.Action.BROWSE)) {
                                    Desktop.getDesktop()
                                            .browse(new URI(appAction));
                                }
                            } catch (Exception eeE) {
                                try {
                                    appAction = getApp(appli + " website");
                                    if (appAction == null)
                                        throw new NullPointerException("Website not in the list");
                                    if (Desktop.isDesktopSupported() && Desktop
                                            .getDesktop()
                                            .isSupported(Desktop.Action.BROWSE)) {
                                        Desktop.getDesktop()
                                                .browse(new URI(appAction));
                                    }
                                } catch (Exception eeee) {
                                    try {
                                        Desktop.getDesktop().open(new File(appli));
                                    } catch (Exception el) {
                                        try {
                                            if (new File(appli).exists()) {
                                                ProcessBuilder processBuilder = new ProcessBuilder(
                                                        "explorer.exe",
                                                        appli);
                                                Process process = processBuilder.start();
                                                process.waitFor();
                                            } else
                                                throw new Exception("Invalid File Path");
                                        } catch (Exception ek) {
                                            try {
                                                int size;
                                                List<Path> searchResult = sending_query
                                                        .file_search(appli);
                                                if (searchResult == null || (size = searchResult.size()) == 0) {
                                                    throw new Exception("no related file found in the system");
                                                } else if (size == 1) {
                                                    Path file = searchResult.get(0);
                                                    try {
                                                        Desktop.getDesktop().open(file.toFile());
                                                    } catch (Exception em) {
                                                        try {
                                                            ProcessBuilder processBuilder = new ProcessBuilder(
                                                                    "explorer.exe",
                                                                    file.toString());
                                                            Process process = processBuilder
                                                                    .start();
                                                            process.waitFor();
                                                        } catch (Exception eqq) {
                                                            error = 1;
                                                        }
                                                    }
                                                } else {
                                                    voicemsg("List of related files shown");
                                                    error = -1;// only to avoid opening voicemsg
                                                }
                                            } catch (Exception eee) {
                                                error = 1;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (error == 1) {
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                            .isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(
                                new URI("https://www.google.com/search?q="
                                        + appli.replaceAll(" ", "+")));
                    }
                } catch (Exception EEEE) {
                    voicemsg("Sorry! can't get info regarding " + appli);
                }
            } else if (error == 0) {
                voicemsg("Opening " + appli);
            }
        }
    }

    // there are some cases where its misfiring like 'open allen act papers'
    // this is somehow instead opens ssc X papers pdf
    // also in case of multiple outputs at times this the last one have more garbage
    // values
    // and its shown even when there even if they are outnumbered
    public static void main(String[] args) {
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
            String app = input.nextLine().strip().toLowerCase();
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
                            nonwebSupport(appli);
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
                    nonwebSupport(appli);
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
                                        voicemsg("Should I change this info or just add this as new?");
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
                try {
                    rt.exec(app.split(" "));
                } catch (Exception e) {
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI(app));
                        }
                    } catch (Exception ew) {
                        try {
                            if (app.contains(".") && !app.contains(" ")) {
                                InetAddress.getByName(app);
                                if (Desktop.isDesktopSupported()
                                        && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                    Desktop.getDesktop().browse(new URI("http://" + app));
                                }
                            } else
                                throw new Exception("Not a webpage");
                        } catch (Exception ee) {
                            String appAction = getApp(app);
                            try {
                                if (appAction == null)
                                    throw new NullPointerException("App not in the list");
                                ProcessBuilder processBuilder = new ProcessBuilder(
                                        "cmd.exe",
                                        "/c",
                                        "Start", appAction);
                                processBuilder.start();
                            } catch (Exception ed) {
                                try {
                                    if (appAction == null)
                                        throw new NullPointerException("App not in the list");
                                    if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                            .isSupported(Desktop.Action.BROWSE)) {
                                        Desktop.getDesktop()
                                                .browse(new URI(appAction));
                                    }
                                } catch (Exception eeE) {
                                    try {
                                        appAction = getApp(app + " website");
                                        if (appAction == null)
                                            throw new NullPointerException("Website not in the list");
                                        if (Desktop.isDesktopSupported() && Desktop
                                                .getDesktop()
                                                .isSupported(Desktop.Action.BROWSE)) {
                                            Desktop.getDesktop()
                                                    .browse(new URI(appAction));
                                        }
                                    } catch (Exception eq) {
                                        try {
                                            if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                                    .isSupported(Desktop.Action.BROWSE)) {
                                                Desktop.getDesktop().browse(
                                                        new URI("https://www.google.com/search?q="
                                                                + appplus));
                                            }
                                        } catch (Exception EEEE) {
                                            voicemsg("Sorry! can't get info regarding " + app);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        input.close();
    }
}
