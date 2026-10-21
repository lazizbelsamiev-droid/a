package uz.xray;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

public class XrayScreen extends Screen {
    private static final int COLS = 3, ROWS = 6, BW = 120, BH = 20;
    private final XrayConfig cfg = XrayConfig.get();
    private TextFieldWidget search;
    private String filter = "";
    private int page = 0;

    public XrayScreen() { super(Text.literal("Xray sozlamalari")); }

    @Override
    protected void init() { rebuild(); }

    private void rebuild() {
        clearChildren();
        int cx = width / 2;
        int left = cx - (COLS * (BW + 4)) / 2;

        addDrawableChild(ButtonWidget.builder(label("Xray", cfg.xray), b -> {
            XrayMod.toggle(MinecraftClient.getInstance()); rebuild();
        }).dimensions(left, 10, BW, BH).build());

        addDrawableChild(ButtonWidget.builder(label("ESP", cfg.esp), b -> {
            cfg.esp = !cfg.esp; rebuild();
        }).dimensions(left + BW + 4, 10, BW, BH).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Rejim: " + (cfg.auto ? "AUTO (rudalar)" : "CUSTOM")), b -> {
            cfg.auto = !cfg.auto;
            if (!cfg.auto && cfg.blocks.isEmpty()) cfg.fillOres();
            rebuild();
        }).dimensions(left + 2 * (BW + 4), 10, BW, BH).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Hammasi (loydan gulgacha)"), b -> {
            cfg.auto = false;
            for (Block bl : Registries.BLOCK) if (bl != Blocks.AIR) cfg.blocks.add(XrayConfig.id(bl));
            rebuild();
        }).dimensions(left, 34, BW, BH).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Faqat rudalar (AUTO)"), b -> {
            cfg.auto = true; rebuild();
        }).dimensions(left + BW + 4, 34, BW, BH).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Tozalash"), b -> {
            cfg.auto = false; cfg.blocks.clear(); rebuild();
        }).dimensions(left + 2 * (BW + 4), 34, BW, BH).build());

        search = new TextFieldWidget(textRenderer, left, 58, COLS * (BW + 4) - 4, 18, Text.literal("Qidirish"));
        search.setPlaceholder(Text.literal("Blok qidirish..."));
        search.setText(filter);
        search.setChangedListener(s -> { filter = s.toLowerCase(); page = 0; rebuildKeepFocus(); });
        addDrawableChild(search);

        List<Block> all = Registries.BLOCK.stream()
                .filter(b -> b != Blocks.AIR && XrayConfig.id(b).contains(filter))
                .collect(Collectors.toList());
        int perPage = COLS * ROWS;
        int pages = Math.max(1, (all.size() + perPage - 1) / perPage);
        page = Math.min(page, pages - 1);

        for (int i = 0; i < perPage; i++) {
            int idx = page * perPage + i;
            if (idx >= all.size()) break;
            Block bl = all.get(idx);
            String id = XrayConfig.id(bl);
            boolean on = cfg.auto ? XrayConfig.isOre(bl) : cfg.blocks.contains(id);
            Text t = Text.literal(on ? "+ " : "- ").append(bl.getName())
                    .formatted(on ? Formatting.GREEN : Formatting.RED);
            ButtonWidget btn = ButtonWidget.builder(t, b -> {
                if (cfg.auto) { cfg.auto = false; if (cfg.blocks.isEmpty()) cfg.fillOres(); }
                if (!cfg.blocks.remove(id)) cfg.blocks.add(id);
                rebuild();
            }).dimensions(left + (i % COLS) * (BW + 4), 82 + (i / COLS) * 22, BW, BH).build();
            addDrawableChild(btn);
        }

        int ny = 82 + ROWS * 22 + 4;
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> { page = Math.max(0, page - 1); rebuild(); })
                .dimensions(cx - 70, ny, 30, BH).build());
        final int maxPage = pages - 1;
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> { page = Math.min(maxPage, page + 1); rebuild(); })
                .dimensions(cx + 40, ny, 30, BH).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Yopish"), b -> close())
                .dimensions(cx - 40, ny + 24, 80, BH).build());
        pageText = (page + 1) + " / " + pages;
        pageY = ny + 6;
    }

    private String pageText = "";
    private int pageY = 0;

    private void rebuildKeepFocus() {
        rebuild();
        setFocused(search);
        search.setFocused(true);
        search.setCursorToEnd(false);
    }

    private Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "ON" : "OFF")).formatted(on ? Formatting.GREEN : Formatting.RED);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, pageText, width / 2, pageY, 0xFFFFFF);
    }

    @Override
    public void removed() {
        cfg.save();
        XrayMod.reloadWorld(MinecraftClient.getInstance());
    }
}
