package net.ioixd.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ioixd.CobblemountsClient;
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
    @Shadow public abstract void playAmbientSound();

    int ticksInLiquid = 0;

    protected PokemonMovementHandler(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void tickControlled(PlayerEntity player, Vec3d movement) {
        //PlayerEntity player = (PlayerEntity) (Object) this;
        //Entity entity = player.getVehicle();
        var pokemon = (PokemonEntity) (Object) this;
        //if (entity == null) return;
        //if (!(entity instanceof PokemonEntity pokemon)) return;


        movement = new Vec3d(player.sidewaysSpeed,0,player.forwardSpeed);
        World world = pokemon.getWorld();
        Pokemon pokemonData = pokemon.getPokemon();
        pokemon.setYaw(player.getYaw());
        pokemon.setPitch(pokemon.getPitch());
        pokemon.setHeadYaw(player.getYaw());

        Block water = pokemon.getBlockStateAtPos().getBlock();
        boolean inLiquid = water instanceof FluidBlock;

        float speedModifier = pokemonData.isLegendary() ? 0.0f : (float) CobblemountsClient.SYNCED_CONFIG.legendaryModifier;
        AtomicBoolean isFlying = new AtomicBoolean(false);
        Vec3d moveXZ = movement;//movement.rotateY((float) Math.toRadians(-player.getYaw()));
        Vec3d forward = player.getRotationVector().normalize().multiply(movement.z);
        //forward = movement.multiply(0, 0, 1).rotateY((float) Math.toRadians(-player.getYaw()));

        Vec3d left = movement.multiply(1, 0, 0).rotateY((float) Math.toRadians(-player.getYaw()));

        Vec3d flyMove = forward.add(left);
        Vec3d moveY = movement.multiply(0, 0, 0);


        double movementSpeed_ = (pokemonData.getSpeed() / 500.0f) + speedModifier;
        if (CobblemountsClient.SYNCED_CONFIG.cappedSpeed) {
            if (movementSpeed_ >= CobblemountsClient.SYNCED_CONFIG.flyingSpeedCap) {
                movementSpeed_ = CobblemountsClient.SYNCED_CONFIG.flyingSpeedCap;
            }
        }
        double[] movementSpeed = new double[]{movementSpeed_};
        var pokemonName = pokemonData.getSpecies().getName().toLowerCase();
        pokemonData.getTypes().forEach(ty -> {
            var name = ty.getName();
            if(CobblemountsClient.SYNCED_CONFIG.alsoFlyList.contains(pokemonName)){
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
                    ;
                    if (condition) {
                        if (flyMove.z != 0.0) {
                            //pokemon.updateVelocity(movementSpeed, player.getRotationVector());
                            pokemon.move(MovementType.SELF, flyMove.multiply(movementSpeed[0]));
                            isFlying.set(true);
                        }
                        if (flying) {
                            pokemon.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true);
                        }
                        pokemon.setPose(animation);
                    } else {
                        //pokemon.updateVelocity(0.0f, moveXZ);
                        pokemon.setPose(EntityPose.STANDING);
                    }
                    break;
            }
        });
        if (!isFlying.get()) {
            if (!(player instanceof ServerPlayerEntity) && MinecraftClient.getInstance().options.jumpKey.wasPressed() && pokemon.isOnGround()) {
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
