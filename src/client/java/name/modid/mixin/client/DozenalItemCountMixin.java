package name.modid.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import name.modid.util.Dozenal;
import net.minecraft.client.gui.GuiGraphics;

@Mixin(GuiGraphics.class)
public class DozenalItemCountMixin {
   
    @Redirect(
        method = "renderItemCount",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"
        )
    )
    private String dozenium$toDozenal(int value) {
        return Dozenal.toDozenal(value);
    } 
}
