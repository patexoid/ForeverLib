package com.patex.opds.extlib;

import com.patex.entities.ExtLibrary;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("NullableProblems")
@Component
class ExtLibScope implements Scope {

    private final Map<Long, Map<String, Object>> extLibObjMap = new HashMap<>();
    private final Map<Long, Map<String, Runnable>> userObjDestructorMap = new HashMap<>();

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    @Autowired
    private ExtLibScopeStorage scopeStorage;

    @PostConstruct
    public void setUp() {
        beanFactory.registerScope("extLibrary", this);
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        ExtLibrary currentExtLib = scopeStorage.getCurrentExtLib();
        if (currentExtLib == null) {
            return null;
        }
        Long libId = currentExtLib.getId();
        Map<String, Object> scope = extLibObjMap.get(libId);
        if (scope == null) {
            synchronized (extLibObjMap) {
                scope = extLibObjMap.computeIfAbsent(libId, k -> new HashMap<>());
            }
        }
        Object obj = scope.get(name);
        if (obj == null) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (scope) {
                obj = scope.computeIfAbsent(name, s -> objectFactory.getObject());
            }
        }
        return obj;
    }

    @Override
    public Object remove(String name) {
        Object obj = null;
        ExtLibrary currentExtLib = scopeStorage.getCurrentExtLib();
        if (currentExtLib == null) {
            return null;
        }
        Long libId = currentExtLib.getId();
        Map<String, Object> scope = extLibObjMap.get(libId);
        if (scope != null) {
            obj = scope.remove(name);
        }
        Map<String, Runnable> destructors = userObjDestructorMap.get(libId);
        Runnable destructor = destructors.remove(name);
        if (destructor != null) {
            destructor.run();
        }
        return obj;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        ExtLibrary currentExtLib = scopeStorage.getCurrentExtLib();
        if (currentExtLib == null) {
            return;
        }
        Long libId = currentExtLib.getId();
        Map<String, Runnable> destructors = userObjDestructorMap.computeIfAbsent(libId, s -> new HashMap<>());
        destructors.put(name, callback);
    }

    @Override
    public Object resolveContextualObject(String key) {
        return scopeStorage.getCurrentExtLib();
    }

    @Override
    public String getConversationId() {
        ExtLibrary currentExtLib = scopeStorage.getCurrentExtLib();
        if (currentExtLib == null) {
            return null;
        }
        return currentExtLib.getName();
    }
}
