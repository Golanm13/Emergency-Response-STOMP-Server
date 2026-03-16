package bgu.spl.net.impl.rci;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.impl.newsfeed.NewsFeed;

public class RemoteCommandInvocationProtocol<T> implements StompMessagingProtocol<T> {

    private boolean shouldTerminate = false;
    private final NewsFeed feed;

    public RemoteCommandInvocationProtocol(NewsFeed feed) {
        this.feed = feed;
    }

    @Override
    public void start(int connectionId, Connections<T> connections) {
    }

    @Override
    public void process(T message) {
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
