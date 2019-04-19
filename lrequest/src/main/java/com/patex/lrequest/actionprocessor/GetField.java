package com.patex.lrequest.actionprocessor;

import static com.patex.lrequest.ResultType.Type.FlatMap;
import static com.patex.lrequest.ResultType.Type.Map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.patex.lrequest.ActionHandler;
import com.patex.lrequest.ActionResult;
import com.patex.lrequest.RequestResult;
import com.patex.lrequest.ResultType;
import com.patex.lrequest.WrongActionSyntaxException;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class GetField implements ActionHandler {

  private final ResultStorage lambdaStorage = new ResultStorage();

  @Override
  public ActionResult execute(Supplier[] params, ResultType input, RequestResult... paramTypes) {
    Class inputType = input.getReturnType();
    if (Void.class.isAssignableFrom(inputType) ||
        paramTypes.length != 1 ||
        !String.class.isAssignableFrom(paramTypes[0].getResultClass())) {
      throw new WrongActionSyntaxException("Object.GetField(\"FieldName\") or List.GetField(\"FieldName\")");
    }

    String fieldName = (String) params[0].get();

    return lambdaStorage.getLambda(inputType,fieldName);
  }


  private class ResultStorage {

    private final Lookup lookup = MethodHandles.lookup();
    LoadingCache<LambdaKey, ActionResult> lambdaCache = CacheBuilder.newBuilder().weakKeys().build(
        new CacheLoader<>() {
          @Override
          public ActionResult load(LambdaKey key) {
            return getLambda(key);
          }
        });

    @SneakyThrows
    private ResultType getResultType(Method method) {
      Class<?> returnType = method.getReturnType();
      if (Collection.class.isAssignableFrom(returnType)) {
        Type type = method.getGenericReturnType();
        if (type instanceof ParameterizedType) {
          Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
          return new ResultType(FlatMap, Class.forName(elementType.getTypeName()));
        }
      }
      return new ResultType(Map, returnType);
    }

    @SneakyThrows
    public ActionResult getLambda(Class type, String field) {
      return lambdaCache.get(new LambdaKey(type, field));
    }


    @SneakyThrows
    private ActionResult getLambda(LambdaKey key) {
      Method reflected = getMethod(key);

      MethodHandle methodHandle = lookup.unreflect(reflected);

      MethodType func = methodHandle.type();
      CallSite site = LambdaMetafactory.metafactory(lookup,
          "apply",
          MethodType.methodType(Function.class),
          func.generic(), methodHandle, func);

      MethodHandle factory = site.getTarget();
      Function function = (Function) factory.invoke();
      return new ActionResult(function, getResultType(reflected));
    }

    private Method getMethod(LambdaKey key) throws NoSuchMethodException {
      String field = key.getField();
      Class type = key.getType();
      String getterName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
      return type.getMethod(getterName);
    }
  }

  @Data
  private class LambdaKey {

    private final Class type;
    private final String field;
  }
}
