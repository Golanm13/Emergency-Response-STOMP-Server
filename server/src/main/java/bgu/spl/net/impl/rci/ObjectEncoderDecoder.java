package bgu.spl.net.impl.rci;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.io.*;

public class ObjectEncoderDecoder<T extends Serializable> implements MessageEncoderDecoder<T> {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private ObjectInputStream inputStream;

    @Override
    public T decodeNextByte(byte nextByte) {
        try {
            outputStream.write(nextByte);
            if (inputStream == null) {
                inputStream = new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
            }
            return (T) inputStream.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public byte[] encode(T message) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(message);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode message", e);
        }
    }
}
