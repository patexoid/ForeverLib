package com.patex.lrequest.actionProcessor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.patex.entities.Author;
import com.patex.lrequest.ActionResult;
import com.patex.lrequest.DataType;
import com.patex.lrequest.Value;
import com.patex.lrequest.WrongActionSyntaxException;
import com.patex.lrequest.actionprocessor.FindAuthor;
import com.patex.service.AuthorService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class FindAuthorTest {

  public static final String AUTHOR_NAME = "authorName";
  public static final Author AUTHOR = mock(Author.class);
  private static final List<Author> AUTHORS = Collections.singletonList(AUTHOR);
  @Mock
  private AuthorService authorService;
  @InjectMocks
  private FindAuthor findAuthor;

  @Before
  public void setUp() {
    when(authorService.findByName(AUTHOR_NAME)).thenReturn(AUTHORS);
  }

  @Test(expected = WrongActionSyntaxException.class)
  public void shouldFailWhenInitialWithWrongParams() {
    findAuthor.createFuncton(DataType.INITIAL, new Value<>(Integer.class, null));
  }

  @Test(expected = WrongActionSyntaxException.class)
  public void shouldfailWhenNoParamsNoInput() {
    findAuthor.createFuncton(DataType.INITIAL);
  }

  @Test(expected = WrongActionSyntaxException.class)
  public void shouldFailWhenWrongStreamInput() {
    findAuthor.createFuncton(DataType.streamResult(Integer.class));
  }

  @Test(expected = WrongActionSyntaxException.class)
  public void shouldFailWhenWrongInputType() {
    findAuthor.createFuncton(DataType.objResult(Integer.class));
  }

  @Test
  public void shouldReturnAuthorWhenParam() {
    ActionResult<?, Stream<Author>> functon = findAuthor
        .createFuncton(DataType.INITIAL, new Value<>(String.class, () -> AUTHOR_NAME));
    assertEquals(DataType.streamResult(Author.class), functon.getDataType());
    List<Author> result = functon.getResultFunc().apply(null).collect(Collectors.toList());
    assertEquals(AUTHORS, result);
  }

  @Test
  public void shouldReturnAuthorWhenInputStreamString() {
    String authorNanem2 = "authorNanem2";
    Author author2 = mock(Author.class);
    when(authorService.findByName(authorNanem2)).thenReturn(Collections.singletonList(author2));
    ActionResult<String, Stream<Author>> functon = findAuthor
        .createFuncton(DataType.streamResult(String.class));
    assertEquals(DataType.streamResult(Author.class), functon.getDataType());
    List<Author> result = functon.getResultFunc().apply(Stream.of(AUTHOR_NAME, authorNanem2)).collect(Collectors.toList());
    assertEquals(Arrays.asList(AUTHOR, author2), result);
  }
}
