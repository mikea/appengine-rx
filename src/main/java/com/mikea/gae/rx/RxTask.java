package com.mikea.gae.rx;

import com.google.common.base.Objects;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxTask<T extends Serializable> {
    private final T payload;

    private RxTask(T payload) {
        this.payload = payload;
    }

    public static <T extends Serializable> Builder<T> newBuilder() {
        return new Builder<>();
    }

    public byte[] toPayLoad() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(payload);
        }
        return baos.toByteArray();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("payload", payload)
                .toString();
    }

    public static <T extends Serializable> RxTask<T> fromRequest(HttpServletRequest request) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(request.getInputStream())) {
            T payload = (T) ois.readObject();
            return new RxTask<>(payload);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder<T extends Serializable> {
        private T payload;

        public RxTask<T> build() {
            return new RxTask<>(payload);
        }

        public Builder<T> payload(T payload) {
            this.payload = payload;
            return this;
        }
    }
}
