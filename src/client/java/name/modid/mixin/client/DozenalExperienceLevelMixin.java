package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
@Mixin(Component.class)
public interface DozenalExperienceLevelMixin {

    /**
     * Перехватываем создание текста "gui.experience.level".
     * Метод должен быть строго 'private static', так как мы находимся внутри интерфейса.
     */
    @Inject(
        method = "translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void dozenium$interceptXpTranslation(String key, Object[] args, CallbackInfoReturnable<MutableComponent> cir) {
        if ("gui.experience.level".equals(key) && args.length > 0) {
            Object firstArg = args[0];
            if (firstArg instanceof Number) {
                int level = ((Number) firstArg).intValue();
                
                // Преобразуем уровень в 12-ричную строку через вашу утилиту
                String dozenStr = name.modid.util.Dozenal.toDozenal(level);
                if (dozenStr == null) {
                    dozenStr = "0";
                }

                // Возвращаем измененный компонент перевода
                cir.setReturnValue(Component.translatable("gui.experience.level", new Object[] { dozenStr }));
            }
        }
    }
}
