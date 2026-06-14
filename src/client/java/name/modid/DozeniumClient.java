package name.modid;

import name.modid.config.DozeniumConfig;
import name.modid.util.Dozenal;
import net.fabricmc.api.ClientModInitializer;

public class DozeniumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DozeniumConfig.HANDLER.load();
        DozeniumConfig.HANDLER.save();
        Dozenal.updateDigitsFromConfig(); 
    }
}
