package com.soriole.web.webrtc_signaling_server.domain;

import org.junit.Before;
import org.junit.Test;
import com.soriole.web.webrtc_signaling_server.BaseTest;
import com.soriole.web.webrtc_signaling_server.repository.Conversations;
import com.soriole.web.webrtc_signaling_server.repository.Members;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@ContextConfiguration(classes = {ServerEventCheck.class, LocalStreamCreated2.class})
public class ServerActorTest extends BaseTest {

    @Autowired
    protected ServerEventCheck eventCheckerCall;
    @Autowired
    protected LocalStreamCreated2 eventLocalStream;
    @Autowired
    private Server server;
    @Autowired
    private Members members;
    @Autowired
    private Conversations conversations;
    @Autowired
    private SignalResolver resolver;

    @Test
    public void shouldExchangeSignalsBetweenActors() throws Exception {
        // given
        TestClientActor john = new TestClientActor("John", server);
        TestClientActor bob = new TestClientActor("Bob", server);

        // when
        john.openSocket();
        john.create("AAA", "MESH");

        // then
        assertTrue(conversations.findBy("AAA").isPresent());
        Conversation conversation = conversations.findBy("AAA").get();
        assertTrue(conversation.has(john.asMember()));

        // when
        bob.openSocket();
        bob.join("AAA");

        // then
        assertTrue(conversation.has(john.asMember()));
        assertTrue(conversation.has(bob.asMember()));

        // when
        bob.closeSocket();

        // then
        assertFalse(conversation.has(bob.asMember()));

        // when
        john.closeSocket();

        // then
        assertFalse(conversation.has(john.asMember()));
        assertFalse(conversations.findBy("AAA").isPresent());

        assertNoErrors(john);
        assertNoErrors(bob);
    }

    @Test
    public void shouldExchangeSignalsBetweenActors_Broadcast() throws Exception {
        // given
        TestClientActor john = new TestClientActor("John", server);
        TestClientActor bob = new TestClientActor("Bob", server);

        // when
        john.openSocket();
        john.create("AAA", "BROADCAST");

        // then
        assertTrue(conversations.findBy("AAA").isPresent());
        Conversation conversation = conversations.findBy("AAA").get();
        assertTrue(conversation.has(john.asMember()));

        // when
        bob.openSocket();
        bob.join("AAA");

        // then
        assertTrue(conversation.has(john.asMember()));
        assertTrue(conversation.has(bob.asMember()));

        // when
        bob.closeSocket();

        // then
        assertFalse(conversation.has(bob.asMember()));

        // when
        john.closeSocket();

        // then
        assertFalse(conversation.has(john.asMember()));
        assertFalse(conversations.findBy("AAA").isPresent());

        assertNoErrors(john);
        assertNoErrors(bob);
    }

    @Test
    public void shouldCheckBehaviorWhenBroadcasterWillEndConnectionFirst() throws Exception {
        // given
        TestClientActor john = new TestClientActor("John", server);
        TestClientActor bob = new TestClientActor("Bob", server);

        // when
        john.openSocket();
        john.create("AAA", "BROADCAST");
        Conversation conversation = conversations.findBy("AAA").get();

        bob.openSocket();
        bob.join("AAA");

        john.closeSocket();
        bob.closeSocket();


        // then
        assertFalse(conversation.has(bob.asMember()));
        assertFalse(conversations.findBy("AAA").isPresent());
        final Message message = bob.getMessages().stream().filter(m -> m.getSignal().equals(Signals.END)).findFirst().get();
        assertThat(message.getContent(), is("AAA"));
        assertThat(message.getSignal(), is(Signals.END));

        assertNoErrors(john);
        assertNoErrors(bob);
    }

