package com.gree.airconditioner.gree.exceptions;

/**
 * Base exception for GREE HVAC client operations
 */
public class ClientException extends Exception {
    private final Throwable origin;
    private final Object props;

    public ClientException(String message) {
        super(message);
        this.origin = null;
        this.props = null;
    }

    public ClientException(String message, Throwable origin) {
        super(message, origin);
        this.origin = origin;
        this.props = null;
    }

    public ClientException(String message, Throwable origin, Object props) {
        super(message, origin);
        this.origin = origin;
        this.props = props;
    }

    public Throwable getOrigin() {
        return origin;
    }

    public Object getProps() {
        return props;
    }
}

class ClientSocketSendException extends ClientException {
    public ClientSocketSendException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}

class ClientMessageParseException extends ClientException {
    public ClientMessageParseException(Throwable cause, Object props) {
        super("Cannot parse device JSON response (" + cause.getMessage() + ")", cause, props);
    }
}

class ClientMessageUnpackException extends ClientException {
    public ClientMessageUnpackException(Throwable cause, Object props) {
        super("Cannot decrypt message (" + cause.getMessage() + ")", cause, props);
    }
}

class ClientUnknownMessageException extends ClientException {
    public ClientUnknownMessageException(Object props) {
        super("Unknown message type received", null, props);
    }
}

class ClientNotConnectedException extends ClientException {
    public ClientNotConnectedException() {
        super("Client is not connected to the HVAC");
    }
}

class ClientConnectTimeoutException extends ClientException {
    public ClientConnectTimeoutException() {
        super("Connecting to HVAC timed out");
    }
}

class ClientCancelConnectException extends ClientException {
    public ClientCancelConnectException() {
        super("Connecting to HVAC was cancelled");
    }
}