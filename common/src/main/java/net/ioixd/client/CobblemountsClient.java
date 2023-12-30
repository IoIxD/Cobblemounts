package net.ioixd.client;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.ClientModInitializer;
import net.ioixd.Cobblemounts;
import net.ioixd.Config;
import net.minecraft.network.PacketByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemountsClient{
    public static final String MOD_ID = "cobblemounts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Config SYNCED_CONFIG = new Config();
    public static void onInitializeClient() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C,Cobblemounts.CONFIG_SYNC_ID, CobblemountsClient::onSyncPacket);
    }
    private static void onSyncPacket(PacketByteBuf buf, NetworkManager.PacketContext packetContext) {
        SYNCED_CONFIG = Config.read(buf);
    }
}