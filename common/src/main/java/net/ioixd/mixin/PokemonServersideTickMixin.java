package net.ioixd.mixin;


import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.ioixd.Cobblemounts;
import net.ioixd.MountIsMoving;
import net.ioixd.client.CobblemountsClient;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
serverside logic here
 */
@Mixin(PokemonEntity.class)
public abstract class PokemonServersideTickMixin extends LivingEntity {

    protected PokemonServersideTickMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick",at = @At("TAIL"))
    protected void tick(CallbackInfo ci) {
        var pokemon = (PokemonEntity) (Object) this;
        var envcfg = pokemon.getWorld() instanceof ServerWorld ? Cobblemounts.CONFIG : CobblemountsClient.SYNCED_CONFIG;
        var pokemonData = pokemon.getPokemon();
        if (!(this.getFirstPassenger() instanceof PlayerEntity))
            return;
        pokemon.fallDistance = 0;
        var mount = (MountIsMoving) (Object) this;
        Block water = pokemon.getBlockStateAtPos().getBlock();
        boolean inLiquid = water instanceof FluidBlock;
        mount.mount_setMoving(this.getFirstPassenger().getVelocity().multiply(1, 0, 1).length() > 0.01);
        var pokemonName = pokemonData.getSpecies().getName().toLowerCase();
        pokemonData.getTypes().forEach(ty -> {
            var name = ty.getName();
            if(envcfg.alsoFlyList.contains(pokemonName)){
                name = "flying";
            }
            switch (name) {
                case "water":
                case "flying":
                    boolean condition;
                    EntityPose animation;
                    boolean flying;
                    switch (ty.getName()) {
                        case "water":
                            if (!envcfg.allowSwimming) {
                                return;
                            }
                            condition = inLiquid;
                            animation = EntityPose.SWIMMING;
                            flying = false;
                            break;
                        case "flying":
                            if (!envcfg.allowSwimming) {
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
        if (envcfg.allowFlying) {
            pokemonData.getTypes().forEach(ty -> {
                var name = ty.getName();
                if(envcfg.alsoFlyList.contains(pokemonName)){
                    name = "flying";
                }
                if (name.equals("flying")) {
                    pokemon.setNoGravity(!pokemon.isOnGround());
                    if (pokemon.isOnGround() && pokemon.getPose() == EntityPose.FALL_FLYING) {
                        pokemon.setPose(EntityPose.STANDING);
                        pokemon.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false);
                        pokemon.updateVelocity(1.0f, pokemon.getFirstPassenger().getRotationVector());
                    }
                }
            });
        }
    }
}
