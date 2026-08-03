package com.wanomaniac.economy.auctioning.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.wanomaniac.economy.auctioning.client.AuctionUi;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class AuctionCommandRegister {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(
                literal("auction")
                        .then(literal("start")
                                .executes((ctx) -> {
                                    AuctionUi.openForAuctioneer(ctx.getSource().getPlayer());
                                    return 0;
                            }))
                        .then(literal("join")
                                .then(argument("username", EntityArgument.player())
                                        .executes((ctx) -> {
                                            ServerPlayer auctioneer = EntityArgument.getPlayer(ctx, "username");

                                            if(!AuctionUi.joinAsBidder(ctx.getSource().getPlayer(), auctioneer.getUUID())){
                                                Objects.requireNonNull(ctx.getSource().getPlayer()).sendSystemMessage(Component.literal("No auction found."));
                                            }
                                            return 0;
                                        }))
                        )
        );

    }
}
