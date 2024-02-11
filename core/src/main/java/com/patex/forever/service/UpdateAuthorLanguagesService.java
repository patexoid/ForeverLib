package com.patex.forever.service;


import com.google.common.collect.Lists;
import com.patex.forever.entities.AuthorRepository;
import com.patex.forever.model.BookAuthor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateAuthorLanguagesService {

    private final AuthorRepository authorRepository;

    private final Set<Long> authorsForLangUpdate = new HashSet<>();

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private boolean scheduled;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public synchronized void updateLanguages(Collection<Long> authorIds) {
        readWriteLock.readLock().lock();

        try {
            authorsForLangUpdate.addAll(authorIds);
            if (!scheduled) {
                scheduled = true;
                executor.schedule(() -> updateLanguages(), 1L, TimeUnit.MINUTES);
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private void updateLanguages() {
        List<Long> ids;
        readWriteLock.writeLock().lock();
        try {
            ids = new ArrayList<>(this.authorsForLangUpdate);
            authorsForLangUpdate.clear();
            scheduled = false;
        } finally {
            readWriteLock.writeLock().unlock();
        }
        Lists.partition(ids, 1000).forEach(authorRepository::updateLang);
    }

    @EventListener
    public void onBookCreation(BookCreationEvent event) {
        try {
            updateLanguages(event.getBook().getAuthors().stream().map(BookAuthor::getId).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
}


