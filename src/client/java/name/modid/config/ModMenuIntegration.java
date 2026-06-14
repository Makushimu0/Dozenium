package name.modid.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import name.modid.util.Dozenal;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.create(DozeniumConfig.HANDLER, (defaults, config, builder) -> builder
                .title(Component.literal("Dozenium Config"))
                .save(() -> {
                    DozeniumConfig.HANDLER.save();
                    Dozenal.updateDigitsFromConfig();
                })
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General Settings"))
                        
                        .option(Option.<String>createBuilder()
                                .name(Component.literal("Dec Symbol"))
                                .binding(defaults.decSymbol, () -> config.decSymbol, newValue -> config.decSymbol = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        
                        .option(Option.<String>createBuilder()
                                .name(Component.literal("El Symbol"))
                                .binding(defaults.elSymbol, () -> config.elSymbol, newValue -> config.elSymbol = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())
        ).generateScreen(parentScreen);
    }
}
