package io.github.jumperonjava.cobblemountsarchfork.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.ioixd.client.CobblemountsClient;

public class CobblemountsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CobblemountsClient.onInitializeClient();
    }
}
