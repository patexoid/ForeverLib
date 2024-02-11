package com.patex.forever.service;

import com.patex.forever.entities.LibUser;
import com.patex.forever.entities.LibUserAuthority;
import com.patex.forever.entities.LibUserConfigEntity;
import com.patex.forever.entities.LibUserConfigRepository;
import com.patex.forever.entities.LibUserRepository;
import com.patex.forever.mapper.UserMapper;
import com.patex.forever.LibException;
import com.patex.forever.model.User;
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
public class LibUserService implements UserService, UserDetailsService {

    public static final String ANONIMUS = "anonimus";
    public static final LibUser anonim = new LibUser(ANONIMUS, true);
    private static final Logger log = LoggerFactory.getLogger(LibUserService.class);

    static {
        LibUserConfigEntity userConfig = new LibUserConfigEntity();
        userConfig.setLang("en");
        anonim.setUserConfig(userConfig);
    }

    private final UserMapper userMapper;
    private final LibUserRepository userRepo;

    private final LibUserConfigRepository userConfigRepo;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher publisher;

    public LibUserService(UserMapper userMapper, LibUserRepository userRepo, LibUserConfigRepository userConfigRepo, PasswordEncoder passwordEncoder, ApplicationEventPublisher publisher) {
        this.userMapper = userMapper;
        this.userRepo = userRepo;
        this.userConfigRepo = userConfigRepo;
        this.passwordEncoder = passwordEncoder;
        this.publisher = publisher;
    }

    private LibUser getCurrentZUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof LibUser) {
            return (LibUser) principal;
        }
        return anonim;
    }

    @Override
    public LibUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findById(username).orElse(anonim);
    }

    public User getCurrentUser() {
        return userMapper.toDto(getCurrentZUser());
    }

    public User save(User user) {
        LibUser currentUser = getCurrentZUser();
        if (currentUser.getUsername().equals(user.getUsername()) ||
                currentUser.getAuthorities().stream().anyMatch(o -> ADMIN_AUTHORITY.equals(o.getAuthority()))) {
            LibUser libUser = loadUserByUsername(user.getUsername());
            LibUser updatedUser = userMapper.updateEntity(user, libUser);
            updatedUser.setPassword(passwordEncoder.encode(user.getPassword()));
            return userMapper.toDto(libUser);
        }
        return user;
    }

    public LibUser createUser(LibUser user) {
        user.getAuthorities().add(new LibUserAuthority(user, USER));
        if (getByRole(ADMIN_AUTHORITY).isEmpty()) {
            user.getAuthorities().add(new LibUserAuthority(user, ADMIN_AUTHORITY));
        }
        if (user.getUserConfig() == null) {
            user.setUserConfig(new LibUserConfigEntity());
        }
        if (user.getUserConfig().getLang() == null) {
            user.getUserConfig().setLang("en");
        }
        user.getUserConfig().setUser(user);
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        LibUser created = userRepo.save(user);
        publisher.publishEvent(new UserCreationEvent(user));
        return created;
    }

    public void updatePassword(String oldPassword, String newPassword) throws LibException {
        User currentUser = getCurrentUser();
        if (currentUser.getUsername().equals(ANONIMUS)) {//TODO FIX that
            throw new LibException("Unable to change password for anonim user");
        }
        LibUser user = loadUserByUsername(currentUser.getUsername());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new LibException("Incorrect old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    public void updateUserConfig(LibUserConfigEntity newConfig) {
        updateUserConfig(getCurrentZUser(), newConfig);
    }


    private void updateUserConfig(LibUser user, LibUserConfigEntity newConfig) {

        LibUserConfigEntity userConfig = user.getUserConfig();
        if (userConfig == null) {
            userConfig = new LibUserConfigEntity();
            user.setUserConfig(userConfig);
            userConfig.setUser(userRepo.findById(user.getUsername()).get());

        }
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(LibUserConfigEntity.class);
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
