package name.modid.mixin.client;

import name.modid.util.Sezimal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Environment(EnvType.CLIENT)
@Mixin(GuiGraphicsExtractor.class)
public abstract class SezimalItemCountMixin {

    @ModifyArgs(
        method = "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V"
        )
    )
    private void senarium$modifyItemCount(Args args) {

        Font font = args.get(0);
        String amount = args.get(1);

        if (amount == null || amount.isEmpty()) {
            return;
        }

        try {
            int count = Integer.parseInt(amount);

            if (count <= 1) {
                return;
            }

            @Nullable String sezimal = Sezimal.toSezimal(count);
            if (sezimal == null || sezimal.isEmpty()) {
                return;
            }

            int oldWidth = font.width(amount);
            int newWidth = font.width(sezimal);

            int x = args.get(2);
            x += oldWidth - newWidth;

            args.set(1, sezimal);
            args.set(2, x);

        } catch (NumberFormatException ignored) {
        }
    }
}