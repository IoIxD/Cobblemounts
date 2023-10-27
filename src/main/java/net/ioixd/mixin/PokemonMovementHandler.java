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
import net.minecraft.util.math.Box;
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
                if (isFlying.get()) {
                    return;
                }
                if (movement.z > 0.0) {
                    World world = living.getWorld();
                    travelWhilePushingOut(player.getRotationVector(), living, player, world);
                    BlockPos forwardPos = getBlockPos(living, player);
                    if (!isBlockPosTransparent(forwardPos, world)) {
                        BlockPos upperPos = new BlockPos(forwardPos.getX(), forwardPos.getY() + 1, forwardPos.getZ());
                        if (isBlockPosTransparent(upperPos, world)) {
                            BlockPos upperUpperPos = new BlockPos(upperPos.getX(), upperPos.getY() + 1,
                                    upperPos.getZ());
                            if (isBlockPosTransparent(upperUpperPos, world)) {
                                living.teleport(upperPos.getX(), upperPos.getY(), upperPos.getZ());
                            }
                        }
                    }
                } else if (movement.z < 0.0) {
                    living.travel(player.getRotationVector().multiply(-1.0, -1.0, -1.0));
                }
            }
        }
    }

    private static void travelWhilePushingOut(Vec3d rot, PokemonEntity living, PlayerEntity player, World world) {
        living.travel(rot);
        while (!isBlockPosTransparent(player.getBlockPos(), world) &&
                !isBlockPosTransparent(player.getBlockPos().add(0, 1, 0), world)) {
            System.out.println("pushing");
            living.travel(rot.multiply(-1.0, -1.0, -1.0));
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
