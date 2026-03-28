package com.reazip.economycraft.fabric.trading.server;

import com.reazip.economycraft.EconomyCraft;
import com.reazip.economycraft.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.reazip.economycraft.fabric.server.InitalizerFabricServer.TRADE_MANAGER;

/**
 * Represents a trading session between two players, allowing for the exchange of money
 * and items in a controlled manner. The session tracks each player's monetary inputs,
 * item offers, and readiness state before completing the trade.
 * This class is final and cannot be extended.
 */
public final class TradeSession {
    public final ServerPlayer a, b;
    public long aMoney, bMoney;
    public final SimpleContainer aOffer = new SimpleContainer(18);
    public final SimpleContainer bOffer = new SimpleContainer(18);

    public boolean aReady = false;
    public boolean bReady = false;

    public TradeMenu menuA;
    public TradeMenu menuB;

    boolean canceled = false;

    public TradeSession(ServerPlayer a, ServerPlayer b) {
        this.a = a;
        this.b = b;
        this.aMoney = 0;
        this.bMoney = 0;
    }

    public void setMoneyA(long amount) {
        aMoney = amount;
    }

    public void setMoneyB(long amount) {
        bMoney = amount;
    }

    public void cancel() {
        if(canceled) return;
        returnItems(a, aOffer);
        returnItems(b, bOffer);
        canceled = true;
        close();
    }

    public void close() {
        if (menuA != null) a.closeContainer();
        if (menuB != null) b.closeContainer();
    }

    private void returnItems(ServerPlayer p, SimpleContainer c) {
        ItemOutput(p, c);
    }

    private void ItemOutput(ServerPlayer p, SimpleContainer c) {
        for (int i = 0; i < c.getContainerSize(); i++) {
            ItemStack s = c.getItem(i);
            if (!s.isEmpty()) {
                if (!p.getInventory().add(s)) {
                    p.drop(s, false);
                }
            }
        }
        if(c.getContainerSize() > 1) c.clearContent();
    }

    public static SimpleContainer copyContainer(SimpleContainer original) {
        SimpleContainer copy = new SimpleContainer(original.getContainerSize());

        for (int i = 0; i < original.getContainerSize(); i++) {
            copy.setItem(i, original.getItem(i).copy());
        }

        return copy;
    }


    public void tryComplete() {
        if (!aReady || !bReady) return;
        // final trade!

        TRADE_MANAGER.snapshotSession(new TradeData(
                List.of(a.getUUID(), b.getUUID()),
                Map.of(
                        a.getUUID(), aMoney,
                        b.getUUID(), bMoney
                ),
                Map.of(
                        a.getUUID(), bMoney,
                        b.getUUID(), aMoney
                ),
                Map.of(
                        a.getUUID(), copyContainer(aOffer),
                        b.getUUID(), copyContainer(bOffer)
                )
        ));


        exchange(a, bOffer);
        exchange(b, aOffer);

        EconomyManager manager = EconomyCraft.getManager(a.getServer());
        handleMoneyTransfer(manager, a, b, aMoney); // A > B
        handleMoneyTransfer(manager, b, a, bMoney); // B > A

        a.sendSystemMessage(Component.literal("Trade complete!").withStyle(ChatFormatting.GREEN));
        b.sendSystemMessage(Component.literal("Trade complete!").withStyle(ChatFormatting.GREEN));
        a.level().playSound(
                null,
                a.getX(), a.getY(), a.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                1.0F,  // volume
                1.0F   // pitch
        );

        close();
    }

    private static void handleMoneyTransfer(
            EconomyManager manager,
            ServerPlayer giver,
            ServerPlayer receiver,
            long amount
    ) {
        if (amount <= 0) return;

        long giverBalance = manager.getBalance(giver.getUUID(), false);
        long receiverBalance = manager.getBalance(receiver.getUUID(), false);

        long giverNewBalance = giverBalance - amount;
        long receiverNewBalance = receiverBalance + amount;

        // update balances
        manager.setMoney(giver.getUUID(), giverNewBalance);
        manager.setMoney(receiver.getUUID(), receiverNewBalance);

        // messages
        giver.sendSystemMessage(
                Component.literal("You paid $" + amount + " to " + receiver.getName().getString()
                                + ". Your new balance is $" + giverNewBalance)
                        .withStyle(ChatFormatting.RED)
        );

        receiver.sendSystemMessage(
                Component.literal("You received $" + amount + " from " + giver.getName().getString()
                                + ". Your new balance is $" + receiverNewBalance)
                        .withStyle(ChatFormatting.GOLD)
        );
    }

    private void exchange(ServerPlayer p, SimpleContainer from) {
        ItemOutput(p, from);
    }
}

