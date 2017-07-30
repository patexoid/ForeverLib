package com.patex.utils.shingle;

import com.patex.LibException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 *
 */
public class Shingler implements Iterable<ShingleHash> {

    private static final int PACK_SIZE = 100;
    private final Shingleable shingleable;
    private final MessageDigest digest;

    private final Set<ShingleHash> shingles = new HashSet<>();
    private final List<ShingleHash> shinglesList = new ArrayList<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final ShinglerConfig config = new ShinglerConfig();

    private List<String> shingleWords = new ArrayList<>(config.shingleSize());

     Shingler(Shingleable shingleable) throws LibException {
        this.shingleable = shingleable;
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
            readNextPack(0, st -> {
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

    private void readNextPack(int shingleCount, Function<StringTokenizer, Integer> consumer) {
        int i = 0;
        do {
            StringTokenizer st = new StringTokenizer(shingleable.next(), config.getDelimiters());
            i += consumer.apply(st);
        } while (shingleable.hasNext() && i < shingleCount);
    }

    private int readNext(StringTokenizer st) {
        int count = 0;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!config.skipWord(token)) {
                ShingleHash shingleHash = new ShingleHash(digest.digest(shingleWords.toString().getBytes()));
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
    public Iterator<ShingleHash> iterator() {
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
        for (ShingleHash shingleHash : smaller) {
            if (!bigger.contains(shingleHash)) {
                notmatch--;
                if (notmatch < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean contains(ShingleHash shingleHash) {
        if (shingles.contains(shingleHash)) {
            return true;
        } else {
            while (shingleable.hasNext()) {
                readNextPack();
                if (shingles.contains(shingleHash)) {
                    return true;
                }
            }
        }
        return false;
    }


    //TODO make interface and support multi language
    private static class ShinglerConfig {

        private static Set<String> SKIP_WORDS = new HashSet<>(Arrays.asList(
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

    private class ShingleHashIterator implements Iterator<ShingleHash> {
        int position = 0;

        @Override
        public boolean hasNext() {
            rwLock.readLock().lock();
            try {
                return position < shinglesList.size() || shingleable.hasNext();
            } finally {
                rwLock.readLock().unlock();
            }
        }

        @Override
        public ShingleHash next() {
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
                return shinglesList.get(position++);
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }
}
