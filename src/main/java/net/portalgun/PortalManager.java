package net.portalgun;

import java.util.*;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.joml.Vector3f;

public final class PortalManager {
    private static final int LIFETIME = 20 * 20;
    private static final List<Portal> PORTALS = new ArrayList<>();
    private static final Map<UUID, Integer> COOLDOWN = new HashMap<>();
    private static final DustParticleEffect GREEN = new DustParticleEffect(new Vector3f(0.2f, 1f, 0.2f), 1.2f);

    private static final class Portal {
        ServerWorld world; Vec3d center, axis, landing; // axis = horizontal "right" direction of the ring
        Portal target; int ticks = LIFETIME;
    }

    private PortalManager() {}

    public static void open(ServerPlayerEntity player) {
        ItemStack gun = player.getMainHandStack();
        if (!(gun.getItem() instanceof PortalGunItem)) return;
        if (player.getItemCooldownManager().isCoolingDown(gun.getItem())) return;
        player.getItemCooldownManager().set(gun.getItem(), 40);

        MinecraftServer server = player.getServer();
        ServerWorld destWorld = DimensionManager.get(server, PortalGunItem.getD(gun));
        int dx = PortalGunItem.getX(gun), dz = PortalGunItem.getZ(gun), dy = PortalGunItem.getY(gun);
        if (dy == PortalGunItem.AUTO_Y) dy = destWorld.getTopY(Heightmap.Type.MOTION_BLOCKING, dx, dz);

        double yaw = Math.toRadians(player.getYaw());
        Vec3d fwd = new Vec3d(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3d axis = new Vec3d(Math.cos(yaw), 0, Math.sin(yaw));

        Portal a = new Portal(), b = new Portal();
        a.world = (ServerWorld) player.getWorld();
        a.center = player.getPos().add(fwd.multiply(2.5)).add(0, 1.1, 0);
        a.axis = axis; a.landing = a.center.add(fwd.multiply(2)).add(0, -1.1, 0);
        b.world = destWorld;
        b.center = new Vec3d(dx + 0.5, dy + 1.1, dz + 0.5);
        b.axis = axis; b.landing = b.center.add(fwd.multiply(2)).add(0, -1.1, 0);
        a.target = b; b.target = a;
        PORTALS.add(a); PORTALS.add(b);
        a.world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 0.5f, 1.6f);
    }

    public static void tick(MinecraftServer server) {
        int now = server.getTicks();
        Iterator<Portal> it = PORTALS.iterator();
        while (it.hasNext()) {
            Portal p = it.next();
            if (--p.ticks <= 0) { it.remove(); continue; }
            for (int i = 0; i < 14; i++) { // ring particles
                double t = Math.random() * Math.PI * 2;
                Vec3d pos = p.center.add(p.axis.multiply(Math.cos(t) * 0.9)).add(0, Math.sin(t) * 1.1, 0);
                p.world.spawnParticles(GREEN, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            }
            for (ServerPlayerEntity pl : p.world.getPlayers()) {
                if (COOLDOWN.getOrDefault(pl.getUuid(), 0) > now) continue;
                Vec3d rel = pl.getPos().add(0, 1, 0).subtract(p.center);
                if (Math.abs(rel.x) < 0.9 && Math.abs(rel.z) < 0.9 && Math.abs(rel.y) < 1.2) {
                    Portal t = p.target;
                    pl.teleport(t.world, t.landing.x, t.landing.y, t.landing.z, pl.getYaw(), pl.getPitch());
                    COOLDOWN.put(pl.getUuid(), now + 40);
                    t.world.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.8f);
                }
            }
        }
        COOLDOWN.values().removeIf(v -> v < now - 100);
    }

    public static void clear() { PORTALS.clear(); COOLDOWN.clear(); }
}
