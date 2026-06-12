package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public abstract class DozenalExperienceLevelMixin {
    @Redirect(
      method = "renderHotbarAndDecorations",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"
      )
    )
    private void redirectDrawXpLevel(GuiGraphics context, Font textRenderer, int level) {
        String dozen = java.util.stream.IntStream.of(level)
            .mapToObj(name.modid.util.Dozenal::toDozenal)
            .findFirst()
            .orElse("0");
        // можно скопировать код оригинального drawExperienceLevel, но с dozen-строкой:
        Component text = net.minecraft.network.chat.Component.translatable("gui.experience.level", dozen);
        int i = (context.guiWidth() - textRenderer.width(text)) / 2;
        int j = context.guiHeight() - 24 - 9 - 2;
        context.drawString(textRenderer, text, i + 1, j, net.minecraft.util.CommonColors.BLACK, false);
        context.drawString(textRenderer, text, i - 1, j, net.minecraft.util.CommonColors.BLACK, false);
        context.drawString(textRenderer, text, i, j + 1, CommonColors.BLACK, false);
        context.drawString(textRenderer, text, i, j - 1, CommonColors.BLACK, false);
        context.drawString(textRenderer, text, i, j, -8323296, false);
    }
}
