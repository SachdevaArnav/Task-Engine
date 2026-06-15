import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class EngineIPC {
    private final DataInputStream in;
    private final DataOutputStream out;
    private final ObjectMapper mapper;

    public EngineIPC(InputStream instream, OutputStream outstream) {
        in = new DataInputStream(instream);
        out = new DataOutputStream(outstream);
        mapper = new ObjectMapper();
    }

    // Receiving response
    public JsonNode recv() throws Exception {
        int length = in.readInt();
        byte[] buf = new byte[length];
        in.readFully(buf);
        return mapper.readTree(buf);
    }

    // Send command
    public void send(Object obj) throws Exception {
        byte[] data = mapper.writeValueAsBytes(obj);
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    public void sendAtom(String atom, JsonNode _args) throws Exception {
        ObjectNode cmd = mapper.createObjectNode();
        // ipc.send(text);
        cmd.put("cmd", "EXECUTE");
        cmd.put("atom", atom);
        cmd.set("args", _args);
        send(cmd);
    }

    public boolean hasAmbiguousChars(String raw) {
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '0' || c == '1' || c == '5' || c == '8') {
                return true;
            }
        }
        return false;
    }

    public String Normalize(String raw) {
        StringBuilder norm = new StringBuilder(raw);
        for (int i = 0; i < norm.length(); i++) {
            if (norm.charAt(i) == '0') {
                norm.setCharAt(i, 'o');
            } else if (norm.charAt(i) == '1') {
                norm.setCharAt(i, 'l');
            } else if (norm.charAt(i) == '5') {
                norm.setCharAt(i, 's');
            } else if (norm.charAt(i) == '8') {
                norm.setCharAt(i, 'b');
            }
        }
        return norm.toString();
    }

    // In WinRT ocr, the x and y properties of a bounding rectangle (Rect struct)
    // represent the "upper-left" corner of the rectangle.
    private static double centerX(JsonNode node) {
        return node.get("x").asDouble() + node.get("w").asDouble() / 2.0;
    }

    private static double centerY(JsonNode node) {
        return node.get("y").asDouble() + node.get("h").asDouble() / 2.0;
    }

    private static boolean sameLine(JsonNode a, JsonNode b) {
        return Math.abs(centerY(a) - centerY(b)) < 0.5 * Math.max(a.get("h").asDouble(), b.get("h").asDouble());
    }

    private static boolean horizontalGroup(JsonNode a, JsonNode b) {
        /*
         * 1. Same line
         * 2. b is to the right of a
         * 3. Horizontal gap is reasonable
         * gap = b.x - (a.x + a.w);
         */
        double gap = b.get("x").asDouble() - (a.get("x").asDouble() + a.get("w").asDouble());
        return (centerX(a) <= centerX(b))
                &&
                gap <= 2 * Math.max(a.get("h").asDouble(), b.get("h").asDouble())
                &&
                sameLine(a, b);
    }

    private static boolean StrongOverlap(JsonNode a, JsonNode b) {
        double left = Math.max(
                a.get("x").asDouble(),
                b.get("x").asDouble());
        double right = Math.min(
                a.get("x").asDouble() + a.get("w").asDouble(),
                b.get("x").asDouble() + b.get("w").asDouble());
        double overlap = Math.max(0, right - left);
        double overlapRatio = overlap / Math.min(a.get("w").asDouble(), b.get("w").asDouble());
        return overlapRatio >= 0.5;
    }

    private static boolean verticalGroup(JsonNode a, JsonNode b) {
        /*
         * 1. b below a
         * 2. Strong X overlap
         * 3. Vertical gap reasonable (Sanity check only; Not very large)
         */
        double gap = (b.get("y").asDouble() + b.get("h").asDouble()) - (a.get("y").asDouble());
        return (centerY(a) <= centerY(b))
                &&
                gap <= 3 * Math.max(a.get("h").asDouble(), b.get("h").asDouble())
                &&
                StrongOverlap(a, b);
    }

    private static boolean canGroup(JsonNode a, JsonNode b) {
        return horizontalGroup(a, b) || verticalGroup(a, b);
    }

    private JsonNode merge(String textA, JsonNode a, String textB, JsonNode b) {
        ObjectNode output = mapper.createObjectNode();
        // The current implementation doesnot consider dependency of order of
        // text here
        // If any change is scoring logic introduce a order-dependent scoring then here
        // we need to add the logic of handling the orders of texts -- which again would
        // depend on vertical and horizontal grouping
        output.put("text", textA + " " + textB);
        output.put("x", Math.min(a.get("x").asDouble(), b.get("x").asDouble()));
        output.put("y", Math.min(a.get("y").asDouble(), b.get("y").asDouble()));
        double left = Math.min(a.get("x").asDouble(), b.get("x").asDouble());
        double top = Math.min(a.get("y").asDouble(), b.get("y").asDouble());
        double right = Math.max(a.get("x").asDouble() + a.get("w").asDouble(),
                b.get("x").asDouble() + b.get("w").asDouble());
        double bottom = Math.max(a.get("y").asDouble() + a.get("h").asDouble(),
                b.get("y").asDouble() + b.get("h").asDouble());
        output.put("w", right - left);
        output.put("h", bottom - top);
        ArrayNode array = mapper.createArrayNode();
        array.add(a);
        array.add(b);
        output.set("parents", array);
        if (output.get("text").asText().equalsIgnoreCase("visual studio"))
            System.out.println("helo:" + output);
        return output;
    }

    public List<JsonNode> OCRhandler() throws Exception {
        try {
            ObjectNode _args = mapper.createObjectNode();
            sendAtom("OCR_TO_COORDINATE", _args);
            JsonNode report = recv();
            if (report.get("status").asText().equalsIgnoreCase("error")) {
                throw new Exception(report.get("error").asText());
            } else if (report.get("status").asText().equalsIgnoreCase("ok")) {
                ArrayNode matches = (ArrayNode) report.get("matches");
                Comparator<JsonNode> byX = Comparator.comparingDouble(node -> node.get("x").asDouble());
                Comparator<JsonNode> byY = Comparator.comparingDouble(node -> node.get("y").asDouble());
                List<JsonNode> originals = StreamSupport.stream(matches.spliterator(), false)
                        .sorted(byY.thenComparing(byX))
                        .collect(Collectors.toList());
                List<JsonNode> twoWordPhrases = new ArrayList<>();
                String textA, textB;
                for (int i = 0; i < originals.size(); i++) {
                    JsonNode a = originals.get(i);
                    if ((textA = a.get("text").asText()
                            .replaceAll("[^A-Za-z]", "")
                            .strip()).isEmpty())
                        continue;
                    for (int j = 0; j < originals.size(); j++) {
                        if (i == j)
                            continue;
                        JsonNode b = originals.get(j);
                        if ((textB = b.get("text").asText()
                                .replaceAll("[^A-Za-z]", "")
                                .strip()).isEmpty())
                            continue;
                        if (!canGroup(a, b))
                            continue;
                        twoWordPhrases.add(merge(textA, a, textB, b));
                    }
                }
                List<JsonNode> threeWordPhrases = new ArrayList<>();
                for (JsonNode jn : twoWordPhrases) {
                    ArrayNode parents = (ArrayNode) jn.get("parents");
                    boolean skip = false;
                    for (JsonNode j : originals) {
                        skip = false;
                        if ((j.get("text").asText())
                                .replaceAll("[^A-Za-z]", "")
                                .strip().isEmpty()) {
                            continue;
                        }
                        // not adding same nodes again
                        for (JsonNode parent : parents) {
                            if (j.equals(parent)) {
                                skip = true;
                                break;
                            }
                        }
                        if (skip)
                            continue;
                        if (!canGroup(jn, j)) {
                            continue;
                        }
                        threeWordPhrases.add(merge(jn.get("text").asText(), jn, j.get("text").asText(), j));
                    }
                }
                // -------TESTING--------
                // for (JsonNode jn : twoWordPhrases) {
                // System.out.println(jn.get("text").asText());
                // }
                // -----------------------
                originals.addAll(twoWordPhrases);
                originals.addAll(threeWordPhrases);
                return originals;
            } else {
                throw new Exception("Invalid Json Format from Python");
            }
        } catch (Exception e) {
            throw new Exception("Got error: " + e);
        }
    }

    public void click(String text) throws Exception {
        click(text, ClickType.DOUBLE_CLICK, ClickSide.LEFT);
    }

    enum ClickType {
        DOUBLE_CLICK,
        SINGLE_CLICK
    }

    enum ClickSide {
        LEFT,
        RIGHT
    }

    public void click(String text, EngineIPC.ClickType ClickType, EngineIPC.ClickSide ClickSide) throws Exception {
        text = text.toLowerCase().strip();
        if (text.isEmpty())
            throw new Exception("The click input don't contain any detail to work on");
        List<JsonNode> matches = OCRhandler();
        String raw;
        double score;
        double bestScore = Double.MIN_VALUE;
        double bestArea = 0.0;
        JsonNode rawNode;
        boolean metrices_match;
        search2.what wordMetrices;
        JsonNode bestMatch = null;
        for (JsonNode node : matches) {
            rawNode = node.get("text");
            if (rawNode == null)
                continue;
            raw = rawNode.asText().replaceAll("[^A-Za-z0-9\\s]", "").strip();
            if (raw.isEmpty())
                continue;
            String[] rawArr = raw.strip().split("\\s+");
            int coverage = 0;
            int total = 0;
            double avgScore = 0;
            for (String a : rawArr) {
                total += a.length();
                wordMetrices = search2.DL_light(text, a, search2.Mode.FAST);
                if ((wordMetrices.score < 0.7 || !wordMetrices.match) && hasAmbiguousChars(a)) {
                    a = Normalize(a);
                    if (!wordMetrices.match) {
                        wordMetrices = search2.DL_light(text, a,
                                search2.Mode.FAST);
                    } else {
                        search2.what metrices2 = search2.DL_light(text, a,
                                search2.Mode.FAST);
                        if (metrices2.match && wordMetrices.score < metrices2.score) {
                            wordMetrices = metrices2;
                        }
                    }
                }
                System.out.println(a + ":" + wordMetrices.score);
                if (!wordMetrices.match)
                    continue;
                avgScore += wordMetrices.score;
                coverage += a.length();
            }
            coverage /= total;
            avgScore /= rawArr.length;

            metrices_match = coverage >= .50;
            // Concatenate the strings, get another score and take the better one
            raw = Normalize(raw.replaceAll("\\s+", ""));
            text = Normalize(text.replaceAll("\\s+", ""));
            search2.what concScore = search2.DL_light(text, raw, search2.Mode.FAST);
            avgScore = Math.max(avgScore, concScore.score);
            metrices_match = metrices_match || concScore.match;
            System.out.println("hello");
            if (!metrices_match) {
                continue;
            }
            score = avgScore;
            System.out.println(raw + ":" + score);
            // this the final score considers fuzziness of DL_light algo
            ((ObjectNode) node).put("score", score);
            if (bestScore < score) {
                bestMatch = (node);
                bestScore = score;
                bestArea = 0;
            } else if (bestScore == score) {
                double area;
                area = node.get("w").asDouble() * node.get("h").asDouble();
                if (area > bestArea) {
                    bestMatch = (node);
                    bestArea = area;
                } else if (area == bestArea) {
                    if (bestMatch.get("y").asDouble() < node.get("y").asDouble()) {
                        bestMatch = node;
                    } else if (bestMatch.get("y").asDouble() == node.get("y").asDouble()) {
                        if (bestMatch.get("x").asDouble() < node.get("x").asDouble()) {
                            bestMatch = node;
                        }
                    }
                }
            }
        }
        if (bestMatch == null) {
            throw new Exception("Failed to find the provided text:" + text);
        }
        JsonNode report = mapper.createObjectNode();
        ((ObjectNode) report).put("x", bestMatch.get("x").asInt() + bestMatch.get("w").asInt() / 2);
        ((ObjectNode) report).put("y", bestMatch.get("y").asInt() + bestMatch.get("h").asInt() / 2);
        System.out.println("text:" + bestMatch.get("text").asText());
        sendAtom("MOVE_MOUSE", report);
        System.out.println("X: " + report.get("x").asInt());
        System.out.println("Y: " + report.get("y").asInt());
        report = recv();
        if (report.get("status").asText().equalsIgnoreCase("error")) {
            throw new Exception(report.get("error").asText());
        } else if (report.get("status").asText().equalsIgnoreCase("ok")) {
            JsonNode _args = mapper.createObjectNode();
            ((ObjectNode) _args).put("button", (ClickSide == EngineIPC.ClickSide.RIGHT ? "right" : "left"));
            if (ClickType == EngineIPC.ClickType.DOUBLE_CLICK)
                sendAtom("DOUBLE_CLICK", _args);
            else
                sendAtom("CLICK", _args);
            report = recv();
            if (report.get("status").asText().equalsIgnoreCase("error")) {
                throw new Exception(report.get("error").asText());
            } else if (report.get("status").asText().equalsIgnoreCase("ok")) {
                System.out.println("Click executed successfully");
            }
        } else {
            throw new Exception("Invalid Json Format from Python");
        }
    }

    // testing
    public static void main(String[] args) throws Exception {
        // send CLI start to Main.py---
        ProcessBuilder pb = new ProcessBuilder(Paths.get("PyEnv", "Scripts", "python.exe").toAbsolutePath().toString(),
                "-u",
                "src/main/py/Main.py");
        // pb.redirectErrorStream(true);
        Process py = pb.start();
        System.out.println("is python alive" + py.isAlive());

        EngineIPC ipc = new EngineIPC(py.getInputStream(), py.getOutputStream());
        System.out.println("is python alive" + py.isAlive());

        JsonNode report;

        Thread t = new Thread(() -> {
            try (var r = new java.io.BufferedReader(
                    new java.io.InputStreamReader(py.getErrorStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    System.out.println("[PY-ERR] " + line);
                }
            } catch (Exception ignored) {
            }
        });
        t.setDaemon(true);
        t.start();
        try {
            System.out.println("is python alive" + py.isAlive());
            report = ipc.recv(); // waits until python response
        } catch (Exception e) {
            System.out.println("is python alive" + py.isAlive());
            report = new TextNode("");
        }
        System.out.println(report.asText());
        // --------------This one is oto list windows and bring froward the one
        // needed-------------------
        // if (report.path("status").asText().equals("Ready")) {
        // // lets ask for list of windows to chose from
        // ObjectNode cmd = ipc.mapper.createObjectNode();
        // cmd.put("cmd", "EXECUTE");
        // cmd.put("atom", "LIST_WINDOWS");
        // ipc.send(cmd);
        // // Wait for response again
        // String status;
        // report = ipc.recv();
        // if ((status = report.path("status").asText()).equalsIgnoreCase("ok")) {
        // // if successful then check bring forward edge browser
        // JsonNode windows = report.path("windows");
        // for (JsonNode window : windows) {
        // if (window.path("WindowTitle").asText().contains("edge")) {
        // // Bring edge forward
        // cmd = ipc.mapper.createObjectNode();
        // cmd.put("cmd", "EXECUTE");
        // cmd.put("atom", "FOCUS_WINDOW");
        // ObjectNode arg = ipc.mapper.createObjectNode();
        // arg.put("handle", window.path("hwnd").asInt());
        // cmd.set("args", arg);
        // ipc.send(cmd);
        // report = ipc.recv();
        // if ((status = report.path("status").asText()).equalsIgnoreCase("ok")) {
        // System.out.println("Sucessfully brought edge forward");
        // } else if (status.equalsIgnoreCase("error")) {
        // System.out.println(report.path("error").asText());
        // }
        // break;
        // }
        // }
        // } else if (status.equalsIgnoreCase("error")) {
        // System.out.println(report.path("error"));
        // }
        // }
        // System.out.println("Recieved:" + report.toString());
        // ---------------------------------------------------------------------------

        // ----------This one is for testing Click and Double click
        // Thread.sleep(5000);
        // if (report.path("status").asText().equals("Ready")) {
        // ObjectNode cmd = ipc.mapper.createObjectNode();
        // cmd.put("cmd", "EXECUTE");
        // cmd.put("atom", "DOUBLE_CLICK");
        // ipc.send(cmd);
        // String status;
        // report = ipc.recv();
        // if ((status = report.path("status").asText()).equalsIgnoreCase("ok")) {
        // System.out.println("YES DONE YAYY!!");
        // } else if (status.equalsIgnoreCase("error")) {
        // System.out.println(report.path("error").asText());
        // }
        // }
        // --------------------MouseMove
        // if (report.get("status").asText().equalsIgnoreCase("Ready")) {
        // ObjectNode cmd = ipc.mapper.createObjectNode();
        // cmd.put("cmd", "EXECUTE");
        // cmd.put("atom", "MOVE_MOUSE");
        // ObjectNode _args = ipc.mapper.createObjectNode();
        // _args.put("x", 0);
        // _args.put("y", 0);
        // cmd.set("args", _args);
        // ipc.send(cmd);
        // report = ipc.recv();
        // String status;
        // if ((status = report.path("status").asText()).equalsIgnoreCase("ok")) {
        // System.out.println("YES DONE YAYY!!");
        // } else if (status.equalsIgnoreCase("error")) {
        // System.out.println(report.path("error").asText());
        // }
        // }
        // --------------------Scrolling testing
        // Thread.sleep(5000);
        // if (report.get("status").asText().equalsIgnoreCase("Ready")) {
        // ObjectNode cmd = ipc.mapper.createObjectNode();
        // cmd.put("cmd", "EXECUTE");
        // cmd.put("atom", "SCROLL");
        // ObjectNode _args = ipc.mapper.createObjectNode();
        // _args.put("delta", -120 * 9);
        // cmd.set("args", _args);
        // ipc.send(cmd);
        // report = ipc.recv();
        // String status;
        // if ((status = report.path("status").asText()).equalsIgnoreCase("ok")) {
        // System.out.println("YES DONE YAYY!!")
        // } else if (status.equalsIgnoreCase("error")) {
        // System.out.println(report.path("error").asText());
        // }
        // }
        // ----------------------------------------------------------------
        Thread.sleep(5000);
        try {
            ipc.click("Visual Studio Code", ClickType.SINGLE_CLICK, ClickSide.RIGHT);
        } catch (Exception e) {
            System.out.println(e);
        }
        // ----------------------------------
        // ipc.OCRhandler();
        ObjectNode cmd = ipc.mapper.createObjectNode();
        // ipc.send(text);
        cmd.put("cmd", "SHUTDOWN");
        ipc.send(cmd);
        System.out.println("SHUTDOWN completed");
    }
}
