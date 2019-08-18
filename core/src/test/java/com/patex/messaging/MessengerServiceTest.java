package com.patex.messaging;

import com.patex.entities.UserEntity;
import com.patex.entities.UserConfigEntity;
import com.patex.service.Resources;
import com.patex.service.ZUserService;
import com.patex.utils.Res;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessengerServiceTest {

    private static final String LOCALIZED_MESSAGE = "localizedMessage";
    private static final String MESSAGE_KEY = "message";
    private static final Object[] OBJS = new Object[0];

    @Mock
    private Resources res;

    @Mock
    private ZUserService userService;

    @Mock
    private Messenger messenger;

    @InjectMocks
    private MessengerService messengerService;

    private UserEntity user = new UserEntity();

    private Locale locale = Locale.PRC;
    private UserConfigEntity userConfig = mock(UserConfigEntity.class);

    @Before
    public void setUp() {
        messengerService.register(messenger);
        when(userConfig.getLocale()).thenReturn(locale);
        user.setUserConfig(userConfig);
        when(res.get(locale, MESSAGE_KEY, OBJS)).thenReturn(LOCALIZED_MESSAGE);
    }

    @Test
    public void shouldSendMessageRes() {
        messengerService.sendMessageToUser(new Res(MESSAGE_KEY, OBJS), user);
        verify(messenger).sendToUser(LOCALIZED_MESSAGE, user);
    }

    @Test
    public void shouldSendMessageToUserWithRole() {
        UserEntity user1 = new UserEntity();
        user1.setUserConfig(userConfig);
        UserEntity user2 = new UserEntity();
        user2.setUserConfig(userConfig);
        String role = "role";
        when(userService.getByRole(role)).thenReturn(Arrays.asList(user1, user2));

        messengerService.toRole(new Res(MESSAGE_KEY, OBJS), role);

        verify(messenger).sendToUser(LOCALIZED_MESSAGE, user1);
        verify(messenger).sendToUser(LOCALIZED_MESSAGE, user2);
    }

    @Test
    public void shouldSendMessagetoAdminWhenStop() {
        when(userService.getByRole(ZUserService.ADMIN_AUTHORITY)).thenReturn(Collections.singletonList(user));
        String stopMessage = "stopMessage";
        when(res.get(locale, "lib.stopped")).thenReturn(stopMessage);

        messengerService.stopEvent();

        verify(messenger).sendToUser(stopMessage, user);

    }
}
