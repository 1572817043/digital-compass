package com.a0000.digicompass.modules.ai.client;

public class AiCallException extends RuntimeException {
    public AiCallException(String message) {
        super(message);
    }

    public AiCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
