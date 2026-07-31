package com.wanomaniac.economy;
import net.minecraft.commands.CommandSourceStack;

public class OperatorCommandUtils {
    public static boolean setCommandSourceStack(CommandSourceStack stack){
        return stack.hasPermission(2);
    }

    public static CommandSourceStack setCommandSourceStackWithPermissions(CommandSourceStack stack){
        return stack.withPermission(4);
    }
}
