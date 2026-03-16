package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.StompMessagingProtocol;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class Reactor<T> implements Server<T> {
    private final int port;
    private final Supplier<StompMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> readerFactory;
    private final ActorThreadPool pool;
    private Selector selector;
    private Thread selectorThread;
    private final Connections<T> connections;
    private final ConcurrentLinkedQueue<Runnable> selectorTasks = new ConcurrentLinkedQueue<>();

    public Reactor(int numThreads, int port,
            Supplier<StompMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> readerFactory,
            Connections<T> connections) {
        this.pool = new ActorThreadPool(numThreads);
        this.port = port;
        this.protocolFactory = protocolFactory;
        this.readerFactory = readerFactory;
        this.connections = connections;
    }

    @Override
    public void serve() {
        selectorThread = Thread.currentThread();
        try (Selector selector = Selector.open();
                ServerSocketChannel serverSock = ServerSocketChannel.open()) {

            this.selector = selector; // just to be able to close

            serverSock.bind(new InetSocketAddress(port));
            serverSock.configureBlocking(false);
            serverSock.register(selector, SelectionKey.OP_ACCEPT);

            while (!selectorThread.isInterrupted()) {
                selector.select();
                runSelectionThreadTasks();
                for (SelectionKey key : selector.selectedKeys()) {
                    if (!key.isValid()) {
                        continue;
                    } else if (key.isAcceptable()) {
                        handleAccept(serverSock, selector);
                    } else {
                        handleReadWrite(key);
                    }
                }
                selector.selectedKeys().clear(); // clear the selected keys set so that we can know about new events
            }
        } catch (ClosedSelectorException ex) { // do nothing - server was requested to be closed
        } catch (IOException ex) { // this is an error
            ex.printStackTrace();
        }
        pool.shutdown();
    }

    private void handleAccept(ServerSocketChannel serverSock, Selector selector) throws IOException {
        SocketChannel clientSock = serverSock.accept();
        clientSock.configureBlocking(false);
        int connectionId = connections.generateConnectionId();
        NonBlockingConnectionHandler<T> handler = new NonBlockingConnectionHandler<>(
                readerFactory.get(),
                protocolFactory.get(),
                clientSock,
                this,
                connections,
                connectionId);
        connections.addCH(connectionId, handler);
        clientSock.register(selector, SelectionKey.OP_READ, handler);
    }

    @SuppressWarnings("unchecked")
    private void handleReadWrite(SelectionKey key) {
        NonBlockingConnectionHandler<T> handler = (NonBlockingConnectionHandler<T>) key.attachment();
        if (key.isReadable()) {
            Runnable task = handler.continueRead();
            if (task != null) {
                pool.submit(handler, task);
            }
        }
        if (key.isWritable()) {
            handler.continueWrite();
        }
    }

    @Override
    public void close() throws IOException {
        selector.close();
        pool.shutdown();
    }

    void updateInterestedOps(SocketChannel chan, int ops) {
        final SelectionKey key = chan.keyFor(selector);
        if (Thread.currentThread() == selectorThread) {
            key.interestOps(ops);
        } else {
            selectorTasks.add(() -> {
                if (key.isValid())
                    key.interestOps(ops);
            });
            selector.wakeup();
        }
    }

    private void runSelectionThreadTasks() {
        while (!selectorTasks.isEmpty()) {
            selectorTasks.remove().run();
        }
    }

}