    @Test
    public void shouldCheckSignalExchangeForThreeMembers() throws Exception {
        // given
        TestClientActor john = new TestClientActor("John", server);
        TestClientActor bob = new TestClientActor("Bob", server);
        TestClientActor alice = new TestClientActor("Alice", server);
        TestClientActor mike = new TestClientActor("Mike", server);

        // when
        alice.openSocket();
        mike.openSocket();

        john.openSocket();
        john.create("AAA", "BROADCAST");

        bob.openSocket();
        bob.join("AAA");
        alice.join("AAA");
        mike.join("AAA");

        john.closeSocket();
        bob.closeSocket();
        alice.closeSocket();
        mike.closeSocket();

        // then
        assertThat(bob.getMessages().size(), is(alice.getMessages().size()));
        List<Message> bobMessages = bob.getMessages();
        List<Message> aliceMessages = alice.getMessages();
        List<Message> mikeMessages = mike.getMessages();
        for (int i = 0; i < bobMessages.size(); i++) {
            Message bobMsg = bobMessages.get(i);
            Message aliceMsg = aliceMessages.get(i);
            Message mikeMsg = mikeMessages.get(i);
            assertTrue(bobMsg.getSignal().equals(aliceMsg.getSignal()));
            assertTrue(mikeMsg.getSignal().equals(aliceMsg.getSignal()));
        }

        assertNoErrors(john);
        assertNoErrors(bob);
        assertNoErrors(alice);
        assertNoErrors(mike);
    }

    @Test
    public void shouldSendTextMessageToOtherAudience() throws Exception {
        // given
        TestClientActor john = new TestClientActor("John", server);
        TestClientActor bob = new TestClientActor("Bob", server);
        TestClientActor alice = new TestClientActor("Alice", server);

        alice.openSocket();
        bob.openSocket();
        john.openSocket();

        john.create("AAA", "MESH");
        Conversation conversation = conversations.findBy("AAA").get();
        bob.join("AAA");
        alice.join("AAA");

        // when
        john.sendToServer(Message.create()
                .to(bob.asMember().getId())
                .signal(Signals.TEXT)
                .content("Hello")
                .build());

        // then
        List<Message> messages = bob.getMessages();
        Message message = messages.get(messages.size() - 1);
        message.getContent().equals("Hello");

        assertNoErrors(john);
        assertNoErrors(bob);
        assertNoErrors(alice);
    }

    @Test
    public void shouldSendTextMessageToOtherAudience_BROADCAST() throws Exception {
        // given
        TestClientActor john = new TestClientActor("John", server);
        TestClientActor bob = new TestClientActor("Bob", server);
        TestClientActor alice = new TestClientActor("Alice", server);

        alice.openSocket();
        bob.openSocket();
        john.openSocket();

        john.create("AAA", "BROADCAST");
        Conversation conversation = conversations.findBy("AAA").get();
        bob.join("AAA");
        alice.join("AAA");

        // when
        john.sendToServer(Message.create()
                .to(bob.asMember().getId())
                .signal(Signals.TEXT)
                .content("Hello")
                .build());

        // then
        List<Message> messages = bob.getMessages();
        Message message = messages.get(messages.size() - 1);
        message.getContent().equals("Hello");

        assertNoErrors(john);
        assertNoErrors(bob);
        assertNoErrors(alice);
    }

    @Test
    public void shouldBeAbleToHandleCustomSignal() throws Exception {
        // given
        resolver.addCustomHandler(Signal.fromString("upperCase"), (message) -> InternalMessage.create()//
                .to(message.getFrom())
                .content(message.getContent().toUpperCase())
                .signal(Signal.fromString("upperCase"))
                .build()
                .send());

        TestClientActor john = new TestClientActor("John", server);
        john.openSocket();

        // when
        john.sendToServer(Message.create()
                .signal("upperCase")
                .content("Hello")
                .build());

        // then
        assertThat(john.getMessages().get(0).getContent(), is("HELLO"));
        assertNoErrors(john);
    }


    private void assertNoErrors(TestClientActor john) {
        assertTrue(john.getMessages().stream().allMatch(m -> !m.getSignal().equals(Signals.ERROR)));
    }


    @Before
    public void resetObjects() {
        eventCheckerCall.reset();
        eventLocalStream.reset();
    }
}
