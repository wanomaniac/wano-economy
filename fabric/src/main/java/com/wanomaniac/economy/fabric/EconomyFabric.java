package com.wanomaniac.economy.fabric;
import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.EconomyCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;

public final class EconomyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonEconomy.onInitalize();
    }
}
