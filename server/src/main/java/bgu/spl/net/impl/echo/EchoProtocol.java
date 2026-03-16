package bgu.spl.net.impl.echo;

import bgu.spl.net.api.StompMessagingProtocol;

public class EchoProtocol<T> implements StompMessagingProtocol<T> {

    private boolean shouldTerminate = false;

    @Override
    public void start(int connectionId, bgu.spl.net.srv.Connections<T> connections) {
    }

    @Override
    public void process(T message) {
        System.out.println("Processing message: " + message);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
