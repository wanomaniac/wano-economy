package com.wanomaniac.economy.fabric.interfaces;
import com.wanomaniac.economy.interfaces.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class PlatformHelperFabric implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "fabric";
    }

    @Override
    public String getPlatformVersion(){
        return FabricLoader.getInstance()
                .getModContainer("fabricloader")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public Path getConfigurationDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
