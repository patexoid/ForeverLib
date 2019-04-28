package com.patex.service;

import com.patex.entities.BookFileID;
import com.patex.entities.DuplicateCheckRequest;
import com.patex.entities.DuplicateCheckResponse;
import com.patex.parser.ParserService;
import com.patex.shingle.ShingleCacheStorage;
import com.patex.shingle.ShingleMatcher;
import com.patex.shingle.Shingleable;
import com.patex.storage.StorageService;
import com.patex.utils.ExecutorCreator;
import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class DuplicateQueueHandler {

    private final ShingleMatcher<BookFileID, Long> shingleMatcher;
    private final ParserService parserService;
    private final StorageService fileStorage;
    private final ExecutorService executorService;
    private final RabbitTemplate rabbitTemplate;
    private final String responseQueue;
    private final String requestQueue;
    private final RabbitAdmin admin;

    public DuplicateQueueHandler(ParserService parserService, StorageService fileStorage,
                                 ExecutorCreator executorCreator, RabbitTemplate rabbitTemplate,
                                 @Value("${duplicateCheck.threadCount:0}") int threadCount,
                                 @Value("${duplicateCheck.shingleCoeff:1}") int coef,
                                 @Value("${duplicateCheck.fastCacheSize:100}") int cacheSize,
                                 @Value("${duplicateCheck.storageCacheFolder:}") String storageFolder,
                                 @Value("${duplicateCheck.requestQueue}") String requestQueue,
                                 @Value("${duplicateCheck.responseQueue}") String responseQueue, RabbitAdmin admin) {
        this.parserService = parserService;
        this.fileStorage = fileStorage;
        this.rabbitTemplate = rabbitTemplate;
        this.responseQueue = responseQueue;
        this.requestQueue = requestQueue;
        this.admin = admin;
        shingleMatcher = new ShingleMatcher<>(ShingleableBookFileID::new,
                BookFileID::getBookId, coef, cacheSize);
        if (StringUtils.isNotEmpty(storageFolder)) {
            shingleMatcher.setStorage(new BookShingleCacheStorage(storageFolder));
        }
        if (threadCount == 0) {
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            threadCount = availableProcessors > 1 ? availableProcessors / 2 : 1;
        }
        executorService = Executors.newFixedThreadPool(threadCount,
                executorCreator.createThreadFactory("checkForDuplicate-", log));
    }

    @RabbitListener(queues = "${duplicateCheck.requestQueue}")
    public void queueListener(DuplicateCheckRequest checkRequest) {
        executorService.execute(() -> checkRequest.getOther().stream().
                filter(o -> shingleMatcher.isSimilar(checkRequest.getBookFileID(), o)).
                findFirst().
                ifPresent(o -> duplicateResponse(checkRequest.getBookFileID(), o, checkRequest.getUsername())));
    }

    private void duplicateResponse(BookFileID first, BookFileID second, String username) {
        DuplicateCheckResponse response = new DuplicateCheckResponse(first.getBookId(), second.getBookId(), username);
        rabbitTemplate.convertAndSend(responseQueue, response);
    }

    private class ShingleableBookFileID implements Shingleable {
        private final BookFileID bookFileID;
        private Iterator<String> contentIterator;
        private InputStream is;

        ShingleableBookFileID(BookFileID bookFileID) {
            this.bookFileID = bookFileID;
            is = fileStorage.load(bookFileID.getFilePath());
            this.contentIterator = parserService.getContentIterator(bookFileID.getFilePath(), is);
        }

        @Override
        public int size() {
            return bookFileID.getContentSize();
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = contentIterator.hasNext();
            if (!hasNext) {
                try {
                    close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            return hasNext;
        }

        @Override
        public String next() {
            return contentIterator.next();
        }

        @Override
        public void close() throws IOException {
            is.close();
            if (contentIterator instanceof Closeable) {
                ((Closeable) contentIterator).close();
            }
        }
    }

    private static class BookShingleCacheStorage implements ShingleCacheStorage<BookFileID> {
        private final String storageFolder;

        BookShingleCacheStorage(String storageFolder) {
            this.storageFolder = storageFolder;
        }

        @Override
        public InputStream load(BookFileID book) {
            File cache = getCacheFile(book);
            if (cache.exists()) {
                try {
                    return new FileInputStream(cache);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private File getCacheFile(BookFileID book) {
            return new File(storageFolder + "/" + book.getBookId());
        }

        @Override
        public void save(byte[] bytes, BookFileID book) {
            try (FileOutputStream fos = new FileOutputStream(getCacheFile(book))) {
                fos.write(bytes);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void waitForFinish() {
        try {
            Thread.sleep(1000);
            waitForQueueFinish(requestQueue);
            Thread.sleep(500);
            waitForQueueFinish(responseQueue);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void waitForQueueFinish(String queueName) {
        while (true) {
            int count = getQueueCount(queueName);
            log.trace(queueName + " count:" + count);
            if (count == 0) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private int getQueueCount(final String name) {
        AMQP.Queue.DeclareOk declareOk = admin.getRabbitTemplate().execute(channel -> channel.queueDeclarePassive(name));
        return declareOk.getMessageCount();
    }
}
