package io.ptokens.commands;

public class InvalidCommandException extends Exception {
    InvalidCommandException(String msg) {
        super(msg);
    }
}
