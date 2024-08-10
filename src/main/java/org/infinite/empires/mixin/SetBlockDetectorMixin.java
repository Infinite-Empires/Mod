package org.infinite.empires.mixin;

import org.infinite.empires.util.IEBlock;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(Level.class)
public abstract class SetBlockDetectorMixin {
    @Shadow
    WritableLevelData levelData;
    
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", at = @At("HEAD"))
    void setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfo callback) {
        var level = (Level) (Object) this;
        IEBlock.Companion.ifIsIE(pos, level, ieBlock -> {
            var data = (IEBlock.DataGetter) levelData;
            var compound = data.getBlockData();
            var blockData = compound.getCompound(pos.toShortString());
            if (blockData.hasUUID(IEBlock.REPR_KEY))
                ieBlock.getDisplay(level, pos, compound).kill();
            compound.remove(pos.toShortString());
            data.setBlockData(compound);
        });
    }
}
