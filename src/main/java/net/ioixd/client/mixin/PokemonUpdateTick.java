package net.ioixd.client.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.ioixd.Cobblemounts;
import net.ioixd.client.CobblemountsClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class PokemonUpdateTick {
    @Inject(at = @At("TAIL"), method = "travel")
    private void tickMovement(Vec3d movement, CallbackInfo info) {
        LivingEntity entity = ((LivingEntity) (Object) this);
        if (entity instanceof PokemonEntity pokemonEntity) {
            if (!pokemonEntity.hasPassengers()) {
                pokemonEntity.setAiDisabled(false);
                return;
            }

            pokemonEntity.setAiDisabled(true);
            Pokemon pokemonData = pokemonEntity.getPokemon();
            Entity firstPassenger = pokemonEntity.getFirstPassenger();
            if (firstPassenger instanceof PlayerEntity player) {
                
                // Make the Pokemon's position match the player's and set their position
                // accordingly.
                float legendaryModifier = pokemonData.isLegendary() ? 0.0f
                    : (float) CobblemountsClient.SYNCED_CONFIG.legendaryModifier;
                boolean isLegendary = pokemonData.isLegendary();
                //float movementSpeed = player.getMovementSpeed() * (pokemonData.getSpeed() / 12.0f) + speedModifier;
                float speedScalar = (float) CobblemountsClient.SYNCED_CONFIG.groundSpeedScalar;
                float speedCap = (float) CobblemountsClient.SYNCED_CONFIG.groundSpeedCap;
                boolean isSpeedCapped = CobblemountsClient.SYNCED_CONFIG.groundCappedSpeed;
                boolean useLogScaling = CobblemountsClient.SYNCED_CONFIG.groundUseLogScaling;
                float movementSpeed = 0.0f;
                if (!useLogScaling) {
                    movementSpeed = player.getMovementSpeed() * ((pokemonData.getSpeed() / 12.0f) * (speedScalar / 2.0f));
                    if (isLegendary) {
                        if (isSpeedCapped) {
                            if (CobblemountsClient.SYNCED_CONFIG.legendaryModifierCapBreak) {
                                if (movementSpeed >= speedCap) {
                                    movementSpeed = (speedCap + ((legendaryModifier * speedScalar) / 2.0f));
                                } else {
                                    movementSpeed = (movementSpeed + ((legendaryModifier * speedScalar) / 2.0f));
                                }
                            } else {
                                movementSpeed = (movementSpeed + ((legendaryModifier * speedScalar) / 2.0f));
                                if (movementSpeed >= speedCap) {
                                    movementSpeed = speedCap;
                                }
                            }
                        } else {
                            movementSpeed = (movementSpeed + ((legendaryModifier * speedScalar) / 2.0f));
                        }
                    } else {
                        if (isSpeedCapped) {
                            if (movementSpeed >= speedCap) {
                                movementSpeed = speedCap;
                            }
                        }
                    }
                } else {
                    movementSpeed = (player.getMovementSpeed() * (2.5f*(float)Math.log((pokemonData.getSpeed() + speedScalar) / speedScalar))) / 5.0f;
                    if (isLegendary) {
                        if (isSpeedCapped) {
                            if (CobblemountsClient.SYNCED_CONFIG.legendaryModifierCapBreak) {
                                if (movementSpeed >= speedCap) {
                                    movementSpeed = (speedCap + legendaryModifier);
                                } else {
                                    movementSpeed = (movementSpeed + legendaryModifier);
                                }
                            } else {
                                movementSpeed = (movementSpeed + legendaryModifier);
                                if (movementSpeed >= speedCap) {
                                    movementSpeed = speedCap;
                                }
                            }
                        } else {
                            movementSpeed = (movementSpeed + legendaryModifier);
                        }
                    } else {
                        if (isSpeedCapped) {
                            if (movementSpeed >= speedCap) {
                                movementSpeed = speedCap;
                            }
                        }
                    }

                }
                pokemonEntity.limbAnimator.setSpeed(pokemonEntity.getMovementSpeed() / 1.3f);

                pokemonEntity.setMovementSpeed(movementSpeed);
                pokemonEntity.setForwardSpeed(0);
                pokemonEntity.setPitch(pokemonEntity.getPitch());

            }
        } else {
            entity.setMovementSpeed(0.1f);
        }
    }
}
