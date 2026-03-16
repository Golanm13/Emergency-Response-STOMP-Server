package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {

    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> subscriptionIds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> registeredUsers = new ConcurrentHashMap<>();
    private final AtomicInteger connectionIdGenerator = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, ConnectionHandler<T>> connections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> activeUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>> channels = new ConcurrentHashMap<>();

    public ConnectionsImpl() {}

    public int generateConnectionId() {
        return connectionIdGenerator.incrementAndGet();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = connections.get(connectionId);
        if (handler == null) {
            return false;
        }
        handler.send(msg);
        return true;
    }


    @Override
    public void send(String channel, T msg) {
        CopyOnWriteArrayList<Integer> recipients = channels.get(channel);
        if (recipients != null) {
            for (Integer connectionId : recipients) {
                send(connectionId, msg);
            }
        }
    }

    @Override
    public void disconnect(int connectionId) {
        connections.remove(connectionId);
        ConcurrentHashMap<Integer, String> subs = subscriptionIds.remove(connectionId);
        if (subs != null) {
            subs.forEach((subId, chan) -> {
                CopyOnWriteArrayList<Integer> subsList = channels.get(chan);
                if (subsList != null) {
                    subsList.remove((Integer) connectionId);
                }
            });
        }
        activeUsers.values().removeIf(id -> id.equals(connectionId));
    }

    @Override
    public void addCH(int connectionId, ConnectionHandler<T> handler) {
        connections.put(connectionId, handler);
    }

    @Override
    public void subscribe(String channel, int subscriptionId, int connectionId) {
        channels.putIfAbsent(channel, new CopyOnWriteArrayList<>());
        channels.get(channel).add(connectionId);
        subscriptionIds.putIfAbsent(connectionId, new ConcurrentHashMap<>());
        subscriptionIds.get(connectionId).put(subscriptionId, channel);
    }
    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> getSubscriptions() {
        return subscriptionIds;
    }

    @Override
    public void unsubscribe(int subscriptionId, int connectionId) {
        ConcurrentHashMap<Integer, String> subs = subscriptionIds.get(connectionId);
        if (subs == null) return;
        String channel = subs.remove(subscriptionId);
        if (channel != null) {
            CopyOnWriteArrayList<Integer> subsList = channels.get(channel);
            if (subsList != null) {
                subsList.remove((Integer) connectionId);
            }
        }
        if (subs.isEmpty()) {
            subscriptionIds.remove(connectionId);
        }
    }

    @Override
    public boolean isUserRegistered(String username) {
        return registeredUsers.containsKey(username);
    }

    @Override
    public boolean passwordValid(String username, String password) {
        return password.equals(registeredUsers.get(username));
    }

    @Override
    public void registerUser(String username, String password) {
        registeredUsers.putIfAbsent(username, password);
    }

    @Override
    public boolean userConnected(String username) {
        return activeUsers.containsKey(username);
    }

    @Override
    public boolean loginUser(int connectionId, String username) {
        if (activeUsers.containsKey(username)) {
            return false;
        }
        activeUsers.put(username, connectionId);
        return true;
    }

    @Override
    public ConnectionHandler<T> getCH(int connectionId) {
        return connections.get(connectionId);
    }

    public void sendToCH(ConnectionHandler<T> connectionhandler, T message) {
        connectionhandler.send(message);
    }
}
