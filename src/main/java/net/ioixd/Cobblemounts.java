package net.ioixd;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import static net.minecraft.server.command.CommandManager.*;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ioixd.Config.ListUse;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.item.group.CobblemonItemGroups;

import kotlin.jvm.JvmStatic;

public class Cobblemounts implements ModInitializer {
	public static final String MOD_ID = "Cobblemounts";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Config CONFIG = new Config();

	@Override
	public void onInitialize() {

		CommandRegistrationCallback.EVENT
				.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("cobblemounts")
						.then(literal("reload")
								.executes(context -> {
									try {
										Cobblemounts.CONFIG.update();
										context.getSource().sendMessage(Text.literal("Reloaded configuration file."));
									} catch (Exception ex) {
										context.getSource().sendMessage(Text.literal(ex.getMessage()));
									}
									return 1;
								}))));

		UseEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {
			if (entity instanceof PokemonEntity pkmnEntity) {
				ServerPlayerEntity serverPlayer = pkmnEntity.getPokemon().getOwnerPlayer();
				if (serverPlayer != null) {
					if (serverPlayer.getUuid() == player.getUuid()) {
						if (!CONFIG.list.isEmpty()) {
							switch (CONFIG.listUse) {
								case WHITELIST:
									if (!CONFIG.list
											.contains(pkmnEntity.getPokemon().getSpecies().getName().toLowerCase())) {
										return ActionResult.PASS;
									}
									break;
								case BLACKLIST:
									if (CONFIG.list
											.contains(pkmnEntity.getPokemon().getSpecies().getName().toLowerCase())) {
										return ActionResult.PASS;
									}
									break;
								default:
									break;
							}

						}
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