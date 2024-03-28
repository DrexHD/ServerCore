package me.wesley1808.servercore.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.wesley1808.servercore.common.config.legacy.CommandConfig;
import me.wesley1808.servercore.common.dynamic.DynamicManager;
import me.wesley1808.servercore.common.services.Formatter;
import me.wesley1808.servercore.common.services.Permission;
import me.wesley1808.servercore.common.utils.Statistics;
import me.wesley1808.servercore.common.utils.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;

public class StatisticsCommand {
    private static final String[] SUB_COMMANDS = {
            "entities",
            "block-entities"
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var statistics = literal("statistics").requires(Permission.require("command.statistics", 2));
        statistics.executes(ctx -> displayOverview(ctx.getSource()));

        for (int i = 0; i < SUB_COMMANDS.length; i++) {
            final int index = i;

            statistics.then(literal(SUB_COMMANDS[index])
                    .executes(ctx -> display(ctx, index, 1))
                    .then(argument("page", integer(1))
                            .executes(ctx -> display(ctx, index, getInteger(ctx, "page")))
                    )

                    .then(literal("byType")
                            .executes(ctx -> display(ctx, index, false, 1))
                            .then(argument("page", integer(1))
                                    .executes(ctx -> display(ctx, index, false, getInteger(ctx, "page")))
                            )

                            .then(argument("player", player())
                                    .executes(ctx -> display(ctx, index, 1, getPlayer(ctx, "player")))
                                    .then(argument("page", integer(1))
                                            .executes(ctx -> display(ctx, index, getInteger(ctx, "page"), getPlayer(ctx, "player")))
                                    )
                            )
                    )

                    .then(literal("byPlayer")
                            .executes(ctx -> display(ctx, index, true, 1))
                            .then(argument("page", integer(1))
                                    .executes(ctx -> display(ctx, index, true, getInteger(ctx, "page")))
                            )
                    )
            );
        }

        dispatcher.register(statistics);
    }

    private static int display(CommandContext<CommandSourceStack> context, int index, int page) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            return display(context, index, false, page, player);
        } else {
            return display(context, index, false, page);
        }
    }

    private static int display(CommandContext<CommandSourceStack> context, int index, boolean byPlayer, int page) {
        return display(context, index, byPlayer, page, null);
    }

    private static int display(CommandContext<CommandSourceStack> context, int index, int page, @Nullable ServerPlayer player) {
        return display(context, index, false, page, player);
    }

    private static int display(CommandContext<CommandSourceStack> context, int index, boolean byPlayer, int page, @Nullable ServerPlayer player) {
        return switch (index) {
            case 0 -> displayEntities(context, byPlayer, page, player);
            case 1 -> displayBlockEntities(context, byPlayer, page, player);
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }

    private static int displayOverview(CommandSourceStack source) {
        final double mspt = DynamicManager.getInstance(source.getServer()).getAverageTickTime();
        final double tps = mspt != 0 ? Math.min((1000 / mspt), 20) : 20;

        Statistics statistics = Statistics.getInstance(source.getServer());
        source.sendSuccess(() -> Formatter.parse(Formatter.line(CommandConfig.STATS_TITLE.get(), 40, source.isPlayer()) + "\n" + CommandConfig.STATS_CONTENT.get()
                .replace("${tps}", String.format("%.2f", tps))
                .replace("${mspt}", String.format("%.2f", mspt))
                .replace("${chunk_count}", String.valueOf(statistics.getChunkCount(true)))
                .replace("${entity_count}", String.valueOf(statistics.getAllEntities().size()))
                .replace("${block_entity_count}", String.valueOf(statistics.getAllBlockEntities().size()))
        ), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int displayEntities(CommandContext<CommandSourceStack> context, boolean byPlayer, int page, @Nullable ServerPlayer player) {
        MinecraftServer server = context.getSource().getServer();
        Statistics statistics = Statistics.getInstance(server);
        Map<String, Integer> map;
        if (byPlayer) {
            map = statistics.getEntitiesByPlayer(server.getPlayerList().getPlayers());
        } else {
            map = statistics.getEntitiesByType(player == null ? statistics.getAllEntities() : statistics.getEntitiesNear(player));
        }

        displayFeedback(context, map, false, byPlayer, page, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int displayBlockEntities(CommandContext<CommandSourceStack> context, boolean byPlayer, int page, @Nullable ServerPlayer player) {
        MinecraftServer server = context.getSource().getServer();
        Statistics statistics = Statistics.getInstance(server);
        Map<String, Integer> map;
        if (byPlayer) {
            map = statistics.getBlockEntitiesByPlayer(server.getPlayerList().getPlayers());
        } else {
            map = statistics.getBlockEntitiesByType(player == null ? statistics.getAllBlockEntities() : statistics.getBlockEntitiesNear(player));
        }

        displayFeedback(context, map, true, byPlayer, page, player);
        return Command.SINGLE_SUCCESS;
    }

    private static void displayFeedback(CommandContext<CommandSourceStack> context, Map<String, Integer> map, boolean isBlockEntity, boolean byPlayer, int page, @Nullable ServerPlayer player) {
        CommandSourceStack source = context.getSource();

        StringBuilder builder = new StringBuilder(createHeader(isBlockEntity, byPlayer, source.isPlayer(), player));
        boolean success = Util.iteratePage(Util.sortByValue(map), page, 8, (entry, index) -> builder.append(createEntry(entry, index, isBlockEntity, byPlayer)));

        if (success) {
            builder.append("\n").append(createFooter(page, Util.getPage(map.size(), 8), isBlockEntity, context));
            source.sendSuccess(() -> Formatter.parse(builder.toString()), false);
        } else if (page == 1) {
            source.sendFailure(Component.literal(isBlockEntity ? "No block entities were found!" : "No entities were found!"));
        } else {
            source.sendFailure(Component.literal("Page doesn't exist!"));
        }
    }

    private static String createEntry(Map.Entry<String, Integer> entry, int index, boolean isBlockEntity, boolean byPlayer) {
        String string = "\n" + CommandConfig.STATS_PAGE_CONTENT.get()
                .replace("${name}", entry.getKey())
                .replace("${index}", String.valueOf(index))
                .replace("${count}", String.valueOf(entry.getValue()));

        if (byPlayer) {
            string = Formatter.command(String.format("statistics %s byType %s", isBlockEntity ? "block-entities" : "entities", entry.getKey()), string);
        }

        return string;
    }

    private static String createHeader(boolean isBlockEntity, boolean byPlayer, boolean isPlayer, ServerPlayer player) {
        String title = player == null
                ? CommandConfig.STATS_PAGE_TITLE.get().replace("${type}", byPlayer ? "Player" : "Type")
                : CommandConfig.STATS_PAGE_TITLE_PLAYER.get().replace("${player}", player.getScoreboardName());

        return Formatter.line(title.replace("${title}", isBlockEntity ? "Block Entities" : "Entities"), 40, isPlayer);
    }

    private static String createFooter(int page, int pageCount, boolean isBlockEntity, CommandContext<CommandSourceStack> context) {
        String title = CommandConfig.STATS_PAGE_FOOTER.get()
                .replace("${page}", String.valueOf(page))
                .replace("${page_count}", String.valueOf(pageCount));

        String command = context.getInput().replaceAll("\\b\\d+\\b", "%page_nr%");
        if (!command.contains("%page_nr%")) {
            command += " %page_nr%";
        }

        return Formatter.line(Formatter.page(title, command, page), isBlockEntity ? 40 : 38, context.getSource().isPlayer());
    }
}
