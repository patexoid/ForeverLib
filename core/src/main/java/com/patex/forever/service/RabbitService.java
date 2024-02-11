package com.patex.forever.service;


import com.patex.forever.model.CheckDuplicateMessage;
import com.patex.forever.model.BookCoverMessage;
import com.patex.forever.model.BookImage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static com.patex.forever.RabbitConfig.BOOK_COVER_EXCHANGE;
import static com.patex.forever.RabbitConfig.BOOK_DUPLICATE_EXCHANGE;

@Service
@RequiredArgsConstructor
public class RabbitService {


    private final RabbitTemplate rabbitTemplate;

    @EventListener
    public void onBookCreation(BookCreationEvent event) {
        checkDuplicate(new CheckDuplicateMessage(event.getBook().getId(), event.getUser().getUsername()));
        updateBookCover(event.getBookInfo().getBookImage(), event.getBook().getId());
    }

    public void checkDuplicate(CheckDuplicateMessage message) {
        rabbitTemplate.convertAndSend(BOOK_DUPLICATE_EXCHANGE, "", message);
    }

    public void updateBookCover(BookImage bookImage, Long bookId) {
        if (bookImage != null) {
            rabbitTemplate.convertAndSend(BOOK_COVER_EXCHANGE, "", new BookCoverMessage(bookId, bookImage));
        }
    }
}
