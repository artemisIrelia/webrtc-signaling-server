package com.soriole.web.webrtc_signaling_server.domain;

import com.google.gson.annotations.Expose;

/**
 * Created by robik on 3/8/17.
 */
public class CallRequest {
    @Expose
    private String callerMemberId;
    @Expose
    private String calleeMemberId;
    @Expose
    private String convId;
    @Expose
    private Boolean enableVideo;

    public String getCallerMemberId() {
        return callerMemberId;
    }

    public void setCallerMemberId(String callerMemberId) {
        this.callerMemberId = callerMemberId;
    }

    public String getCalleeMemberId() {
        return calleeMemberId;
    }

    public void setCalleeMemberId(String calleeMemberId) {
        this.calleeMemberId = calleeMemberId;
    }

    public String getConvId() {
        return convId;
    }

    public void setConvId(String convId) {
        this.convId = convId;
    }

    public Boolean getEnableVideo() {
        return enableVideo;
    }

    public void setEnableVideo(Boolean enableVideo) {
        this.enableVideo = enableVideo;
    }
}
