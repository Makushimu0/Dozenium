package name.modid.mixin.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;
import name.modid.util.Dozenal;
import net.minecraft.network.chat.Component;

@Mixin(targets = "net.minecraft.client.gui.screens.multiplayer.ServerSelectionList$OnlineServerEntry")
public class ServerEntryMixin {

    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");

    /**
     * Перехватываем локальную переменную 'text' в методе render.
     * Мы используем ordinal = 0, так как это первая переменная типа Text, 
     * которая объявляется и сохраняется (STORE) в этом методе.
     */
    @ModifyVariable(
        method = "renderContent",
        at = @At(value = "STORE"),
        ordinal = 0
    )
    private Component convertServerPlayerCount(Component text) {
        if (text == null) return null;

        String rawString = text.getString();
        
        // Если в строке нет цифр (например, там написано "Ошибка" или "Пинг..."), ничего не трогаем
        if (!DIGIT_PATTERN.matcher(rawString).find()) {
            return text;
        }

        // Парсим строку (заменяем 11542 и 15000 на 12-ричные эквиваленты)
        Matcher matcher = DIGIT_PATTERN.matcher(rawString);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            try {
                // Извлекаем число
                int value = Integer.parseInt(matcher.group());
                // Конвертируем
                matcher.appendReplacement(sb, Dozenal.toDozenal(value));
            } catch (NumberFormatException e) {
                // На случай очень больших чисел, которые не влезут в int
                matcher.appendReplacement(sb, matcher.group());
            }
        }
        matcher.appendTail(sb);

        // Возвращаем новый текст, сохраняя оригинальный стиль (цвет и т.д.)
        String convertedString = sb.toString();
        return Component.literal(Objects.requireNonNull(convertedString)).setStyle(text.getStyle());
    }
}