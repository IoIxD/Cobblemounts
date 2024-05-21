package net.ioixd.client.mixin;

import com.cobblemon.mod.common.api.types.ElementalType;
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

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

        var pokemonName = pokemonData.getSpecies().getName().toLowerCase();

        ArrayList<ElementalType> types_raw = new ArrayList<>();
        pokemonData.getTypes().iterator().forEachRemaining(types_raw::add);
        var types = types_raw.stream()
                .map(ty -> ty.getName())
                .collect(Collectors.toList());

        if (CobblemountsClient.SYNCED_CONFIG.alsoFlyList.contains(pokemonName)) {
            types.add("flying");
        }

        if (CobblemountsClient.SYNCED_CONFIG.alsoSwimList.contains(pokemonName)) {
            types.add("water");
        }

        types.forEach(name -> {
            switch (name) {
                case "water":
                case "flying":
                    boolean condition;
                    EntityPose animation;
                    boolean flying;
                    float speedScalar;
                    float speedCap;
                    boolean isSpeedCapped;
                    boolean useLogScaling;

                    switch (name) {
                        case "water":
                            if (!CobblemountsClient.SYNCED_CONFIG.allowSwimming) {
                                return;
                            }
                            condition = inLiquid;
                            animation = EntityPose.SWIMMING;
                            flying = false;
                            speedScalar = (float) CobblemountsClient.SYNCED_CONFIG.swimSpeedScalar;
                            speedCap = (float) CobblemountsClient.SYNCED_CONFIG.swimSpeedCap;
                            isSpeedCapped = CobblemountsClient.SYNCED_CONFIG.swimCappedSpeed;
                            useLogScaling = CobblemountsClient.SYNCED_CONFIG.swimUseLogScaling;

                            break;
                        case "flying":
                            if (!CobblemountsClient.SYNCED_CONFIG.allowFlying) {
                                return;
                            }
                            condition = !pokemon.isOnGround() && !inLiquid;
                            animation = EntityPose.FALL_FLYING;
                            flying = true;
                            speedScalar = (float) CobblemountsClient.SYNCED_CONFIG.flightSpeedScalar;
                            speedCap = (float) CobblemountsClient.SYNCED_CONFIG.flightSpeedCap;
                            isSpeedCapped = CobblemountsClient.SYNCED_CONFIG.flightCappedSpeed;
                            useLogScaling = CobblemountsClient.SYNCED_CONFIG.flightUseLogScaling;
                            break;
                        // We will never hit this part but we need to set the values anyways
                        // to make the compiler happy.
                        default:
                            condition = false;
                            animation = null;
                            flying = false;
                            speedScalar = 0.0f;
                            speedCap = 0.0f;
                            isSpeedCapped = false;
                            useLogScaling = true;
                            break;
                    }

                    if (condition) {
                        float legendaryModifier = pokemonData.isLegendary() ? 0.0f
                                : (float) CobblemountsClient.SYNCED_CONFIG.legendaryModifier;
                        boolean isLegendary = pokemonData.isLegendary();

                        float flyingSpeed;
                        if (flyMove.z != 0.0) {
                            if (!useLogScaling) {
                                flyingSpeed = ((pokemonData.getSpeed() / 256.0f) * (speedScalar / 2.0f));
                            } else {
                                flyingSpeed = 2.5f
                                        * (float) Math.log((pokemonData.getSpeed() + speedScalar) / speedScalar);
                            }
                            if (isLegendary) {
                                if (isSpeedCapped) {
                                    if (CobblemountsClient.SYNCED_CONFIG.legendaryModifierCapBreak) {
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
