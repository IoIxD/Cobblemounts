package io.github.jumperonjava.cobblemountsarchfork.fabric;

import net.fabricmc.api.ModInitializer;
import net.ioixd.Cobblemounts;

public class CobblemountsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Cobblemounts.onInitialize();
    }
}