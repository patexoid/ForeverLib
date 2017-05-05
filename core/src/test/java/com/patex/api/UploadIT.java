package com.patex.api;


import com.patex.BookUploadInfo;
import com.patex.entities.Author;
import com.patex.entities.Book;
import com.patex.entities.ZUser;
import fb2.Fb2Creator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;


@SuppressWarnings("Duplicates")
@RunWith(SpringJUnit4ClassRunner.class)
public class UploadIT {

    private HttpTestClient httpClient;

    @Before
    public void setUp() throws IOException {
        httpClient = new HttpTestClient("http://localhost:8080");
        httpClient.setCreds("testUser","simplePassword");
        try {
            httpClient.get("user/current", ZUser.class);
        } catch (HttpClientErrorException e) {
            httpClient.setCreds(null,null);
            httpClient.post("user/create", "{\"username\":\"testUser\", \"password\":\"simplePassword\"}",
                    MediaType.APPLICATION_JSON, ZUser.class);
        }
        httpClient.setCreds("testUser","simplePassword");
    }

    @Test
    public void uploadFile() throws IOException {
        String fileName = randomAlphanumeric(10) + ".fb2";
        Map<String, InputStream> files = new HashMap<>();
        files.put(fileName, new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(randomAlphanumeric(10), randomAlphanumeric(10), randomAlphanumeric(10)).
                getFbook());


        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(1));
        BookUploadInfo bookInfo = response.getBody().get(0);
        assertThat(bookInfo.getFileName(), equalTo(fileName));
        assertThat(bookInfo.getStatus(), equalTo(BookUploadInfo.Status.Success));

    }


    @Test
    public void uploadFiles() throws IOException {

        Map<String, InputStream> files = new HashMap<>();

        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(randomAlphanumeric(10), randomAlphanumeric(10), randomAlphanumeric(10)).getFbook());
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(randomAlphanumeric(10), randomAlphanumeric(10), randomAlphanumeric(10)).getFbook());
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(randomAlphanumeric(10), randomAlphanumeric(10), randomAlphanumeric(10)).getFbook());
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(randomAlphanumeric(10), randomAlphanumeric(10), randomAlphanumeric(10)).getFbook());

        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(4));
        Assert.assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
    }

    @Test
    public void uploadOneBook() throws IOException {

        Map<String, InputStream> files = new HashMap<>();

        String title = randomAlphanumeric(10);
        String annotationLine = randomAlphanumeric(50);
        String firstName = randomAlphanumeric(10);
        String middleName = randomAlphanumeric(10);
        String lastName = randomAlphanumeric(10);
        String sequence = randomAlphanumeric(10);
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(title).
                addAuthor(firstName, middleName, lastName).addSequence(sequence, 1).
                addAnnotationLine(annotationLine).getFbook());

        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(1));
        Assert.assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        Book book = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        assertThat(book.getTitle(), equalTo(title));
        assertThat(book.getDescr().trim(), equalTo(annotationLine));
        assertThat(book.getAuthorBooks(), hasSize(1));
        Author author = book.getAuthorBooks().get(0).getAuthor();
        assertThat(author.getName(), equalTo(lastName + " " + firstName + " " + middleName));

    }


    @Test
    public void uploadOneAuthor() throws IOException {

        Map<String, InputStream> files = new HashMap<>();

        String firstName = randomAlphanumeric(10);
        String middleName = randomAlphanumeric(10);
        String lastName = randomAlphanumeric(10);
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).addAuthor(firstName, middleName, lastName).getFbook());
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).addAuthor(firstName, middleName, lastName).getFbook());

        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(2));
        Assert.assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        Book book1 = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        Book book2 = httpClient.get("book/" + response.getBody().get(1).getId(), Book.class);
        long authorId = book1.getAuthorBooks().get(0).getAuthor().getId();
        assertThat(book1.getAuthorBooks(), hasSize(1));
        assertThat(book2.getAuthorBooks(), hasSize(1));
        Assert.assertTrue(authorId > 0);
        Assert.assertTrue(book2.getAuthorBooks().get(0).getAuthor().getId() == authorId);
    }

    @Test
    public void uploadOneSequence() throws IOException {

        Map<String, InputStream> files = new HashMap<>();

        String firstName = randomAlphanumeric(10);
        String middleName = randomAlphanumeric(10);
        String lastName = randomAlphanumeric(10);
        String sequence = randomAlphanumeric(10);
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(firstName, middleName, lastName).addSequence(sequence, 1).getFbook());
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(firstName, middleName, lastName).addSequence(sequence, 1).getFbook());

        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(2));
        Assert.assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        Book book1 = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        Book book2 = httpClient.get("book/" + response.getBody().get(1).getId(), Book.class);
        long sequenceId = book1.getSequences().get(0).getSequence().getId();
        Assert.assertTrue(sequenceId > 0);
        Assert.assertTrue(book2.getSequences().get(0).getSequence().getId() == sequenceId);
    }

    @Test
    @Repeat(value = 10)
    public void uploadOneSequenceDifferentAuthors() throws IOException {
        Map<String, InputStream> files = new HashMap<>();
        String firstFirstName = randomAlphanumeric(10);
        String firstMiddleName = randomAlphanumeric(10);
        String firstLastName = randomAlphanumeric(10);

        String secondFirstName = randomAlphanumeric(10);
        String secondMiddleName = randomAlphanumeric(10);
        String secondLastName = randomAlphanumeric(10);

        String sequence = randomAlphanumeric(10);
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(firstFirstName, firstMiddleName, firstLastName).
                addSequence(sequence, 1).getFbook());
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(secondFirstName, secondMiddleName, secondLastName).
                addSequence(sequence, 2).getFbook());
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(firstFirstName, firstMiddleName, firstLastName).
                addAuthor(secondFirstName, secondMiddleName, secondLastName).
                addSequence(sequence, 2).getFbook());

        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(3));
        Assert.assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        Book book1 = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        Book book2 = httpClient.get("book/" + response.getBody().get(1).getId(), Book.class);
        Book book3 = httpClient.get("book/" + response.getBody().get(2).getId(), Book.class);
        long sequenceId = book1.getSequences().get(0).getSequence().getId();
        Assert.assertTrue(sequenceId > 0);
        Assert.assertTrue(book2.getSequences().get(0).getSequence().getId() == sequenceId);
        Assert.assertTrue(book3.getSequences().get(0).getSequence().getId() == sequenceId);
    }


    @Test
    @Repeat(value = 10)
    public void uploadOneSequenceDifferentAuthorsDifferentRequests() throws IOException {
        Map<String, InputStream> files = new HashMap<>();
        String firstFirstName = randomAlphanumeric(10);
        String firstMiddleName = randomAlphanumeric(10);
        String firstLastName = randomAlphanumeric(10);

        String secondFirstName = randomAlphanumeric(10);
        String secondMiddleName = randomAlphanumeric(10);
        String secondLastName = randomAlphanumeric(10);

        String sequence = randomAlphanumeric(10);
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(firstFirstName, firstMiddleName, firstLastName).
                addSequence(sequence, 1).getFbook());
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(secondFirstName, secondMiddleName, secondLastName).
                addSequence(sequence, 2).getFbook());

        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        Assert.assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        Book book1 = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        Book book2 = httpClient.get("book/" + response.getBody().get(1).getId(), Book.class);
        long sequenceId = book1.getSequences().get(0).getSequence().getId();
        Assert.assertTrue(book2.getSequences().get(0).getSequence().getId() != sequenceId);


        Map<String, InputStream> file = new HashMap<>();
        file.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(firstFirstName, firstMiddleName, firstLastName).
                addAuthor(secondFirstName, secondMiddleName, secondLastName).
                addSequence(sequence, 2).getFbook());

        ResponseEntity<List<BookUploadInfo>> response2 = httpClient.uploadFiles("book/upload",
                "file", file, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });
        assertThat(response2.getStatusCode(), equalTo(HttpStatus.OK));
        Assert.assertTrue(response2.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        book1 = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        book2 = httpClient.get("book/" + response.getBody().get(1).getId(), Book.class);
        Book book3 = httpClient.get("book/" + response2.getBody().get(0).getId(), Book.class);
        sequenceId = book1.getSequences().get(0).getSequence().getId();
        Assert.assertTrue(sequenceId > 0);
        Assert.assertTrue(book2.getSequences().get(0).getSequence().getId() == sequenceId);
        Assert.assertTrue(book3.getSequences().get(0).getSequence().getId() == sequenceId);

    }


    @Test
    public void updateBookDescription() throws IOException {
        Map<String, InputStream> files = new HashMap<>();
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(randomAlphanumeric(10), randomAlphanumeric(10), randomAlphanumeric(10)).
                addAnnotationLine(randomAlphanumeric(50)).
                getFbook());

        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });

        Book book = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        String newDescr = book.getDescr() + "\n  new line";
        book.setDescr(newDescr);
        ResponseEntity<Book> responceBook = httpClient.post("book", book, MediaType.APPLICATION_JSON, Book.class);
        Book updatedBook = responceBook.getBody();
        assertThat(updatedBook.getDescr(), equalTo(newDescr));
    }

    @Test
    public void updateBookTitle() throws IOException {
        Map<String, InputStream> files = new HashMap<>();
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(randomAlphanumeric(10), randomAlphanumeric(10), randomAlphanumeric(10)).
                getFbook());


        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });

        Book book = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        String newTitle = book.getTitle() + ". Few words";
        book.setTitle(newTitle);
        ResponseEntity<Book> responceBook = httpClient.post("book", book, MediaType.APPLICATION_JSON, Book.class);
        Book updatedBook = responceBook.getBody();
        assertThat(updatedBook.getTitle(), equalTo(newTitle));
    }
}

