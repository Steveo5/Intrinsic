package com.hotmail.intrinsic.exception;

public class RegionLoadException extends Exception {

    private String message;

    public RegionLoadException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
