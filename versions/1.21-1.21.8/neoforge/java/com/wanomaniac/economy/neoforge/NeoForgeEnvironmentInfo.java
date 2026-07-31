package com.wanomaniac.economy.neoforge;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgeEnvironmentInfo {
public static boolean isClient(){
    return FMLEnvironment.dist.isClient();
}

public static boolean isDedicatedServer(){
    return FMLEnvironment.dist.isDedicatedServer();
}

public static boolean isProduction(){
    return FMLLoader.isProduction();
}

public static boolean isDebug(){
    return !FMLLoader.isProduction();
}
}
