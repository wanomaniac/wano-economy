package com.wanomaniac.economy.neoforge.interfaces;
import com.wanomaniac.economy.interfaces.IPlatformHelper;
import com.wanomaniac.economy.neoforge.NeoForgeEnvironmentInfo;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class PlatformHelperNeoForge implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "com/wanomaniac/economy/neoforge";
    }

    @Override
    public String getPlatformVersion(){
        return ModList.get()
                .getModContainerById("com/wanomaniac/economy/neoforge")
                .map(mod -> mod.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public Path getConfigurationDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !NeoForgeEnvironmentInfo.isProduction();
    }
}
