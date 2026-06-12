package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Mixin(targets = "net.minecraft.client.gui.screens.achievement.StatsScreen$MobsStatisticsList$MobRow")
public class EntityStatsEntryMixin {

    /**
     * Мы перехватываем вызов Text.translatable ВНУТРИ конструктора Entry.
     * В этот метод игра передает массив объектов args.
     * Для "killed": args[0] = число, args[1] = имя моба.
     * Для "killed_by": args[0] = имя моба, args[1] = число.
     */
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"
        )
    )
    private MutableComponent convertNumbersToDozenal(String key, Object[] args) {
        // Проверяем, что это нужные нам ключи статистики (чтобы не сломать другие тексты)
        if (key.startsWith("stat_type.minecraft.killed")) { // Ловит и "killed", и "killed_by", и ".none"
            
            // Проходимся по всем аргументам
            for (int i = 0; i < args.length; i++) {
                // Если аргумент - это целое число (Integer)
                if (args[i] instanceof Integer value) {
                    // Заменяем число (int) на нашу строку (String) в 12-ричной системе
                    // Java позволяет класть String в массив Object[], так что всё легально.
                    args[i] = Dozenal.toDozenal(value);
                }
            }
        }

        // Вызываем оригинальный метод, но уже с нашим подмененным массивом args
        return Component.translatable(key, args);
    }
}