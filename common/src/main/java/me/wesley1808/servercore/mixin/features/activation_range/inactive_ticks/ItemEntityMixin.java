package me.wesley1808.servercore.mixin.features.activation_range.inactive_ticks;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Based on: Spigot (Entity-Activation-Range.patch)
 * <p>
 * Patch Author: Aikar (aikar@aikar.co)
 * <br>
 * License: GPL-3.0 (licenses/GPL.md)
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow
    @Final
    private static int INFINITE_PICKUP_DELAY;

    @Shadow
    @Final
    private static int INFINITE_LIFETIME;

    @Shadow
    @Final
    private static int LIFETIME;

    @Shadow
    private int pickupDelay;

    @Shadow
    private int age;

    private ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void servercore$inactiveTick() {
        super.servercore$inactiveTick();

        if (this.pickupDelay > 0 && this.pickupDelay != INFINITE_PICKUP_DELAY) {
            this.pickupDelay--;
        }

        if (this.age != INFINITE_LIFETIME) {
            this.age++;
        }

        if (this.age >= LIFETIME) {
            this.discard();
        }
    }
}
