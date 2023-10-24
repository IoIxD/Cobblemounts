package net.ioixd.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public class EntityMovementMixin {
    @Inject(at = @At("TAIL"), method = "travel")
    private void tickMovement(CallbackInfo info) {
        LivingEntity entity = ((LivingEntity) (Object) this);
        if(entity.hasPassengers()) {
            if(entity instanceof PokemonEntity pokemon) {
                Entity firstPassenger = entity.getFirstPassenger();
                if(firstPassenger instanceof PlayerEntity player) {
                    entity.bodyYaw = entity.headYaw = player.getYaw();
                    entity.setMovementSpeed(player.getMovementSpeed() * (pokemon.getPokemon().getSpeed() / 12.0f));
                    entity.setPitch(entity.getPitch());
                }
            }
        }
    }


}
