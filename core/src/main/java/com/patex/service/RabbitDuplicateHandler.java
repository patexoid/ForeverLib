package com.patex.service;

import com.patex.model.BookCheckQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class RabbitDuplicateHandler {

    private final RabbitTemplate rabbitTemplate;
    private final DuplicateHandler duplicateHandler;

    @EventListener
    public void onBookCreation(BookCreationEvent event) {
        BookCheckQueue bookCheckQueue = new BookCheckQueue(event.getBook().getId(), event.getUser().getUsername());
        rabbitTemplate.convertAndSend("newBookExchange","", bookCheckQueue);
    }

    @RabbitListener(queues = "duplicateQueue")
    @Transactional
    public void check(BookCheckQueue bcq) {
        duplicateHandler.checkForDuplicate(bcq);
    }

    public void waitForFinish() {
        while (true) {
            long count = rabbitTemplate.getUnconfirmedCount();
            log.trace("duplicateCheck count:" + count);
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

}
