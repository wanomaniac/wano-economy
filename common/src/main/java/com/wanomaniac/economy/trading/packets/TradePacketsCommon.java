package com.wanomaniac.economy.trading.packets;
import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.trading.packets.msgs.RequestPlayerBalanceC2SPacket;
import com.wanomaniac.economy.trading.packets.msgs.SetClientMoneyTextboxStatusS2CPacket;
import com.wanomaniac.economy.trading.packets.msgs.SyncExtraMoneyPayloadS2CPacket;
import com.wanomaniac.economy.trading.packets.msgs.UpdateExtraMoneyPayloadC2SPacket;

public class TradePacketsCommon {
    public static void register(){
        // Client -> Server
        CommonEconomy.packets.registerC2SPayload(
                UpdateExtraMoneyPayloadC2SPacket.TYPE,
                UpdateExtraMoneyPayloadC2SPacket.CODEC
        );
        CommonEconomy.packets.registerC2SPayload(
                RequestPlayerBalanceC2SPacket.TYPE,
                RequestPlayerBalanceC2SPacket.CODEC
        );
        CommonEconomy.packets.registerC2SPayload(
                com.wanomaniac.economy.trading.packets.msgs.NotifySnapshotGuestLongEscapeC2SPacket.TYPE,
                com.wanomaniac.economy.trading.packets.msgs.NotifySnapshotGuestLongEscapeC2SPacket.CODEC
        );
        // Server -> Client
        CommonEconomy.packets.registerS2CPayload(
                com.wanomaniac.economy.trading.packets.msgs.SendPlayerBalanceS2CPacket.TYPE,
                com.wanomaniac.economy.trading.packets.msgs.SendPlayerBalanceS2CPacket.CODEC
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
