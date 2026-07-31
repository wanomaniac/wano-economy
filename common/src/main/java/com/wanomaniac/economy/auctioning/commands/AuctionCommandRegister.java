package com.wanomaniac.economy.auctioning.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.wanomaniac.economy.auctioning.client.AuctionUi;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

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
        );

    }
}
