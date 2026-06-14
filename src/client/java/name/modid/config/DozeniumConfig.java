package name.modid.config;

// 🌟 ADD THESE CORRECT YACL IMPORTS:
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.resources.Identifier;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

public class DozeniumConfig {

    public String decSymbol = "X";
    public String elSymbol = "E";

    // This handler sets up the direct saving loop to your config folder
    public static final @NotNull ConfigClassHandler<DozeniumConfig> HANDLER = ConfigClassHandler.createBuilder(DozeniumConfig.class)
            .id(Identifier.fromNamespaceAndPath("dozenium", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("dozenium.json"))
                    .build())
            .build();
}
