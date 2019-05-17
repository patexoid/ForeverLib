package com.patex.lrequest.actionProcessor;

import static org.junit.Assert.assertEquals;

import com.patex.lrequest.ActionResult;
import com.patex.lrequest.DataType;
import com.patex.lrequest.Value;
import com.patex.lrequest.WrongActionSyntaxException;
import com.patex.lrequest.actionprocessor.GetField;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class GetFieldTest {

  public static final String SINGLE_VALUE = "singleValue";
  public static final List<String> VALUES = Arrays.asList("one", "two");
  public static final List<String> VALUES_2 = Arrays.asList("three", "four");
  public static final String SINGLE_VALUE_2 = "singleValue2";
  @InjectMocks
  private GetField getField;

  @Test(expected = WrongActionSyntaxException.class)
  public void shouldFailWheWrongInput() {
    getField.createFuncton(DataType.INITIAL, new Value<>(String.class, () -> "fieldName"));
  }

  @Test
  public void shouldReturnValueStreamForObjectStream() {
    ActionResult<TestGetter, Stream<String>> actionResult = getField
        .createFuncton(DataType.streamResult(TestGetter.class), new Value<>(String.class, () -> "value"));

    assertEquals(DataType.streamResult(String.class), actionResult.getDataType());
    List<String> result = actionResult.getResultFunc().apply(Stream.of(new TestGetter(SINGLE_VALUE), new TestGetter(
        SINGLE_VALUE_2))).collect(Collectors.toList());
    assertEquals(Arrays.asList(SINGLE_VALUE, SINGLE_VALUE_2), result);
  }

  @Test
  public void shouldReturnCollectionValuesStreamForObjectStream() {
    ActionResult<TestGetter, Stream<String>> actionResult = getField
        .createFuncton(DataType.streamResult(TestGetter.class), new Value<>(String.class, () -> "values"));

    assertEquals(DataType.streamResult(String.class), actionResult.getDataType());
    List<String> result = actionResult.getResultFunc().apply(Stream.of(new TestGetter(VALUES), new TestGetter(
        VALUES_2))).collect(Collectors.toList());

    assertEquals(Stream.of(VALUES,VALUES_2).flatMap(Collection::stream).collect(Collectors.toList()), result);
  }
  @Getter
  public static class TestGetter {

    private final String value;
    private final List<String> values;

    public TestGetter(String value) {
      this.value = value;
      this.values = null;
    }

    public TestGetter(List<String> values) {
      this.values = values;
      this.value = null;
    }
  }
/*

  @Test
  public void testWrongInput() {
    GetField getField = new GetField();
    try {
      getField.createFuncton(new ResultType(Type.None, Void.class), new Value<>(String.class, null));
      fail();
    } catch (WrongActionSyntaxException e) {
      //expected
    }
  }

  @Test
  public void testWrongParam() {
    GetField getField = new GetField();
    try {
      getField.createFuncton(new ResultType(Type.None, Object.class), new Value<>(Integer.class, null));
      fail();
    } catch (WrongActionSyntaxException e) {
      //expected
    }
  }

  @Test
  public void testGetFieldValue() {
    GetField getField = new GetField();
    ActionResult result = getField
        .createFuncton(new ResultType(Type.None, Author.class),
            new Value<>(String.class, () -> AUTHOR_NAME_FIELD_NAME));
    Author author = new Author();
    author.setName(AUHTOR_NAME);
    String actualAuthorName = (String) result.getResult().apply(author);
    assertEquals(new ResultType(Type.Map, String.class), result.getResultType());
    assertEquals(AUHTOR_NAME, actualAuthorName);

  }
*/
}
