package net.ioixd.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.ioixd.MountIsMoving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PokemonEntity.class)
public class PokemonMixin implements MountIsMoving {
    @Unique
    private boolean mount_moving;

    @Override
    public boolean mount_isMoving() {
        return this.mount_moving;
    }

    @Override
    public void mount_setMoving(boolean b) {
        mount_moving = b;
    }
}
