package com.brightline.loyalty.app;

public final class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException() {
        super("Member not found");
    }
}
