package com.soriole.web.webrtc_signaling_server.cases;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.soriole.web.webrtc_signaling_server.domain.*;
import com.soriole.web.webrtc_signaling_server.repository.Conversations;
import org.apache.log4j.Logger;
import com.soriole.web.webrtc_signaling_server.repository.Members;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component(Signals.REJECT_HANDLER)
public class RejectHandler implements SignalHandler {
    private static final Logger log = Logger.getLogger(RejectHandler.class);
    private Gson gson = new GsonBuilder().create();

    @Autowired
    private Conversations conversations;
    @Autowired
    private Members members;

    public void execute(InternalMessage context) {
        String content = context.getContent();
        Reject reject = gson.fromJson(content, Reject.class);
        Optional<Member> rejectedMember = members.findBy(reject.getRejectedMemberId());
        if (rejectedMember.isPresent()) {
            sendRejected(rejectedMember.get(), content);
        }
    }

    private void sendRejected(Member member, String content) {
        InternalMessage.create()//
                .to(member)//
                .signal(Signal.REJECTED)//
                .addCustom("type", "MESH")
                .content(content)//
                .build()//
                .send();
    }

    private void sendConversationNotPresent(Member from, String convId) {

    }


}
