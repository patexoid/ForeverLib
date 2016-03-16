package com.patex.parser;

import java.util.Collection;
import java.util.Date;

/**
 * Created by alex on 15.03.2015.
 */
public class Fb2BookDescription {

    private final Collection<String> genres;
    private final Fb2Author author;
    private final String title;
    private final String annotation;
    private final Date date;
    private final  String lang;

    public Fb2BookDescription(Collection<String> genres, Fb2Author author, String title, String annotation, Date date, String lang) {
        this.genres = genres;
        this.author = author;
        this.title = title;
        this.annotation = annotation;
        this.date = date;
        this.lang = lang;
    }

    public Collection<String> getGenres() {
        return genres;
    }

    public Fb2Author getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getAnnotation() {
        return annotation;
    }

    public Date getDate() {
        return date;
    }

    public String getLang() {
        return lang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Fb2BookDescription that = (Fb2BookDescription) o;

        if (annotation != null ? !annotation.equals(that.annotation) : that.annotation != null) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (genres != null ? !genres.equals(that.genres) : that.genres != null) return false;
        if (lang != null ? !lang.equals(that.lang) : that.lang != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = genres != null ? genres.hashCode() : 0;
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (annotation != null ? annotation.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (lang != null ? lang.hashCode() : 0);
        return result;
    }
}
