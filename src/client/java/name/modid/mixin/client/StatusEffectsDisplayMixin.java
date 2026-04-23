package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal; // Импорт твоего утилитного класса
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;

@Mixin(StatusEffectsDisplay.class)
public abstract class StatusEffectsDisplayMixin {

    /**
     * Мы перехватываем вызов StatusEffectUtil.getDurationText внутри метода drawStatusEffects.
     * Вместо ванильного форматирования времени (MM:SS) мы используем твою 12-ричную систему.
     */
    @Redirect(
        method = "drawStatusEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/effect/StatusEffectUtil;getDurationText(Lnet/minecraft/entity/effect/StatusEffectInstance;FF)Lnet/minecraft/text/Text;"
        )
    )

    private Text injectDozenalDuration(StatusEffectInstance instance, float multiplier, float tickRate) {
        // 1. Проверяем, не является ли эффект бесконечным (например, от маяка или в креативе)
        if (instance.isInfinite()) {
            return Text.translatable("effect.duration.infinite");
        }

        // 2. Получаем длительность в тиках
        int durationSecs = instance.getDuration() / 20;
        
        // 3. Конвертируем в 12-ричную систему через твой утилитный класс.
        // Я предполагаю, что toDozenal принимает int (тики) и возвращает String.
        // Если твой метод принимает секунды, раздели durationTicks на 20.
        String dozenalString;
        String H = "";
        String M = pad2(Dozenal.toDozenal((durationSecs%3599) / 60));
        String S = pad2(Dozenal.toDozenal(durationSecs%60));
        if (durationSecs >= 3600) {
            H = Dozenal.toDozenal(durationSecs / 3600);
            dozenalString = pad2(H) + ":" + M + ":" + S;
        } else {
            dozenalString = M + ":" + S;
        }

        // 4. Возвращаем текстовый объект
        return Text.of(dozenalString);
    }
    @Unique
    private static String pad2(String s) {
        return s == null
            ? "00"
            : "0".repeat(Math.max(0, 2 - s.length())) + s;
        }
}
