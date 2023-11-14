package net.ioixd;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

public class PlayerCrouch implements NetworkManager.NetworkReceiver {

    @Override
    public void receive(PacketByteBuf packetByteBuf, NetworkManager.PacketContext packetContext) {
        Entity vehicle = packetContext.getPlayer().getVehicle();
        if (vehicle != null) {
            if (vehicle instanceof PokemonEntity living) {
                living.initGoals();
            }
        }
    }
}
