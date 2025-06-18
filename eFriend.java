import java.util.Scanner;
import java.awt.Desktop;
import java.net.URI;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Write a description of class eFriend here.
 *
 * @author (Arnav Sachdeva)
 * @version (1)
 * @date start(18/03/2021)
 */
// presently available search-ms is to be replaced with dir-based search
// protocol
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

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        voicemsg("i am ur Friend");
        voicemsg("Welcome");
        voicemsg("How can i help you!");
        voicemsg("To open any file from your device say'open'");
        voicemsg(
                "I can learn info and later tell u. To make me learn say 'Learn that' and to access it later use 'What is'");
        int i = 0;
        int nummatchFoundd = 0;
        int nummatchFoundd1 = 0;
        int error = 0;
        while (i < 1) {
            String app = input.nextLine().strip();
            Pattern pattern8 = Pattern.compile(".", Pattern.LITERAL);
            Matcher matcher8 = pattern8.matcher(app);
            boolean matchFound8 = matcher8.find();
            if (matchup("Open ", app)) {
                String[] arrOfStr = app.split("(?i)open ", 2);
                String appli = arrOfStr[1].strip();
                String appliplus = appli.replaceAll(" ", "+");
                Pattern patternw = Pattern.compile("www.", Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
                Matcher matcherw = patternw.matcher(appli);
                boolean matchFoundw = matcherw.find();
                if (matchFoundw && !matchup("http", appli)) {
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI("http://" + appli));
                        }
                    } catch (Exception ee) {
                        voicemsg("Sorry!Can't reach this webpage.");
                        voicemsg("Please check the URL");
                    }
                } else if (matchup_literal(".com", appli) || matchup_literal(".in", appli)
                        || matchup_literal(".org", appli) || matchup_literal(".gov", appli)
                        || matchup_literal(".ai", appli)) {
                    if (matchup("http", appli)) {
                        try {
                            if (Desktop.isDesktopSupported()
                                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(new URI(appli));
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
                                }

                                catch (Exception EEEe) {
                                    try (Scanner myReader = new Scanner(new File("DATABASEKABASIC.txt"))) {
                                        while (myReader.hasNextLine()) {
                                            String database = myReader.nextLine();
                                            Pattern patternd = Pattern.compile("|" + appli + "|",
                                                    Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
                                            Matcher matcherd = patternd.matcher(database);
                                            boolean matchFoundd = matcherd.find();
                                            if (matchFoundd) {
                                                nummatchFoundd = 1;
                                                String[] arrOfdatabase = database.split(":", 2);
                                                System.out.print(arrOfdatabase[1]);
                                                try {
                                                    ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c",
                                                            "Start", arrOfdatabase[1]);
                                                    processBuilder.start();
                                                } catch (Exception ed) {
                                                    try {
                                                        if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                                                .isSupported(Desktop.Action.BROWSE)) {
                                                            Desktop.getDesktop().browse(new URI(arrOfdatabase[1]));
                                                        }
                                                    } catch (Exception eeE) {
                                                        String database1 = myReader.nextLine();
                                                        Pattern patternd1 = Pattern.compile("|" + appli + " website|",
                                                                Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
                                                        Matcher matcherd1 = patternd1.matcher(database1);
                                                        boolean matchFoundd1 = matcherd1.find();
                                                        if (matchFoundd1) {
                                                            nummatchFoundd1 = 1;
                                                            String[] arrOfdatabase1 = database1.split(":", 2);
                                                            System.out.print(arrOfdatabase1[1]);
                                                            try {
                                                                if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                                                        .isSupported(Desktop.Action.BROWSE)) {
                                                                    Desktop.getDesktop()
                                                                            .browse(new URI(arrOfdatabase1[1]));
                                                                }
                                                            } catch (Exception eeee) {
                                                                try {
                                                                    if (Desktop.isDesktopSupported()
                                                                            && Desktop.getDesktop().isSupported(
                                                                                    Desktop.Action.BROWSE)) {
                                                                        Desktop.getDesktop().browse(new URI(
                                                                                "search-ms:displayname=Searching%20in%20your%20device&crumb=System.Generic.String%3A"
                                                                                        + arrOfdatabase[1]
                                                                                                .replaceAll(" ", "+")));
                                                                    }
                                                                } catch (Exception eee) {
                                                                    System.out.println(eee);
                                                                }
                                                            }

                                                        }
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    } catch (FileNotFoundException er) {
                                        error = 1;

                                    }
                                    if (nummatchFoundd != 1 && nummatchFoundd1 != 1) {

                                        try {
                                            ProcessBuilder processBuilder = new ProcessBuilder("explorer.exe",
                                                    "ms-search:query=" + appliplus);
                                            Process process = processBuilder.start();
                                            process.waitFor();

                                        } catch (Exception eee) {
                                            error = 1;
                                        }
                                    }

                                }

                            }
                            if (error == 1) {
                                voicemsg("Sorry I am Unable to reach " + appli + " at the moment");
                            } else {
                                voicemsg("Opening " + appli);
                            }
                        }
                    }
                }
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
                Pattern patternh = Pattern.compile("http://", Pattern.CASE_INSENSITIVE);
                Matcher matcherh = patternh.matcher(app);
                boolean matchFoundh = matcherh.find();
                if (matchFoundh) {
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
