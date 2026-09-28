package net.portalgun;

import java.util.Random;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

/** D = 0 is the normal Overworld. Any other D is its own generated overworld-style dimension with its own seed. */
public final class DimensionManager {
    private DimensionManager() {}

    public static ServerWorld get(MinecraftServer server, long d) {
        if (d == 0) return server.getOverworld();

        Identifier id = new Identifier("portalgun", "dim_" + d);
        long seed = new Random(d * 341873128712L ^ server.getOverworld().getSeed()).nextLong();

        var reg = server.getRegistryManager();
        var biomes = MultiNoiseBiomeSource.create(reg.get(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
                .getEntry(MultiNoiseBiomeSourceParameterLists.OVERWORLD).orElseThrow());
        var settings = reg.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS)
                .getEntry(ChunkGeneratorSettings.OVERWORLD).orElseThrow();

        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.OVERWORLD)
                .setGenerator(new NoiseChunkGenerator(biomes, settings))
                .setSeed(seed)
                .setShouldTickTime(true);

        return Fantasy.get(server).getOrOpenPersistentWorld(id, config).asWorld();
    }
}
