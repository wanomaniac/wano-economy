package com.reazip.economycraft.fabric;
import com.reazip.economycraft.fabric.trading.types.TradeMenuTypes;
import net.fabricmc.api.ModInitializer;
import com.reazip.economycraft.EconomyCraft;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;


public final class EconomyCraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EconomyCraft.registerEvents();
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.parse("economycraft:trade_menu"), TradeMenuTypes.TRADE_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.parse("economycraft:trade_menu_snapshot"), TradeMenuTypes.TRADE_SNAPSHOT_MENU);

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayer victim)) return;

            var src = damageSource.getEntity();
            if (src instanceof ServerPlayer killer) {
                EconomyCraft.getManager(victim.level().getServer()).handlePvpKill(victim, killer);
            }
        });
    }
}
