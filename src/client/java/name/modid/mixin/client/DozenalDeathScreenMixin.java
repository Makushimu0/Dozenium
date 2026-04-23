package name.modid.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(DeathScreen.class)
public abstract class DozenalDeathScreenMixin extends Screen {

    @Shadow @Final private Text scoreText;

    protected DozenalDeathScreenMixin(Text title) {
        super(title);
    }

    @Redirect(
        method = "drawTitles",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screen/DeathScreen;scoreText:Lnet/minecraft/text/Text;",
            opcode = Opcodes.GETFIELD
        )
    )
    private Text dozenium$interceptScoreTextRead(DeathScreen instance) {
        if (this.client != null && this.client.player != null) {
            
            // 1. Получаем твои 12-ричные цифры
            int score = this.client.player.getScore();
            String dozenalScore = Dozenal.toDozenal(score);

            // 2. Берем полную строку, которая УЖЕ переведена игрой
            // Например: "Счет: 50" или "Score: 50"
            String fullString = this.scoreText.getString();

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
            return Text.literal(label)
                    .append(": ")
                    .append(Text.literal(dozenalScore).formatted(Formatting.YELLOW));
        }

        return this.scoreText;
    }
}