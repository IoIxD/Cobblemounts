package net.ioixd;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod.EventBusSubscriber(bus = MOD, modid = Cobblemounts.MOD_ID)
public class CobblemountsClient {
    public static final Identifier PLAYER_JUMP_PACKET = new Identifier("cobblemounts:player_jumped");
    public static final Identifier PLAYER_CROUCH_PACKET = new Identifier("cobblemounts:player_crouchedplay");
    public static final String MOD_ID = "Cobblemounts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @SubscribeEvent
    public static void onInitializeClient(FMLClientSetupEvent event) {
        ClientTickEvent.CLIENT_POST.register(client -> {
            while (client.options.jumpKey.wasPressed()) {
                if (client.player != null) {
                    PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                    passedData.writeUuid(client.player.getUuid());
                    NetworkManager.sendToServer(PLAYER_JUMP_PACKET, passedData);
                }
            }
            while (client.options.sneakKey.wasPressed()) {
                if (client.player != null) {
                    PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                    passedData.writeUuid(client.player.getUuid());
                    NetworkManager.sendToServer(PLAYER_CROUCH_PACKET, passedData);
                }
            }
        });
    }
}