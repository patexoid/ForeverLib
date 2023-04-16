package com.patex.service;


import com.patex.model.BookCoverMessage;
import com.patex.zombie.model.BookImage;
import com.patex.zombie.model.FileResource;
import com.patex.zombie.service.BookService;
import com.patex.zombie.service.StorageService;
import com.patex.zombie.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoverService {

    private final BookService bookService;

    private final StorageService storageService;

    public static final String COVER_QUEUE = "coverQueue";

    @RabbitListener(queues = COVER_QUEUE)
    @Transactional
    public void saveCover(BookCoverMessage coverMessage) {
        bookService.getBook(coverMessage.book()).ifPresent(
                book -> {
                    String filePath = book.getFileResource().getFilePath();
                    BookImage bookImage = coverMessage.bookImage();
                    String coverPath = saveCover(filePath, bookImage);
                    FileResource cover = new FileResource();
                    cover.setSize(bookImage.getImage().length);
                    cover.setType(bookImage.getType());
                    cover.setFilePath(coverPath);
                    book.setCover(cover);
                    bookService.updateBook(book);
                }
        );


    }

    public String saveCover(String filePath, BookImage bookImage) {
        String coverPath = filePath;
        String[] type = bookImage.getType().split("/");
        if (type.length > 1) {
            coverPath = filePath + "." + type[1];
        }
        return storageService.save(bookImage.getImage(), "image", coverPath);
    }


}
