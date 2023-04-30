package com.patex.service;


import com.patex.model.BookCoverMessage;
import com.patex.zombie.model.BookImage;
import com.patex.zombie.model.FileResource;
import com.patex.zombie.service.BookService;
import com.patex.zombie.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        String[] coverPath = getCoverPath(filePath, bookImage.getType());
        return storageService.save(bookImage.getImage(), false, coverPath);
    }

    static String[] getCoverPath(String filePath, String type) {
        String coverPath = filePath;
        String[] typeA = type.split("/");
        if (typeA.length > 1) {
            coverPath = filePath + "." + typeA[1];
        }
        return new String[]{"image", coverPath};
    }


}
