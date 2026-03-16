package bgu.spl.net.impl.echo;

import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.ConnectionsImpl;


public class EchoServer {

    public static void main(String[] args) {

        Server<String> threadPerClientServer = Server.threadPerClient(
                7777, // port
                EchoProtocol::new, // protocol factory
                LineMessageEncoderDecoder::new, // message encoder decoder factory
                new ConnectionsImpl<>() // connections implementation
        );

        threadPerClientServer.serve();

        // Optionally, use the reactor server:
        /*
        Server<String> reactorServer = Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                7777, // port
                EchoProtocol::new, // protocol factory
                LineMessageEncoderDecoder::new // message encoder decoder factory
        );

        reactorServer.serve();
        */
    }
}
