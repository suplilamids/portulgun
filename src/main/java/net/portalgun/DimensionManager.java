package net.portalgun;

import java.util.Random;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
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

        // Copy biome source + terrain settings from the normal Overworld; the different seed makes a different world.
        ChunkGenerator base = server.getOverworld().getChunkManager().getChunkGenerator();
        ChunkGenerator generator = base;
        if (base instanceof NoiseChunkGenerator noise) {
            generator = new NoiseChunkGenerator(noise.getBiomeSource(), noise.getSettings());
        }

        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.OVERWORLD)
                .setGenerator(generator)
                .setSeed(seed)
                .setShouldTickTime(true);

        return Fantasy.get(server).getOrOpenPersistentWorld(id, config).asWorld();
    }
}
