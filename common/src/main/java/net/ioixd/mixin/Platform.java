package net.ioixd.mixin;

import net.ioixd.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Consumer;

public interface Platform {
    void sendConfig(PlayerEntity player, Config config);
    void onConfigRecieve(Consumer<Config> consumer);
}
