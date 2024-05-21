package net.ioixd.client.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ioixd.client.CobblemountsClient;
import net.ioixd.MountIsMoving;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.TameableShoulderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(PokemonEntity.class)
public abstract class PokemonMovementHandler extends LivingEntity {
    @Shadow
    public abstract void playAmbientSound();

    int ticksInLiquid = 0;

    protected PokemonMovementHandler(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void tickControlled(PlayerEntity player, Vec3d movement) {
        // PlayerEntity player = (PlayerEntity) (Object) this;
        // Entity entity = player.getVehicle();
        var pokemon = (PokemonEntity) (Object) this;
        // if (entity == null) return;
        // if (!(entity instanceof PokemonEntity pokemon)) return;

        movement = new Vec3d(player.sidewaysSpeed, 0, player.forwardSpeed);
        World world = pokemon.getWorld();
        Pokemon pokemonData = pokemon.getPokemon();
        pokemon.setYaw(player.getYaw());
        pokemon.setPitch(pokemon.getPitch());
        pokemon.setHeadYaw(player.getYaw());

        Block water = pokemon.getBlockStateAtPos().getBlock();
        boolean inLiquid = water instanceof FluidBlock;


        AtomicBoolean isFlying = new AtomicBoolean(false);
        Vec3d moveXZ = movement;// movement.rotateY((float) Math.toRadians(-player.getYaw()));
        Vec3d forward = player.getRotationVector().normalize().multiply(movement.z);

        Vec3d left = movement.multiply(1, 0, 0).rotateY((float) Math.toRadians(-player.getYaw()));

        Vec3d flyMove = forward.add(left);

        pokemonData.getTypes().forEach(ty -> {
            var pokemonName = pokemonData.getSpecies().getName().toLowerCase();
            var name = ty.getName();
            if (CobblemountsClient.SYNCED_CONFIG.alsoFlyList.contains(pokemonName)) {
                name = "flying";
            }
            switch (name) {
                case "water":
                case "flying":
                    boolean condition;
                    EntityPose animation;
                    boolean flying;
                    switch (name) {
                        case "water":
                            if (!CobblemountsClient.SYNCED_CONFIG.allowSwimming) {
                                return;
                            }
                            condition = inLiquid;
                            animation = EntityPose.SWIMMING;
                            flying = false;
                            break;
                        case "flying":
                            if (!CobblemountsClient.SYNCED_CONFIG.allowFlying) {
                                return;
                            }
                            condition = !pokemon.isOnGround() && !inLiquid;
                            animation = EntityPose.FALL_FLYING;
                            flying = true;
                            break;
                        // We will never hit this part but we need to set the values anyways
                        // to make the compiler happy.
                        default:
                            condition = false;
                            animation = null;
                            flying = false;
                            break;
                    }

                    if (condition) {
                        // Let the spaghetti begin. Now may be time to say that I have NEVER coded Java before :s
                        // First, decide between swimming or flying scalars
                        float speedScalar = (float) CobblemountsClient.SYNCED_CONFIG.swimSpeedScalar;
                        float speedCap = (float) CobblemountsClient.SYNCED_CONFIG.swimSpeedCap;
                        boolean isSpeedCapped = CobblemountsClient.SYNCED_CONFIG.swimCappedSpeed;
                        boolean useLogScaling = CobblemountsClient.SYNCED_CONFIG.swimUseLogScaling;
                        
                        if (flying) {
                            speedScalar = (float) CobblemountsClient.SYNCED_CONFIG.flightSpeedScalar;
                            speedCap = (float) CobblemountsClient.SYNCED_CONFIG.flightSpeedCap;
                            isSpeedCapped = CobblemountsClient.SYNCED_CONFIG.flightCappedSpeed;
                            useLogScaling = CobblemountsClient.SYNCED_CONFIG.flightUseLogScaling;
                        }
                        // Then, do some additional checks for legendaries
                        float legendaryModifier = pokemonData.isLegendary() ? 0.0f
                            : (float) CobblemountsClient.SYNCED_CONFIG.legendaryModifier;
                        boolean isLegendary = pokemonData.isLegendary();
                 
                        // Fun fact, in a previous itteration, I repeated this code twice with a check to see if we
                        // were opperating in swim mode or flight mode. I need coffee.
                        // elifelifelifelifelifelifelifelifelifelifelif...
                        float flyingSpeed = 0.0f;
                        if (flyMove.z != 0.0) {
                            // Step 1 : check if using log or lin scaling
                            if (!useLogScaling) {
                                // Step 2 : compute the base flight speed
                                flyingSpeed = ((pokemonData.getSpeed() / 256.0f) * (speedScalar / 2.0f));
                                // Step 3 : check if the cobblemon is legendary
                                 if (isLegendary) {
                                    // Step 3.1 : check if speed cap is applied
                                    if (isSpeedCapped) {
                                        // Step 3.2 : check if speed cap breaking is allowed
                                        if (CobblemountsClient.SYNCED_CONFIG.legendaryModifierCapBreak) {
                                            // Step 3.3 : compute final speed based on broken cap
                                            if (flyingSpeed >= speedCap) {
                                                flyingSpeed = (speedCap + ((legendaryModifier * speedScalar) / 2.0f));
                                            } else {
                                                flyingSpeed = (flyingSpeed + ((legendaryModifier * speedScalar) / 2.0f));
                                            }
                                        } else {
                                            flyingSpeed = (flyingSpeed + ((legendaryModifier * speedScalar) / 2.0f));
                                            if (flyingSpeed >= speedCap) {
                                                flyingSpeed = speedCap;
                                            }
                                        }
                                    } else {
                                        flyingSpeed = (flyingSpeed + ((legendaryModifier * speedScalar) / 2.0f));
                                    }
                                } else {
                                    // If the cobblemon isn't legendary, at least there's no cap-breaking shenanigans.
                                    if (isSpeedCapped) {
                                        if (flyingSpeed >= speedCap) {
                                            flyingSpeed = speedCap;
                                        }
                                    }
                                }
                            } else {
                                // We're not done yet ! We still need to do the SAME EXACT THING but for LOGARITHMIC SCALING !!!!!!
                                // Step 2
                                flyingSpeed = 2.5f * (float)Math.log((pokemonData.getSpeed() + speedScalar) / speedScalar);
                                // Step 3
                                if (isLegendary) {
                                    // Step 3.1
                                    if (isSpeedCapped) {
                                        // Step 3.2
                                        if (CobblemountsClient.SYNCED_CONFIG.legendaryModifierCapBreak) {
                                            // Step 3.3
                                            if (flyingSpeed >= speedCap) {
                                                flyingSpeed = (speedCap + legendaryModifier);
                                            } else {
                                                flyingSpeed = (flyingSpeed + legendaryModifier);
                                            }
                                        } else {
                                            flyingSpeed = (flyingSpeed + legendaryModifier);
                                            if (flyingSpeed >= speedCap) {
                                                flyingSpeed = speedCap;
                                            }
                                        }
                                    } else {
                                        flyingSpeed = (flyingSpeed + legendaryModifier);
                                    }
                                } else {
                                    if (isSpeedCapped) {
                                        if (flyingSpeed >= speedCap) {
                                           flyingSpeed = speedCap;
                                        }
                                    }
                                }

                            }
                            pokemon.move(MovementType.SELF, flyMove.multiply(flyingSpeed));
                            isFlying.set(true);
                        }
                        if (flying) {
                            pokemon.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true);
                        }
                        pokemon.setPose(animation);
                    } else {
                        pokemon.setPose(EntityPose.STANDING);
                    }
                    break;
            }        
        });
        if (!isFlying.get()) {
            if (!(player instanceof ServerPlayerEntity) && MinecraftClient.getInstance().options.jumpKey.isPressed()
                    && pokemon.isOnGround()) {
                pokemon.addVelocity(0, 0.7, 0);
            }
            pokemon.travel(moveXZ);
            BlockPos forwardPos = getBlockPos(pokemon, player);
            if (!isBlockPosTransparent(forwardPos, world)) {
                BlockPos upperPos = new BlockPos(forwardPos.getX(), forwardPos.getY() + 1, forwardPos.getZ());
                if (isBlockPosTransparent(upperPos, world)) {
                    BlockPos upperUpperPos = new BlockPos(upperPos.getX(), upperPos.getY() + 1,
                            upperPos.getZ());
                    if (isBlockPosTransparent(upperUpperPos, world)) {
                        pokemon.teleport(upperPos.getX(), upperPos.getY(), upperPos.getZ());
                    }
                }
            }
        }
    }

    private static boolean isBlockPosTransparent(BlockPos pos, World world) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return block.isTransparent(state, world, pos) && !(block instanceof FluidBlock);
    }

    private static BlockPos getBlockPos(PokemonEntity living, PlayerEntity player) {
        BlockPos forwardPos = living.getBlockPos();
        int width = (int) Math.floor(living.getWidth());
        for (int i = 0; i < width; i++) {
            forwardPos = switch (player.getMovementDirection()) {
                case NORTH -> forwardPos.north();
                case SOUTH -> forwardPos.south();
                case EAST -> forwardPos.east();
                case WEST -> forwardPos.west();
                default -> forwardPos;
            };
        }
        return forwardPos;
    }

}
