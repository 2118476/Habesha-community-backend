package com.habesha.community.exception;

public class EmailNotVerifiedException extends RuntimeException {
    private final String email;

    public EmailNotVerifiedException(String email) {
        super("Email not verified. Please check your inbox and verify your email before signing in.");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
