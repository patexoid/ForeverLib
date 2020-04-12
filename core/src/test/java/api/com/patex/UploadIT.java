package api.com.patex;


import com.patex.Application;
import com.patex.entities.ZUser;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.BookAuthor;
import com.patex.zombie.model.BookUploadInfo;
import com.patex.zombie.model.Tuple;
import fb2Generator.Fb2Creator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SuppressWarnings("ConstantConditions")
@RunWith(SpringRunner.class)
@Import(TestConfiguration.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {Application.class})
@ActiveProfiles(value = {"test", "tempStorage"})
public class UploadIT {

    @LocalServerPort
    private int port;

    private HttpTestClient httpClient;

    @Before
    public void setUp() throws IOException {
        httpClient = new HttpTestClient("http://localhost:" + port);
        httpClient.setCreds("testUser", "simplePassword");
        try {
            httpClient.get("user/current", ZUser.class);
        } catch (HttpClientErrorException e) {
            httpClient.setCreds(null, null);
            httpClient.post("user/create", "{\"username\":\"testUser\", \"password\":\"simplePassword\"}",
                    MediaType.APPLICATION_JSON, ZUser.class);
        }
        httpClient.setCreds("testUser", "simplePassword");
    }

    @Test
    public void uploadFile() {
        String fileName = randomAlphanumeric(10) + ".fb2";
        Map<String, InputStream> files = new HashMap<>();
        files.put(fileName, new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(randomAlphanumeric(10), randomAlphanumeric(10), randomAlphanumeric(10)).
                getFbook());


        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(1));
        BookUploadInfo bookInfo = response.getBody().get(0);
        assertThat(bookInfo.getFileName(), equalTo(fileName));
        assertThat(bookInfo.getStatus(), equalTo(BookUploadInfo.Status.Success));

    }


    @Test
    public void uploadFiles() {

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
                "file", files, new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(4));
        assertTrue(response.getBody().stream().
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
                addAnnotationPLine(annotationLine).getFbook());

        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(1));
        assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        Book book = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        assertThat(book.getTitle(), equalTo(title));
        assertThat(book.getDescr().trim(), equalTo(annotationLine));
        assertThat(book.getAuthors(), hasSize(1));
        BookAuthor author = book.getAuthors().get(0);
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
                "file", files, new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(2));
        assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        Book book1 = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        Book book2 = httpClient.get("book/" + response.getBody().get(1).getId(), Book.class);
        long authorId = book1.getAuthors().get(0).getId();
        assertThat(book1.getAuthors(), hasSize(1));
        assertThat(book2.getAuthors(), hasSize(1));
        assertTrue(authorId > 0);
        assertEquals((long) book2.getAuthors().get(0).getId(), authorId);
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
                "file", files, new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(2));
        assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        Book book1 = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        Book book2 = httpClient.get("book/" + response.getBody().get(1).getId(), Book.class);
        long sequenceId = book1.getSequences().get(0).getId();
        assertTrue(sequenceId > 0);
        assertEquals((long) book2.getSequences().get(0).getId(), sequenceId);
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
                "file", files, new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(3));
        assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        Book book1 = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        Book book2 = httpClient.get("book/" + response.getBody().get(1).getId(), Book.class);
        Book book3 = httpClient.get("book/" + response.getBody().get(2).getId(), Book.class);
        long sequenceId = book1.getSequences().get(0).getId();
        assertTrue(sequenceId > 0);
        assertEquals((long) book2.getSequences().get(0).getId(), sequenceId);
        assertEquals((long) book3.getSequences().get(0).getId(), sequenceId);
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
                "file", files, new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertTrue(response.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        Book book1 = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        Book book2 = httpClient.get("book/" + response.getBody().get(1).getId(), Book.class);
        long sequenceId = book1.getSequences().get(0).getId();
        assertTrue(book2.getSequences().get(0).getId() != sequenceId);


        Map<String, InputStream> file = new HashMap<>();
        file.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(firstFirstName, firstMiddleName, firstLastName).
                addAuthor(secondFirstName, secondMiddleName, secondLastName).
                addSequence(sequence, 2).getFbook());

        ResponseEntity<List<BookUploadInfo>> response2 = httpClient.uploadFiles("book/upload",
                "file", file, new ParameterizedTypeReference<>() {
                });
        assertThat(response2.getStatusCode(), equalTo(HttpStatus.OK));
        assertTrue(response2.getBody().stream().
                allMatch(info -> info.getStatus().equals(BookUploadInfo.Status.Success)));
        book1 = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        book2 = httpClient.get("book/" + response.getBody().get(1).getId(), Book.class);
        Book book3 = httpClient.get("book/" + response2.getBody().get(0).getId(), Book.class);
        sequenceId = book1.getSequences().get(0).getId();
        assertTrue(sequenceId > 0);
        assertEquals((long) book2.getSequences().get(0).getId(), sequenceId);
        assertEquals((long) book3.getSequences().get(0).getId(), sequenceId);

    }


    @Test
    public void updateBookDescription() throws IOException {
        Map<String, InputStream> files = new HashMap<>();
        files.put(randomAlphanumeric(10) + ".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(randomAlphanumeric(10), randomAlphanumeric(10), randomAlphanumeric(10)).
                addAnnotationPLine(randomAlphanumeric(50)).
                getFbook());

        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<>() {
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
                "file", files, new ParameterizedTypeReference<>() {
                });

        Book book = httpClient.get("book/" + response.getBody().get(0).getId(), Book.class);
        String newTitle = book.getTitle() + ". Few words";
        book.setTitle(newTitle);
        ResponseEntity<Book> responceBook = httpClient.post("book", book, MediaType.APPLICATION_JSON, Book.class);
        Book updatedBook = responceBook.getBody();
        assertThat(updatedBook.getTitle(), equalTo(newTitle));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(timeout = 20000)
//    @Ignore //TODO
    public void duplicateCheck() throws IOException {
        Random random = new Random();
        Fb2Creator fb2Creator = new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(randomAlphanumeric(10), randomAlphanumeric(10), randomAlphanumeric(10)).
                addContent(Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                        limit(150).reduce((s, s2) -> s + " " + s2).get());
        InputStream fbook1 = fb2Creator.getFbook();
        InputStream fbook2 = fb2Creator.
                addContent(Stream.generate(() -> RandomStringUtils.randomAlphabetic(1 + random.nextInt(8))).
                        limit(20).reduce((s, s2) -> s + " " + s2).get()).
                getFbook();

        ResponseEntity<List<BookUploadInfo>> response1 = uploadBooks(t(randomAlphanumeric(10) + ".fb2", fbook1));
        ResponseEntity<List<BookUploadInfo>> response2 = uploadBooks(t(randomAlphanumeric(10) + ".fb2", fbook2));

        httpClient.get("book/waitForDuplicateCheck", String.class);
        Book book1 = httpClient.get("book/" + response1.getBody().get(0).getId(), Book.class);
        Book book2 = httpClient.get("book/" + response2.getBody().get(0).getId(), Book.class);
        assertTrue(book1.isDuplicate());
        assertFalse(book2.isDuplicate());
    }

    public Tuple<String, InputStream> t(String _1, InputStream _2) {
        return new Tuple<>(_1, _2);
    }

    @SafeVarargs
    private ResponseEntity<List<BookUploadInfo>> uploadBooks(Tuple<String, InputStream>... obj) {
        Map<String, InputStream> files = new HashMap<>();
        for (Tuple<String, InputStream> t : obj) {
            files.put(t._1, t._2);
        }
        return httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<>() {
                });
    }

}
