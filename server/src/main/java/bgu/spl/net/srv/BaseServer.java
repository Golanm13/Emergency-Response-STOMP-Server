package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {
    private final int port;
    private final Supplier<StompMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    private final Connections<T> connections;

    public BaseServer(int port, Supplier<StompMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory, Connections<T> connections) {
        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
        this.connections = connections;
        this.sock = null;
    }

    @Override
    public void serve() {
        try (ServerSocket serverSock = new ServerSocket(port)) {
            this.sock = serverSock;

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSock = serverSock.accept();
                int id = connections.generateConnectionId();

                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<T>(
                        clientSock,
                        encdecFactory.get(),
                        connections,
                        protocolFactory.get(),
                        id);
                connections.addCH(id, handler);
                execute(handler);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        if (sock != null) {
            sock.close();
        }
    }

    protected abstract void execute(BlockingConnectionHandler<T> handler);
}
