package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocolImpl;

import java.io.Closeable;
import java.util.function.Supplier;

public interface Server<T> extends Closeable {

    void serve();

    public static <T> Server<T> threadPerClient(
            int port,
            Supplier<StompMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory,
            Connections<T> connections) {
        return new BaseServer<T>(port, protocolFactory, encoderDecoderFactory, connections) {
            @Override
            protected void execute(BlockingConnectionHandler<T> handler) {
                new Thread(handler).start();
            }
        };
    }

    static <T> Server<T> reactor(
            int nthreads,
            int port,
            Supplier<StompMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory,
            Connections<T> connections) {

        return new Reactor<T>(nthreads, port, protocolFactory, encoderDecoderFactory, connections);
    }
}
