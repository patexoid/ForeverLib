package com.patex.shingle;

import com.patex.shingle.byteSet.ByteHashSet;
import com.patex.shingle.byteSet.ByteSetFactory;

import java.io.*;
import java.util.Optional;

class ShingleCache<T> {

    private ShingleCacheStorage<T> storage= new ShingleCacheStorage<>() {
        @Override
        public InputStream load(T t) {
            return null;
        }

        @Override
        public void save(byte[] bytes, T t) {

        }
    };

    public ShingleCache() {
    }

    public ShingleCacheStorage<T> getStorage() {
        return storage;
    }

    public void setStorage(ShingleCacheStorage<T> storage) {
        this.storage = storage;
    }

    public Optional<Shingler> getFromCache(T o) throws IOException {
        try(InputStream is = storage.load(o)) {
            if (is == null) {
                return Optional.empty();
            }
            int size = readInt(is);
            int byteArrayLength = readInt(is);
            ByteHashSet set = ByteSetFactory.createByteSet(size, byteArrayLength);
            byte[] buffer = new byte[128 * 128];
            int bufferReadOff = 0;
            while (true) {
                int readBytesCount = is.read(buffer, bufferReadOff, buffer.length - bufferReadOff);
                if (readBytesCount == -1) {
                    if (bufferReadOff != 0) {
                        System.out.println("warning broken cache");
                        return Optional.empty();
                    }
                    break;
                }
                readBytesCount += bufferReadOff;
                int position = 0;
                while (readBytesCount - position >= byteArrayLength) {
                    byte[] shingle = new byte[byteArrayLength];
                    for (int i = 0; i < byteArrayLength; i++) {
                        shingle[i] = buffer[position++];
                    }
                    set.add(shingle);
                }
                if (position < readBytesCount) {
                    int i = 0;
                    for (; i < byteArrayLength && position < readBytesCount; i++) {
                        buffer[i] = buffer[position++];
                    }
                    bufferReadOff = i;
                }
            }
            LoadedShingler shingler = new LoadedShingler(set);
            return Optional.of(shingler);
        }
    }

    private int readInt(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
    }

    public void saveToCache(Shingler shingler, T t) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeInt(baos, shingler.size());
        writeInt(baos, shingler.getByteArraySize());
        for (byte[] bytes : shingler) {
            baos.write(bytes);
        }
        storage.save(baos.toByteArray(), t);
    }

    private void writeInt(OutputStream out, int v) throws IOException {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write(v & 0xFF);
    }
}
