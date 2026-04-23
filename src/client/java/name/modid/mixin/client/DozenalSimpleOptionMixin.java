package name.modid.mixin.client;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import name.modid.util.Dozenal;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

@Mixin(SimpleOption.class)
public abstract class DozenalSimpleOptionMixin<T> {

    @Shadow @Final @Mutable
    private Function<T, Text> textGetter;

    /**
     * Уникальное поле, которое мы добавляем в класс SimpleOption.
     * Оно будет помнить, обработали мы уже эту настройку или нет.
     */
    @Unique
    private boolean dozenium$isWrapped = false;

    // Паттерн: (число + %)|(дробное)|(целое)
    private static final Pattern NUMBERS = Pattern.compile("(\\d+)(\\s*%)|(\\d+\\.\\d+)|(\\d+)");

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void dozenium$wrapTextGetter(CallbackInfo ci) {
        // --- ЗАЩИТА ОТ ДВОЙНОГО СРАБАТЫВАНИЯ ---
        if (this.dozenium$isWrapped) {
            return; // Если уже обернули, выходим и ничего не делаем
        }
        this.dozenium$isWrapped = true; // Ставим метку "Обернуто"
        // ----------------------------------------

        Function<T, Text> originalGetter = this.textGetter;

        this.textGetter = (value) -> {
            Text originalText = originalGetter.apply(value);
            if (originalText == null) return null;

            String raw = originalText.getString();
            
            // Если строка пустая, возвращаем сразу
            if (raw.isEmpty()) return originalText;

            // Специальная проверка для процентов: если есть знак %, значит все цифры - это проценты
            if (raw.contains("%")) {
                 return processPercentString(raw, originalText);
            }

            // Обычная обработка (для FOV, Render Distance и т.д.)
            return processNormalString(raw, originalText);
        };
    }

    /**
     * Метод обработки строк с процентами (сохраняет структуру, меняет только числа)
     */
    @Unique
    private Text processPercentString(String raw, Text originalText) {
        // Используем простой паттерн только для целых чисел, так как знаем, что это проценты
        Matcher matcher = Pattern.compile("\\d+").matcher(raw);
        StringBuffer sb = new StringBuffer();
        
        boolean found = false;
        while (matcher.find()) {
            found = true;
            try {
                int val = Integer.parseInt(matcher.group());
                // Твой метод конвертации процентов
                // ВАЖНО: Он должен возвращать ЧИСТОЕ число (например "12"), без знака %
                String replacement = Dozenal.toDozenalPercent(val);
                
                // Если твой метод возвращает "12%", убери знак процента, чтобы не дублировать
                if (replacement.endsWith("%")) {
                     replacement = replacement.substring(0, replacement.length() - 1);
                }

                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } catch (NumberFormatException e) {
                matcher.appendReplacement(sb, matcher.group());
            }
        }
        
        if (!found) return originalText;
        
        matcher.appendTail(sb);
        return Text.literal(sb.toString()).setStyle(originalText.getStyle());
    }

    /**
     * Метод обработки обычных строк (ищет float и int)
     */
    @Unique
    private Text processNormalString(String raw, Text originalText) {
        Matcher matcher = NUMBERS.matcher(raw);
        if (!matcher.find()) return originalText;

        matcher.reset();
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            try {
                // Группа 1+2: Проценты (на всякий случай, если logic flow сюда попадет)
                if (matcher.group(1) != null) {
                    int val = Integer.parseInt(matcher.group(1));
                    String suffix = matcher.group(2); 
                    String dozenalVal = Dozenal.toDozenalPercent(val);
                    if (dozenalVal.endsWith("%")) dozenalVal = dozenalVal.substring(0, dozenalVal.length() - 1);
                    
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(dozenalVal + suffix));
                }
                // Группа 3: Float (1.5)
                else if (matcher.group(3) != null) {
                    float val = Float.parseFloat(matcher.group(3));
                    String replacement = Dozenal.toFloatDozenal(val);
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }
                // Группа 4: Int (12)
                else if (matcher.group(4) != null) {
                    int val = Integer.parseInt(matcher.group(4));
                    String replacement = Dozenal.toDozenal(val);
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }
            } catch (NumberFormatException e) {
                matcher.appendReplacement(sb, matcher.group());
            }
        }
        matcher.appendTail(sb);
        return Text.literal(sb.toString()).setStyle(originalText.getStyle());
    }
}