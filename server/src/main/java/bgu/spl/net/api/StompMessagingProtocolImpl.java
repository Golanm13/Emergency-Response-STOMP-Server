package bgu.spl.net.api;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.impl.stomp.StompFrame;

public class StompMessagingProtocolImpl implements StompMessagingProtocol<String> {
    private int connectionId;
    private Connections<String> connections;
    private boolean shouldTerminate;
    private int messageId = 1;

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        this.shouldTerminate = false;
    }

    @Override
    public void process(String message) {
        StompFrame frame = StompFrame.parse(message);
        switch (frame.getCommand()) {
            case "CONNECT":
                handleConnect(frame);
                break;
            case "SUBSCRIBE":
                handleSubscribe(frame);
                break;
            case "UNSUBSCRIBE":
                handleUnsubscribe(frame);
                break;
            case "SEND":
                handleSend(frame);
                break;
            case "DISCONNECT":
                handleDisconnect(frame);
                break;
            default:
                sendError("Invalid frame command", frame.getHeaders().get("receipt"), frame.toString(),
                        "Invalid frame command " + frame.getCommand());
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private void handleConnect(StompFrame frame) {
        String username = frame.getHeaders().get("login");
        String password = frame.getHeaders().get("passcode");
        String receiptId = frame.getHeaders().get("receipt");
        if (username == null || password == null) {
            sendError("Missing login or passcode", receiptId, frame.toString(), null);
            shouldTerminate = true;
            return;
        }

        if (connections.userConnected(username)) {
            sendError("User already logged in", receiptId, frame.toString(),
                    "User " + username + " already logged in, logout first");
            shouldTerminate = true;
            return;
        }

        if (!connections.isUserRegistered(username)) {
            connections.registerUser(username, password);
        } else if (!connections.passwordValid(username, password)) {
            sendError("Wrong password", receiptId, frame.toString(), null);
            shouldTerminate = true;
            return;
        }

        if (connections.loginUser(connectionId, username)) {
            connections.send(connectionId, "CONNECTED\nversion:1.2\n\n\u0000");
            shouldTerminate = false;
        } else {
            sendError("Login failed", receiptId, frame.toString(), null);
            shouldTerminate = true;
        }
    }

    private void handleSubscribe(StompFrame frame) {
        String destination = frame.getHeaders().get("destination");
        String id = frame.getHeaders().get("id");
        String receiptId = frame.getHeaders().get("receipt");

        if (destination == null || id == null) {
            sendError("Missing destination or id", receiptId, frame.toString(), null);
            return;
        }

        try {
            int subscriptionId = Integer.parseInt(id);
            connections.subscribe(destination, subscriptionId, connectionId);
            if (receiptId != null) {
                sendReceipt(receiptId);
            }
        } catch (NumberFormatException e) {
            sendError("Invalid subscription id", receiptId, frame.toString(), null);
        }
    }

    private void handleUnsubscribe(StompFrame frame) {
        String id = frame.getHeaders().get("id");
        String receiptId = frame.getHeaders().get("receipt");

        if (id == null) {
            sendError("Missing id", receiptId, frame.toString(), null);
            return;
        }

        try {
            int subscriptionId = Integer.parseInt(id);
            connections.unsubscribe(subscriptionId, connectionId);
            if (receiptId != null) {
                sendReceipt(receiptId);
            }
        } catch (NumberFormatException e) {
            sendError("Invalid subscription id", receiptId, frame.toString(), null);
        }
    }

    private void handleSend(StompFrame frame) {
        String destination = frame.getHeaders().get("destination");
        String receiptId = frame.getHeaders().get("receipt");
        String body = frame.getBody();

        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> subscriptions = connections.getSubscriptions();
        ConcurrentHashMap<Integer, String> subscription = subscriptions.getOrDefault(connectionId, null);
        if (subscription == null) {
            sendError("User is not subscribed to any destination", receiptId, frame.toString(), "test description");
            return;
        }
        // find the subscription id of the user that the value is the destination
        int subscriptionId = -1;
        for (Integer key : subscription.keySet()) {
            if (subscription.get(key).equals(destination)) {
                subscriptionId = key;
                break;
            }
        }
        if (subscriptionId == -1) {
            sendError("User is not subscribed to this destination", receiptId, frame.toString(),
                    "User is not subscribed to destination " + destination);
            return;
        }
        HashMap<String, String> headers = new HashMap<>();
        headers.put("destination", destination);
        headers.put("subscription", subscriptionId + "");
        headers.put("message-id", messageId++ + "");

        StompFrame destinationFrame = new StompFrame("MESSAGE", headers, body);

        if (destination == null) {
            sendError("Missing destination", receiptId, frame.toString(), null);
            return;
        }

        connections.send(destination, destinationFrame.toString());
        if (receiptId != null) {
            sendReceipt(receiptId);
        }
    }

    private void handleDisconnect(StompFrame frame) {
        String receiptId = null;
        receiptId = frame.getHeaders().get("receipt");
        ConnectionHandler<String> handler = connections.getCH(connectionId);
        connections.disconnect(connectionId);

        if (receiptId != null) {
            String receiptFrame = "RECEIPT\nreceipt-id:" + receiptId + "\n\n\u0000";
            connections.sendToCH(handler, receiptFrame);
        }

        shouldTerminate = true;
    }

    private void sendError(String message, String receiptId, String originalFrame, String description) {
        StringBuilder errorFrame = new StringBuilder("ERROR");

        if (receiptId != null) {
            errorFrame.append("\nreceipt-id:").append(receiptId);
        }
        errorFrame.append("\nmessage:").append(message);

        if (originalFrame != null) {
            errorFrame.append("\n\nThe message:\n").append("-----\n");
            errorFrame.append(originalFrame);
            errorFrame.append("\n-----\n");
            if (description != null) {
                errorFrame.append(description);
            }
        } else {
            errorFrame.append("\n\n");
        }

        errorFrame.append("\u0000");
        connections.send(connectionId, errorFrame.toString());
        shouldTerminate = true;
        connections.disconnect(connectionId);
    }

    private void sendReceipt(String receiptId) {
        String receiptFrame = "RECEIPT\nreceipt-id:" + receiptId + "\n\n\u0000";
        connections.send(connectionId, receiptFrame);
    }
}
