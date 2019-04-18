package com.patex.lrequest.actionprocessor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.patex.lrequest.ActionHandler;
import com.patex.lrequest.ActionResult;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Data;
import lombok.SneakyThrows;

public class GetField implements ActionHandler {

  private final LambdaStorage lambdaStorage = new LambdaStorage();

  @Override
  public ActionResult execute(Supplier... params) {
    return null;
  }

  @Override
  public boolean isApplicableParams(Class[] types) {
    return types.length == 1;
  }

  @Override
  public boolean isApplicableData(Class type) {
    return false;
  }

  private Object getField(Object obj, String field) {
    Function lambda = lambdaStorage.getLambda(obj.getClass(), field);
    return lambda.apply(obj);
  }

  private class LambdaStorage {

    Cache<LambdaKey, Function> lambdaCache = CacheBuilder.newBuilder().weakKeys().build(
        new CacheLoader<>() {
          @Override
          public Function load(LambdaKey key) {
            return getLambda(key);
          }
        });

    public Function getLambda(Class type, String field) {
      return lambdaCache.getIfPresent(new LambdaKey(type, field));
    }

    @SneakyThrows
    private Function getLambda(LambdaKey key) {
      String field = key.getField();
      Class type = key.getType();
      MethodHandles.Lookup caller = MethodHandles.lookup();

      String getterName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
      Method reflected = type.getDeclaredMethod(getterName);
      MethodHandle methodHandle = caller.unreflect(reflected);

      MethodType func = methodHandle.type();
      CallSite site = LambdaMetafactory.metafactory(caller,
          "apply",
          MethodType.methodType(Function.class),
          func.generic(), methodHandle, func);

      MethodHandle factory = site.getTarget();
      return (Function) factory.invoke();
    }
  }

  @Data
  private class LambdaKey {

    private final Class type;
    private final String field;
  }
}
