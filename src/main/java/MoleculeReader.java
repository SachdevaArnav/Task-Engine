import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class MoleculeReader {
    enum section {
        PRE, POST, OTHER
    };

    public void read(Scanner input, Path molecule) throws Exception {
        // try {
        // String molContent = Files.readString(molecule);
        // this string contains seperators \r\n
        // better use these
        int handle = 0;
        Path lastFile;
        try (EngineIPC ipc = new EngineIPC()) {
            MoleculeReader.section current = MoleculeReader.section.OTHER;
            for (String line : Files.readAllLines(molecule)) {
                line = line.strip();
                if (line.startsWith("WINDOW ")) {
                    String windowName = line.replace("WINDOW", "").strip();
                    handle = ipc.getHandle(windowName);
                } else if (line.equals("PRE")) {
                    current = MoleculeReader.section.PRE;
                } else if (line.equals("END_PRE")) {
                    current = MoleculeReader.section.OTHER;
                } else if (line.equals("POST")) {
                    current = MoleculeReader.section.POST;
                } else if (line.equals("END_POST")) {
                    current = MoleculeReader.section.OTHER;
                } else {
                    String lowerLine = line.toLowerCase();
                    // Define your 5 method prefixes in "lowercase"
                    String[] prefixes = { "opentarget(\"", "getfile(\"", "click(\"", "activatecontrol(\"",
                            "controlexists(\"", "textexists(\"", "delay(\"" };
                    boolean stateFunction, result = false;
                    for (String prefix : prefixes) {
                        int start = lowerLine.indexOf(prefix);
                        if (start != -1) {
                            // Extract the passed argument
                            int startIdx = start + prefix.length();
                            int endIdx = line.indexOf("\"", startIdx);

                            if (endIdx != -1) {
                                String file = line.substring(startIdx, endIdx);

                                stateFunction = prefix.startsWith("controlexists") ||
                                        prefix.startsWith("textexists");
                                if (current != section.OTHER && !stateFunction) {
                                    throw new Exception("Only state functions allowed in PRE/POST");
                                }

                                if (current == section.OTHER && stateFunction) {
                                    throw new Exception("State functions not allowed in action section");
                                }

                                // Routing to specific functions based on the matched prefix
                                if (prefix.startsWith("opentarget")) {
                                    eFriend.openTarget(input, file);
                                } else if (prefix.startsWith("getfile")) {
                                    lastFile = sending_query.file_search(input, file);
                                    /// this will be used wherever typetext function will call $file or something
                                    /// like that
                                } else if (prefix.startsWith("click")) {
                                    ipc.click(file);
                                } else if (prefix.startsWith("activatecontrol")) {
                                    ipc.activateControl(handle, file);
                                } else if (prefix.startsWith("controlexists")) {
                                    result = ipc.controlExists(handle, file);
                                    System.out.println(handle + file);
                                } else if (prefix.startsWith("textexists")) {
                                    result = ipc.TextExists(handle, file);
                                } else if (prefix.startsWith("delay")) {
                                    Thread.sleep(Long.parseLong(file));
                                    System.out.println("NOW");
                                } else {
                                    throw new Exception("unexpected function" + line);
                                }
                                if (current == section.PRE && !result) {
                                    throw new Exception("Pre condition didnt match");
                                } else if (current == section.POST && !result) {
                                    throw new Exception("Post condition didnt match");
                                }
                            }
                            break; // Stop checking other prefixes once a match is found
                        }
                    }
                }
            }
        }

    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Path path = Path.of("TestMolecules/notepad.mol");
        MoleculeReader reader = new MoleculeReader();
        try {
            reader.read(input, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
