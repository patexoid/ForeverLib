package com.patex.opds;

import com.patex.entities.Author;
import com.patex.entities.Book;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Person;
import com.rometools.rome.feed.synd.SyndPerson;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Service(AuthorOpdsView.OPDS_AUTHOR_VIEW)
public class AuthorOpdsView extends AbstractAtomFeedView {


  public static final String AUTHOR = "author";
  public static final String OPDS_AUTHOR_VIEW = "opdsAuthor";

  @Override
  protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    Author author = (Author) model.get(AUTHOR);
    ArrayList<Entry> entries = new ArrayList<>(author.getBooks().size());
    for (Book book : author.getBooks()) {
      Entry entry=new Entry();
      entry.setTitle(book.getTitle());
      entries.add(entry);
    }
    return entries;
  }

  @Override
  protected void buildFeedMetadata(Map<String, Object> model, Feed feed, HttpServletRequest request) {
    super.buildFeedMetadata(model, feed, request);
    Author author = (Author) model.get(AUTHOR);
    feed.setTitle(author.getName());
    ArrayList<SyndPerson> authors = new ArrayList<>();
    Person person = new Person();
    person.setName(author.getName());
    feed.setAuthors(authors);
  }
}
