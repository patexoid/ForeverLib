package com.patex.forever.opds.model;

import com.patex.forever.model.Book;
import com.patex.forever.model.Res;
import com.patex.forever.service.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 05.07.2017.
 */
public class DownloadAllResult extends Res {

    private final List<String> authors;
    private final List<Res> emptyBooks;
    private final List<Res> failed;
    private final List<Book> success;

    public DownloadAllResult(List<String> authors, List<Res> emptyBooks, List<Res> failed, List<Book> success) {
        super("opds.extLib.download.all.result");
        this.authors = authors;
        this.emptyBooks = emptyBooks;
        this.failed = failed;
        this.success = success;
    }

    public static DownloadAllResult empty(List<String> authors, Res empty) {
        return new DownloadAllResult(authors,
                Collections.singletonList(empty), Collections.emptyList(), Collections.emptyList());
    }

    public static DownloadAllResult failed(List<String> authors, Res failed) {
        return new DownloadAllResult(authors,
                Collections.emptyList(), Collections.singletonList(failed), Collections.emptyList());
    }

    public static DownloadAllResult success(List<String> authors, Book success) {
        return new DownloadAllResult(authors,
                Collections.emptyList(), Collections.emptyList(), Collections.singletonList(success));
    }

    public DownloadAllResult concat(DownloadAllResult other) {
        List<String> authors = new ArrayList<>(this.authors);
        authors.addAll(other.authors);
        ArrayList<Res> emptyBooks = new ArrayList<>(this.emptyBooks);
        emptyBooks.addAll(other.emptyBooks);
        ArrayList<Res> failed = new ArrayList<>(this.failed);
        failed.addAll(other.failed);
        ArrayList<Book> success = new ArrayList<>(this.success);
        success.addAll(other.success);
        return new DownloadAllResult(authors, emptyBooks, failed, success);
    }


    public boolean hasResult() {
        return success.size() > 0 || failed.size() > 0;
    }

    public String getMessage(Resources resources, Locale loc) {
        BinaryOperator<String> concat = (s, s2) -> s + ", " + s2;
        String authors = this.authors.stream().collect(Collectors.groupingBy(o -> o)).
                entrySet().stream().sorted(Comparator.comparingInt(o -> -o.getValue().size())).
                limit(5).map(Map.Entry::getKey).reduce(concat).orElse("");
        String success = this.success.stream().map(Book::getTitle).sorted().reduce(concat).orElse("");
        String empty = emptyBooks.stream().
                map(res -> res.getMessage(resources, loc)).sorted().
                reduce(concat).orElse("");
        String failed = this.failed.stream().
                map(res -> res.getMessage(resources, loc)).sorted().
                reduce(concat).orElse("");
        return resources.get(loc, getKey(), authors, success, empty, failed);
    }

    public List<Book> getSuccess() {
        return success;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public List<Res> getEmptyBooks() {
        return emptyBooks;
    }

    public List<Res> getFailed() {
        return failed;
    }
}
