package net.ioixd.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ioixd.Cobblemounts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntity.class)
public class PokemonUpdateTick {
    @Inject(at = @At("TAIL"), method = "travel")
    private void tickMovement(CallbackInfo info) {
        LivingEntity entity = ((LivingEntity) (Object) this);
        if (entity.hasPassengers()) {
            if (entity instanceof PokemonEntity pokemonEntity) {
                Pokemon pokemon = pokemonEntity.getPokemon();
                Entity firstPassenger = entity.getFirstPassenger();
                if (firstPassenger instanceof PlayerEntity player) {
                    // Make the Pokemon's position match the player's and set their position
                    // accordingly.
                    entity.bodyYaw = entity.headYaw = player.getYaw();
                    float speedModifier = pokemon.isLegendary() ? 0.0f : (float) Cobblemounts.CONFIG.legendaryModifier;
                    float movementSpeed = player.getMovementSpeed() * (pokemon.getSpeed() / 12.0f) + speedModifier;
                    System.out.println(movementSpeed + ", " + Cobblemounts.CONFIG.cappedSpeed + ", "
                            + Cobblemounts.CONFIG.speedCap);
                    if (Cobblemounts.CONFIG.cappedSpeed) {
                        if (movementSpeed >= Cobblemounts.CONFIG.speedCap) {
                            movementSpeed = (float) Cobblemounts.CONFIG.speedCap;
                            System.out.println(movementSpeed);
                        }
                    }
                    entity.setMovementSpeed(movementSpeed);
                    entity.setPitch(entity.getPitch());

                    if (Cobblemounts.CONFIG.allowFlying) {
                        // If they're a flying type, set whether they have gravity or not.
                        pokemon.getTypes().forEach(ty -> {
                            if (ty.getName().equals("flying")) {
                                entity.setNoGravity(!entity.isOnGround());
                                if (entity.isOnGround() && pokemonEntity.getPose() == EntityPose.FALL_FLYING) {
                                    pokemonEntity.setPose(EntityPose.STANDING);
                                    pokemonEntity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false);
                                    pokemonEntity.updateVelocity(1.0f,
                                            player.getRotationVector());
                                }
                            }
                        });
                    }

                }
            }
        } else {
            entity.setMovementSpeed(0.1f);
        }
    }
}
