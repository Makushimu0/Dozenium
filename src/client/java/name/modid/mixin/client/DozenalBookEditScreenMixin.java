package name.modid.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import name.modid.util.Dozenal;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

@Mixin(BookEditScreen.class)
public abstract class DozenalBookEditScreenMixin {

    @Shadow private Component numberOfPages;
    @Shadow private int currentPage;
    @Shadow @Final private List<String> pages; // Список страниц книги

    /**
     * Внедряемся в НАЧАЛО (HEAD) метода отрисовки индикатора.
     * Мы обновляем текст прямо перед тем, как игра попытается его нарисовать.
     */
    @Inject(
        method = "visitText", // Ваше имя метода
        at = @At("HEAD")
    )
    private void dozenium$updatePageIndicatorEdit(ActiveTextCollector drawnTextConsumer, CallbackInfo ci) {
        // 1. Сохраняем стиль текущего текста (чтобы не потерять цвет/шрифт)
        Style originalStyle = Style.EMPTY;
        if (this.numberOfPages != null) {
            originalStyle = this.numberOfPages.getStyle();
        }

        // 2. Считаем количество страниц
        // В режиме редактирования, если мы на последней пустой странице, она может еще не быть в списке,
        // но обычно размер списка pages корректен.
        int totalPages = this.pages.size();
        
        // 3. Конвертируем в 12-ричную систему
        String dozenalCurrent = Dozenal.toDozenal(this.currentPage + 1);
        String dozenalTotal = Dozenal.toDozenal(totalPages);

        // 4. Создаем новый текст
        MutableComponent newText = Component.translatable("book.pageIndicator", dozenalCurrent, dozenalTotal);

        // 5. Возвращаем оригинальный стиль
        newText.setStyle(originalStyle);

        // 6. Подменяем поле. Теперь, когда оригинальный код продолжит выполнение,
        // он нарисует уже наш обновленный текст.
        this.numberOfPages = newText;
    }
}