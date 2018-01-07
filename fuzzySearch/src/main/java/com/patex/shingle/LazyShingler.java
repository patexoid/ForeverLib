package com.patex.shingle;


import com.patex.shingle.byteSet.ByteHashSet;
import com.patex.shingle.byteSet.ByteSetFactory;

import java.io.Closeable;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 *
 */
public class LazyShingler implements Shingler, Closeable {

    private static final int PACK_SIZE = 100;
    private final int coef;
    private final MessageDigest digest;
    private final ShinglerConfig config = new ShinglerConfig();
    private final ByteHashSet shingles;
    private final List<byte[]>  shinglesList = new ArrayList<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final List<String> shingleWords = new ArrayList<>(config.shingleSize());
    private Shingleable shingleable;
    private int size;

    {
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    LazyShingler(Shingleable shingleable, int coef, int byteArraySize) {
        this.coef = coef;
        this.shingleable = shingleable;
        size = shingleable.size() / config.averageWordLength();

        shingles = ByteSetFactory.createByteSet(size/2/coef, byteArraySize);//some words will be skipped,(2 is magic number)
        prepare();
    }

    private void readNextPack() {
        readNextPack(PACK_SIZE, this::readNext);
    }

    private void prepare() {
        int shingleSize = config.shingleSize();
        while (isLoading() && shingleWords.size() < shingleSize) {
            readNextPack(1, st -> {
                while (shingleWords.size() < shingleSize && st.hasMoreTokens()) {
                    String token = st.nextToken();
                    token = config.normalize(token);
                    if (!config.skipWord(token)) {
                        shingleWords.add(token);
                    }
                }
                addShingle(this.digest.digest(shingleWords.toString().getBytes()));
                if (st.hasMoreTokens()) {
                    return readNext(st);
                }
                return 0;
            });
        }
    }

    private boolean isLoading() {
        return shingleable != null;
    }

    ByteHashSet getShingles() {
        return shingles;
    }

    private void closeIfRequiered() {
        if (isLoading() && !shingleable.hasNext()) {
            close();
        }
    }

    private void readNextPack(int shingleCount, Function<StringTokenizer, Integer> f) {
        int i = 0;
        while (shingleable.hasNext() && i < shingleCount) {
            StringTokenizer st = new StringTokenizer(shingleable.next(), config.getDelimiters());
            i += f.apply(st);
        }
        closeIfRequiered();
    }

    private int readNext(StringTokenizer st) {
        int count = 0;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!config.skipWord(token)) {
                shingleWords.remove(0);
                shingleWords.add(token);
                byte[] digest = this.digest.digest(shingleWords.toString().getBytes());
                byte d=0;
                for (byte b : digest) {
                    d^=b;
                }
                if (d % coef == 0) {
                    addShingle(digest);
                    count++;
                }
            }
        }
        return count;
    }


    private void addShingle(byte[] shingleHash) {
        shingles.add(shingleHash);
        shinglesList.add(shingleHash);
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new ShingleHashIterator();
    }


    @Override
    public int size() {
        return size;
    }

    public boolean contains(byte[] shingleHash) {
        if (shingles.contains(shingleHash)) {
            return true;
        } else {
            rwLock.readLock().lock();
            try {
                while (isLoading()) {
                    rwLock.readLock().unlock();
                    rwLock.writeLock().lock();
                    try {
                        readNextPack();
                    } finally {
                        rwLock.writeLock().unlock();
                        rwLock.readLock().lock();
                    }
                    if (shingles.contains(shingleHash)) {
                        return true;
                    }
                }
            } finally {
                rwLock.readLock().unlock();
            }
        }
        return false;
    }


    public void loadAll(){
        while (isLoading()){
            readNextPack();
        }
    }
    @Override
    public void close() {
        try {
            if (shingleable != null)
                shingleable.close();
            shingleable = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO make interface and support multi language
    private static class ShinglerConfig {

        private static Set<String> SKIP_WORDS = new java.util.HashSet<>(Arrays.asList(
                "это", "как", "так", "и", "в", "над", "к", "до", "не", "на", "но",
                "за", "то", "с", "ли", "а", "во", "от", "со", "для", "о", "же", "ну",
                "вы", "что", "кто'", "он", "она", " "));

        boolean skipWord(String word) {
            return SKIP_WORDS.contains(word);
        }

        String normalize(String s) {
            return s.toLowerCase();
        }

        private int shingleSize() {
            return 10;
        }

        private int averageWordLength() {
            return 6;
        }

        private String getDelimiters() {
            return ".,!?:;„“…'\"-—–+\n\r()»« 1234567890/%№";
        }


    }

    private class ShingleHashIterator implements Iterator<byte[]> {
        int position = 1;
        byte[] next;

        ShingleHashIterator() {
            if (shinglesList.isEmpty()) {
                readNextPack();
            }
            if (!shinglesList.isEmpty()) {
                next = shinglesList.get(0);
            }
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public byte[] next() {
            rwLock.readLock().lock();
            try {
                if (position >= shinglesList.size()) {
                    rwLock.readLock().unlock();
                    rwLock.writeLock().lock();
                    try {
                        if (isLoading() && position >= shinglesList.size()) {
                            readNextPack();
                        }
                    } finally {
                        rwLock.writeLock().unlock();
                        rwLock.readLock().lock();
                    }
                }
                byte[] next0 = next;
                if (position >= shinglesList.size()) {
                    next = null;
                } else {
                    next = shinglesList.get(position++);
                }
                return next0;
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }
}
