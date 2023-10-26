package net.ioixd.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ioixd.Cobblemounts;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(PlayerEntity.class)
public class PokemonMovementHandler {
    int ticksInLiquid = 0;

    @Inject(at = @At("HEAD"), method = "travel", locals = LocalCapture.CAPTURE_FAILHARD)
    private void travel(Vec3d movement, CallbackInfo info) {
        PlayerEntity player = ((PlayerEntity) (Object) this);
        Entity entity = player.getVehicle();
        if (entity != null) {
            if (entity instanceof PokemonEntity living) {
                Pokemon pokemon = living.getPokemon();

                living.bodyYaw = living.headYaw = player.getYaw();
                entity.setPitch(entity.getPitch());

                living.setMovementSpeed(player.getMovementSpeed() * (pokemon.getSpeed() / 12.0f));

                Block water = living.getBlockStateAtPos().getBlock();
                boolean inLiquid = water instanceof FluidBlock;

                float speedModifier = pokemon.isLegendary() ? 0.0f : (float) Cobblemounts.CONFIG.legendaryModifier;
                AtomicBoolean isFlying = new AtomicBoolean(false);

                double movementSpeed_ = (pokemon.getSpeed() / 500.0f) + speedModifier;
                if (Cobblemounts.CONFIG.cappedSpeed) {
                    if (movementSpeed_ >= Cobblemounts.CONFIG.flyingSpeedCap) {
                        movementSpeed_ = Cobblemounts.CONFIG.flyingSpeedCap;
                    }
                }
                float movementSpeed = (float) movementSpeed_;

                pokemon.getTypes().forEach(ty -> {
                    switch (ty.getName()) {
                        case "water":
                        case "flying":
                            boolean condition;
                            EntityPose animation;
                            boolean flying;
                            switch (ty.getName()) {
                                case "water":
                                    if (!Cobblemounts.CONFIG.allowSwimming) {
                                        return;
                                    }
                                    condition = inLiquid;
                                    animation = EntityPose.SWIMMING;
                                    flying = false;
                                    break;
                                case "flying":
                                    if (!Cobblemounts.CONFIG.allowFlying) {
                                        return;
                                    }
                                    condition = !living.isOnGround() && !inLiquid;
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
                                if (movement.z != 0.0) {
                                    living.updateVelocity(movementSpeed, player.getRotationVector());
                                    living.move(MovementType.SELF, living.getVelocity());
                                    isFlying.set(true);
                                }
                                if (flying) {
                                    living.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true);
                                }
                                living.setPose(animation);
                            } else {
                                living.updateVelocity(0.0f, player.getRotationVector());
                                living.setPose(EntityPose.STANDING);
                            }
                            break;
                    }
                });
                if (movement.z > 0.0) {
                    if (!isFlying.get()) {
                        living.travel(player.getRotationVector());
                    }
                    World world = living.getWorld();
                    BlockPos forwardPos = switch (player.getMovementDirection()) {
                        case NORTH -> living.getBlockPos().north();
                        case SOUTH -> living.getBlockPos().south();
                        case EAST -> living.getBlockPos().east();
                        case WEST -> living.getBlockPos().west();
                        default -> living.getBlockPos();
                    };
                    BlockState state = world.getBlockState(forwardPos);
                    Block forwardBlock = state.getBlock();
                    if (!forwardBlock.isTransparent(state, world, forwardPos)
                            && !(forwardBlock instanceof FluidBlock)) {
                        BlockPos upperPos = new BlockPos(forwardPos.getX(), forwardPos.getY() + 1, forwardPos.getZ());
                        BlockState upperState = world.getBlockState(upperPos);
                        Block upperBlock = upperState.getBlock();
                        if (upperBlock.isTransparent(upperState, world, upperPos)) {
                            living.teleport(upperPos.getX(), upperPos.getY(), upperPos.getZ());
                        }
                    }
                } else if (movement.z < 0.0) {
                    if (!isFlying.get()) {
                        living.travel(player.getRotationVector().multiply(-1.0, -1.0, -1.0));
                    }
                }
            }
        }
    }

}
