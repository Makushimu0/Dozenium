package name.modid.mixin.client;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import name.modid.util.Dozenal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

@Mixin(ItemStack.class)
public class DozenalItemTooltipMixin {

    // Паттерн для поиска десятичных чисел в строках (поддерживает знаки и дроби)
    @Unique
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("(?<!\\d)[-+]?\\d+([,\\d]*\\d+)?(\\.\\d+)?(?!\\d)");

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void injectTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        List<Text> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) return;

        // Проходим по каждой строке тултипа и трансформируем её
        List<Text> transformed = original.stream()
                .map(this::convertText)
                .collect(Collectors.toList());

        cir.setReturnValue(transformed);
    }

    /**
     * Рекурсивно пересоздает Text объект с замененными числами, сохраняя стили.
     */
    @Unique
    private Text convertText(Text text) {
        TextContent content = text.getContent();
        MutableText newText;

        if (content instanceof TranslatableTextContent translatable) {
            // Если это переводимый текст (например, атрибуты)
            newText = handleTranslatable(translatable);
        } else if (content instanceof PlainTextContent.Literal literal) {
            // Если это обычный текст (название, лор)
            String raw = literal.string();
            // Тут контекст всегда обычный, так как проценты обычно идут через Translatable
            String converted = replaceNumbersInString(raw, false);
            newText = Text.literal(converted);
        } else {
            // Для остальных типов просто копируем контент без изменений (Keybinds, NBT и т.д.)
            newText = MutableText.of(content);
        }

        // 1. Копируем стиль исходного текста
        newText.setStyle(text.getStyle());

        // 2. Рекурсивно обрабатываем и добавляем дочерние элементы (siblings)
        for (Text sibling : text.getSiblings()) {
            newText.append(convertText(sibling));
        }

        return newText;
    }

    @Unique
    private MutableText handleTranslatable(TranslatableTextContent translatable) {
        String key = translatable.getKey();
        Object[] args = translatable.getArgs();
        Object[] newArgs = new Object[args.length];

        // Проверяем, является ли это атрибутом-процентом
        // Ключи атрибутов: attribute.modifier.plus.0 (число), .1 (процент), .2 (процент)
        // Также бывает attribute.modifier.take...
        boolean isPercentAttribute = key.startsWith("attribute.modifier") && 
                                     (key.endsWith(".1") || key.endsWith(".2"));
        
        // Бывает, что attribute.modifier просто общий, тогда считаем его числом, но аргументы проверим
        boolean isAttribute = key.startsWith("attribute.modifier");

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];

            if (arg instanceof Text textArg) {
                // Если аргумент сам по себе текст - рекурсия
                newArgs[i] = convertText(textArg);
            } else if (arg instanceof Number numberArg) {
                // Если аргумент число
                double val = numberArg.doubleValue();
                if (isAttribute && i == 0) { // Обычно значение атрибута - это 0-й аргумент
                    newArgs[i] = convertAttributeValue(val, isPercentAttribute);
                } else {
                    newArgs[i] = Dozenal.toDozenal((int) Math.round(val)); // Дефолтная конвертация
                }
            } else if (arg instanceof String stringArg) {
                // Если аргумент строка (ванилла часто форматирует числа в строки заранее)
                if (isAttribute && i == 0) {
                    // Пытаемся распарсить строку обратно в число
                    try {
                        // Убираем запятые если есть, меняем на точки
                        String clean = stringArg.replace(",", ".");
                        Double val = Double.parseDouble(clean);
                        newArgs[i] = convertAttributeValue(val, isPercentAttribute);
                    } catch (NumberFormatException e) {
                        // Не смогли распарсить - прогоняем как обычный текст
                        newArgs[i] = replaceNumbersInString(stringArg, isPercentAttribute);
                    }
                } else {
                    newArgs[i] = replaceNumbersInString(stringArg, false);
                }
            } else {
                newArgs[i] = arg;
            }
        }

        return Text.translatable(key, newArgs);
    }

    /**
     * Логика выбора правильной функции из твоего Util
     */
    @Unique
    private String convertAttributeValue(double val, boolean isPercent) {
        if (isPercent) {
            // Если контекст проценты. 
            // ВАЖНО: Ванилла передает сюда уже умноженное на 100 число?
            // Обычно для attribute.modifier.plus.1 передается значение типа "20" (строка) для +20%.
            // Значит val уже 20.0. Используем toDozenalPercent для целых процентов.
            return Dozenal.toDozenalPercent((int) Math.round(val));
        } else {
            // Обычное число (урон, броня)
            // Используем Float версию для точности (например +1.5 урона)
            return Dozenal.toFloatDozenal((float) val);
        }
    }

    /**
     * Поиск и замена чисел в строке с проверкой на контекст времени.
     */
    @Unique
    private String replaceNumbersInString(String input, boolean forcePercentContext) {
        Matcher m = DECIMAL_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (m.find()) {
            String match = m.group();
            int start = m.start();
            int end = m.end();

            boolean isTimeContext = (start > 0 && input.charAt(start - 1) == ':') ||
                                    (end < input.length() && input.charAt(end) == ':');

            try {
                // 2. ОЧИСТКА: Удаляем запятые перед парсингом, чтобы "5,000" стало "5000"
                String cleanMatch = match.replace(",", "");
                double val = Double.parseDouble(cleanMatch);
                
                String replacement;
                if (forcePercentContext) {
                    replacement = Dozenal.toDozenalPercent((int) Math.round(val));
                } else {
                    if (val == Math.floor(val) && !Double.isInfinite(val)) {
                        replacement = Dozenal.toDozenal((int) val);
                        if (isTimeContext && replacement.length() < 2) {
                            replacement = "0" + replacement;
                        }
                    } else {
                        replacement = Dozenal.toFloatDozenal((float) val);
                    }
                }
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } catch (Exception e) {
                m.appendReplacement(sb, Matcher.quoteReplacement(match));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}