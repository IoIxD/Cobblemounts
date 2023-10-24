package net.ioixd;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.ServerTask;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemountsClient implements ClientModInitializer {
    public static final Identifier PLAYER_JUMP_PACKET = new Identifier("cobblemounts:player_jumped");
    public static final String MOD_ID = "Cobblemounts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (client.options.jumpKey.wasPressed()) {
                if(client.player != null) {
                    PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                    passedData.writeUuid(client.player.getUuid());
                    ClientPlayNetworking.send(PLAYER_JUMP_PACKET,passedData);
                }
            }
        });
    }
}