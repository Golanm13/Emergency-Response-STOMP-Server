package bgu.spl.net.impl.stomp;

import java.util.HashMap;
import java.util.Map;

public class StompFrame {
    private String command;
    private Map<String, String> headers;
    private String body;

    public StompFrame(String command, Map<String, String> headers, String body) {
        this.command = command;
        this.headers = headers;
        this.body = body;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    // Parse incoming message string into a StompFrame object
    public static StompFrame parse(String message) {
        String[] parts = message.split("\n\n", 2);
        String[] lines = parts[0].split("\n");
        String command = lines[0];
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String[] keyValue = lines[i].split(":", 2);
            headers.put(keyValue[0], keyValue[1]);
        }
        String body = parts.length > 1 ? parts[1].trim() : "";
        return new StompFrame(command, headers, body);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(command + "\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey() + ":" + entry.getValue() + "\n");
        }
        sb.append("\n" + body + "\u0000");
        return sb.toString();
    }
}
