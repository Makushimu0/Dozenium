package name.modid.mixin.client;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal;

@Mixin(targets = "net.minecraft.client.gui.screen.world.WorldListWidget$WorldEntry")
public class WorldEntryMixin {

    // Паттерн ищет любую последовательность цифр (\d+)
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");

    /**
     * Мы перехватываем вызов метода format у DateTimeFormatter.
     * Это происходит внутри конструктора WorldEntry.
     */
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/time/format/DateTimeFormatter;format(Ljava/time/temporal/TemporalAccessor;)Ljava/lang/String;"
        )
    )
    private String convertDateNumbersToDozenal(DateTimeFormatter formatter, TemporalAccessor temporal) {
        // 1. Сначала даем игре сделать стандартное форматирование.
        // Мы получим что-то вроде "30/12/23 15:45" или "12/30/23 3:45 PM" (зависит от ПК).
        String originalDateString = formatter.format(temporal);

        // 2. Запускаем поиск всех чисел в этой строке
        Matcher matcher = DIGIT_PATTERN.matcher(originalDateString);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            try {
                // Берем найденное число (например, "30")
                int value = Integer.parseInt(matcher.group());
                
                // Переводим его в 12-ричную (например, "26")
                String dozenalValue = Dozenal.toDozenal(value);
                if (dozenalValue.length() < 2) {
                    dozenalValue = "0" + dozenalValue;
                }
                
                // Заменяем в строке
                matcher.appendReplacement(sb, dozenalValue);
            } catch (NumberFormatException e) {
                // Если вдруг число слишком огромное (вряд ли в дате такое будет), оставляем как есть
                matcher.appendReplacement(sb, matcher.group());
            }
        }
        matcher.appendTail(sb);

        // 3. Возвращаем строку, где все числа заменены, а знаки препинания остались на месте
        return sb.toString();
    }
}