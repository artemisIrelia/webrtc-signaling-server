package com.soriole.web.webrtc_signaling_server.eventbus;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.soriole.web.webrtc_signaling_server.Names;
import com.soriole.web.webrtc_signaling_server.api.dto.NextRTCEvent;
import org.apache.log4j.Logger;
import com.soriole.web.webrtc_signaling_server.api.NextRTCEvents;
import com.soriole.web.webrtc_signaling_server.api.NextRTCHandler;
import com.soriole.web.webrtc_signaling_server.api.annotation.NextRTCEventListener;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

import static org.springframework.core.annotation.AnnotationUtils.getValue;

@Component(Names.EVENT_DISPATCHER)
@Scope("singleton")
@NextRTCEventListener
public class EventDispatcher {
    private static final Logger log = Logger.getLogger(EventDispatcher.class);

    @Autowired
    private ApplicationContext context;

    @Subscribe
    @AllowConcurrentEvents
    public void handle(NextRTCEvent event) {
        getNextRTCEventListeners().stream()
                .filter(listener -> isNextRTCHandler(listener) && supportsCurrentEvent(listener, event))
                .forEach(listener -> doTry(() -> ((NextRTCHandler) listener).handleEvent(event)));

    }

    private void doTry(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Handler throws an exception", e);
        }
    }

    private boolean isNextRTCHandler(Object listener) {
        return listener instanceof NextRTCHandler;
    }

    private boolean supportsCurrentEvent(Object listener, NextRTCEvent event) {
        NextRTCEvents[] events = getSupportedEvents(listener);
        for (NextRTCEvents supportedEvent : events) {
            if (isSupporting(event, supportedEvent)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupporting(NextRTCEvent msg, NextRTCEvents supportedEvent) {
        return supportedEvent.equals(msg.type());
    }

    private NextRTCEvents[] getSupportedEvents(Object listener) {
        try {
            if (AopUtils.isJdkDynamicProxy(listener)) {
                listener = ((Advised) listener).getTargetSource().getTarget();
            }
        } catch (Exception e) {
            return new NextRTCEvents[0];
        }
        return (NextRTCEvents[]) getValue(listener.getClass().getAnnotation(NextRTCEventListener.class));
    }

    private Collection<Object> getNextRTCEventListeners() {
        Map<String, Object> beans = context.getBeansWithAnnotation(NextRTCEventListener.class);
        beans.remove(Names.EVENT_DISPATCHER);
        return beans.values();
    }
}
