package net.portalgun.client;

import java.util.Random;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.portalgun.Net;
import net.portalgun.PortalGunItem;

public class PortalGunScreen extends Screen {
    private final ItemStack gun;
    private TextFieldWidget fx, fy, fz, fd;

    public PortalGunScreen(ItemStack gun) {
        super(Text.translatable("screen.portalgun.title"));
        this.gun = gun;
    }

    private TextFieldWidget field(int x, int y, String label, String value) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, 80, 20, Text.literal(label));
        f.setMaxLength(18);
        f.setText(value);
        f.setPlaceholder(Text.literal(label));
        addDrawableChild(f);
        return f;
    }

    @Override
    protected void init() {
        int cx = width / 2, y = height / 2 - 20;
        int yv = PortalGunItem.getY(gun);
        fx = field(cx - 130, y, "X", String.valueOf(PortalGunItem.getX(gun)));
        fy = field(cx - 42, y, "Y (auto)", yv == PortalGunItem.AUTO_Y ? "" : String.valueOf(yv));
        fz = field(cx + 46, y, "Z", String.valueOf(PortalGunItem.getZ(gun)));
        fd = field(cx - 40, y + 30, "D", String.valueOf(PortalGunItem.getD(gun)));

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.portalgun.random"),
                b -> fd.setText(String.valueOf(new Random().nextInt(1_000_000)))).dimensions(cx + 50, y + 30, 80, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.portalgun.save"), b -> save())
                .dimensions(cx - 50, y + 62, 100, 20).build());
    }

    private void save() {
        try {
            int x = Integer.parseInt(fx.getText().trim());
            int z = Integer.parseInt(fz.getText().trim());
            String ys = fy.getText().trim();
            int y = ys.isEmpty() ? PortalGunItem.AUTO_Y : Integer.parseInt(ys);
            long d = fd.getText().trim().isEmpty() ? 0 : Long.parseLong(fd.getText().trim());
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(x); buf.writeInt(y); buf.writeInt(z); buf.writeLong(d);
            ClientPlayNetworking.send(Net.SET, buf);
            close();
        } catch (NumberFormatException e) {
            // ignore invalid input; keep screen open
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 55, 0x55FF55);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("X   Y   Z   |   D = dimension number (0 = Overworld)"), width / 2, height / 2 - 40, 0xAAAAAA);
    }
}
