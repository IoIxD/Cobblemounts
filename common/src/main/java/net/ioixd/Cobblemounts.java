package net.ioixd;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

public class Cobblemounts  {
	public static final String MOD_ID = "cobblemounts";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier CONFIG_SYNC_ID = new Identifier(MOD_ID,"sync");
	public static final Config CONFIG = new Config();
	public static void onInitialize() {
		PlayerEvent.PLAYER_JOIN.register(player -> {
			var buf = new PacketByteBuf(Unpooled.buffer());
			CONFIG.write(buf);
			NetworkManager.sendToPlayer(player,CONFIG_SYNC_ID, buf);
		});
		CommandRegistrationEvent.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) -> dispatcher.register(literal("cobblemounts")
				.then(literal("reload")
						.executes(context -> {
							try {
								Cobblemounts.CONFIG.update();
								var buf = new PacketByteBuf(Unpooled.buffer());
								CONFIG.write(buf);
								NetworkManager.sendToPlayers(
										context.getSource().getServer().getPlayerManager().getPlayerList(),
										CONFIG_SYNC_ID,buf);

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
	}
}