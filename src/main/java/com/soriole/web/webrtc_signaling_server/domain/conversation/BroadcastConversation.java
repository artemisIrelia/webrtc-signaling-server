package com.soriole.web.webrtc_signaling_server.domain.conversation;

import com.google.common.collect.Sets;
import com.soriole.web.webrtc_signaling_server.cases.ExchangeSignalsBetweenMembers;
import com.soriole.web.webrtc_signaling_server.domain.InternalMessage;
import com.soriole.web.webrtc_signaling_server.domain.Signal;
import com.soriole.web.webrtc_signaling_server.domain.Conversation;
import com.soriole.web.webrtc_signaling_server.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Scope("prototype")
public class BroadcastConversation extends Conversation {
    @Autowired
    private ExchangeSignalsBetweenMembers exchange;

    private Member broadcaster;
    private Set<Member> audience = Sets.newConcurrentHashSet();

    public BroadcastConversation(String id) {
        super(id);
    }

    @Override
    public void call(Member caller, Member callee, String content) {
        throw new UnsupportedOperationException("Calling is not supported when performing broadcasts!");
    }

    @Override
    public synchronized void join(Member sender) {
        assignSenderToConversation(sender);

        informSenderThatHasBeenJoined(sender);

        beginSignalExchangeBetweenBroadcasterAndNewAudience(sender);

        if (!broadcaster.equals(sender)) {
            audience.add(sender);
        }
    }

    @Override
    public synchronized boolean remove(Member leaving) {
        if (broadcaster.equals(leaving)) {
            for (Member member : audience) {
                sendLeftMessage(broadcaster, member);
                sendEndMessage(broadcaster, member);
                member.unassignConversation(this);
            }
            audience.clear();
            broadcaster.unassignConversation(this);
            broadcaster = null;
            return true;
        }
        sendLeftMessage(leaving, broadcaster);
        boolean remove = audience.remove(leaving);
        if (remove) {
            leaving.unassignConversation(this);
        }
        return remove;
    }

    private void sendEndMessage(Member leaving, Member recipient) {
        InternalMessage.create()//
                .from(leaving)//
                .to(recipient)//
                .signal(Signal.END)//
                .content(id)//
                .build()//
                .send();
    }

    @Override
    public synchronized boolean isWithoutMember() {
        if (broadcaster != null) {
            return false;
        }
        return audience.isEmpty();
    }

    @Override
    public synchronized boolean has(Member from) {
        if (broadcaster == null) {
            return false;
        }
        if (broadcaster.equals(from)) {
            return true;
        }
        return audience.contains(from);
    }

    @Override
    public void exchangeSignals(InternalMessage message) {
        exchange.execute(message);
    }

    @Override
    public void broadcast(Member from, InternalMessage message) {
        audience.stream()
                .filter(member -> !member.equals(from))
                .forEach(to -> message.copy()
                        .from(from)
                        .to(to)
                        .build()
                        .send());
        if (from != broadcaster) {
            message.copy()
                    .from(from)
                    .to(broadcaster)
                    .build()
                    .send();
        }
    }

    private void informSenderThatHasBeenJoined(Member sender) {
        if (isWithoutMember()) {
            broadcaster = sender;
            sendJoinedToBroadcaster(sender, id);
        } else {
            sendJoinedToConversation(sender, id);
        }
    }

    private void beginSignalExchangeBetweenBroadcasterAndNewAudience(Member sender) {
        if (!sender.equals(broadcaster)) {
            sendJoinedFrom(sender, broadcaster);
            exchange.begin(broadcaster, sender);
        }
    }

    private void sendJoinedToBroadcaster(Member sender, String id) {
        InternalMessage.create()//
                .to(sender)//
                .signal(Signal.CREATED)//
                .addCustom("type", "BROADCAST")
                .content(id)//
                .build()//
                .send();
    }

}
