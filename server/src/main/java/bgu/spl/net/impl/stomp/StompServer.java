package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessageEncoderDecoderImpl;
import bgu.spl.net.api.StompMessagingProtocolImpl;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.ConnectionsImpl;

public class StompServer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: StompServer <port> <type>");
            System.out.println("<type> should be either 'tpc' or 'reactor'");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String type = args[1].toLowerCase();
        ConnectionsImpl connections = new ConnectionsImpl<String>();

        switch (type) {
            case "tpc":
                Server<String> server = Server.threadPerClient(
                        port,
                        () -> new StompMessagingProtocolImpl(),
                        () -> new StompMessageEncoderDecoderImpl(),
                        connections);
                server.serve();
                break;

            case "reactor":
                int numThreads = Runtime.getRuntime().availableProcessors();
                Server<String> server1 = Server.reactor(
                        numThreads,
                        port,
                        () -> new StompMessagingProtocolImpl(),
                        () -> new StompMessageEncoderDecoderImpl(),
                        connections);
                server1.serve();
                break;

            default:
                System.out.println("Invalid type. Use 'tpc' for Thread-Per-Client or 'reactor' for Reactor.");
        }
    }
}
