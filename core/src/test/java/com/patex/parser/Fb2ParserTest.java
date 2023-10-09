package com.patex.parser;

import com.patex.entities.BookEntity;
import com.patex.zombie.model.BookImage;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


public class Fb2ParserTest {


    private static final String TITLE = "title";
    private static final String CONTENT_1 = "content 1";
    private static final String CONTENT_2 = "content 2";
    private static final String CONTENT_3 = "content 3";
    private static final String IMAGE_TYPE = "image/dummy";

    @Test
    public void verifyOneAuthorFullName() {
        InputStream fbook = new Fb2Creator(TITLE).
                addAuthor("firstName", "middleName", "lastName")
                .getFbook();
        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(1, book.getAuthorBooks().size());
        assertEquals("lastName firstName middleName", book.getAuthorBooks().get(0).getAuthor().getName());
    }

    @Test
    public void verifyOneAuthorFullNameWithSpaces() {
        InputStream fbook = new Fb2Creator(TITLE).
                addAuthor(" firstName ", " middleName ", " lastName ")
                .getFbook();
        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(1, book.getAuthorBooks().size());
        assertEquals("lastName firstName middleName", book.getAuthorBooks().get(0).getAuthor().getName());
    }
    @Test
    public void verifyOneAuthorFirstName() {
        InputStream fbook = new Fb2Creator(TITLE).
                addAuthor("firstName", null, null)
                .getFbook();
        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(1, book.getAuthorBooks().size());
        assertEquals("firstName", book.getAuthorBooks().get(0).getAuthor().getName());
    }

    @Test
    public void verifyOneAuthorMiddleName() {
        InputStream fbook = new Fb2Creator(TITLE).
                addAuthor(null, "middleName", null)
                .getFbook();
        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(1, book.getAuthorBooks().size());
        assertEquals("middleName", book.getAuthorBooks().get(0).getAuthor().getName());
    }

    @Test
    public void verifyBookTitle() {
        InputStream fbook = new Fb2Creator(TITLE).
                getFbook();
        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(TITLE, book.getTitle());
    }

    @Test
    public void verifyOneAuthorLastName() {
        InputStream fbook = new Fb2Creator(TITLE).
                addAuthor(null, null, "lastName")
                .getFbook();
        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(1, book.getAuthorBooks().size());
        assertEquals("lastName", book.getAuthorBooks().get(0).getAuthor().getName());
    }

    @Test
    public void verifyOneAuthorFirstNameLastName() {
        InputStream fbook = new Fb2Creator(TITLE).
                addAuthor("firstName", null, "lastName")
                .getFbook();
        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(1, book.getAuthorBooks().size());
        assertEquals("lastName firstName", book.getAuthorBooks().get(0).getAuthor().getName());
    }

    @Test
    public void verifyOneAuthorFirstNameMiddleName() {
        InputStream fbook = new Fb2Creator(TITLE).
                addAuthor("firstName", "middleName", null)
                .getFbook();
        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(1, book.getAuthorBooks().size());
        assertEquals("firstName middleName", book.getAuthorBooks().get(0).getAuthor().getName());
    }

    @Test
    public void verifyOneAuthorMiddleNameLastName() {
        InputStream fbook = new Fb2Creator(TITLE).
                addAuthor(null, "middleName", "lastName")
                .getFbook();
        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(1, book.getAuthorBooks().size());
        assertEquals("lastName middleName", book.getAuthorBooks().get(0).getAuthor().getName());
    }

    @Test
    public void verifyTwoAuthors() {
        InputStream fbook = new Fb2Creator(TITLE).
                addAuthor("firstName1", "middleName1", "lastName1").
                addAuthor("firstName2", "middleName2", "lastName2").
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(2, book.getAuthorBooks().size());
        assertEquals("lastName1 firstName1 middleName1", book.getAuthorBooks().get(0).getAuthor().getName());
        assertEquals("lastName2 firstName2 middleName2", book.getAuthorBooks().get(1).getAuthor().getName());
    }

    @Test
    public void verifyCoverpageImageHref() {
        String covePageHref = "covePageHref";
        InputStream fbook = new Fb2Creator(TITLE).
                setCoverpage(covePageHref).
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        String coverpageImageHref = bookInfo.getCoverage();
        assertEquals(covePageHref, coverpageImageHref);
    }

    @Test
    public void verifyAnnotationPLine() {
        String annotation = "annotationP";
        InputStream fbook = new Fb2Creator(TITLE).
                addAnnotationPLine(annotation).
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        String descr = bookInfo.getBook().getDescr();
        assertEquals(annotation, descr);
    }

