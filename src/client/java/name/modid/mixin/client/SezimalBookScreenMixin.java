package name.modid.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import name.modid.util.Sezimal;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

@Mixin(BookViewScreen.class)
public abstract class SezimalBookScreenMixin {

    @Shadow private Component pageMsg;
    @Shadow private int currentPage;
    @Shadow protected abstract int getNumPages();

    @Inject(
        method = "visitText", 
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screens/inventory/BookViewScreen;pageMsg:Lnet/minecraft/network/chat/Component;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void senarium$replacePageText(ActiveTextCollector drawer, boolean bl, CallbackInfo ci) {
        // 1. Получаем оригинальный стиль (цвет, шрифт и т.д.), который игра только что присвоила
        Style originalStyle = this.pageMsg.getStyle();

        // 2. Вычисляем наши 12-ричные значения
        String dozenalCurrent = Sezimal.toSezimal(this.currentPage + 1);
        String dozenalTotal = Sezimal.toSezimal(this.getNumPages());
        
        // 3. Создаем новый текст
        MutableComponent newText = Component.translatable("book.pageIndicator", dozenalCurrent, dozenalTotal);
        
        // 4. ВАЖНО: Применяем оригинальный стиль к нашему новому тексту
        newText.setStyle(originalStyle);

        // 5. Подменяем поле
        this.pageMsg = newText;
    }
}