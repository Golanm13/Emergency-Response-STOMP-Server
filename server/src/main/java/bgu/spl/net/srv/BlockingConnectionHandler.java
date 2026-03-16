package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.StompMessagingProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final StompMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private final Queue<T> messageQueue = new ConcurrentLinkedQueue<>(); // Queue for messages
    private final Connections<T> connections;
    private final int connectionId;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, Connections<T> connections,
            StompMessagingProtocol<T> protocol, int connectionId) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = connections;
        this.connectionId = connectionId;
        protocol.start(connectionId, connections);
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) {
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            int read;

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    messageQueue.add(nextMessage);
                }

                processQueue(); // Process all messages in the queue
            }
            close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void processQueue() {
        while (!messageQueue.isEmpty()) {
            T message = messageQueue.poll(); // Take message from queue
            if (message != null) {
                protocol.process(message); // Process the message
            }
        }
    }

    @Override
    public void send(T msg) {
        try {
            out.write(encdec.encode(msg));
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        connected = false; // Mark the handler as disconnected
        processQueue();
        connections.disconnect(connectionId);
        sock.close();
    }
}
