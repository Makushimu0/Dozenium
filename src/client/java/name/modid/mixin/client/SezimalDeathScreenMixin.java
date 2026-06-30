package name.modid.mixin.client;

import java.util.Objects;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Sezimal;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Mixin(DeathScreen.class)
public abstract class SezimalDeathScreenMixin extends Screen {

    @Shadow @Final private Component deathScore;

    protected SezimalDeathScreenMixin(Component title) {
        super(title);
    }
    
    @SuppressWarnings("null")
    @Redirect(
        method = "visitText",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screens/DeathScreen;deathScore:Lnet/minecraft/network/chat/Component;",
            opcode = Opcodes.GETFIELD
        )
    )
    private Component senarium$interceptScoreTextRead(DeathScreen instance) {
        if (this.minecraft != null && this.minecraft.player != null) {
            
            // 1. Получаем твои 12-ричные цифры
            int score = this.minecraft.player.getScore();
            String dozenalScore = Sezimal.toSezimal(score);

            // 2. Берем полную строку, которая УЖЕ переведена игрой
            // Например: "Счет: 50" или "Score: 50"
            String fullString = this.deathScore.getString();

            // 3. Ищем двоеточие
            String label = "Score"; // Запасной вариант, если двоеточия нет
            int colonIndex = fullString.indexOf(':');

            if (colonIndex > 0) {
                // Отрезаем всё от начала до двоеточия.
                // "Счет: 50".substring(0, 4) -> "Счет"
                label = fullString.substring(0, colonIndex);
            }

            // 4. Собираем конструктор LEGO заново:
            // [Слово из языка] + [: ] + [Твои цифры]
            return Component.literal(Objects.requireNonNullElse(label, "Score"))
                    .append(": ")
                    .append(Component.literal(dozenalScore).withStyle(ChatFormatting.YELLOW));
        }

        return this.deathScore;
    }
}