package net.ioixd;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
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

public class PlayerCrouch implements ServerPlayNetworking.PlayChannelHandler  {
    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        Entity vehicle = player.getVehicle();
        if(vehicle != null) {
            if(vehicle instanceof PokemonEntity living) {
                living.initGoals();
            }
        }
    }
}
