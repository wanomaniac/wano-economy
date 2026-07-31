package com.wanomaniac.economy.client.input;

public record KeyEvent(int key, int scancode, int modifiers) implements InputWithModifiers {
    @Override
    public int input() {
        return this.key;
    }
}
