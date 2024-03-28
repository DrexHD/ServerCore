package me.wesley1808.servercore.common.config.legacy;

public class OptimizationConfig {
    public static final ConfigEntry<Boolean> REDUCE_SYNC_LOADS = new ConfigEntry<>(
            true, """
            (Default = true) Prevents many different lagspikes caused by loading chunks synchronously.
            This for example causes maps to only update loaded chunks, which depending on the viewdistance can be a smaller radius than vanilla."""
    );

    public static final ConfigEntry<Boolean> CACHE_TICKING_CHUNKS = new ConfigEntry<>(
            true, """
            (Default = true) Can significantly reduce the time spent on chunk iteration by caching ticking chunks every second.
            This is especially useful for servers with a high playercount and / or viewdistance.
            Note: The list of ticking chunks is only updated every second, rather than every tick (but that is very unlikely to matter)."""
    );

    public static final ConfigEntry<Boolean> FAST_BIOME_LOOKUPS = new ConfigEntry<>(
            false, """
            (Default = false) Can significantly reduce time spent on mobspawning, but isn't as accurate as vanilla on biome borders.
            This may cause mobs from another biome to spawn a few blocks across a biome border (this does not affect structure spawning!)."""
    );

    public static final ConfigEntry<Boolean> CANCEL_DUPLICATE_FLUID_TICKS = new ConfigEntry<>(
            false, """
            (Default = false) Fluid random ticks, like lava spreading fire, are run twice each game tick.
            Enabling this will cancel the 'duplicate' second fluid tick, but this may cause slight behavior changes."""
    );
}
