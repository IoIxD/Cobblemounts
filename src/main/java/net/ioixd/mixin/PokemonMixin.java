package net.ioixd.mixin;

import java.util.concurrent.CompletableFuture;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.ioixd.MountIsMoving;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokemonEntity.class)
public abstract class PokemonMixin extends MobEntity implements MountIsMoving {
    @Unique
    private boolean mount_moving;

    protected PokemonMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean mount_isMoving() {
        return this.mount_moving;
    }

    @Override
    public void mount_setMoving(boolean b) {
        mount_moving = b;
    }

    @Inject(method = "recallWithAnimation", at = @At("HEAD"), remap = false)
    public void recall(CallbackInfoReturnable<CompletableFuture<Pokemon>> cir) {
        if (getControllingPassenger() != null) {
            getControllingPassenger().stopRiding();
        }
    }
}
