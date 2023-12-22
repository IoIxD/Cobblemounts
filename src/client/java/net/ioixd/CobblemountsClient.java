package net.ioixd;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.network.ClientPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemountsClient implements ClientModInitializer {
    public static final String MOD_ID = "cobblemounts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Config SYNCED_CONFIG = new Config();
    private PacketType<Config> configPacketType = PacketType.create(Cobblemounts.CONFIG_SYNC_ID,Config::read);
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(configPacketType, CobblemountsClient::onSyncPacket);
    }
    private static void onSyncPacket(Config packet, ClientPlayerEntity player, PacketSender responseSender) {
        SYNCED_CONFIG = packet;
    }

}