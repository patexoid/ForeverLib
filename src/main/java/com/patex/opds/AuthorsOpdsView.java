package com.patex.opds;

import com.patex.entities.Author;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Link;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Service(AuthorsOpdsView.OPDS_AUTHORS_VIEW)
public class AuthorsOpdsView extends AbstractAtomFeedView {


  public static final String LIST = "list";
  public static final String OPDS_AUTHORS_VIEW = "opdsAuthorsList";

  @Override
  protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<Author> authors = (List<Author>) model.get(LIST);
    ArrayList<Entry> entries = new ArrayList<>(authors.size());
    for (Author author : authors) {
      Entry entry=new Entry();
      entry.setTitle(author.getName());
      Link link = new Link();
      link.setHref("opds/author/"+author.getId());
      entry.setAlternateLinks(Collections.singletonList(link));
      entries.add(entry);
    }
    return entries;
  }


}
