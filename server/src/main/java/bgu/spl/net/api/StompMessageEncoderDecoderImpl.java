package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StompMessageEncoderDecoderImpl<T> implements MessageEncoderDecoder<T> {

    private byte[] buffer = new byte[1024];
    private int length = 0;

    @Override
    public T decodeNextByte(byte nextByte) {
        if (nextByte == '\u0000') {
            String message = new String(buffer, 0, length, StandardCharsets.UTF_8);
            length = 0;
            return (T) message; // המרה ל-T
        } else {
            if (length >= buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length * 2);
            }
            buffer[length++] = nextByte;
            return null;
        }
    }

    @Override
    public byte[] encode(T message) {
        return (message.toString()).getBytes(StandardCharsets.UTF_8);
    }
}
