import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class MoleculeReader {
    enum section {
        PRE, POST, OTHER
    };

    String[] prefixes = { "opentarget(\"", "getfile(\"", "click(\"", "activatecontrol(\"",
            "controlexists(\"", "textexists(\"", "delay(\"" };

    public Molecule read(Path molecule) throws Exception {
        try {
            Molecule moleculeObj = new Molecule();
            MoleculeReader.section current = MoleculeReader.section.OTHER;
            for (String line : Files.readAllLines(molecule)) {
                line = line.strip();
                if (line.equals("PRE")) {
                    current = MoleculeReader.section.PRE;
                } else if (line.equals("END_PRE")) {
                    current = MoleculeReader.section.OTHER;
                } else if (line.equals("POST")) {
                    current = MoleculeReader.section.POST;
                } else if (line.equals("END_POST")) {
                    current = MoleculeReader.section.OTHER;
                } else {
                    String lowerLine = line.toLowerCase();
                    boolean stateFunction, windowControl = false;
                    for (String prefix : prefixes) {
                        if ((windowControl = lowerLine.startsWith("window ")) || lowerLine.startsWith(prefix)) {
                            stateFunction = prefix.startsWith("controlexists") ||
                                    prefix.startsWith("textexists");
                            // window is allowed to be everywhere due to its unique usage
                            if (current != section.OTHER && !stateFunction && !windowControl) {
                                throw new Exception("Only state functions allowed in PRE/POST");
                            }

                            if (current == section.OTHER && stateFunction) {
                                throw new Exception("State functions not allowed in action section");
                            }
                            // Now molecule's current line's structural validity is verified
                            if (current == section.PRE) {
                                moleculeObj.pre.add(line);
                            } else if (current == section.POST) {
                                moleculeObj.post.add(line);
                            } else {
                                moleculeObj.actions.add(line);
                            }
                            break;
                        }
                    }
                }
            }
            return moleculeObj;
        } catch (Exception e) {
            throw new Exception("Failed to load this Molecule.");
        }
    }

    public void run(Molecule molecule, Scanner input) throws Exception {
        boolean result = true;
        try (EngineIPC ipc = new EngineIPC()) {
            int handle = 0, start = 0;
            String file, lowerCase;
            for (String preStates : molecule.pre) {
                if ((lowerCase = preStates.toLowerCase()).startsWith("window ")) {
                    String windowName = lowerCase.replace("window", "").strip();
                    handle = ipc.getHandle(windowName);
                    continue;
                }
                for (String prefix : new String[] { "textexists(\"", "controlexists(\"" }) {
                    start = lowerCase.indexOf(prefix);
                    if (start != -1) {
                        // Extract the passed argument
                        int startIdx = start + prefix.length();
                        int endIdx = lowerCase.indexOf("\"", startIdx);
                        if (endIdx != -1) {
                            file = preStates.substring(startIdx, endIdx);
                            try {
                                if (prefix.startsWith("controlexists")) {
                                    result = ipc.controlExists(handle, file);
                                } else if (prefix.startsWith("textexists")) {
                                    result = ipc.TextExists(handle, file);
                                } else {
                                    throw new Exception("Unknown function in preState section");
                                }
                            } catch (Exception e) {
                                throw new Exception("Failed in checking preState.");
                            }
                            if (!result) {
                                throw new Exception("Pre state " + file + " didn't match");
                            }
                        }
                    }
                }
            }
            for (String actionStates : molecule.actions) {
                Path lastFile;
                if ((lowerCase = actionStates.toLowerCase()).startsWith("window ")) {
                    String windowName = lowerCase.replace("window", "").strip();
                    handle = ipc.getHandle(windowName);
                    continue;
                }
                for (String prefix : prefixes) {
                    start = lowerCase.indexOf(prefix);
                    if (start != -1) {
                        // Extract the passed argument
                        int startIdx = start + prefix.length();
                        int endIdx = actionStates.indexOf("\"", startIdx);
                        if (endIdx != -1) {
                            file = actionStates.substring(startIdx, endIdx);
                            // Routing to specific functions based on the matched prefix
                            try {
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
                                } else if (prefix.startsWith("delay")) {
                                    Thread.sleep(Long.parseLong(file));
                                } else {
                                    throw new Exception("Unknown function in Action section");
                                }
                            } catch (Exception e) {
                                throw new Exception("Failed to run this Molecule.");
                            }
                            break;
                        }
                    }
                }
            }
            for (String postState : molecule.post) {
                if ((lowerCase = postState.toLowerCase()).startsWith("window ")) {
                    String windowName = lowerCase.replace("window", "").strip();
                    handle = ipc.getHandle(windowName);
                }
                for (String prefix : new String[] { "textexists(\"", "controlexists(\"" }) {
                    start = lowerCase.indexOf(prefix);
                    if (start != -1) {
                        // Extract the passed argument
                        int startIdx = start + prefix.length();
                        int endIdx = postState.indexOf("\"", startIdx);
                        if (endIdx != -1) {
                            file = postState.substring(startIdx, endIdx);
                            try {
                                if (prefix.startsWith("controlexists")) {
                                    result = ipc.controlExists(handle, file);
                                } else if (prefix.startsWith("textexists")) {
                                    result = ipc.TextExists(handle, file);
                                } else {
                                    throw new Exception("Unknown function in postState section");
                                }
                            } catch (Exception e) {
                                throw new Exception("Failed in checking postState.");
                            }
                            if (!result) {
                                throw new Exception("Failed to reach " + file + " Post State");
                            }
                        }
                    }
                }
            }
        }
    }
}
