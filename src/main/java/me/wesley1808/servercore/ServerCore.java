package me.wesley1808.servercore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import me.wesley1808.servercore.commands.MobcapsCommand;
import me.wesley1808.servercore.commands.ServerCoreCommand;
import me.wesley1808.servercore.commands.StatisticsCommand;
import me.wesley1808.servercore.config.Config;
import me.wesley1808.servercore.utils.TickManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.Optional;

public final class ServerCore implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static MinecraftServer server;
    private static String version;

    public static String getVersion() {
        return version;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[ServerCore] Initializing...");
        Config.load();

        ServerCore.version = this.findVersion();
        this.registerEvents();
    }

    private String findVersion() {
        Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer("servercore");
        return optional.map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("Unknown");
    }

    private void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onShutdown);
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
    }

    private void onTick(MinecraftServer server) {
        TickManager.update(server);
    }

    private void onServerStarted(MinecraftServer server) {
        ServerCore.server = server;
        TickManager.initValues(server.getPlayerList());
    }

    private void onShutdown(MinecraftServer server) {
        Config.save();
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        ServerCoreCommand.register(dispatcher);
        StatisticsCommand.register(dispatcher);
        MobcapsCommand.register(dispatcher);
    }
}
