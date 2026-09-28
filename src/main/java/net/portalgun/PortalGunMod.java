package net.portalgun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PortalGunMod implements ModInitializer {
    public static final PortalGunItem PORTAL_GUN = Registry.register(Registries.ITEM,
            new Identifier("portalgun", "portal_gun"), new PortalGunItem(new FabricItemSettings().maxCount(1)));

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(e -> e.add(PORTAL_GUN));

        // GUI -> server: save X Y Z D onto the gun in the main hand
        ServerPlayNetworking.registerGlobalReceiver(Net.SET, (server, player, handler, buf, sender) -> {
            int x = buf.readInt(), y = buf.readInt(), z = buf.readInt();
            long d = buf.readLong();
            server.execute(() -> {
                ItemStack st = player.getMainHandStack();
                if (st.getItem() instanceof PortalGunItem) {
                    PortalGunItem.setDest(st, x, y, z, d);
                    player.sendMessage(Text.translatable("message.portalgun.set"), true);
                }
            });
        });
        // left click -> open portal
        ServerPlayNetworking.registerGlobalReceiver(Net.FIRE, (server, player, handler, buf, sender) ->
                server.execute(() -> PortalManager.open(player)));

        ServerTickEvents.END_SERVER_TICK.register(PortalManager::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> PortalManager.clear());
    }
}
