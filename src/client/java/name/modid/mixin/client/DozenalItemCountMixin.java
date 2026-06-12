package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import name.modid.util.Dozenal;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
@Mixin(GuiGraphicsExtractor.class)
public abstract class DozenalItemCountMixin {

    @ModifyArg(
        method = "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V"
        ),
        index = 1
    )
    private String dozenium$modifyItemCountText(String amount) {
        if (amount == null || amount.isEmpty()) {
            return amount;
        }

        try {
            int count = Integer.parseInt(amount);
            if (count > 1) {
                return Dozenal.toDozenal(count);
            }
        } catch (NumberFormatException ignored) {
        }

        return amount;
    }
}
