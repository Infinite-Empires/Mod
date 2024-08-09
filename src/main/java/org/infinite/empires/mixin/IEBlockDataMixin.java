package org.infinite.empires.mixin;

import com.mojang.serialization.*;
import com.mojang.serialization.Dynamic;
import org.infinite.empires.util.IEBlock;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;

import java.util.stream.Collector;

@Mixin(PrimaryLevelData.class)
public class IEBlockDataMixin implements IEBlock.DataGetter {
    @Unique
    private static final String BLOCK_DATA_KEY = "ie:BlockData";
    
    @Unique
    private CompoundTag blockData;
    
    @NotNull
    @Override
    public CompoundTag getBlockData() {
        return blockData.copy();
    }
    
    @Override
    public void setBlockData(@NotNull CompoundTag compoundTag) {
        blockData = compoundTag;
    }
    
    @Inject(method = "setTagData", at = @At("HEAD"))
    public void setTagData(RegistryAccess registry, CompoundTag nbt, @Nullable CompoundTag playerNBT, CallbackInfo callback) {
        nbt.put(BLOCK_DATA_KEY, getBlockData());
    }
    
    @Inject(method = "parse", at = @At("RETURN"), cancellable = true)
    public <T> void parse(Dynamic<T> tag, LevelSettings levelSettings,
                          PrimaryLevelData.SpecialWorldProperty specialWorldProperty, WorldOptions worldOptions,
                          Lifecycle worldGenSettingsLifecycle, CallbackInfoReturnable<PrimaryLevelData> callback) {
        var data = (IEBlock.DataGetter) callback.getReturnValue();
        var blocks = tag.get(BLOCK_DATA_KEY).get();
        if (blocks.isError()) {
            data.setBlockData(new CompoundTag());
            return;
        }
        data.setBlockData(CompoundTag.CODEC.parse(blocks.getOrThrow()).getOrThrow());
        callback.setReturnValue((PrimaryLevelData) data);
    }
}
