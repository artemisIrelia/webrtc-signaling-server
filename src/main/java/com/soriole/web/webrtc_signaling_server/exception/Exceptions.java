package com.soriole.web.webrtc_signaling_server.exception;

public enum Exceptions {
    MEMBER_NOT_FOUND("0001"), //
    INVALID_RECIPIENT("0002"), //
    MEMBER_IN_OTHER_CONVERSATION("0003"),

    INVALID_CONVERSATION_NAME("0101"), //
    CONVERSATION_NAME_OCCUPIED("0102"), //
    CONVERSATION_NOT_FOUND("0103"), //

    UNKNOWN_ERROR("0501"),;

    private String code;

    Exceptions(String code) {
        this.code = code;
    }

    public String getErrorCode() {
        return code;
    }

    public SignalingException exception() {
        return new SignalingException(this);
    }

    public SignalingException exception(String customMesage) {
        return new SignalingException(this, customMesage);
    }

    public SignalingException exception(Exception reason) {
        return new SignalingException(this, reason);
    }

}
