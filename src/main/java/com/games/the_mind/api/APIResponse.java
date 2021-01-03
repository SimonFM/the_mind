package com.games.the_mind.api;

import org.springframework.http.HttpStatus;

public class APIResponse {
    HttpStatus status;
    String message;

    public APIResponse(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
