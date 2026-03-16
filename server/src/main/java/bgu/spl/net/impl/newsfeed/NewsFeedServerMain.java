package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;

public class NewsFeedServerMain {

    public static void main(String[] args) {
        NewsFeed feed = new NewsFeed();
        Connections<String> connections = new ConnectionsImpl<>();

        Server.<String>threadPerClient(
            7777,
            () -> new RemoteCommandInvocationProtocol<>(feed),
            ObjectEncoderDecoder::new,
            connections
        ).serve();
    }
}
