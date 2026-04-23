package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Mixin(AnvilScreen.class)
public abstract class DozenalAnvilScreenMixin {

    /**
     * Перехватываем создание текста "container.repair.cost" (Стоимость зачарования).
     * method = "drawForeground" - это стандартное имя метода отрисовки переднего плана в Yarn.
     */
    @Redirect(
        method = "drawForeground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
        )
    )
    private MutableText dozenium$interceptAnvilCost(String key, Object[] args) {
        // Проверяем, что это именно текст стоимости ремонта
        if ("container.repair.cost".equals(key)) {
            
            // Создаем копию аргументов (на всякий случай, как и раньше)
            Object[] newArgs = new Object[args.length];
            System.arraycopy(args, 0, newArgs, 0, args.length);

            // Первый аргумент (args[0]) - это int i (стоимость опыта)
            if (newArgs.length > 0 && newArgs[0] instanceof Number) {
                int cost = ((Number) newArgs[0]).intValue();
                
                // Превращаем число в 12-ричную строку
                newArgs[0] = Dozenal.toDozenal(cost);
            }

            // Возвращаем текст с новым аргументом.
            // Игра подставит нашу строку "10" (вместо числа 12) в шаблон перевода.
            return Text.translatable(key, newArgs);
        }

        // Если вдруг там есть другие переводимые тексты, не трогаем их
        return Text.translatable(key, args);
    }
}