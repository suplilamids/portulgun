package net.portalgun.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.portalgun.PortalGunItem;

public class PortalGunClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PortalGunItem.screenOpener = stack -> MinecraftClient.getInstance().setScreen(new PortalGunScreen(stack));
    }
}
