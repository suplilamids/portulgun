package net.portalgun;

import java.util.function.Consumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class PortalGunItem extends Item {
    public static final int AUTO_Y = Integer.MIN_VALUE;
    /** Set by the client entrypoint; opens the coordinate screen. */
    public static Consumer<ItemStack> screenOpener = s -> {};

    public PortalGunItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) screenOpener.accept(stack);   // right click -> GUI
        return TypedActionResult.success(stack, world.isClient);
    }

    public static void setDest(ItemStack s, int x, int y, int z, long d) {
        NbtCompound n = s.getOrCreateNbt();
        n.putInt("X", x); n.putInt("Y", y); n.putInt("Z", z); n.putLong("D", d);
    }
    public static int getX(ItemStack s) { return s.hasNbt() ? s.getNbt().getInt("X") : 0; }
    public static int getY(ItemStack s) { return s.hasNbt() && s.getNbt().contains("Y") ? s.getNbt().getInt("Y") : AUTO_Y; }
    public static int getZ(ItemStack s) { return s.hasNbt() ? s.getNbt().getInt("Z") : 0; }
    public static long getD(ItemStack s) { return s.hasNbt() ? s.getNbt().getLong("D") : 0L; }
}
