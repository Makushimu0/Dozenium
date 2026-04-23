package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class DozenalExperienceLevelMixin {
    @Redirect(
      method = "renderMainHud",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/gui/hud/bar/Bar;drawExperienceLevel(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;I)V"
      )
    )
    private void redirectDrawXpLevel(DrawContext context, TextRenderer textRenderer, int level) {
        String dozen = java.util.stream.IntStream.of(level)
            .mapToObj(name.modid.util.Dozenal::toDozenal)
            .findFirst()
            .orElse("0");
        // можно скопировать код оригинального drawExperienceLevel, но с dozen-строкой:
        Text text = net.minecraft.text.Text.translatable("gui.experience.level", dozen);
        int i = (context.getScaledWindowWidth() - textRenderer.getWidth(text)) / 2;
        int j = context.getScaledWindowHeight() - 24 - 9 - 2;
        context.drawText(textRenderer, text, i + 1, j, net.minecraft.util.Colors.BLACK, false);
        context.drawText(textRenderer, text, i - 1, j, net.minecraft.util.Colors.BLACK, false);
        context.drawText(textRenderer, text, i, j + 1, Colors.BLACK, false);
        context.drawText(textRenderer, text, i, j - 1, Colors.BLACK, false);
        context.drawText(textRenderer, text, i, j, -8323296, false);
    }
}
