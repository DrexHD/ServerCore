package me.wesley1808.servercore.common.utils;

import me.wesley1808.servercore.common.config.legacy.EntityLimitConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.Set;
import java.util.function.IntSupplier;

public enum BreedingCap {
    ANIMAL(EntityLimitConfig.ANIMAL_COUNT::get, EntityLimitConfig.ANIMAL_RANGE::get),
    VILLAGER(EntityLimitConfig.VILLAGER_COUNT::get, EntityLimitConfig.VILLAGER_RANGE::get);

    private static final Map<EntityType<?>, Set<EntityType<?>>> CUSTOM_TYPES = Map.of(
            EntityType.FROG, Set.of(EntityType.TADPOLE, EntityType.FROG)
    );

    private final IntSupplier limit;
    private final IntSupplier range;

    BreedingCap(IntSupplier limit, IntSupplier range) {
        this.limit = limit;
        this.range = range;
    }

    public static void resetLove(Animal owner, Animal mate) {
        resetAge(owner, mate);
        owner.resetLove();
        mate.resetLove();
    }

    public static void resetAge(AgeableMob owner, AgeableMob mate) {
        owner.setAge(6000);
        mate.setAge(6000);
    }

    public boolean exceedsLimit(EntityType<?> type, Level level, BlockPos pos) {
        if (!EntityLimitConfig.ENABLED.get() || this.limit.getAsInt() < 0) {
            return false;
        }

        AABB area = this.getAreaAt(pos);
        Set<EntityType<?>> set = CUSTOM_TYPES.get(type);

        int count;
        if (set != null && !set.isEmpty()) {
            count = level.getEntities((Entity) null, area, (entity) -> set.contains(entity.getType())).size();
        } else {
            count = level.getEntities(type, area, EntitySelector.NO_SPECTATORS).size();
        }

        return this.limit.getAsInt() <= count;
    }

    public boolean exceedsLimit(Entity entity) {
        return this.exceedsLimit(entity.getType(), entity.level(), entity.blockPosition());
    }

    private AABB getAreaAt(BlockPos pos) {
        int range = this.range.getAsInt();
        return AABB.encapsulatingFullBlocks(pos.offset(range, range, range), pos.offset(-range, -range, -range));
    }
}
