package net.ioixd;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemountsClient implements ClientModInitializer {
    public static final Identifier PLAYER_JUMP_PACKET = new Identifier("cobblemounts:player_jumped");
    public static final Identifier PLAYER_CROUCH_PACKET = new Identifier("cobblemounts:player_crouched");
    public static final String MOD_ID = "Cobblemounts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (client.options.jumpKey.wasPressed()) {
                if (client.player != null) {
                    PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                    passedData.writeUuid(client.player.getUuid());
                    ClientPlayNetworking.send(PLAYER_JUMP_PACKET, passedData);
                }
            }
            while (client.options.sneakKey.wasPressed()) {
                if (client.player != null) {
                    PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                    passedData.writeUuid(client.player.getUuid());
                    ClientPlayNetworking.send(PLAYER_CROUCH_PACKET, passedData);
                }
            }
        });
    }
}