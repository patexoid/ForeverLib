package com.patex.extlib;

import com.patex.entities.Book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 05.07.2017.
 */
class DownloadAllResult {

    private final List<String> authors;
    private final List<String> emptyBooks;
    private final List<String> failed;
    private final List<Book> success;

    public DownloadAllResult(List<String> authors, List<String> emptyBooks, List<String> failed, List<Book> success) {
        this.authors = authors;
        this.emptyBooks = emptyBooks;
        this.failed = failed;
        this.success = success;
    }

    public static DownloadAllResult empty(List<String> authors, String empty) {
        return new DownloadAllResult(authors,
                Collections.singletonList(empty), Collections.emptyList(), Collections.emptyList());
    }

    public static DownloadAllResult failed(List<String> authors, String failed) {
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
        ArrayList<String> emptyBooks = new ArrayList<>(this.emptyBooks);
        emptyBooks.addAll(other.emptyBooks);
        ArrayList<String> failed = new ArrayList<>(this.failed);
        failed.addAll(other.failed);
        ArrayList<Book> success = new ArrayList<>(this.success);
        success.addAll(other.success);
        return new DownloadAllResult(authors, emptyBooks, failed, success);
    }


    public boolean hasResult(){
        return success.size() > 0 || failed.size() > 0;
    }

    public String getResultMessage() {
        BinaryOperator<String> concat = (s, s2) -> s + ", " + s2;
        return "result\n" +
                "Authors:\n" +
                authors.stream().collect(Collectors.groupingBy(o -> o)).
                        entrySet().stream().sorted(Comparator.comparingInt(o -> -o.getValue().size())).
                        limit(5).map(Map.Entry::getKey).reduce(concat).orElse("") + "\n" +
                "\n" +
                "Success:\n" +
                success.stream().map(Book::getTitle).reduce(concat).orElse("") + "\n" +
                "\n" +
                "Empty:\n" +
                emptyBooks.stream().reduce(concat).orElse("") + "\n" +
                "\n" +
                "Failed:\n" +
                failed.stream().reduce(concat).orElse("") + "\n";
    }
}
