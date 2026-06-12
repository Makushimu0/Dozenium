package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Mixin(EnchantmentScreen.class)
public abstract class DozenalEnchantmentScreenMixin {

    /**
     * Перехватываем создание переводимого текста внутри метода render (который рисует тултипы).
     */
    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"
        )
    )
    private MutableComponent dozenium$interceptEnchantTooltip(String key, Object[] args) {
        // Нас интересует только текст требования уровня (например, "Required: 30")
        if ("container.enchant.level.requirement".equals(key)) {
            
            // Создаем копию аргументов
            Object[] newArgs = new Object[args.length];
            System.arraycopy(args, 0, newArgs, 0, args.length);

            // Аргумент [0] - это необходимый уровень (например, 30)
            if (newArgs.length > 0 && newArgs[0] instanceof Number) {
                int levelReq = ((Number) newArgs[0]).intValue();
                
                // Переводим 30 -> 26 (в 12-ричной)
                newArgs[0] = Dozenal.toDozenal(levelReq);
            }

            return Component.translatable(key, newArgs);
        }

        // Все остальные тексты (названия чар, кол-во лазурита 1-3) пропускаем без изменений
        return Component.translatable(key, args);
    }
}