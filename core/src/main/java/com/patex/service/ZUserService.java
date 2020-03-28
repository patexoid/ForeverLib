package com.patex.service;

import com.patex.entities.ZUser;
import com.patex.entities.ZUserAuthority;
import com.patex.entities.ZUserConfigEntity;
import com.patex.entities.ZUserConfigRepository;
import com.patex.entities.ZUserRepository;
import com.patex.mapper.UserMapper;
import com.patex.zombie.LibException;
import com.patex.zombie.model.User;
import com.patex.zombie.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 25.03.2017.
 */
@Service
public class ZUserService implements UserService, UserDetailsService {

    public static final String ANONIMUS = "anonimus";
    public static final ZUser anonim = new ZUser(ANONIMUS, true);
    private static final Logger log = LoggerFactory.getLogger(ZUserService.class);

    static {
        ZUserConfigEntity userConfig = new ZUserConfigEntity();
        userConfig.setLang("en");
        anonim.setUserConfig(userConfig);
    }

    private final UserMapper userMapper;
    private final ZUserRepository userRepo;

    private final ZUserConfigRepository userConfigRepo;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher publisher;

    public ZUserService(UserMapper userMapper, ZUserRepository userRepo, ZUserConfigRepository userConfigRepo, PasswordEncoder passwordEncoder, ApplicationEventPublisher publisher) {
        this.userMapper = userMapper;
        this.userRepo = userRepo;
        this.userConfigRepo = userConfigRepo;
        this.passwordEncoder = passwordEncoder;
        this.publisher = publisher;
    }

    private ZUser getCurrentZUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof ZUser) {
            return (ZUser) principal;
        }
        return anonim;
    }

    @Override
    public ZUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findById(username).orElse(anonim);
    }

    public User getCurrentUser() {
        return userMapper.toDto(getCurrentZUser());
    }

    public User save(User user) {
        ZUser currentUser = getCurrentZUser();
        if (currentUser.getUsername().equals(user.getUsername()) ||
                currentUser.getAuthorities().stream().anyMatch(o -> ADMIN_AUTHORITY.equals(o.getAuthority()))) {
            ZUser zUser = loadUserByUsername(user.getUsername());
            ZUser updatedUser = userMapper.updateEntity(user, zUser);
            updatedUser.setPassword(passwordEncoder.encode(user.getPassword()));
            return userMapper.toDto(zUser);
        }
        return user;
    }

    public ZUser createUser(ZUser user) {
        user.getAuthorities().add(new ZUserAuthority(user, USER));
        if (getByRole(ADMIN_AUTHORITY).isEmpty()) {
            user.getAuthorities().add(new ZUserAuthority(user, ADMIN_AUTHORITY));
        }
        if (user.getUserConfig() == null) {
            user.setUserConfig(new ZUserConfigEntity());
        }
        if (user.getUserConfig().getLang() == null) {
            user.getUserConfig().setLang("en");
        }
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        ZUser created = userRepo.save(user);
        publisher.publishEvent(new UserCreationEvent(user));
        return created;
    }

    public void updatePassword(String oldPassword, String newPassword) throws LibException {
        User currentUser = getCurrentUser();
        if (currentUser.getUsername().equals(ANONIMUS)) {//TODO FIX that
            throw new LibException("Unable to change password for anonim user");
        }
        ZUser user = loadUserByUsername(currentUser.getUsername());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new LibException("Incorrect old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    public void updateUserConfig(ZUserConfigEntity newConfig) {
        updateUserConfig(getCurrentZUser(), newConfig);
    }


    private void updateUserConfig(ZUser user, ZUserConfigEntity newConfig) {

        ZUserConfigEntity userConfig = user.getUserConfig();
        if (userConfig == null) {
            userConfig = new ZUserConfigEntity();
            user.setUserConfig(userConfig);
            userConfig.setUser(userRepo.findById(user.getUsername()).get());

        }
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(ZUserConfigEntity.class);
        for (PropertyDescriptor pd : pds) {
            if (!"id".equals(pd.getName()) && !"user".equals(pd.getName())) {
                Method writeMethod = pd.getWriteMethod();
                Method readMethod = pd.getReadMethod();
                if (writeMethod != null && readMethod != null) {
                    try {
                        Object value = readMethod.invoke(newConfig);
                        if (value != null) {
                            writeMethod.invoke(userConfig, value);
                        }
                    } catch (ReflectiveOperationException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
        userConfigRepo.save(userConfig);
    }

    @Override
    public Collection<User> getByRole(String role) {
        return userRepo.findAllByAuthoritiesIs(role).stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Locale getUserLocale() {
        return getCurrentUser().getUserConfig().getLocale();
    }

    public User getUser(String username) {
        return userRepo.findById(username).map(userMapper::toDto).orElseThrow();
    }
}
