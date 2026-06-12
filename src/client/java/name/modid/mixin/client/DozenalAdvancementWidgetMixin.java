package name.modid.mixin.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference; // Нужен для передачи переменной из лямбды
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

@Mixin(AdvancementWidget.class)
public abstract class DozenalAdvancementWidgetMixin {

    @Shadow
    protected abstract void extractMultilineText(GuiGraphicsExtractor context, List<FormattedCharSequence> text, int x, int y, int color);

    private static final Pattern DIGITS = Pattern.compile("\\d+");

    // Часть 1: Прогресс (5/10) - теперь перехватываем правильный метод extractHover для Minecraft 26.1
    @ModifyVariable(
        method = "extractHover",
        at = @At("STORE"),
        ordinal = 0
    )
    private Component dozenium$modifyProgressText(Component original) {
        if (original == null) return null;
        String raw = original.getString();
        String replaced = replaceNumbersInString(raw);
        return Component.literal(Objects.requireNonNull(replaced)).setStyle(original.getStyle());
    }

    /**
     * Часть 2: Описание (Убить 10 зомби)
     * Исправлено: теперь мы сохраняем оригинальный цвет (фиолетовый/зеленый)
     */
    @Redirect(
        method = "extractHover",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementWidget;extractMultilineText(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/List;III)V"
        )
    )
    private void dozenium$redirectDescriptionDraw(AdvancementWidget instance, GuiGraphicsExtractor context, List<FormattedCharSequence> lines, int x, int y, int color) {
        List<FormattedCharSequence> newLines = new ArrayList<>();

        for (FormattedCharSequence line : lines) {
            StringBuilder sb = new StringBuilder();
            
            // Используем AtomicReference, чтобы вытащить стиль из лямбды (visitor)
            // Change your line to look exactly like this:
            AtomicReference<@NonNull Style> savedStyle = new AtomicReference<>(Style.EMPTY);


            // Проходимся по строке.
            // OrderedText работает так: он перебирает символы по одному.
            line.accept((index, style, codePoint) -> {
                // Если это первый символ, запоминаем его стиль!
                // Именно этот стиль определяет цвет всей строки (фиолетовый или зеленый).
                if (sb.length() == 0) {
                    savedStyle.set(style);
                }
                sb.append((char) codePoint);
                return true;
            });

            String textContent = sb.toString();

            // Если строка пустая, просто добавляем её обратно
            if (textContent.isEmpty()) {
                newLines.add(line);
                continue;
            }

            // Меняем цифры на 12-ричные
            String newContent = replaceNumbersInString(textContent);

            // Создаем новый текст и ПРИМЕНЯЕМ сохраненный стиль
            @NonNull Style finalStyle = Objects.requireNonNull(savedStyle.get());
            MutableComponent newTextComp = Component.literal(Objects.requireNonNull(newContent)).setStyle(finalStyle);

            newLines.add(newTextComp.getVisualOrderText());
        }

        // Рисуем обновленный список с правильными цветами
        this.extractMultilineText(context, newLines, x, y, color);
    }

    private String replaceNumbersInString(String input) {
        Matcher m = DIGITS.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            try {
                int value = Integer.parseInt(m.group());
                m.appendReplacement(sb, Dozenal.toDozenal(value));
            } catch (NumberFormatException e) {
                m.appendReplacement(sb, m.group());
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}