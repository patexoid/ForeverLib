package com.patex.zombie.user.service;

import com.patex.LibException;
import com.patex.model.Creds;
import com.patex.model.User;
import com.patex.zombie.user.controller.UserCreateRequest;
import com.patex.zombie.user.entities.AuthorityEntity;
import com.patex.zombie.user.entities.UserConfigEntity;
import com.patex.zombie.user.entities.UserEntity;
import com.patex.zombie.user.entities.UserRepository;
import com.patex.zombie.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.patex.model.User.anonim;

/**
 * Created by Alexey on 25.03.2017.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    public static final String GUEST = "GUEST";
    public static final String USER = "ROLE_USER";
    public static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final UserRepository userRepo;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper mapper;

    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findById(username).map(mapper::toDto).get();
    }

    public User save(User user, User currentUser) {
        if (currentUser.getUsername().equals(user.getUsername()) ||
                currentUser.getAuthorities().stream().anyMatch(ADMIN_AUTHORITY::equals)) {
            UserEntity updatedUser = new UserEntity();
            BeanUtils.copyProperties(user, updatedUser);
            userRepo.findById(user.getUsername())
                    .ifPresent(entity -> updatedUser.setPassword(entity.getPassword()));
            return mapper.toDto(userRepo.save(updatedUser));
        }
        return user;
    }

    public List<User> getAll(){
       return userRepo.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public User createUser(UserCreateRequest createRequest, User currentUser) {
        UserEntity user = mapper.toEntity(createRequest);
        user.setUserConfig(new UserConfigEntity());

        if (!currentUser.getAuthorities().contains(ADMIN_AUTHORITY)) {
            if (getByRole(ADMIN_AUTHORITY).isEmpty()) {
                user.getAuthorities().add(new AuthorityEntity(user, ADMIN_AUTHORITY));
            } else {
                throw new LibException("user have no permissions");
            }
        }
        user.getAuthorities().add(new AuthorityEntity(user, USER));
        if (createRequest.getLang() == null) {
            user.getUserConfig().setLang("en");
        } else {
            user.getUserConfig().setLang(createRequest.getLang());
        }
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        UserEntity created = userRepo.save(user);
        return mapper.toDto(created);
    }

    @Transactional
    public void updatePassword(String oldPassword, String newPassword, User current) throws LibException {
        if (current == anonim) {
            throw new LibException("Unable to change password for anonim user");
        }
        userRepo.findById(current.getUsername())
                .filter(entity -> passwordEncoder.matches(oldPassword, entity.getPassword()))
                .ifPresentOrElse(entity -> entity.setPassword(passwordEncoder.encode(newPassword)),
                        () -> {
                            throw new LibException("Incorrect old password");
                        });
    }


    public Collection<UserEntity> getByRole(String role) {
        return userRepo.findAllByAuthoritiesIs(role);
    }

    public User validateUser(Creds creds) {
        return userRepo.findById(creds.getUsername())
                .filter(entity -> passwordEncoder.matches(entity.getPassword(), creds.getPassword()))
                .map(mapper::toDto).orElse(anonim);
    }

}
