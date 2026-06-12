package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import name.modid.util.Dozenal;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;

@Mixin(EnchantmentScreen.class)
public abstract class DozenalEnchantmentScreenBackgroundMixin {

    /**
     * Изменяем локальную переменную типа String внутри метода extractBackground.
     * Мы ищем первую попавшуюся переменную String (ordinal = 0), которая сохраняется (STORE).
     * В этом методе это как раз наша 'String string = o + ""'.
     */
    @ModifyVariable(
        method = "extractBackground",
        at = @At("STORE"),
        ordinal = 0
    )
    private String dozenium$modifyEnchantLevelNumber(String original) {
        // На всякий случай проверяем, является ли строка числом.
        // Ведь если мы случайно перехватим что-то другое, игра может упасть.
        try {
            int level = Integer.parseInt(original);
            
            // Если это число, конвертируем его в 12-ричную систему
            return Dozenal.toDozenal(level);
        } catch (NumberFormatException e) {
            // Если это не число, возвращаем как было
            return original;
        }
    }
}