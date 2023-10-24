package net.ioixd.mixin;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntity.class)
public class PlayerMixin {
    @Inject(at = @At("HEAD"), method="travel", locals = LocalCapture.CAPTURE_FAILHARD)
    private void travel(Vec3d movement, CallbackInfo info) {
        PlayerEntity player = ((PlayerEntity) (Object) this);
        Entity entity = player.getVehicle();
        if(entity != null) {
            if(entity instanceof PokemonEntity living) {
                if(movement.z > 0.0) {
                    living.travel(player.getRotationVector());
                }
                if(movement.z < 0.0) {
                    living.travel(player.getRotationVector().multiply(-1.0,-1.0,-1.0));
                }

            }
        }
    }


}
