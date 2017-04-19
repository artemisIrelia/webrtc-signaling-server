package com.soriole.web.webrtc_signaling_server.domain;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.soriole.web.webrtc_signaling_server.BaseTest;
import com.soriole.web.webrtc_signaling_server.EventChecker;
import com.soriole.web.webrtc_signaling_server.api.NextRTCEvents;
import com.soriole.web.webrtc_signaling_server.api.annotation.NextRTCEventListener;
import com.soriole.web.webrtc_signaling_server.repository.Members;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static com.soriole.web.webrtc_signaling_server.api.NextRTCEvents.SESSION_CLOSED;
import static com.soriole.web.webrtc_signaling_server.api.NextRTCEvents.SESSION_OPENED;

@ContextConfiguration(classes = {SessionOpened.class, SessionClosed.class, UnexpectedSituation.class})
public class EventContentTest extends BaseTest {

    @Rule
    public ExpectedException expect = ExpectedException.none();

    @Autowired
    private Server server;

    @Autowired
    private Members members;

    @Autowired
    private SessionOpened sessionOpened;

    @Autowired
    private SessionClosed sessionClosed;

    @Autowired
    private UnexpectedSituation unexpectedSituation;

    @Test
    public void shouldPostSessionOpenedCloseAndExceptionEvent() throws Exception {
        // given
        Session s1 = mockSession("s1");
        Session s2 = mockSession("s2");

        // when
        server.register(s1);
        server.register(s2);

        server.unregister(s1, new CloseReason(CloseCodes.NORMAL_CLOSURE, ""));
        server.handleError(s2, new RuntimeException());

        // then
        assertThat(sessionOpened.getEvents().size(), is(2));
        assertTrue(sessionOpened.get(0).from().isPresent());
        assertTrue(sessionOpened.get(1).from().isPresent());
        sessionOpened.get(0).from().ifPresent(from ->
                assertThat(from.getId(), is("s1"))
        );
        sessionOpened.get(1).from().ifPresent(from ->
                assertThat(from.getId(), is("s2"))
        );

        assertThat(sessionClosed.getEvents().size(), is(1));
        assertTrue(sessionClosed.get(0).from().isPresent());
        sessionClosed.get(0).from().ifPresent(from ->
                assertThat(from.getId(), is("s1"))
        );

        assertThat(unexpectedSituation.getEvents().size(), is(1));
        assertTrue(unexpectedSituation.get(0).from().isPresent());
        unexpectedSituation.get(0).from().ifPresent(from ->
                assertThat(from.getId(), is("s2"))
        );
    }
}

@NextRTCEventListener(SESSION_OPENED)
class SessionOpened extends EventChecker {

}

@NextRTCEventListener(SESSION_CLOSED)
class SessionClosed extends EventChecker {

}

@NextRTCEventListener(NextRTCEvents.UNEXPECTED_SITUATION)
class UnexpectedSituation extends EventChecker {

}

@NextRTCEventListener(NextRTCEvents.CONVERSATION_CREATED)
class ConversationCreated extends EventChecker {

}

@NextRTCEventListener(NextRTCEvents.CONVERSATION_DESTROYED)
class ConversationDestroyed extends EventChecker {

}

@NextRTCEventListener(NextRTCEvents.MEDIA_LOCAL_STREAM_CREATED)
class LocalStreamCreated extends EventChecker {

}

@NextRTCEventListener(NextRTCEvents.MEDIA_LOCAL_STREAM_REQUESTED)
class LocalStreamRequested extends EventChecker {

}

@NextRTCEventListener(NextRTCEvents.MEDIA_STREAMING)
class Streaming extends EventChecker {

}

@NextRTCEventListener(NextRTCEvents.MEMBER_JOINED)
class MemberJoined extends EventChecker {

}

@NextRTCEventListener(NextRTCEvents.MEMBER_LEFT)
class MemberLeft extends EventChecker {

}
