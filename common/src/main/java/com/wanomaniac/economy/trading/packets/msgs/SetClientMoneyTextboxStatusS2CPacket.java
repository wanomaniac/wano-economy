package com.wanomaniac.economy.trading.packets.msgs;
import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetClientMoneyTextboxStatusS2CPacket(boolean enabled) implements CustomPacketPayload {
    public static final Type<SetClientMoneyTextboxStatusS2CPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "set_client_money_textbox_status")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<FriendlyByteBuf, SetClientMoneyTextboxStatusS2CPacket> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeBoolean(payload.enabled),
                    buf -> new SetClientMoneyTextboxStatusS2CPacket(buf.readBoolean())
            );
}
