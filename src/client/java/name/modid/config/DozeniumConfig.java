package name.modid.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.resources.Identifier;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

public class DozeniumConfig {

    // 🌟 1. Put your data fields inside a distinct inner class so GSON can read them
    public static class Data {
        @SerialEntry
        public String decSymbol = "X";
        @SerialEntry
        public String elSymbol = "E";
    }

    // 🌟 2. Update the Builder target type to use your inner Data class template
    public static final @NotNull ConfigClassHandler<Data> HANDLER = ConfigClassHandler.createBuilder(Data.class)
            .id(Identifier.fromNamespaceAndPath("dozenium", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("dozenium.json"))
                    .build())
            .build();
}
