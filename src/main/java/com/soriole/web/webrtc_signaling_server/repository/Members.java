package com.soriole.web.webrtc_signaling_server.repository;

import com.google.common.collect.Maps;
import com.soriole.web.webrtc_signaling_server.Names;
import com.soriole.web.webrtc_signaling_server.api.NextRTCEventBus;
import com.soriole.web.webrtc_signaling_server.api.NextRTCEvents;
import com.soriole.web.webrtc_signaling_server.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.websocket.Session;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Repository
public class Members {

    @Autowired
    @Qualifier(Names.EVENT_BUS)
    private NextRTCEventBus eventBus;

    private Map<String, Member> members = Maps.newConcurrentMap();

    public Collection<String> getAllIds() {
        return members.keySet();
    }

    public Optional<Member> findBy(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(members.get(id));
    }

    public void register(Member member) {
        //members.computeIfAbsent(member.getId(), put -> member);

        if (members.containsKey(member.getId())) {
            Member oldMember = members.get(member.getId());
            unregisterBy(oldMember.getSession(), "New registration for same member id");
        }
        members.put(member.getId(), member);
        eventBus.post(NextRTCEvents.SESSION_OPENED.occurFor(member.getSession()));
    }

    public void unregisterBy(Session session, String reason) {
        unregister(session.getId());
        eventBus.post(NextRTCEvents.SESSION_CLOSED.occurFor(session, reason));
    }

    private void unregister(String id) {
        findBy(id).ifPresent(Member::markLeft);
        Member removed = members.remove(id);
        if (removed != null) {
            removed.getConversation().ifPresent(c -> c.left(removed));
        }
    }

    public void dropOutAfterException(Session session, String reason) {
        unregister(session.getId());
        eventBus.post(NextRTCEvents.UNEXPECTED_SITUATION.occurFor(session, reason));
    }
}
