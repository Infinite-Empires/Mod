package org.infinite.empires.mixin;

import org.infinite.empires.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;

@Mixin(ServerGamePacketListenerImpl.class)
public class MenuClickMixin {
    @Shadow
    public ServerPlayer player;
    
    @Inject(method = "handleContainerClick", at = @At("HEAD"), cancellable = true)
    public void handleContainerClick(ServerboundContainerClickPacket packet, CallbackInfo info) {
        var result = MenuClickCallback.Companion.getEVENT().invoker().interact(player, packet);
        if (result == InteractionResult.FAIL) info.cancel();
    }
    
    @Inject(method = "handleContainerClose", at = @At("HEAD"), cancellable = true)
    public void handleContainerClose(ServerboundContainerClosePacket packet, CallbackInfo info) {
        var result = MenuCloseCallback.Companion.getEVENT().invoker().interact(player, packet);
        if (result == InteractionResult.FAIL) info.cancel();
    }
}
