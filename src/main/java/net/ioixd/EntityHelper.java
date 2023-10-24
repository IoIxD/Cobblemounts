package net.ioixd;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class EntityHelper {
    public static BlockPos GetPosWithYOffset(LivingEntity entity, float offset) {
        if (entity.supportingBlockPos.isPresent()) {
            BlockPos blockPos = entity.supportingBlockPos.get();
            if (offset > 1.0E-5f) {
                BlockState blockState = entity.getWorld().getBlockState(blockPos);
                if ((double)offset <= 0.5 && blockState.isIn(BlockTags.FENCES) || blockState.isIn(BlockTags.WALLS) || blockState.getBlock() instanceof FenceGateBlock) {
                    return blockPos;
                }
                return blockPos.withY(MathHelper.floor(entity.getPos().y - (double)offset));
            }
            return blockPos;
        }
        int i = MathHelper.floor(entity.getPos().x);
        int j = MathHelper.floor(entity.getPos().y - (double)offset);
        int k = MathHelper.floor(entity.getPos().z);
        return new BlockPos(i, j, k);
    }

    public static BlockPos GetVelocityAffectingPos(LivingEntity entity) {
        return EntityHelper.GetPosWithYOffset(entity,0.500001f);
    }

    public static float GetJumpVelocityMultiplier(LivingEntity entity) {
        float f = entity.getWorld().getBlockState(entity.getBlockPos()).getBlock().getJumpVelocityMultiplier();
        float g = entity.getWorld().getBlockState(EntityHelper.GetVelocityAffectingPos(entity)).getBlock().getJumpVelocityMultiplier();
        return (double)f == 1.0 ? g : f;
    }
}
