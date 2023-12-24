package net.ioixd;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

public class Cobblemounts implements ModInitializer {
	public static final String MOD_ID = "cobblemounts";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier CONFIG_SYNC_ID = new Identifier(MOD_ID,"sync");
	public static final Config CONFIG = new Config();
	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.JOIN.register((networkHandler,packetSender,server)->{
			ServerPlayNetworking.send(networkHandler.player,CONFIG);
		});
		CommandRegistrationCallback.EVENT
				.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("cobblemounts")
						.then(literal("reload")
								.executes(context -> {
									try {
										Cobblemounts.CONFIG.update();
										context.getSource().getServer().getPlayerManager().getPlayerList()
												.forEach(p-> ServerPlayNetworking.send(p,CONFIG));
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
						Item item = player.getMainHandStack().getItem();
						if (!item.getTranslationKey().contains("item.cobblemon")) {
							player.startRiding(entity, false);
							pkmnEntity.clearGoalsAndTasks();
						}

					}
				}
			}
			return ActionResult.PASS;
		});
	}
}