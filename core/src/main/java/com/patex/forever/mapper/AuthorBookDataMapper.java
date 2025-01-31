package com.patex.forever.mapper;

import com.patex.forever.model.Author;
import com.patex.forever.model.AuthorBookData;
import com.patex.forever.model.Book;
import com.patex.forever.model.BookAuthor;
import com.patex.forever.model.BookSequence;
import com.patex.forever.model.Sequence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorBookDataMapper {

    @Mapping(target = "id", source = "bookId")
    @Mapping(target = "title", source = "bookTitle")
    @Mapping(target = "descr", source = "bookDescr")
    @Mapping(target = "created", source = "bookCreated")
    @Mapping(target = "cover.id", source = "coverId")
    @Mapping(target = "cover.type", source = "coverType")
    @Mapping(target = "duplicate", source = "bookDuplicate")
    Book getBookDto(AuthorBookData authorBookData);

    @Mapping(target = "id", source = "authorId")
    @Mapping(target = "name", source = "authorName")
    BookAuthor getAuthorDto(AuthorBookData authorBookData);

    @Mapping(target = "id", source = "sequenceId")
    @Mapping(target = "seqOrder", source = "seqOrder")
    @Mapping(target = "sequenceName", source = "sequenceName")
    BookSequence getBookSequenceDto(AuthorBookData authorBookDatum);

    @Mapping(target = "id", source = "sequenceId")
    @Mapping(target = "name", source = "sequenceName")
    Sequence getSequenceDto(AuthorBookData authorBookDatum);
}
