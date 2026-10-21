package uz.xray;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import org.joml.Matrix4f;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class XrayMod implements ClientModInitializer {
    private static KeyBinding openKey, toggleKey;
    private static final int RADIUS = 20;
    private static final int MAX_ESP = 2000;

    private record Ore(BlockPos pos, float r, float g, float b) {}
    private static volatile List<Ore> ores = new ArrayList<>();
    private int tick = 0;

    @Override
    public void onInitializeClient() {
        XrayConfig cfg = XrayConfig.get();
        openKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("Xray sozlamalari", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "Xray"));
        toggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("Xray yoqish/o'chirish", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "Xray"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.wasPressed()) mc.setScreen(new XrayScreen());
            while (toggleKey.wasPressed()) toggle(mc);
            if (mc.player == null || mc.world == null) return;
            if (cfg.xray && ++tick % 20 == 0) {
                mc.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
                if (cfg.esp) scan(mc, cfg);
            }
        });

        WorldRenderEvents.LAST.register(ctx -> {
            List<Ore> list = ores;
            if (!cfg.xray || !cfg.esp || list.isEmpty()) return;
            Vec3d cam = ctx.camera().getPos();
            MatrixStack m = new MatrixStack();
            m.multiplyPositionMatrix(ctx.positionMatrix());
            m.translate(-cam.x, -cam.y, -cam.z);
            Matrix4f mat = m.peek().getPositionMatrix();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buf = Tessellator.getInstance()
                    .begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (Ore o : list) box(buf, mat, o);
            BufferRenderer.drawWithGlobalProgram(buf.end());
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        });

        // Pastdagi to'rtburchak (HUD)
        HudRenderCallback.EVENT.register((ctx, delta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden) return;
            // Wurst uslubidagi sarlavha (chap tepada)
            ctx.drawTextWithShadow(mc.textRenderer, "Xray Client v1.0", 4, 4, 0xFFFFFF);
            if (!cfg.xray) return;
            // Yoqilgan hacklar ro'yxati (o'ng tepada)
            String[] hacks = cfg.esp ? new String[]{"X-Ray", "ESP"} : new String[]{"X-Ray"};
            int sw = mc.getWindow().getScaledWidth();
            for (int i = 0; i < hacks.length; i++) {
                int tw = mc.textRenderer.getWidth(hacks[i]);
                ctx.drawTextWithShadow(mc.textRenderer, hacks[i], sw - tw - 4, 4 + i * 10, 0xFFFFFF);
            }
            String s = "XRAY: " + (cfg.auto ? "AUTO (rudalar)" : "CUSTOM")
                    + " | ESP: " + (cfg.esp ? "ON" : "OFF") + " | [X] sozlash";
            int w = mc.textRenderer.getWidth(s) + 10;
            int y = mc.getWindow().getScaledHeight() - 16;
            ctx.fill(2, y, 2 + w, y + 13, 0x90000000);
            ctx.drawTextWithShadow(mc.textRenderer, s, 7, y + 3, 0x55FF55);
        });
    }

    private static final int[][] EDGES = {{0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{0,4},{1,5},{2,6},{3,7}};

    private static void box(BufferBuilder b, Matrix4f m, Ore o) {
        float x0 = o.pos().getX(), y0 = o.pos().getY(), z0 = o.pos().getZ();
        float x1 = x0 + 1, y1 = y0 + 1, z1 = z0 + 1;
        float[][] c = {
            {x0,y0,z0},{x1,y0,z0},{x1,y0,z1},{x0,y0,z1},
            {x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1}};
        for (int[] e : EDGES) {
            b.vertex(m, c[e[0]][0], c[e[0]][1], c[e[0]][2]).color(o.r(), o.g(), o.b(), 1f);
            b.vertex(m, c[e[1]][0], c[e[1]][1], c[e[1]][2]).color(o.r(), o.g(), o.b(), 1f);
        }
    }

    public static void toggle(MinecraftClient mc) {
        XrayConfig cfg = XrayConfig.get();
        cfg.xray = !cfg.xray;
        if (!cfg.xray) {
            ores = new ArrayList<>();
            if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        cfg.save();
        reloadWorld(mc);
    }

    public static void reloadWorld(MinecraftClient mc) {
        if (mc.worldRenderer != null && mc.world != null) mc.worldRenderer.reload();
    }

    private static void scan(MinecraftClient mc, XrayConfig cfg) {
        List<Ore> found = new ArrayList<>();
        BlockPos c = mc.player.getBlockPos();
        BlockPos.Mutable p = new BlockPos.Mutable();
        for (int x = -RADIUS; x <= RADIUS; x++)
            for (int y = -RADIUS; y <= RADIUS; y++)
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    p.set(c.getX() + x, c.getY() + y, c.getZ() + z);
                    BlockState st = mc.world.getBlockState(p);
                    Block b = st.getBlock();
                    if (!XrayConfig.isOre(b) || !cfg.visible(b)) continue;
                    float[] col = color(XrayConfig.id(b));
                    found.add(new Ore(p.toImmutable(), col[0], col[1], col[2]));
                    if (found.size() >= MAX_ESP) { ores = found; return; }
                }
        ores = found;
    }

    private static float[] color(String id) {
        if (id.contains("diamond")) return new float[]{0.3f, 1f, 1f};
        if (id.contains("emerald")) return new float[]{0.2f, 1f, 0.3f};
        if (id.contains("gold"))    return new float[]{1f, 0.9f, 0.1f};
        if (id.contains("iron"))    return new float[]{0.9f, 0.7f, 0.55f};
        if (id.contains("redstone"))return new float[]{1f, 0.1f, 0.1f};
        if (id.contains("lapis"))   return new float[]{0.2f, 0.3f, 1f};
        if (id.contains("copper"))  return new float[]{1f, 0.5f, 0.2f};
        if (id.contains("coal"))    return new float[]{0.5f, 0.5f, 0.5f};
        if (id.contains("debris"))  return new float[]{0.6f, 0.3f, 0.1f};
        return new float[]{1f, 1f, 1f};
    }
}
