package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Sezimal;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Mixin(RecipeBookPage.class)
public abstract class SezimalRecipeBookMixin {

    /**
     * Перехватываем создание текста "gui.recipebook.page" внутри метода extractRenderState.
     * method = "extractRenderState" - актуальное имя в Yarn для Minecraft 26.1.
     */
    @Redirect(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"
        )
    )
    @SuppressWarnings("null")
    private MutableComponent senarium$interceptRecipePageText(String key, Object[] args) {
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
                    newArgs[i] = Sezimal.toSezimal(value);
                }
            }

            // Возвращаем текст с подмененными (строковыми) аргументами
            return Component.translatable(key != null ? key : "", newArgs);
        }

        // Для всех остальных текстов ничего не меняем
        return Component.translatable(key != null ? key : "", args);
    }
}
