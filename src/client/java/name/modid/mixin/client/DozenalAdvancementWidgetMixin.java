package name.modid.mixin.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference; // Нужен для передачи переменной из лямбды
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@Mixin(AdvancementWidget.class)
public abstract class DozenalAdvancementWidgetMixin {

    @Shadow
    protected abstract void drawText(DrawContext context, List<OrderedText> text, int x, int y, int color);

    private static final Pattern DIGITS = Pattern.compile("\\d+");

    // Часть 1: Прогресс (5/10) - оставляем как было, тут все работает
    @ModifyVariable(
        method = "drawTooltip",
        at = @At("STORE"),
        ordinal = 0
    )
    private Text dozenium$modifyProgressText(Text original) {
        if (original == null) return null;
        String raw = original.getString();
        String replaced = replaceNumbersInString(raw);
        return Text.literal(replaced).setStyle(original.getStyle());
    }

    /**
     * Часть 2: Описание (Убить 10 зомби)
     * Исправлено: теперь мы сохраняем оригинальный цвет (фиолетовый/зеленый)
     */
    @Redirect(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementWidget;drawText(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;III)V"
        )
    )
    private void dozenium$redirectDescriptionDraw(AdvancementWidget instance, DrawContext context, List<OrderedText> lines, int x, int y, int color) {
        List<OrderedText> newLines = new ArrayList<>();

        for (OrderedText line : lines) {
            StringBuilder sb = new StringBuilder();
            
            // Используем AtomicReference, чтобы вытащить стиль из лямбды (visitor)
            AtomicReference<Style> savedStyle = new AtomicReference<>(Style.EMPTY);

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
            MutableText newTextComp = Text.literal(newContent).setStyle(savedStyle.get());

            newLines.add(newTextComp.asOrderedText());
        }

        // Рисуем обновленный список с правильными цветами
        this.drawText(context, newLines, x, y, color);
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