import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    static boolean matchup(String word, String value) {
        Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        boolean matchFound = matcher.find();
        return matchFound;
    }

    static boolean matchup_literal(String word, String value) {
        Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
        Matcher matcher = pattern.matcher(value);
        boolean matchFound = matcher.find();
        return matchFound;
    }

    static HashMap<String, String> AppList = new HashMap<String, String>();

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

    final static Pattern pattern8 = Pattern.compile(".", Pattern.LITERAL);
    final static Pattern patternh = Pattern.compile("http", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) {
        loadApps();
        Scanner input = new Scanner(System.in);
        // voicemsg("i am ur Friend");
        voicemsg("Welcome");
        // voicemsg("How can i help you!");
        // voicemsg("To open any file from your device say'open'");
        // voicemsg(
        // "I can learn info and later tell u. To make me learn say 'Learn that' and to
        // access it later use 'What is'");
        int i = 0;
        int nummatchFoundd = 0;
        int nummatchFoundd1 = 0;
        // if required then for finding any random app use explorer.exe ->
        // shell:AppsFolder
        // in this window use eFriend's ability to see and percive and search for the
        // required app
        // near 100% chance for all launchable apps
        // need to define like queries asking for "apps" explicitly only use this
        while (i < 1) {
            int error = 0;
            String app = input.nextLine().strip().toLowerCase();
            Matcher matcher8 = pattern8.matcher(app);
            boolean matchFound8 = matcher8.find();
            if (app.contains("open ")) {
                String[] arrOfStr = app.split("open ", 2);
                String appli = arrOfStr[1].strip();
                String appliplus = appli.replaceAll(" ", "+");
                if (appli.contains("www.") && !(appli.contains("http"))) {
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI("http://" + appli));
                        }
                    } catch (Exception ee) {
                        voicemsg("Sorry!Can't reach this webpage.");
                        voicemsg("Please check the URL");
                    }
                } else if (appli.contains(".com") || appli.contains(".in")
                        || appli.contains(".org") || appli.contains(".gov")
                        || appli.contains(".ai")) {
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
                                    String appAction = getApp(appli);
                                    try {
                                        ProcessBuilder processBuilder = new ProcessBuilder(
                                                "cmd.exe",
                                                "/c",
                                                "Start", appAction);
                                        processBuilder.start();
                                    } catch (Exception ed) {
                                        try {
                                            if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                                    .isSupported(Desktop.Action.BROWSE)) {
                                                Desktop.getDesktop()
                                                        .browse(new URI(appAction));
                                            }
                                        } catch (Exception eeE) {
                                            try {
                                                appAction = getApp(appli + " website");
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
                                                            List<Path> searchResult = sending_query
                                                                    .file_search(appli);
                                                            if (searchResult.size() == 1) {
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
                            voicemsg("Sorry I am Unable to reach " + appli + " at the moment");
                        } else if (error == 0) {
                            voicemsg("Opening " + appli);
                        }
                    }
                }
                // <>
            } else if (matchup("Learn", app) || matchup("Learn this", app) || matchup("Learn that", app)
                    || matchup("remember", app) || matchup("remember this", app) || matchup("remember that", app)) {
                String[] arrOfStr;
                if (matchup("Learn that", app))
                    arrOfStr = app.split("(?i)learn that", 2);
                else if (matchup("Learn this", app))
                    arrOfStr = app.split("(?i)learn this", 2);
                else if (matchup("remember that", app))
                    arrOfStr = app.split("(?i)remember that", 2);
                else if (matchup("remember this", app))
                    arrOfStr = app.split("(?i)remember this", 2);
                else if (matchup("Learn", app))
                    arrOfStr = app.split("(?i)learn", 2);
                else
                    arrOfStr = app.split("(?i)remember", 2);
                for (String appli : arrOfStr) {
                    boolean matchFoundis = matchup(" is ", appli);
                    boolean matchFoundist = matchup(" is the ", appli);
                    boolean matchFoundas = matchup(" as ", appli);
                    boolean matchFoundast = matchup(" as the ", appli);
                    int nummatchFoundf = 0;
                    if (matchFoundis || matchFoundas || matchFoundist || matchFoundast) {
                        String a, x;
                        if (matchFoundist) {
                            String[] applix = appli.split("(?i) is the ", 2);
                            a = "|" + applix[0].strip() + "|";
                            x = "|" + applix[1].strip() + "|";
                        } else if (matchFoundis) {
                            String[] applix = appli.split("(?i) is ", 2);
                            a = "|" + applix[0].strip() + "|";
                            x = "|" + applix[1].strip() + "|";
                        } else if (matchFoundast) {
                            String[] applix = appli.split("(?i) as the ", 2);
                            a = "|" + applix[0].strip() + "|";
                            x = "|" + applix[1].strip() + "|";
                        } else {
                            String[] applix = appli.split("(?i) as ", 2);
                            a = "|" + applix[0].strip() + "|";
                            x = "|" + applix[1].strip() + "|";
                        }
                        try (Scanner myReader = new Scanner(new File("friendshipkasahimatlab.txt"));) {
                            while (myReader.hasNextLine()) {
                                String fd = myReader.nextLine();
                                Pattern patternf = Pattern.compile(a, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
                                Matcher matcherf = patternf.matcher(fd);
                                boolean matchFoundf = matcherf.find();
                                String ans;
                                if (matchFoundf) {
                                    nummatchFoundf = 1;
                                    try {
                                        String[] arrOffd = fd.split(":", 2);
                                        if (arrOffd[0].equalsIgnoreCase(a)) {
                                            ans = arrOffd[1];
                                        } else {
                                            ans = arrOffd[0];
                                        }
                                        if ((x).equalsIgnoreCase(ans)) {
                                            voicemsg("Ok! you once told me about that");
                                        } else {
                                            for (int chance = 0; chance < 2; chance++) {
                                                String anss = ans.replaceAll("[|]", "");
                                                if (chance == 0) {
                                                    voicemsg("But last time you told that it is " + anss);
                                                    voicemsg("Should I change this info ?");
                                                }
                                                String reply = input.nextLine();
                                                boolean matchFoundy = matchup("yes", reply);
                                                boolean matchFoundn = matchup("no", reply);
                                                if (matchFoundn) {
                                                    voicemsg("ok! No change made");
                                                    break;
                                                } else if (matchFoundy) {
                                                    try (Scanner myReadertemp = new Scanner(
                                                            new File("friendshipkasahimatlab.txt"))) {
                                                        while (myReadertemp.hasNextLine()) {
                                                            String temp = myReadertemp.nextLine();
                                                            try (FileWriter myWriter = new FileWriter(
                                                                    "friendshipkasahimatlab_temp.txt", true)) {
                                                                Pattern patternans = Pattern.compile(ans,
                                                                        Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
                                                                Matcher matcherans = patternans.matcher(temp);
                                                                boolean matchFoundans = matcherans.find();
                                                                if (matchFoundans)
                                                                    myWriter.write(a + ":" + x + "\n");
                                                                else
                                                                    myWriter.write(temp + "\n");
                                                            } catch (Exception temperr) {
                                                                System.out.println(temperr);
                                                            }
                                                        }
                                                        try (Scanner myReadert = new Scanner(
                                                                new File("friendshipkasahimatlab_temp.txt"))) {
                                                            try (FileWriter myWriterc = new FileWriter(
                                                                    "friendshipkasahimatlab.txt")) {
                                                                myWriterc.write("");
                                                            } catch (Exception ew) {
                                                                System.out.println(ew);
                                                            }
                                                            try (FileWriter myWriter = new FileWriter(
                                                                    "friendshipkasahimatlab.txt", true)) {
                                                                while (myReadert.hasNextLine()) {
                                                                    myWriter.write(myReadert.nextLine() + "\n");
                                                                }
                                                                voicemsg("Ok! Now I understood it correctly");

                                                                voicemsg("I will give this correct info next time");
                                                            } catch (Exception emw) {
                                                                System.out.println(emw);
                                                            }
                                                            try (FileWriter myWritercl = new FileWriter(
                                                                    "friendshipkasahimatlab_temp.txt")) {
                                                                myWritercl.write("");
                                                            } catch (Exception ewm1) {
                                                                System.out.println(ewm1);
                                                            }

                                                        } catch (IOException ef) {
                                                            voicemsg("Sorry! I didn't understand");
                                                        }
                                                        break;
                                                    } catch (Exception eq) {
                                                        System.out.println(eq);
                                                    }
                                                } else {
                                                    voicemsg("Please give a clear yes or no reply");
                                                }
                                            }
                                        }
                                    } catch (Exception EEE) {
                                        voicemsg("Yes U once told me something about that.can you please try again");
                                    }
                                    break;
                                }
                            }
                            if (nummatchFoundf == 0) {
                                try (FileWriter myWriter = new FileWriter("friendshipkasahimatlab.txt", true)) {
                                    myWriter.write("\n" + a + ":" + x);
                                    voicemsg("ok! Now I have learnt that.");
                                } catch (Exception fe) {
                                    System.out.println(fe);
                                }
                            }
                        } catch (Exception eeeeeee) {
                            voicemsg(
                                    "Sorry! due to some internal error I didn't saved any new data. You can try again");
                        }

                    }
                }
            } else if (matchup("what", app) || matchup("who", app) || matchup("when", app)) {// start from here we have
                                                                                             // to remove whfamily and
                                                                                             // helping verbs and also
                                                                                             // articles
                if (matchup("what", app)) {
                }
                try (Scanner myReader1 = new Scanner(new File("friendshipkasahimatlab.txt"))) {
                    while (myReader1.hasNextLine()) {

                    }
                } catch (FileNotFoundException ea) {
                    voicemsg("I am really sorry I don't got what you said just");
                }
            } else if (matchFound8) {
                Matcher matcherh = patternh.matcher(app);
                if (matcherh.find()) {
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI(app));
                        }
                    } catch (Exception eE) {
                    }
                } else {
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI("http://" + app));
                        }
                    } catch (Exception eE) {
                    }
                }

            }

            else if (app.equalsIgnoreCase("end") || matchup(" end ", " " + app + " ")
                    || matchup(" bye ", " " + app + " ") || matchup(" see you ", " " + app + " ")
                    || matchup(" good night ", " " + app + " ")) {
                voicemsg("bye");
                i++;
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
                    } catch (Exception ee) {
                        try (Scanner myReader = new Scanner(new File("DATABASEKABASIC.txt"))) {
                            while (myReader.hasNextLine()) {
                                String database = myReader.nextLine();
                                Pattern patternd = Pattern.compile("|" + app + "|",
                                        Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
                                Matcher matcherd = patternd.matcher(database);
                                boolean matchFoundd = matcherd.find();
                                if (matchFoundd) {
                                    nummatchFoundd = 1;
                                    String[] arrOfdatabase = database.split(":", 2);
                                    voicemsg(arrOfdatabase[1]);
                                    try {
                                        if (Desktop.isDesktopSupported()
                                                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                            Desktop.getDesktop().browse(new URI(arrOfdatabase[1]));
                                        }
                                    } catch (Exception eeE) {
                                        String database1 = myReader.nextLine();
                                        Pattern patternd1 = Pattern.compile("|" + app + " website|",
                                                Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
                                        Matcher matcherd1 = patternd1.matcher(database1);
                                        boolean matchFoundd1 = matcherd1.find();
                                        if (matchFoundd1) {
                                            nummatchFoundd1 = 1;
                                            String[] arrOfdatabase1 = database1.split(":", 2);
                                            System.out.print(arrOfdatabase1[1]);
                                            try {
                                                if (Desktop.isDesktopSupported()
                                                        && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                                    Desktop.getDesktop().browse(new URI(arrOfdatabase1[1]));
                                                }
                                            } catch (Exception EE) {
                                                try {
                                                    if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                                            .isSupported(Desktop.Action.BROWSE)) {
                                                        Desktop.getDesktop().browse(
                                                                new URI("https://www.google.com/search?q=" + appplus));
                                                    }
                                                } catch (Exception EEEE) {
                                                    voicemsg("Sorry! can't get info regarding " + app);
                                                }
                                            }
                                        }
                                    }
                                    break;
                                } else {
                                    try {
                                        if (Desktop.isDesktopSupported()
                                                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                            Desktop.getDesktop()
                                                    .browse(new URI("https://www.google.com/search?q=" + appplus));
                                        }
                                    } catch (Exception EEEE) {
                                        voicemsg("Sorry! can't get info regarding " + app);

                                    }
                                    break;
                                }
                            }
                        } catch (FileNotFoundException er) {
                            voicemsg("An error occurred.");
                            er.printStackTrace();
                        }
                    }
                    if (error == 2) {
                        voicemsg("Sorry I am Unable to reach " + app + " at the moment");
                    }

                }
            }
        }
        input.close();
    }
}
