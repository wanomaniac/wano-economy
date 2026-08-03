package com.wanomaniac.economy.trading.packets;
import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.trading.packets.msgs.*;

public class TradePacketsCommon {
    public static void register(){
        // Client -> Server
        CommonEconomy.packets.registerC2SPayload(
                UpdateExtraMoneyPayloadC2SPacket.TYPE,
                UpdateExtraMoneyPayloadC2SPacket.CODEC
        );
        CommonEconomy.packets.registerC2SPayload(
                TradeRequestPlayerBalanceC2SPacket.TYPE,
                TradeRequestPlayerBalanceC2SPacket.CODEC
        );
        CommonEconomy.packets.registerC2SPayload(
                com.wanomaniac.economy.trading.packets.msgs.NotifySnapshotGuestLongEscapeC2SPacket.TYPE,
                com.wanomaniac.economy.trading.packets.msgs.NotifySnapshotGuestLongEscapeC2SPacket.CODEC
        );
        // Server -> Client
        CommonEconomy.packets.registerS2CPayload(
                TradeSendPlayerBalanceS2CPacket.TYPE,
                TradeSendPlayerBalanceS2CPacket.CODEC
        );
        CommonEconomy.packets.registerS2CPayload(
                SyncExtraMoneyPayloadS2CPacket.TYPE,
                SyncExtraMoneyPayloadS2CPacket.CODEC
        );
        CommonEconomy.packets.registerS2CPayload(
                SetClientMoneyTextboxStatusS2CPacket.TYPE,
                SetClientMoneyTextboxStatusS2CPacket.CODEC
        );
    }
}
