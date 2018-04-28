package com.patex.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.persistence.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
@Entity
public class BookSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int seqOrder;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = {CascadeType.PERSIST})
    @JsonIgnoreProperties({Sequence.BOOK_SEQUENCES})
    private Sequence sequence;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = {CascadeType.PERSIST})
    @JsonIgnoreProperties(value = {Book.AUTHORS_BOOKS, Book.SEQUENCES, Book.GENRES, Book.DESCR})
    @JsonDeserialize(using = MyDeserializer.class)
//TODO workaround for Could not read document: No _valueDeserializer assigned
    private Book book;


    public BookSequence(int order, Sequence sequence) {
        this.seqOrder = order;
        this.sequence = sequence;
    }

    public BookSequence(int seqOrder, Sequence sequence, Book book) {
        this(seqOrder, sequence);
        this.book = book;
    }

    public BookSequence() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSeqOrder() {
        return seqOrder;
    }

    public void setSeqOrder(int order) {
        this.seqOrder = order;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public static class MyDeserializer extends JsonDeserializer<Book> {
        private final static Map<String, Method> fieldsM = new HashMap<>();

        static {
            try {
                for (String field : Arrays.asList("id", "title")) {
                    Method getMethod = Book.class.getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1));
                    Class<?> typeCLass = getMethod.getReturnType();
                    Method method = Book.class.getMethod("set" + field.substring(0, 1).toUpperCase() + field.substring(1), typeCLass);
                    fieldsM.put(field, method);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Book deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            try {
                Book value = new Book();
                while (!p.currentToken().isStructEnd()) {
                    p.nextToken();
                    String field = p.getCurrentName();
                    if (field != null && fieldsM.containsKey(field)) {
                        p.nextToken();
                        Method method = fieldsM.get(field);
                        if (method.getParameterTypes()[0] == Long.class) {
                            method.invoke(value, p.getLongValue());
                        } else if (method.getParameterTypes()[0] == String.class) {
                            method.invoke(value, p.getValueAsString());
                        }

                    }
                }
                return value;

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IOException(e);
            }
        }
    }
}
