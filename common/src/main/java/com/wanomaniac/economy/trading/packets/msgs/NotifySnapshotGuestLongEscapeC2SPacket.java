package com.wanomaniac.economy.trading.packets.msgs;


import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Notifies the server that the player on a snapshot menu is doing a guest long escape, so no previous menus.
public record NotifySnapshotGuestLongEscapeC2SPacket(boolean state) implements CustomPacketPayload {
    public static final Type<NotifySnapshotGuestLongEscapeC2SPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID,"notify_guest_long")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<FriendlyByteBuf, NotifySnapshotGuestLongEscapeC2SPacket> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeBoolean(payload.state),
                    buf -> new NotifySnapshotGuestLongEscapeC2SPacket(buf.readBoolean())
            );
}

