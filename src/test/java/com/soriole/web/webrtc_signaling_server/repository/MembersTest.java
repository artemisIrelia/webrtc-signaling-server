package com.soriole.web.webrtc_signaling_server.repository;

import org.junit.Test;
import com.soriole.web.webrtc_signaling_server.BaseTest;
import com.soriole.web.webrtc_signaling_server.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;

import javax.websocket.Session;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MembersTest extends BaseTest {

    @Autowired
    private Members members;

    @Test
    public void shouldAddMember() throws Exception {
        // given

        // when
        members.register(mockMember("s1"));

        // then
        assertThat(members.findBy("s1").isPresent(), is(true));
    }

    @Test
    public void shouldWorkWhenMemberDoesntExists() throws Exception {
        // given

        // when
        Optional<Member> findBy = members.findBy("not existing one");

        // then
        assertThat(findBy.isPresent(), is(false));
    }

    @Test
    public void shouldWorkWhenMemberIsNull() throws Exception {
        // given

        // when
        Optional<Member> findBy = members.findBy(null);

        // then
        assertThat(findBy.isPresent(), is(false));
    }

    @Test
    public void shouldUnregisterMember() throws Exception {
        // given
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("s1");
        members.register(mockMember("s1"));
        assertTrue(members.findBy("s1").isPresent());

        // when
        members.unregisterBy(session, null);

        // then
        assertFalse(members.findBy("s1").isPresent());
    }

}
