package me.wesley1808.servercore.mixin.optimizations.players;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Based on: Paper (Don-t-move-existing-players-to-world-spawn.patch)
 * <p>
 * This can cause a nasty server lag the spawn chunks are not kept loaded,
 * or they aren't finished loading yet, or if the world spawn radius is
 * larger than the keep loaded range.
 * <p>
 * By skipping this, we avoid potential for a large spike on server start.
 * <p>
 * Patch Author: Aikar (aikar@aikar.co)
 * <br>
 * License: GPL-3.0 (licenses/GPL.md)
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @WrapWithCondition(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;moveTo(Lnet/minecraft/core/BlockPos;FF)V"
            )
    )
    private boolean servercore$noop(ServerPlayer instance, BlockPos blockPos, float v, float w) {
        // Moves operation to PlayerList.
        return false;
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"
        )
    )
    private BlockPos servercore$noop(ServerPlayer instance, ServerLevel serverLevel, BlockPos blockPos) {
        return BlockPos.ZERO;
    }
}
