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
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.AirBlockItem;
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
import net.ioixd.PlayerJump;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public class Cobblemounts implements ModInitializer {
	public static final String MOD_ID = "Cobblemounts";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		UseEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {
			if (entity instanceof PokemonEntity pkmnEntity) {
				ServerPlayerEntity serverPlayer = pkmnEntity.getPokemon().getOwnerPlayer();
				if (serverPlayer != null) {
					if (serverPlayer.getUuid() == player.getUuid()) {
						player.startRiding(entity, false);
						pkmnEntity.clearGoalsAndTasks();
					}
				}
			}
			return ActionResult.PASS;
		});
		ServerPlayNetworking.registerGlobalReceiver(new Identifier("cobblemounts:player_jumped"), new PlayerJump());
		ServerPlayNetworking.registerGlobalReceiver(new Identifier("cobblemounts:player_crouched"), new PlayerCrouch());
	}
}