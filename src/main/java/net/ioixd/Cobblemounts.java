package net.ioixd;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public class Cobblemounts implements ModInitializer {
	public static final String MOD_ID = "Cobblemounts";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		UseEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {
			if (entity instanceof PokemonEntity) {
				PokemonEntity pkmnEntity = (PokemonEntity) entity;
				ServerPlayerEntity serverPlayer = pkmnEntity.getPokemon().getOwnerPlayer();
				if (serverPlayer != null) {
					if (serverPlayer.getUuid() == player.getUuid()) {
						player.startRiding(entity, false);
					}
				}
			}
			return ActionResult.PASS;
		});
		ServerPlayNetworking.registerGlobalReceiver(new Identifier("cobblemounts:player_jumped"), new ServerPlayNetworking.PlayChannelHandler() {
			@Override
			public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
				Entity vehicle = player.getVehicle();
				if(vehicle != null) {
					if(vehicle.isOnGround()) {
						if(vehicle instanceof PokemonEntity living) {
							living.fallDistance = -10.0f;
							Vec3d vec3d = living.getVelocity();
							Direction moveDir = living.getMovementDirection();
							living.setVelocity(vec3d.x, EntityHelper.GetJumpVelocityMultiplier(living), vec3d.z);
							if (living.isSprinting()) {
								float f = living.getYaw() * ((float)Math.PI / 180);
								living.setVelocity(living.getVelocity().add(-MathHelper.sin(f) * 0.2f, 0.0, MathHelper.cos(f) * 0.2f));
							}
							living.velocityDirty = true;
						}

					}

				}
			}
		});
	}
}