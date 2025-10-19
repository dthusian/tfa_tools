package dev.wateralt.mc.tfa_tools;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.DynamicRegistryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TfaTools implements ModInitializer {
    public static DynamicRegistryManager dynamicRegistries;
    public static final Logger LOGGER = LoggerFactory.getLogger(TfaTools.class); 
    
    @Override
    public void onInitialize() {
    }
}
