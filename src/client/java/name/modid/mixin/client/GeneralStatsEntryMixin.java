package name.modid.mixin.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import name.modid.util.Dozenal;

@Mixin(targets = "net.minecraft.client.gui.screens.achievement.StatsScreen$GeneralStatisticsList$Entry")
public class GeneralStatsEntryMixin {

    // Паттерн: ищем цифры, точки или запятые в начале строки.
    // Группа 1: само число. Группа 2: всё, что идет после (например, " km").
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^([0-9.,]+)(.*)$");

    /**
     * Перехватываем строку 'string', которую игра подготовила для рисования.
     * Она уже содержит "1.50 km" или "10".
     */
    @ModifyVariable(
        method = "renderContent",
        at = @At(value = "STORE"),
        ordinal = 0
    )
    private String formatInDozenal(String original) {
        if (original == null || original.isEmpty()) return original;

        Matcher matcher = NUMBER_PATTERN.matcher(original);
        
        // Если нашли число в начале строки
        if (matcher.find()) {
            String numberStr = matcher.group(1); // Например "1.50" или "12"
            String suffix = matcher.group(2);    // Например " km" или ""

            try {
                // Проверяем, дробное ли это число (есть ли точка или запятая)
                if (numberStr.contains(".") || numberStr.contains(",")) {
                    // Заменяем запятую на точку, чтобы Java поняла
                    float val = Float.parseFloat(numberStr.replace(",", "."));
                    
                    // Используем твой метод для float
                    return Dozenal.toFloatDozenal(val) + suffix;
                } else {
                    // Если точек нет, это целое число
                    // Убираем возможные разделители тысяч (если они есть), хотя в ваниле их обычно нет
                    int val = Integer.parseInt(numberStr);
                    
                    // Используем твой метод для int
                    return Dozenal.toDozenal(val) + suffix;
                }
            } catch (NumberFormatException e) {
                // Если попалось что-то сложное (например, время "0:15"), оставляем как есть
                return original;
            }
        }

        return original;
    }
}