package com.wanomaniac.economy.auctioning.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.wanomaniac.economy.OperatorCommandUtils;
import com.wanomaniac.economy.auctioning.client.AuctionUi;
import com.wanomaniac.economy.auctioning.server.HistoryMenuDetails;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class AuctionCommandRegister {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(
                literal("auction")
                        .then(literal("get-by-id")
                                .requires(OperatorCommandUtils::setCommandSourceStack)
                                .then(argument("target_uuid", UuidArgument.uuid())
                                        .executes(ctx -> {
                                            UUID targetUuid = UuidArgument.getUuid(ctx, "target_uuid");
                                            boolean didWork = AuctionUi.openSnapshot(ctx.getSource().getPlayerOrException(), targetUuid);
                                            if(!didWork){
                                                Objects.requireNonNull(ctx.getSource().getPlayer()).sendSystemMessage(Component.literal("No auction found."));
                                            }

                                            return 0;
                                        })
                                )
                        )
                        .then(literal("history")
                                .then(argument("page", IntegerArgumentType.integer())
                                    .executes((ctx) -> {
                                    int pageNumber = IntegerArgumentType.getInteger(ctx, "page");
                                    if(pageNumber == 0) pageNumber++;
                                    pageNumber--;

                                    AuctionUi.openHistoryMenu(ctx.getSource().getPlayer(), new HistoryMenuDetails(pageNumber));
                                    return 0;
                                }))
                                .executes((ctx) -> {
                                    AuctionUi.openHistoryMenu(ctx.getSource().getPlayer(), new HistoryMenuDetails(0));
                                    return 0;
                                }))
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
                                .executes((ctx) -> {
                                    if(!AuctionUi.joinAsBidder(ctx.getSource().getPlayer())){
                                        Objects.requireNonNull(ctx.getSource().getPlayer()).sendSystemMessage(Component.literal("No auction found."));
                                    }
                                    return 0;
                                })
                        )
        );

    }
}
