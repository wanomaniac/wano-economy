package com.wanomaniac.economy.neoforge;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgeEnvironmentInfo {
public static boolean isClient(){
    return FMLEnvironment.getDist().isClient();
}

public static boolean isDedicatedServer(){
    return FMLEnvironment.getDist().isDedicatedServer();
}

public static boolean isProduction(){
    return FMLLoader.getCurrent().isProduction();
}

public static boolean isDebug(){
    return !FMLLoader.getCurrent().isProduction();
}
}
