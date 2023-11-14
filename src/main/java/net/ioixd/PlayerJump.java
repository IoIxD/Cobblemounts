package net.ioixd;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerJump implements NetworkManager.NetworkReceiver {
    @Override
    public void receive(PacketByteBuf packetByteBuf, NetworkManager.PacketContext packetContext) {
        var player = packetContext.getPlayer();
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            if (vehicle.isOnGround()) {
                if (vehicle instanceof PokemonEntity living) {
                    Pokemon pokemon = living.getPokemon();
                    player.fallDistance = -1024.0f;
                    living.fallDistance = -1024.0f;
                    Vec3d vec3d = living.getVelocity();
                    living.setVelocity(vec3d.x, 0.75, vec3d.z);
                    float f = living.getYaw() * ((float) Math.PI / 180);
                    if (living.isMoving().get()) {
                        living.addVelocity(-MathHelper.sin(f) * (pokemon.getSpeed() / 12.0f), 0.0,
                                MathHelper.cos(f) * (pokemon.getSpeed() / 12.0f));
                    }
                    living.velocityDirty = true;
                }
            }
        }
    }
}
