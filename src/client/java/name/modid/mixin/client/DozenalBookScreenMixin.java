package name.modid.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import name.modid.util.Dozenal;
import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@Mixin(BookScreen.class)
public abstract class DozenalBookScreenMixin {

    @Shadow private Text pageIndexText;
    @Shadow private int pageIndex;
    @Shadow protected abstract int getPageCount();

    @Inject(
        method = "method_75835", 
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screen/ingame/BookScreen;pageIndexText:Lnet/minecraft/text/Text;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void dozenium$replacePageText(DrawnTextConsumer drawer, boolean bl, CallbackInfo ci) {
        // 1. Получаем оригинальный стиль (цвет, шрифт и т.д.), который игра только что присвоила
        Style originalStyle = this.pageIndexText.getStyle();

        // 2. Вычисляем наши 12-ричные значения
        String dozenalCurrent = Dozenal.toDozenal(this.pageIndex + 1);
        String dozenalTotal = Dozenal.toDozenal(this.getPageCount());
        
        // 3. Создаем новый текст
        MutableText newText = Text.translatable("book.pageIndicator", dozenalCurrent, dozenalTotal);
        
        // 4. ВАЖНО: Применяем оригинальный стиль к нашему новому тексту
        newText.setStyle(originalStyle);

        // 5. Подменяем поле
        this.pageIndexText = newText;
    }
}