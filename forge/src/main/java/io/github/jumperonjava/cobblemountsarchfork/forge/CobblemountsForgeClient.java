package io.github.jumperonjava.cobblemountsarchfork.forge;

import net.ioixd.Cobblemounts;
import net.ioixd.client.CobblemountsClient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod.EventBusSubscriber(bus = MOD, modid = Cobblemounts.MOD_ID)
public class CobblemountsForgeClient {
    @SubscribeEvent
    public static void onInitializeClient(FMLClientSetupEvent event) {
        CobblemountsClient.onInitializeClient();
    }
}
