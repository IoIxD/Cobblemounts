package net.ioixd;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerJump implements ServerPlayNetworking.PlayChannelHandler  {
    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        Entity vehicle = player.getVehicle();
        if(vehicle != null) {
            if(vehicle.isOnGround()) {
                if(vehicle instanceof PokemonEntity living) {
                    Pokemon pokemon = living.getPokemon();
                    living.fallDistance = -1024.0f;
                    Vec3d vec3d = living.getVelocity();
                    living.addVelocity(vec3d.x, EntityHelper.GetJumpVelocityMultiplier(living), vec3d.z);
                    float f = living.getYaw() * ((float)Math.PI / 180);
                    if (living.isMoving().get()) {
                        living.addVelocity(-MathHelper.sin(f) * (pokemon.getSpeed() / 12.0f), 0.0, MathHelper.cos(f) * (pokemon.getSpeed() / 12.0f));
                    }
                    living.velocityDirty = true;
                }

            }

        }
    }
}
