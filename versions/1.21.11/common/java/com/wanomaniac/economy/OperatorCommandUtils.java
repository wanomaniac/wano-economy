package com.wanomaniac.economy;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

public class OperatorCommandUtils {
    public static boolean setCommandSourceStack(CommandSourceStack stack){
        return stack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR);
    }

    public static CommandSourceStack setCommandSourceStackWithPermissions(CommandSourceStack stack){
        return stack.withPermission(PermissionSet.ALL_PERMISSIONS);
    }
}
