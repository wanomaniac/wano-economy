package com.wanomaniac.economy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;

public class ServerTaskSchedulingUtil {
    public static void Schedule(MinecraftServer server, TickTask task){
        server.schedule(task);
    }
}
