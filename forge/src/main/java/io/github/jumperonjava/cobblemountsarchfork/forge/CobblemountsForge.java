package io.github.jumperonjava.cobblemountsarchfork.forge;

import dev.architectury.platform.forge.EventBuses;
import net.ioixd.Cobblemounts;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Cobblemounts.MOD_ID)
public class CobblemountsForge {
    public CobblemountsForge() {
		// Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Cobblemounts.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Cobblemounts.onInitialize();
    }
}