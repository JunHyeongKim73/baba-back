package com.baba.back.content.exception;

import com.baba.back.exception.BadRequestException;

public class ContentDateBadRequestException extends BadRequestException {
    public ContentDateBadRequestException(String message) {
        super(message);
    }
}
