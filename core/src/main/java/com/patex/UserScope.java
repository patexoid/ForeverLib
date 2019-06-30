package com.patex;

import com.patex.entities.ZUser;
import com.patex.service.ZUserService;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserScope implements Scope {

    private final Map<String, Map<String, Object>> userObjMap = new HashMap<>();
    private final Map<String, Map<String, Runnable>> userObjDestructorMap = new HashMap<>();

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    @PostConstruct
    public void setUp() {
        beanFactory.registerScope("userScope",this);
    }


    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof ZUser) {
            return ((ZUser) principal).getUsername();
        }
        return ZUserService.anonim.getUsername();
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        String user = getCurrentUser();
        Map<String, Object> scope = userObjMap.get(user);
        if (scope == null) {
            synchronized (userObjMap) {
                scope = userObjMap.computeIfAbsent(user, k -> new HashMap<>());
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
        String user = getCurrentUser();
        Map<String, Object> scope = userObjMap.get(user);
        if (scope != null) {
            obj = scope.remove(name);
        }
        Map<String, Runnable> destructors = userObjDestructorMap.get(user);
        Runnable destructor = destructors.remove(name);
        if (destructor != null) {
            destructor.run();
        }
        return obj;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        String user = getCurrentUser();
        Map<String, Runnable> destructors = userObjDestructorMap.computeIfAbsent(user, s -> new HashMap<>());
        destructors.put(name, callback);

    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return getCurrentUser();
    }
}
