package org.infinite.empires.util

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult

/**
 * Callback for taking an item out of a menu.
 * Upon return:
 * - SUCCESS cancels further processing and send the packet.
 * - PASS falls back to further processing and defaults to SUCCESS if no other listeners are available
 * - FAIL cancels further processing and does not send the packet.
 */
fun interface MenuClickCallback {
    fun interact(player: ServerPlayer, packet: ServerboundContainerClickPacket): InteractionResult
    
    companion object {
        val EVENT: Event<MenuClickCallback> = EventFactory.createArrayBacked(MenuClickCallback::class.java) { listeners ->
            MenuClickCallback { player, packet ->
                for (listener in listeners) {
                    val result: InteractionResult = listener.interact(player, packet)
                    if (result !== InteractionResult.PASS) {
                        return@MenuClickCallback result
                    }
                }
                return@MenuClickCallback InteractionResult.PASS
            }
        }
    }
}

/**
 * Callback for taking an item out of a menu.
 * Upon return:
 * - SUCCESS cancels further processing and send the packet.
 * - PASS falls back to further processing and defaults to SUCCESS if no other listeners are available
 * - FAIL cancels further processing and does not send the packet.
 */
fun interface MenuCloseCallback {
    fun interact(player: ServerPlayer, packet: ServerboundContainerClosePacket): InteractionResult
    
    companion object {
        val EVENT: Event<MenuCloseCallback> = EventFactory.createArrayBacked(MenuCloseCallback::class.java) { listeners ->
            MenuCloseCallback { player, packet ->
                for (listener in listeners) {
                    val result: InteractionResult = listener.interact(player, packet)
                    if (result !== InteractionResult.PASS) {
                        return@MenuCloseCallback result
                    }
                }
                return@MenuCloseCallback InteractionResult.PASS
            }
        }
    }
}