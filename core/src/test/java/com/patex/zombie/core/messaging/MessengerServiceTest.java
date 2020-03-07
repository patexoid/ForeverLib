package com.patex.zombie.core.messaging;

import com.patex.model.User;
import com.patex.utils.Resources;
import com.patex.utils.Res;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Locale;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class MessengerServiceTest {

    private static final String LOCALIZED_MESSAGE = "localizedMessage";
    private static final String MESSAGE_KEY = "message";
    private static final Object[] OBJS = new Object[0];

    @Mock
    private Resources res;


    @Mock
    private Messenger messenger;

    @InjectMocks
    private MessengerService messengerService;

    private User user = new User();

    private Locale locale = Locale.PRC;

    @Before
    public void setUp() {
        messengerService.register(messenger);
//        when(userConfig.getLocale()).thenReturn(locale);
//        user.setUserConfig(userConfig);
        when(res.get(locale, MESSAGE_KEY, OBJS)).thenReturn(LOCALIZED_MESSAGE);
    }

    @Test
    public void shouldSendMessageRes() {
        messengerService.sendMessageToUser(new Res(MESSAGE_KEY, OBJS), user);
        verify(messenger).sendToUser(LOCALIZED_MESSAGE, user);
    }

    @Test
    public void shouldSendMessageToUserWithRole() {
        User user1 = new User();
        User user2 = new User();
        String role = "role";
//        when(userService.getByRole(role)).thenReturn(Arrays.asList(user1, user2));

        messengerService.toRole(new Res(MESSAGE_KEY, OBJS), role);

        verify(messenger).sendToUser(LOCALIZED_MESSAGE, user1);
        verify(messenger).sendToUser(LOCALIZED_MESSAGE, user2);
    }

    @Test
    public void shouldSendMessagetoAdminWhenStop() {
//        when(userService.getByRole(ZUserService.ADMIN_AUTHORITY)).thenReturn(Collections.singletonList(user));
        String stopMessage = "stopMessage";
        when(res.get(locale, "lib.stopped")).thenReturn(stopMessage);

        messengerService.stopEvent();

        verify(messenger).sendToUser(stopMessage, user);

    }
}
