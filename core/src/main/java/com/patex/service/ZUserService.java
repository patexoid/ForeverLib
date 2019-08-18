package com.patex.service;

import com.patex.LibException;
import com.patex.entities.UserEntity;
import com.patex.entities.AuthorityEntity;
import com.patex.entities.UserConfigEntity;
import com.patex.entities.UserConfigRepository;
import com.patex.entities.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Created by Alexey on 25.03.2017.
 */
@Service
public class ZUserService implements UserDetailsService {

    public static final String GUEST = "GUEST";
    public static final String USER = "ROLE_USER";
    public static final String ADMIN_AUTHORITY = "ROLE_ADMIN";
    public static final UserEntity anonim = new UserEntity("anonimus", true);
   private static final Logger log = LoggerFactory.getLogger(ZUserService.class);

    static {
        UserConfigEntity userConfig = new UserConfigEntity();
        userConfig.setLang("en");
        anonim.setUserConfig(userConfig);
    }

    private final UserRepository userRepo;

    private final UserConfigRepository userConfigRepo;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher publisher;

    @Autowired
    public ZUserService(UserRepository userRepo,
                        UserConfigRepository userConfigRepo,
                        PasswordEncoder passwordEncoder,
                        ApplicationEventPublisher publisher) {
        this.userRepo = userRepo;
        this.userConfigRepo = userConfigRepo;
        this.passwordEncoder = passwordEncoder;
        this.publisher = publisher;
    }

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepo.findById(username).get();
        return user == null ? anonim : user;
    }

    public UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof UserEntity) {
            return (UserEntity) principal;
        }
        return anonim;
    }

    public UserEntity save(UserEntity user) {
        UserEntity currentUser = getCurrentUser();
        if (currentUser.getUsername().equals(user.getUsername()) ||
                currentUser.getAuthorities().stream().anyMatch(o -> ADMIN_AUTHORITY.equals(o.getAuthority()))) {
            UserEntity updatedUser = new UserEntity();
            BeanUtils.copyProperties(user, updatedUser);
            updatedUser.setPassword(currentUser.getPassword());
            return userRepo.save(updatedUser);
        }
        return user;
    }

    public UserEntity createUser(UserEntity user) {
        user.getAuthorities().add(new AuthorityEntity(user, USER));
        if (getByRole(ADMIN_AUTHORITY).isEmpty()) {
            user.getAuthorities().add(new AuthorityEntity(user, ADMIN_AUTHORITY));
        }
        if (user.getUserConfig() == null) {
            user.setUserConfig(new UserConfigEntity());
        }
        if (user.getUserConfig().getLang() == null) {
            user.getUserConfig().setLang("en");
        }
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        UserEntity created = userRepo.save(user);
        publisher.publishEvent(new UserCreationEvent(user));
        return created;
    }

    public void updatePassword(String oldPassword, String newPassword) throws LibException {
        UserEntity currentUser = getCurrentUser();
        if (currentUser == anonim) {
            throw new LibException("Unable to change password for anonim user");
        }
        UserEntity user = loadUserByUsername(currentUser.getUsername());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new LibException("Incorrect old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    public void updateUserConfig(UserConfigEntity newConfig) {
        updateUserConfig(getCurrentUser(), newConfig);
    }


    private void updateUserConfig(UserEntity user, UserConfigEntity newConfig) {

        UserConfigEntity userConfig = user.getUserConfig();
        if (userConfig == null) {
            userConfig = new UserConfigEntity();
            user.setUserConfig(userConfig);
            userConfig.setUser(userRepo.findById(user.getUsername()).get());

        }
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(UserConfigEntity.class);
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

    public Collection<UserEntity> getByRole(String role) {
        return userRepo.findAllByAuthoritiesIs(role);
    }

    public Locale getUserLocale() {
        return getCurrentUser().getUserConfig().getLocale();
    }

}
