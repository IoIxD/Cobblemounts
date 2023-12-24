package net.ioixd.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to allow player to control pokemon
 */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = "getControllingPassenger",at = @At("HEAD"),cancellable = true)
    void isPokemonWithPlayer(CallbackInfoReturnable<LivingEntity> cir){
        MobEntity entity = ((MobEntity) (Object) this);
        if(entity instanceof PokemonEntity){
            if(getFirstPassenger() instanceof PlayerEntity player){
                cir.setReturnValue(player);
            }
        }
    }
}
