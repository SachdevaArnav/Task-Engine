import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        // new Thread(() -> {
        // try (var r = new java.io.BufferedReader(
        // new java.io.InputStreamReader(py.getInputStream()))) {
        // String line;
        // while ((line = r.readLine()) != null) {
        // System.out.println("[PY] " + line);
        // }
        // } catch (Exception ignored) {
        // }
        // }).start();
        try {
            System.out.println("is python alive" + py.isAlive());
            report = ipc.recv(); // waits until python response
        } catch (Exception e) {
            System.out.println("is python alive" + py.isAlive());
            report = new TextNode("");
        }
        System.out.println(report.asText());
        if (report.path("status").asText().equals("Ready")) {
            // lets ask for list of windows to chose from
            ObjectNode cmd = ipc.mapper.createObjectNode();
            cmd.put("cmd", "EXECUTE");
            cmd.put("atom", "LIST_WINDOWS");
            ipc.send(cmd);
            // Wait for response again
            String status;
            report = ipc.recv();
            if ((status = report.path("status").asText()).equalsIgnoreCase("ok")) {
                // if successful then check bring forward edge browser
                JsonNode windows = report.path("windows");
                for (JsonNode window : windows) {
                    if (window.path("WindowTitle").asText().contains("edge")) {
                        // Bring edge forward
                        cmd = ipc.mapper.createObjectNode();
                        cmd.put("cmd", "EXECUTE");
                        cmd.put("atom", "FOCUS_WINDOW");
                        ObjectNode arg = ipc.mapper.createObjectNode();
                        arg.put("handle", window.path("hwnd").asInt());
                        cmd.set("args", arg);
                        ipc.send(cmd);
                        report = ipc.recv();
                        if ((status = report.path("status").asText()).equalsIgnoreCase("ok")) {
                            System.out.println("Sucessfully brought edge forward");
                        } else if (status.equalsIgnoreCase("error")) {
                            System.out.println(report.path("error").asText());
                        }
                        break;
                    }
                }
            } else if (status.equalsIgnoreCase("error")) {
                System.out.println(report.path("error"));
            }
        }
        System.out.println("Recieved:" + report.toString());
    }
}
