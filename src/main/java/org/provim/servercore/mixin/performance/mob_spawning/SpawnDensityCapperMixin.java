package org.provim.servercore.mixin.performance.mob_spawning;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.SpawnDensityCapper;
import org.provim.servercore.config.tables.FeatureConfig;
import org.provim.servercore.interfaces.IThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(SpawnDensityCapper.class)
public abstract class SpawnDensityCapperMixin {

    @Shadow
    @Final
    private ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

    @Inject(method = "getMobSpawnablePlayers", at = @At("HEAD"), cancellable = true)
    private void getMobSpawnablePlayers(ChunkPos chunkPos, CallbackInfoReturnable<List<ServerPlayerEntity>> cir) {
        if (FeatureConfig.USE_DISTANCE_MAP.get()) {
            cir.setReturnValue(null); // Return nothing as we won't be using it.
        }
    }

    @Redirect(method = "canSpawn", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<ServerPlayerEntity> useDistanceMap$1(List<ServerPlayerEntity> list, SpawnGroup spawnGroup, ChunkPos chunkPos) {
        return this.getPlayerIterator(list, chunkPos);
    }

    @Redirect(method = "increaseDensity", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<ServerPlayerEntity> useDistanceMap$2(List<ServerPlayerEntity> list, ChunkPos chunkPos, SpawnGroup spawnGroup) {
        return this.getPlayerIterator(list, chunkPos);
    }

    private Iterator<ServerPlayerEntity> getPlayerIterator(List<ServerPlayerEntity> list, ChunkPos chunkPos) {
        return list == null ? ((IThreadedAnvilChunkStorage) this.threadedAnvilChunkStorage).getDistanceMap().getPlayersInRange(chunkPos).iterator() : list.listIterator();
    }
}