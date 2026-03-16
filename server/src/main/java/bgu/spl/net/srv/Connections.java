package bgu.spl.net.srv;
import java.util.concurrent.ConcurrentHashMap;

import java.io.IOException;

public interface Connections<T> {

    boolean send(int connectionId, T msg);
    void send(String channel, T msg);
    void subscribe(String channel,int subscribeid, int connectionId);
    void unsubscribe(int subscriptionId, int connectionId);
    ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> getSubscriptions();
    int generateConnectionId();
    void addCH(int connectionId, ConnectionHandler<T> handler);
    void disconnect(int connectionId);
    void registerUser(String username, String password);
    boolean loginUser(int connectionId, String username);
    ConnectionHandler<T> getCH(int connectionId);
    boolean isUserRegistered(String username);
    boolean userConnected(String username);
    boolean passwordValid(String username, String password);
    void sendToCH(ConnectionHandler<T> connectionhandler, T message);
}

