package com.patex.stateMachine;

import lombok.SneakyThrows;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MachineFactory {

    private final MethodHandles.Lookup lookup = MethodHandles.lookup();


    public StateMachine createStateMachine(List<?> stateProcs, Object initialState) {
        Map<Class, Class> stateToProcessorMap = stateProcs.stream().
                collect(Collectors.toMap(this::getStateClass, Object::getClass));


        return new StateMachine(null);
    }

    private Class getStateClass(Object o) {
        Class<?> stateProcClass = o.getClass();
        StateConfig stateConfig = stateProcClass.getAnnotation(StateConfig.class);
        return stateConfig.stateClass();
    }

    @SneakyThrows
    private Map<Class<?>, Method> getTransitions(Object stateProc) {
        Class<?> configClass = stateProc.getClass();

        return null;//
        // Stream.of(configClass.getMethods()).filter(m->m.isAnnotationPresent(Transition.class)).collect(ti);
    }


}
