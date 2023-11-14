package net.ioixd;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.ioixd.Cobblemounts.MOD_ID;
import static net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import static net.minecraft.server.command.CommandManager.literal;

@Mod(MOD_ID)
public class Cobblemounts {
	public static final String MOD_ID = "cobblemounts";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Config CONFIG = new Config();

	public Cobblemounts() {
		IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();
		EventBuses.registerModEventBus(MOD_ID, MOD_BUS);

		CommandRegistrationEvent.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) -> dispatcher.register(literal("cobblemounts")
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

		InteractionEvent.INTERACT_ENTITY.register(new InteractionEvent.InteractEntity() {
			@Override
			public EventResult interact(PlayerEntity player, Entity entity, Hand hand) {
				if (entity instanceof PokemonEntity pkmnEntity) {
					ServerPlayerEntity serverPlayer = pkmnEntity.getPokemon().getOwnerPlayer();
					if (serverPlayer != null) {
						if (serverPlayer.getUuid() == player.getUuid()) {
							if (!CONFIG.list.isEmpty()) {
								switch (CONFIG.listUse) {
									case WHITELIST -> {
										if (!CONFIG.list
												.contains(pkmnEntity.getPokemon().getSpecies().getName().toLowerCase())) {
											return EventResult.pass();
										}
									}
									case BLACKLIST -> {
										if (CONFIG.list
												.contains(pkmnEntity.getPokemon().getSpecies().getName().toLowerCase())) {
											return EventResult.pass();
										}
									}
									default -> {
									}
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
				return EventResult.pass();
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, new Identifier("cobblemounts:player_jumped"), new PlayerJump());
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, new Identifier("cobblemounts:player_crouched"), new PlayerCrouch());
	}
}