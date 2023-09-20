package me.wesley1808.servercore.mixin.optimizations.ticking.chunk.random;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LiquidBlock.class, priority = 900)
public class LiquidBlockMixin {

    /**
     * Liquid blocks run their {@link FluidState#randomTick} twice each tick.
     * <p>
     * Once in {@link LiquidBlock#randomTick} and once in {@link ServerLevel#tickChunk}.
     * <p>
     * This patch gets rid of the second 'duplicate' random tick.
     */
    @Redirect(
            method = "isRandomlyTicking",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;isRandomlyTicking()Z"
            )
    )
    private boolean servercore$cancelDuplicateFluidTicks(FluidState fluidState) {
        return false;
    }
}
