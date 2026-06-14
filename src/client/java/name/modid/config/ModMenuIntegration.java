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
                
                // 🌟 THE CORRECT WAY TO SAVE AND UPDATE IN YACL v3:
                .save(() -> {
                    DozeniumConfig.HANDLER.save();     // Save the data to dozenium.json
                    Dozenal.updateDigitsFromConfig();  // Tell your mod to update characters instantly!
                })
                
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General Settings"))
                        
                        // 1. Dec Symbol Property Field
                        .option(Option.<String>createBuilder()
                                .name(Component.literal("Dec"))
                                .description(OptionDescription.of(Component.literal("Custom character for DEC (Default: X)")))
                                // Triggers real-time value assignment
                                .binding(defaults.decSymbol, () -> config.decSymbol, newValue -> config.decSymbol = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        
                        // 2. El Symbol Property Field
                        .option(Option.<String>createBuilder()
                                .name(Component.literal("El"))
                                .description(OptionDescription.of(Component.literal("Custom character for EL (Default: E)")))
                                // Triggers real-time value assignment
                                .binding(defaults.elSymbol, () -> config.elSymbol, newValue -> config.elSymbol = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())
        ).generateScreen(parentScreen);
    }
}
