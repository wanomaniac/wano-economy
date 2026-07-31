package com.wanomaniac.economy.trading.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record SyncExtraMoneyPayloadS2CPacket(UUID player, long amount) implements CustomPacketPayload {

    public static final Type<SyncExtraMoneyPayloadS2CPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "sync_extra_money")));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncExtraMoneyPayloadS2CPacket> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeUUID(payload.player);
                        buf.writeLong(payload.amount);
                    },
                    buf -> new SyncExtraMoneyPayloadS2CPacket(buf.readUUID(), buf.readLong())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
