package com.wanomaniac.economy.client.input;
import net.minecraft.util.StringUtil;

public record CharacterEvent(int codepoint, int modifiers) {
    public String codepointAsString() {
        return Character.toString(this.codepoint);
    }

    public boolean isAllowedChatCharacter() {
        return StringUtil.isAllowedChatCharacter((char) this.codepoint);
    }
}
