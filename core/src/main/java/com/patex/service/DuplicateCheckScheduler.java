package com.patex.service;

import com.patex.LibException;
import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.entities.BookCreationEvent;
import com.patex.entities.BookFileID;
import com.patex.entities.BookSequence;
import com.patex.entities.DuplicateCheckRequest;
import com.patex.entities.DuplicateCheckResponse;
import com.patex.entities.Sequence;
import com.patex.messaging.MessengerService;
import com.patex.utils.Res;
import com.patex.utils.StreamU;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DuplicateCheckScheduler {

    private final RabbitTemplate rabbitTemplate;
    private final TransactionService transactionService;
    private final BookService bookService;
    private final MessengerService messenger;
    private final String requestQueue;

    public DuplicateCheckScheduler(RabbitTemplate rabbitTemplate, TransactionService transactionService,
                                   BookService bookService, MessengerService messenger,
                                   @Value("${duplicateCheck.requestQueue}") String requestQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.transactionService = transactionService;
        this.bookService = bookService;
        this.messenger = messenger;
        this.requestQueue = requestQueue;
    }

    @EventListener
    public void onBookCreation(BookCreationEvent event) {
        Book book = event.getBook();
        List<BookFileID> sameAuthorsBookFiles = getSameAuthorsBookFiles(book);
        if (!sameAuthorsBookFiles.isEmpty()) {
            DuplicateCheckRequest request = new DuplicateCheckRequest(new BookFileID(book), sameAuthorsBookFiles,
                    event.getUser().getUsername());
            rabbitTemplate.convertAndSend(requestQueue, request);
        }
    }

    private List<BookFileID> getSameAuthorsBookFiles(Book primaryBook) {
        return primaryBook.getAuthorBooks().stream().map(AuthorBook::getAuthor).
                flatMap(a -> a.getBooks().stream().map(AuthorBook::getBook)).
                filter(book -> !book.getId().equals(primaryBook.getId())).
                filter(StreamU.distinctByKey(Book::getId)).
                filter(book -> !book.isDuplicate()).
                sorted(Comparator.comparing(
                        book -> StringUtils.getLevenshteinDistance(book.getTitle(), primaryBook.getTitle()))).
                map(BookFileID::new).
                collect(Collectors.toList());
    }

    @RabbitListener(queues = "${duplicateCheck.responseQueue}")
    public void processResponse(DuplicateCheckResponse response) {
        transactionService.transactionRequired(() -> {
            Book first = bookService.getBook(response.getFirst());
            Book second = bookService.getBook(response.getSecond());
            markDuplications(first, second, response.getUsername());
        });
    }


    private void markDuplications(Book first, Book second, String user) {
        try {
            Book primary, secondary;
            if (first.getContentSize() > second.getContentSize()) {
                primary = first;
                secondary = second;
            } else {
                primary = second;
                secondary = first;
            }
            secondary.setDuplicate(true);

            mergeAuthors(primary, secondary);
            mergeSequences(primary, secondary);

            String message = "Book:" + first.getTitle() + "\nPrimary: " + primary.getTitle() + "\nDuplicate: " +
                    secondary.getTitle();
            log.info(message);
            if (user != null) {
                Res messageRes = new Res("duplicate.check.result", first.getTitle(),
                        primary.getTitle(), secondary.getTitle());
                messenger.sendMessageToUser(messageRes, user);
            }
        } catch (Exception e) {
            throw new LibException("Duplication mark exception book " +
                    " first.id= " + first.getId() +
                    " first.title = " + first.getTitle() +
                    " first.filename" + first.getFileName() +
                    "\n second.id= " + second.getId() +
                    " second.title = " + second.getTitle() +
                    " second.filename" + second.getFileName() +
                    " exception=" + e.getMessage(), e);
        }
    }

    private void mergeSequences(Book primary, Book secondary) {
        Set<Long> sequences = primary.getSequences().stream().
                map(BookSequence::getSequence).
                map(Sequence::getId).
                collect(Collectors.toSet());

        secondary.getSequences().stream().
                filter(s -> !sequences.contains(s.getSequence().getId())).
                map(bs -> new BookSequence(bs.getSeqOrder(), bs.getSequence(), primary)).
                forEach(bs -> primary.getSequences().add(bs));
    }

    private void mergeAuthors(Book primary, Book secondary) {
        Set<Long> authors = primary.getAuthorBooks().stream().map(AuthorBook::getAuthor).map(Author::getId).
                collect(Collectors.toSet());

        secondary.getAuthorBooks().stream().
                map(AuthorBook::getAuthor).
                filter(a -> !authors.contains(a.getId())).
                map(a -> new AuthorBook(a, primary)).
                forEach(ab -> primary.getAuthorBooks().add(ab));
    }
}
