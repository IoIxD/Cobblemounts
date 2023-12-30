package net.ioixd.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonServerDelegate;
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonNavigation;
import net.ioixd.MountIsMoving;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin to control movement animation
 */
@Mixin(PokemonServerDelegate.class)
public class PokemonServerDelegateMixin {
    @Redirect(method = "tick(Lcom/cobblemon/mod/common/entity/pokemon/PokemonEntity;)V",at = @At(value = "INVOKE",target = "Lcom/cobblemon/mod/common/entity/pokemon/ai/PokemonNavigation;isIdle()Z"))
    boolean isNotMoving(PokemonNavigation instance){
        boolean result = false;
        var pokemon = instance.getPokemonEntity();
        if (pokemon.getFirstPassenger() instanceof PlayerEntity player) {
            var mount = (MountIsMoving) (Object)pokemon;
            result = mount.mount_isMoving();
        }
        var b1 = !result;
        var b2 = instance.isIdle();
        return b1 && b2;
    }
}
