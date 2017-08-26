package com.patex.utils.shingle;

import com.patex.LibException;

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
public class Shingler implements Iterable<byte[]> {

    private static final int PACK_SIZE = 100;
    private final Shingleable shingleable;
    private final MessageDigest digest;

    private final Byte16HashSet shingles ;
    private final List<byte[]> shinglesList = new ArrayList<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final ShinglerConfig config = new ShinglerConfig();

    private List<String> shingleWords = new ArrayList<>(config.shingleSize());

     Shingler(Shingleable shingleable) throws LibException {
        this.shingleable = shingleable;
        shingles=new Byte16HashSet(shingleable.size()/config.averageWordLength()/2);//some words will be skipped,(2 is magic number)
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new LibException(e.getMessage(), e);
        }
        prepare();
    }

    private void readNextPack() {
        readNextPack(PACK_SIZE, this::readNext);
    }

    private void prepare() {
        int shingleSize = config.shingleSize();
        while (shingleable.hasNext() && shingleWords.size() < shingleSize) {
            readNextPack(1, st -> {
                while (shingleWords.size() < shingleSize && st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (!config.skipWord(token)) {
                        shingleWords.add(token);
                    }
                }
                if (st.hasMoreTokens()) {
                    return readNext(st);
                }
                return 0;
            });
        }
    }

    private void readNextPack(int shingleCount, Function<StringTokenizer, Integer> f) {
        int i = 0;
        while (shingleable.hasNext() && i < shingleCount) {
            StringTokenizer st = new StringTokenizer(shingleable.next(), config.getDelimiters());
            i += f.apply(st);
        }
    }

    private int readNext(StringTokenizer st) {
        int count = 0;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!config.skipWord(token)) {
                byte[] shingleHash = this.digest.digest(shingleWords.toString().getBytes());
                shingles.add(shingleHash);
                shinglesList.add(shingleHash);
                shingleWords.remove(0);
                shingleWords.add(token);
                count++;
            }
        }
        return count;
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new ShingleHashIterator();
    }


    public boolean isSimilar(Shingler other) {
        Shingler bigger, smaller;
        if (other.shingleable.size() > this.shingleable.size()) {
            bigger = other;
            smaller = this;
        } else {
            smaller = other;
            bigger = this;
        }
        int notmatch = smaller.shingleable.size() / config.averageWordLength() / 5;
        for (byte[] shingleHash : smaller) {
            if (!bigger.contains(shingleHash)) {
                notmatch--;
                if (notmatch < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean contains(byte[] shingleHash) {
        if (shingles.contains(shingleHash)) {
            return true;
        } else {
            rwLock.readLock().lock();
            try {
                while (shingleable.hasNext()) {
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
            // TODO lemmatization
        }

        private int shingleSize() {
            return 10;
        }

        private int averageWordLength() {
            return 6;
        }

        public String getDelimiters() {
            return ".,!?:;„“…'-—–+\n\r()»« 1234567890/%№";
        }


    }

    private class ShingleHashIterator implements Iterator<byte[]> {
        int position = 1;
        byte[] next;

        public ShingleHashIterator() {
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
                        if (position >= shinglesList.size()) {
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
