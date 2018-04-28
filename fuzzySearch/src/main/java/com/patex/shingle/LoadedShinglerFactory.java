package com.patex.shingle;


import com.google.common.collect.EvictingQueue;
import com.patex.shingle.byteSet.ByteHashSet;
import com.patex.shingle.byteSet.ByteSetFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 */
class LoadedShinglerFactory {

    private final int coef;
    private final Function<byte[], byte[]> hasher;
    private final ShinglerConfig config = new ShinglerConfig();
    private final int byteArraySize;

    LoadedShinglerFactory(int coef, int byteArraySize,
                          Supplier<Function<byte[], byte[]>> hasherCreator) {
        this.coef = coef;
        this.hasher = hasherCreator.get();
        this.byteArraySize = byteArraySize;
    }

    public ByteHashSet createShingles(Shingleable shingleable) {
        ShingleIterator shingleIterator = new ShingleIterator(shingleable);
        int skip = config.shingleSize();
        while (shingleIterator.hasNext() && skip-- > 1) {
            shingleIterator.next();
        }
        int size = shingleable.size() / config.averageWordLength() / 2 / coef;
        ByteHashSet byteSet = ByteSetFactory.createByteSet(size, this.byteArraySize);
        while (shingleIterator.hasNext()) {
            byte[] bytes = shingleIterator.next();
            byte d = 0;
            for (byte b : bytes) {
                d ^= b;
            }
            if (d % coef == 0) {
                byteSet.add(bytes);
            }
        }
        return byteSet;
    }

    //TODO make interface and support multi language
    private static class ShinglerConfig {

        private final static Set<String> SKIP_WORDS = new java.util.HashSet<>(Arrays.asList(
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

    private class ShingleIterator implements Iterator<byte[]> {
        private final Shingleable shingleable;
        private final List<String> chunk = new ArrayList<>();
        private final EvictingQueue<String> words = EvictingQueue.create(10);

        ShingleIterator(Shingleable shingleable) {
            this.shingleable = shingleable;
            loadNextChunk();
        }

        @Override
        public boolean hasNext() {
            return shingleable.hasNext() || !chunk.isEmpty();
        }

        @Override
        public byte[] next() {
            words.add(chunk.remove(0));
            if (chunk.isEmpty()) {
                loadNextChunk();
            }
            return hasher.apply(words.toString().getBytes());
        }

        private void loadNextChunk() {
            while (shingleable.hasNext() && chunk.isEmpty()) {
                StringTokenizer st = new StringTokenizer(shingleable.next(), config.getDelimiters());
                while (st.hasMoreTokens()) {
                    String word = config.normalize(st.nextToken());
                    if (!config.skipWord(word)) {
                        chunk.add(word);
                    }
                }
            }
        }
    }
}
