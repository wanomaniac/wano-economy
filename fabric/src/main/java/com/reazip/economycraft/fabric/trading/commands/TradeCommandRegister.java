package com.reazip.economycraft.fabric.trading.commands;
import com.mojang.brigadier.CommandDispatcher;
import com.reazip.economycraft.fabric.trading.client.TradeUi;
import com.reazip.economycraft.fabric.trading.server.TradeData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;
import static com.reazip.economycraft.fabric.server.InitalizerFabricServer.TRADE_MANAGER;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TradeCommandRegister {
    private static int getLastSnapshot(ServerPlayer player){
        TradeData lastSnapshot = TRADE_MANAGER.history.getLast();
        if(lastSnapshot != null){
            player.sendSystemMessage(Component.literal("Opening last trade snapshot "+lastSnapshot.getId().toString()));
            TradeUi.openSnapshot(player, lastSnapshot);
            return 1;
        } else {
            return 0;
        }

    }

    private static int acceptLatest(ServerPlayer receiver) {
        var pending = TRADE_MANAGER.getPendingFor(receiver.getUUID());
        if (pending.isEmpty()) {
            receiver.sendSystemMessage(Component.literal("No pending requests."));
            return 0;
        }
        UUID first = pending.iterator().next();
        ServerPlayer sender = TRADE_MANAGER.getPlayer(first);
        if (sender == null) return 0;

        return acceptSpecific(receiver, sender);
    }

    private static int acceptSpecific(ServerPlayer receiver, ServerPlayer sender) {
        if (!TRADE_MANAGER.hasRequestFrom(receiver.getUUID(), sender.getUUID())) {
            receiver.sendSystemMessage(Component.literal("No request from this player."));
            return 0;
        }

        TRADE_MANAGER.accept(receiver.getUUID(), sender.getUUID());

        receiver.sendSystemMessage(Component.literal("Accepted trade request from " + sender.getName().getString()));
        sender.sendSystemMessage(Component.literal(receiver.getName().getString() + " accepted your trade request."));

        TradeUi.open(receiver, sender);

        return 1;
    }

    private static int rejectLatest(ServerPlayer receiver) {
        var pending = TRADE_MANAGER.getPendingFor(receiver.getUUID());
        if (pending.isEmpty()) {
            receiver.sendSystemMessage(Component.literal("No pending requests."));
            return 0;
        }
        UUID first = pending.iterator().next();
        ServerPlayer sender = TRADE_MANAGER.getPlayer(first);
        if (sender == null) return 0;

        return rejectSpecific(receiver, sender);
    }

    private static int rejectSpecific(ServerPlayer receiver, ServerPlayer sender) {
        if (!TRADE_MANAGER.reject(receiver.getUUID(), sender.getUUID())) {
            receiver.sendSystemMessage(Component.literal("No request from " + sender.getName().getString()));
            return 0;
        }

        receiver.sendSystemMessage(Component.literal("Rejected trade request from " + sender.getName().getString()));
        sender.sendSystemMessage(Component.literal(receiver.getName().getString() + " rejected your trade request."));

        return 1;
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(
                literal("trade")
                        // /trade request <player>
                        .then(literal("request")
                                .then(argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer sender = ctx.getSource().getPlayerOrException();
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");

                                            if (sender.equals(target)) {
                                                sender.sendSystemMessage(Component.literal("You cannot trade yourself."));
                                                return 0;
                                            }

                                            TRADE_MANAGER.requestTrade(sender, target);

                                            sender.sendSystemMessage(Component.literal("Trade request sent to " + target.getName().getString()));
                                            target.sendSystemMessage(Component.literal(sender.getName().getString() + " has requested to trade with you."));
                                            target.sendSystemMessage(Component.literal("Run /trade accept " + sender.getName().getString() + " or /trade reject " + sender.getName().getString()));

                                            return 1;
                                        })
                                )
                        )

                        .then(literal("getLastSnapshot")
                                        .executes(ctx -> getLastSnapshot(ctx.getSource().getPlayerOrException()))
                        )
                        // /trade accept [player]
                        .then(literal("accept")
                                .executes(ctx -> acceptLatest(ctx.getSource().getPlayerOrException()))
                                .then(argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer receiver = ctx.getSource().getPlayerOrException();
                                            ServerPlayer sender = EntityArgument.getPlayer(ctx, "player");
                                            return acceptSpecific(receiver, sender);
                                        })
                                )
                        )

                        // /trade reject [player]
                        .then(literal("reject")
                                .executes(ctx -> rejectLatest(ctx.getSource().getPlayerOrException()))
                                .then(argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer receiver = ctx.getSource().getPlayerOrException();
                                            ServerPlayer sender = EntityArgument.getPlayer(ctx, "player");
                                            return rejectSpecific(receiver, sender);
                                        })
                                )
                        )

                        // /trade pending
                        .then(literal("pending")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    var set = TRADE_MANAGER.getPendingFor(player.getUUID());
                                    if (set.isEmpty()) {
                                        player.sendSystemMessage(Component.literal("No pending trade requests."));
                                    } else {
                                        player.sendSystemMessage(Component.literal("Pending requests:"));
                                        for (UUID id : set) {
                                            ServerPlayer sp = TRADE_MANAGER.getPlayer(id);
                                            if (sp != null)
                                                player.sendSystemMessage(Component.literal("- " + sp.getName().getString()));
                                        }
                                    }
                                    return 1;
                                })
                        )

                // /trade history
//                            .then(literal("history")
//                                    .executes(ctx -> {
//                                        ServerPlayer player = ctx.getSource().getPlayerOrException();
//                                        var list = TRADE_MANAGER.getHistory(player.getUUID());
//
//                                        player.sendSystemMessage(Component.literal("Your last trades:"));
//                                        for (String entry : list) {
//                                            player.sendSystemMessage(Component.literal(entry));
//                                        }
//                                        return 1;
//                                    })
//                            )
        );
    }
}
