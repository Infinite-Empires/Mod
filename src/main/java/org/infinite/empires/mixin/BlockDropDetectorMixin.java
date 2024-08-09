package org.infinite.empires.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import org.infinite.empires.util.IEBlock;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

@Mixin(Block.class)
public class BlockDropDetectorMixin {
    @Unique
    void handleIEBlock(BlockPos pos, Level level, CallbackInfoReturnable<List<ItemStack>> callback, LootParams.Builder params) {
        IEBlock.Companion.ifIsIE(pos, level, ieBlock -> {
            callback.setReturnValue(ieBlock.drops().getRandomItems(params.create(LootContextParamSets.BLOCK)));
        });
    }
    
    @Inject(at = @At("RETURN"), cancellable = true, method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;")
    void getDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, CallbackInfoReturnable<List<ItemStack>> callback, @Local LootParams.Builder params) {
        handleIEBlock(pos, level, callback, params);
    }
    
    @Inject(at = @At("RETURN"), cancellable = true, method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;")
    void getDrops(BlockState state, ServerLevel world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> callback, @Local LootParams.Builder params) {
        handleIEBlock(pos, entity.level(), callback, params);
    }
}
