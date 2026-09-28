package net.portalgun.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.portalgun.Net;
import net.portalgun.PortalGunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow public ClientPlayerEntity player;

    /** Left click (even into empty air) with the portal gun = fire a portal. */
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void portalgun$attack(CallbackInfoReturnable<Boolean> cir) {
        if (player != null && player.getMainHandStack().getItem() instanceof PortalGunItem) {
            ClientPlayNetworking.send(Net.FIRE, PacketByteBufs.create());
            cir.setReturnValue(false);
        }
    }
}
