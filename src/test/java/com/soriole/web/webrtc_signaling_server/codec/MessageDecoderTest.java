package com.soriole.web.webrtc_signaling_server.codec;

import org.junit.Test;
import com.soriole.web.webrtc_signaling_server.domain.Message;

import javax.websocket.DecodeException;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class MessageDecoderTest {

    private MessageDecoder decoder = new MessageDecoder();

    @Test
    public void shouldParseBasicObject() throws DecodeException {
        // given
        String validJson = "{'from' : 'Alice',"//
                + "'to' : 'Bob',"//
                + "'signal' : 'join',"//
                + "'content' : 'something'}";

        // when
        Message result = decoder.decode(validJson);

        // then
        assertNotNull(result);
        assertThat(result.getFrom(), is("Alice"));
        assertThat(result.getTo(), is("Bob"));
        assertThat(result.getSignal(), is("join"));
        assertThat(result.getContent(), is("something"));
    }

    @Test
    public void shouldParseAlmostEmptyObject() throws DecodeException {
        // given
        String validJson = "{'signal' : 'join',"//
                + "'content' : 'something'}";

        // when
        Message result = decoder.decode(validJson);

        // then
        assertNotNull(result);
        assertThat(result.getFrom(), is(EMPTY));
        assertThat(result.getTo(), is(EMPTY));
        assertThat(result.getSignal(), is("join"));
        assertThat(result.getContent(), is("something"));
    }

    @Test
    public void shouldRecognizeAndDisposeXSSAttack() throws DecodeException {
        // given
        String validJson = "{'signal' : 'join',"//
                + "'content':'<script>alert(1);</script>'}";

        // when
        Message result = decoder.decode(validJson);

        // then
        assertNotNull(result);
        assertThat(result.getContent(), containsString("&lt;script&gt;alert"));
    }

    @Test
    public void shouldParseRequestWithDoubleQuotes() throws DecodeException {
        // given
        String validJson = "{'from' : 'Alice',"//
                + "'to' : 'Bob',"//
                + "'signal' : 'join',"//
                + "'content' : 'something',"//
                + "'parameters' : {'param1' : 'value1'}}".replace("'", "\"");

        // when
        Message result = decoder.decode(validJson);

        // then
        assertNotNull(result);
        assertThat(result.getFrom(), is("Alice"));
        assertThat(result.getTo(), is("Bob"));
        assertThat(result.getSignal(), is("join"));
        assertThat(result.getContent(), is("something"));
    }

}
