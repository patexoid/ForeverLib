package com.patex.forever.service;

import com.patex.forever.model.CheckDuplicateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.patex.forever.RabbitConfig.DUPLICATE_QUEUE;

@RequiredArgsConstructor
@Slf4j
@Service
public class RabbitDuplicateHandler {

    private final DuplicateHandler duplicateHandler;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = DUPLICATE_QUEUE)
    @Transactional
    public void check(CheckDuplicateMessage bcq) {
        duplicateHandler.checkForDuplicate(bcq);
    }

    public void waitForFinish() {
        while (true) {
            int count = rabbitTemplate.execute(channel -> channel.queueDeclarePassive(DUPLICATE_QUEUE)).getMessageCount();
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