    @Test
    public void verifyAnnotationTwoPLine() {
        String annotation1 = "annotationP1";
        String annotation2 = "annotationP2";
        InputStream fbook = new Fb2Creator(TITLE).
                addAnnotationPLine(annotation1).
                addAnnotationPLine(annotation2).
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        String descr = bookInfo.getBook().getDescr();
        assertEquals(annotation1 + "\n" + annotation2, descr);
    }


    @Test
    public void verifyGenre() {
        String genre = "foreign_adventure";
        InputStream fbook = new Fb2Creator(TITLE).
                addGenre(genre).
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(1, book.getGenres().size());

        assertEquals(genre, book.getGenres().get(0).getGenre().getName());
    }

    @Test
    public void verifySequence() {
        String sequence = "sequence";
        int number = 1;
        InputStream fbook = new Fb2Creator(TITLE).
                addSequence(sequence, number).
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();

        assertEquals(1, book.getSequences().size());
        assertEquals(sequence, book.getSequences().get(0).getSequence().getName());
        assertEquals(number, book.getSequences().get(0).getSeqOrder());
    }

    @Test
    public void verifySequenceWithoutNumber() {
        String sequence = "sequence";
        InputStream fbook = new Fb2Creator(TITLE).
                addSequence(sequence, null).
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();

        assertEquals(1, book.getSequences().size());
        assertEquals(sequence, book.getSequences().get(0).getSequence().getName());
        assertEquals(0, book.getSequences().get(0).getSeqOrder());
    }

    @Test
    public void verifyTwoSequence() {
        String sequence1 = "sequence";
        int number1 = 1;
        String sequence2 = "sequence";
        int number2 = 42;

        InputStream fbook = new Fb2Creator(TITLE).
                addSequence(sequence1, number1).
                addSequence(sequence2, number2).
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();

        assertEquals(2, book.getSequences().size());
        assertEquals(sequence1, book.getSequences().get(0).getSequence().getName());
        assertEquals(number1, book.getSequences().get(0).getSeqOrder());
        assertEquals(sequence2, book.getSequences().get(1).getSequence().getName());
        assertEquals(number2, book.getSequences().get(1).getSeqOrder());
    }


    @Test
    public void verifyTwoGenres() {
        String genre1 = "foreign_adventure";
        String genre2 = "foreign_desc";
        InputStream fbook = new Fb2Creator(TITLE).
                addGenre(genre1).
                addGenre(genre2).
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(2, book.getGenres().size());

        assertEquals(genre1, book.getGenres().get(0).getGenre().getName());
        assertEquals(genre2, book.getGenres().get(1).getGenre().getName());
    }

    @Test
    public void verifyContentSize() {
        InputStream fbook = new Fb2Creator(TITLE).
                addContent("content 1").
                addContent("content 2").
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals(18, book.getContentSize().intValue());
    }

    @Test
    public void verifyLang() {
        InputStream fbook = new Fb2Creator(TITLE).
                setLang("ua").
                setSrcLang("en").
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookEntity book = bookInfo.getBook();
        assertEquals("ua", book.getLang());
        assertEquals("en", book.getSrcLang());
    }

    @Test
    public void verifyContentIterator() {
        InputStream fbook = new Fb2Creator(TITLE).
                addContent(CONTENT_1).
                addContent(CONTENT_2).
                nextSection().
                addContent(CONTENT_3).
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        Iterator<String> iterator = parser.getContentIterator("fileName", fbook);
        assertTrue(iterator.hasNext());
        assertEquals(CONTENT_1, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(CONTENT_2, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(CONTENT_3, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void verifyCoverpageImage() {
        String covePageHref = "covePageHref";
        byte[] content = {1, 2, 3, 4, 5, 6};
        InputStream fbook = new Fb2Creator(TITLE).
                setCoverpage(covePageHref).
                addBinary(covePageHref, content, IMAGE_TYPE).
                getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        BookInfo bookInfo = parser.parseFile("fileName", fbook, true);
        BookImage image = bookInfo.getBookImage();

        assertArrayEquals(content, image.getImage());
        assertEquals(IMAGE_TYPE, image.getType());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testFb2Content() {
        RandomStringGenerator rsg = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
        Random random = new Random();
        String content1 = Stream.generate(() -> rsg.generate(1 + random.nextInt(8))).
                limit(5).reduce((s, s2) -> s + " " + s2).get();
        String content2 = Stream.generate(() -> rsg.generate(1 + random.nextInt(8))).
                limit(5).reduce((s, s2) -> s + " " + s2).get();

        InputStream book = new Fb2Creator("book").
                addContent(content1).
                addContent(content2).getFbook();

        Fb2FileParser parser = new Fb2FileParser();
        Iterator<String> contentIterator =  parser.getContentIterator("book.fb2", book);
        assertEquals(content1, contentIterator.next().trim());
        assertEquals(content2, contentIterator.next().trim());
        assertFalse(contentIterator.hasNext());
    }
}
