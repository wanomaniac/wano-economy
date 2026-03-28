package com.reazip.economycraft.fabric.trading.packets;
import com.reazip.economycraft.fabric.trading.packets.msgs.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class TradePacketsCommon {
    public static void register(){
        // Client -> Server
        PayloadTypeRegistry.playC2S().register(
                UpdateExtraMoneyPayloadC2SP.TYPE,
                UpdateExtraMoneyPayloadC2SP.CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                RequestPlayerBalanceC2SPacket.TYPE,
                RequestPlayerBalanceC2SPacket.CODEC
        );
        // Server -> Client
        PayloadTypeRegistry.playS2C().register(
                SendPlayerBalanceS2CPacket.TYPE,
                SendPlayerBalanceS2CPacket.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                SyncExtraMoneyPayloadS2CP.TYPE,
                SyncExtraMoneyPayloadS2CP.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                SetClientMoneyTextboxStatusS2CP.TYPE,
                SetClientMoneyTextboxStatusS2CP.CODEC
        );

    }
}
