import java.io.DataInputStream;
import java.io.DataOutputStream;
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

public class EngineIPC implements AutoCloseable {
    private final DataInputStream in;
    private final DataOutputStream out;
    private final ObjectMapper mapper;
    private final Process py;

    public EngineIPC() throws Exception {
        mapper = new ObjectMapper();
        ProcessBuilder pb = new ProcessBuilder(Paths.get("PyEnv", "Scripts", "python.exe").toAbsolutePath().toString(),
                "-u",
                "src/main/py/Main.py");
        // pb.redirectErrorStream(true);
        py = pb.start();
        in = new DataInputStream(py.getInputStream());
        out = new DataOutputStream(py.getOutputStream());
        // System.out.println("is python alive" + py.isAlive());
        JsonNode report;
        // Thread t = new Thread(() -> {
        // try (var r = new java.io.BufferedReader(
        // new java.io.InputStreamReader(py.getErrorStream()))) {
        // String line;
        // while ((line = r.readLine()) != null) {
        // System.out.println("[PY-ERR] " + line);
        // }
        // } catch (Exception ignored) {
        // }
        // });
        // t.setDaemon(true);
        // t.start();
        report = recv();
    }

    @Override
    public void close() throws Exception {
        try {
            if (py != null && py.isAlive()) {
                ObjectNode cmd = this.mapper.createObjectNode();
                cmd.put("cmd", "SHUTDOWN");
                this.send(cmd);
                py.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
            }
        } finally {
            // 1. Flush and close the output stream to release resources
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }

            // 2. Close the input stream (Python's stdout)
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // Log or ignore
                }
            }

            // 3. Last line of defense: Force kill the process if it's still running
            if (py != null && py.isAlive()) {
                py.destroyForcibly();
            }
        }
    }

    // Receiving response
    private JsonNode recv() throws Exception {
        int length = in.readInt();
        // Catch corrupted or giant length inputs immediately
        if (length <= 0 || length > 10_000_000) {
            throw new Exception("Stream corruption detected! Invalid packet length: " + length);
        }
        byte[] buf = new byte[length];
        in.readFully(buf);
        return mapper.readTree(buf);
    }

    // Send command
    private void send(Object obj) throws Exception {
        byte[] data = mapper.writeValueAsBytes(obj);
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    private void sendAtom(String atom, JsonNode _args) throws Exception {
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
        return output;
    }

    private List<JsonNode> OCRhandler() throws Exception {
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
                // System.out.println(a + ":" + wordMetrices.score);
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
            // System.out.println("hello");
            if (!metrices_match) {
                continue;
            }
            score = avgScore;
            // System.out.println(raw + ":" + score);
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
        // System.out.println("text:" + bestMatch.get("text").asText());
        sendAtom("MOVE_MOUSE", report);
        // System.out.println("X: " + report.get("x").asInt());
        // System.out.println("Y: " + report.get("y").asInt());
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
                // System.out.println("Click executed successfully");
            }
        } else {
            throw new Exception("Invalid Json Format from Python");
        }
    }

    private JsonNode findControl(int handle, String query) throws Exception {
        query = query.replaceAll("[^A-Za-z0-9\\s]", "").strip().toLowerCase();
        // bring all the strings and compare with all and take the max or with some
        // weighted for all the 4 5 strings for each element
        // and repeat this for all actionable element
        try {
            ObjectNode _args = mapper.createObjectNode();
            _args.put("handle", handle);
            sendAtom("INSPECT_WINDOW", _args);
            JsonNode report = recv();
            if (report.get("status").asText().equalsIgnoreCase("error")) {
                throw new Exception(report.get("traceback").asText());
            } else if (report.get("status").asText().equalsIgnoreCase("ok")) {
                ArrayNode list = (ArrayNode) report.get("list");
                float score = 0, max = 0, bestScore = 0, coverage = 0;
                int total = 0;
                boolean selectThis;
                search2.what temp;
                JsonNode bestMatch = list.get(0);
                String requestedType = null;
                final String[] actonable_types = { "button",
                        "menuitem",
                        "edit",
                        "checkbox",
                        "radiobutton",
                        "combobox",
                        "tabitem",
                        "treeitem",
                        "listitem",
                        "hyperlink" };
                for (String t : actonable_types) {
                    if ((temp = search2.DL_light(query, t, search2.Mode.FAST)).match) {
                        query = query.replace(temp.replacement, "");
                        requestedType = t;
                        break;
                    }
                }
                String[] queryArr = query.strip().split("\\s+");
                String b;
                for (JsonNode node : list) {
                    selectThis = false;
                    for (String x : new String[] { "name", "parentName", "class_name", "automation_id",
                            "control_type" }) {
                        b = node.get(x).asText();
                        if (b == null || (b = b.strip()) == "")
                            continue;
                        score = 0;
                        total = 0;
                        coverage = 0;
                        for (String a : queryArr) {
                            total += a.length();
                            if (!(temp = search2.DL_light(b, a, search2.Mode.FAST)).match)
                                continue;
                            score += temp.score * a.length();
                            if (!x.equalsIgnoreCase("parentName"))
                                coverage += a.length();
                        }
                        coverage /= total;
                        selectThis = selectThis | (coverage >= 0.5);
                        score /= total;
                        max = Math.max(score, max);
                    }
                    // if (max > 0.7) {
                    // for (String a : queryArr) {
                    // if ((temp = search2.DL_light(node.get("control_type").asText(), a,
                    // search2.Mode.FAST)).match) {
                    // // giving extra points for those having control type info like button or
                    // // checkbox becuase different UI element of different type are more likely to
                    // // share same name
                    // max += 0.2;
                    // selectThis = true;
                    // break;
                    // }
                    // }
                    // }
                    if (requestedType != null && max > 0.5
                            && node.get("control_type").asText().equalsIgnoreCase(requestedType)) {
                        max += 0.2;
                    }
                    if (selectThis && max > bestScore) {
                        bestScore = max;
                        bestMatch = node;
                    }
                    // if (max == 1) {
                    // System.out.println("SEE");
                    // }
                }
                // send bestMatch
                if (bestScore == 0)
                    throw new Exception("Failed to find the desired Actionable element");
                return (bestMatch);
            }
        } catch (Exception e) {
            throw e;
        }
        throw new Exception("Failed in control query handling");
    }

    public void activateControl(int handle, String query) throws Exception {
        try {
            JsonNode target = findControl(handle, query);
            // System.out.println(target);
            ObjectNode _args = mapper.createObjectNode();
            _args.set("target", target);
            _args.put("handle", handle);
            sendAtom("ACTIVATE_CONTROL", _args);
            JsonNode report = recv();
            // System.out.println(report);
            if (report.get("status").asText().equalsIgnoreCase("error")) {
                throw new Exception(report.get("error").asText());
            }
        } catch (Exception e) {
            // single click the query matching control
            // and before that bring the window forward
            try {
                ObjectNode _args = mapper.createObjectNode();
                _args.put("handle", handle);
                sendAtom("FOCUS_WINDOW", _args);
                JsonNode report = recv();
                if (report.get("status").asText().equalsIgnoreCase("error")) {
                    throw new Exception(report.get("error").asText());
                }
                click(query, ClickType.SINGLE_CLICK, ClickSide.LEFT);
            } catch (Exception ee) {
                throw new Exception("Failed to activate Control " + query + " : " + ee + "from base: " + e);
            }
        }
    }

    public boolean controlExists(int handle, String query) {
        try {
            findControl(handle, query);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean TextExists(int handle, String query) {
        try {
            query = query.replaceAll("[^A-Za-z0-9. ]", "").strip().toLowerCase();
            if (query == "") {
                throw new Exception("seemingly empty query");
            }
            String[] queryArr = query.split(" ");
            ObjectNode _args = mapper.createObjectNode();
            _args.put("handle", handle);
            sendAtom("FOCUS_WINDOW", _args);
            JsonNode report = recv();
            if (report.get("status").asText().equalsIgnoreCase("error"))
                throw new Exception(report.get("error").asText());
            List<JsonNode> visibleTextNodes = OCRhandler();
            for (JsonNode textNode : visibleTextNodes) {
                float coverage = 0;
                int total = 0;
                for (String q : queryArr) {
                    if (search2.DL_light(textNode.get("text").asText().toLowerCase(), q,
                            search2.Mode.FAST).match) {
                        coverage += q.length();
                    }
                    total += q.length();
                }
                coverage /= total;
                if (coverage >= 0.5)
                    return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public int getHandle(String windowQuery) throws Exception {
        windowQuery = windowQuery.replaceAll("[^A-Za-z0-9 ]", "").strip().toLowerCase();
        if (windowQuery == "") {
            throw new Exception("seemingly empty query");
        }
        String[] queryArr = windowQuery.split(" ");
        ObjectNode _args = mapper.createObjectNode();
        sendAtom("LIST_WINDOWS", _args);
        JsonNode report = recv();
        if (report.get("status").asText().equalsIgnoreCase("error")) {
            throw new Exception(report.get("error").asText());
        }
        ArrayNode list = (ArrayNode) report.get("windows");
        float bestScore = 0;
        JsonNode bestMatch = list.get(0);
        for (JsonNode node : list) {
            float coverage1 = 0, coverage2 = 0, score = 0;
            int total = 0;
            for (String q : queryArr) {
                if (search2.DL_light((node.get("WindowTitle").asText().toLowerCase()), q,
                        search2.Mode.FAST).match) {
                    coverage1 += q.length();
                }
                if (search2.DL_light(node.get("process").asText().toLowerCase(), q, search2.Mode.FAST).match) {
                    coverage2 += q.length();
                }
                total += q.length();
            }
            coverage1 /= total;
            coverage2 /= total;
            score = Math.max(coverage1, coverage2);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = node;
            }
        }
        if (bestScore == 0) {
            throw new Exception("Cannot find the needed window");
        }
        return bestMatch.get("hwnd").asInt();
    }
}
