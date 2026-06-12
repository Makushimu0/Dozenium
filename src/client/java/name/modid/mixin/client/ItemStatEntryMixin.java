package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal;
import net.minecraft.stats.Stat;

// Мы используем targets, чтобы "достать" глубоко спрятанный внутренний класс StatEntry
// без использования Access Widener.
@Mixin(targets = "net.minecraft.client.gui.screens.achievement.StatsScreen$ItemStatisticsList$ItemRow")
public class ItemStatEntryMixin {

    /**
     * Перехватываем вызов stat.format(int), который превращает число в строку.
     * Это происходит внутри метода render(...).
     */
    @Redirect(
        method = "renderStat(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/stats/Stat;IIZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/stats/Stat;format(I)Ljava/lang/String;"
        )
    )
    private String changeToDuodecimal(Stat<?> instance, int value) {
        // Если значение 0, возвращаем как есть (хотя в 12-ричной 0 тоже 0)
        if (value == 0) {
            return "0";
        }
        
        // Превращаем число в 12-ричную строку (0-9, a, b) и делаем буквы заглавными (0-9, A, B)
        return Dozenal.toDozenal(value);
    }
}