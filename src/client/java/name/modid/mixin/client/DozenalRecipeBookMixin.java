package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Mixin(RecipeBookResults.class)
public abstract class DozenalRecipeBookMixin {

    /**
     * Перехватываем создание текста "gui.recipebook.page" внутри метода draw.
     * method = "draw" - это имя в Yarn (development environment).
     */
    @Redirect(
        method = "draw",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
        )
    )
    private MutableText dozenium$interceptRecipePageText(String key, Object[] args) {
        // Проверяем, что это именно тот текст (страница книги рецептов)
        if ("gui.recipebook.page".equals(key)) {
            
            // args[0] - это (this.currentPage + 1)
            // args[1] - это this.pageCount
            
            // Создаем копию аргументов для безопасности
            Object[] newArgs = new Object[args.length];
            System.arraycopy(args, 0, newArgs, 0, args.length);

            for (int i = 0; i < newArgs.length; i++) {
                if (newArgs[i] instanceof Number) {
                    int value = ((Number) newArgs[i]).intValue();
                    // Превращаем число (int) в 12-ричную строку
                    newArgs[i] = Dozenal.toDozenal(value);
                }
            }

            // Возвращаем текст с подмененными (строковыми) аргументами
            return Text.translatable(key, newArgs);
        }

        // Для всех остальных текстов ничего не меняем
        return Text.translatable(key, args);
    }
}
