package net.ioixd.mixin;

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonFloatingState;
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose;
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.blockpredicate.SolidBlockPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository;

@Mixin(PlayerEntity.class)
public class PlayerMixin {
    int ticksInLiquid = 0;

    @Inject(at = @At("HEAD"), method = "travel", locals = LocalCapture.CAPTURE_FAILHARD)
    private void travel(Vec3d movement, CallbackInfo info) {
        System.out.println("=============\ntravel function called!");
        PlayerEntity player = ((PlayerEntity) (Object) this);
        Entity entity = player.getVehicle();
        if (entity != null) {
            System.out.println("entity isn't null");
            if (entity instanceof PokemonEntity living) {
                System.out.println("entity is Pokemon");
                Pokemon pokemon = living.getPokemon();

                living.bodyYaw = living.headYaw = player.getYaw();
                entity.setPitch(entity.getPitch());

                living.setMovementSpeed(player.getMovementSpeed() * (pokemon.getSpeed() / 12.0f));

                Block water = living.getBlockStateAtPos().getBlock();
                boolean inLiquid = water instanceof FluidBlock;


                float speedModifier = pokemon.isLegendary() ? 0.0f : 0.05f;

                System.out.println("checking pokemon types");
                pokemon.getTypes().forEach(ty -> {
                    switch(ty.getName()) {
                        case "water":
                                if(inLiquid) {
                                    if (movement.z != 0.0) {
                                        living.updateVelocity((pokemon.getSpeed() / 500.0f) + speedModifier,
                                                player.getRotationVector());
                                    }
                                    living.setPose(EntityPose.SWIMMING);
                                } else {
                                    living.updateVelocity(0.0f, player.getRotationVector());
                                    living.setPose(EntityPose.STANDING);
                                }
                            break;
                        case "flying":
                            if (!living.isOnGround() && !inLiquid) {
                                if (movement.z != 0.0) {
                                    living.updateVelocity((pokemon.getSpeed() / 500.0f) + speedModifier,
                                            player.getRotationVector());
                                }
                                living.setPose(EntityPose.FALL_FLYING);
                                living.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true);
                            } else {
                                living.updateVelocity(0.0f, player.getRotationVector());
                                living.setPose(EntityPose.STANDING);
                            }
                            break;
                    }
                });
                System.out.println("checking movement");
                if (movement.z > 0.0) {
                    System.out.println("movement.z greater then zero");
                    living.travel(player.getRotationVector());
                    World world = living.getWorld();
                    BlockPos forwardPos = switch (player.getMovementDirection()) {
                        case NORTH -> living.getBlockPos().north();
                        case SOUTH -> living.getBlockPos().south();
                        case EAST -> living.getBlockPos().east();
                        case WEST -> living.getBlockPos().west();
                        default -> living.getBlockPos();
                    };
                    BlockState state =world.getBlockState(forwardPos);
                    Block forwardBlock = state.getBlock();
                    if(!forwardBlock.isTransparent(state, world, forwardPos) && !(forwardBlock instanceof FluidBlock)) {
                        BlockPos upperPos = new BlockPos(forwardPos.getX(), forwardPos.getY() + 1, forwardPos.getZ());
                        BlockState upperState = world.getBlockState(upperPos);
                        Block upperBlock = upperState.getBlock();
                        if(upperBlock.isTransparent(upperState, world, upperPos)) {
                            living.teleport(upperPos.getX(), upperPos.getY(), upperPos.getZ());
                        }
                    }

                } else if (movement.z < 0.0) {
                    System.out.println("movement.z lesser then zero");
                    living.travel(player.getRotationVector().multiply(-1.0, -1.0, -1.0));
                }
            }
        }
        System.out.println("=============\n");
    }

}
