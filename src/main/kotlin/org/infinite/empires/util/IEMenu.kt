package org.infinite.empires.util

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData

class IEMenu(val data: Data) {
    class Data(
        val slots: Map<Int, (ItemStack) -> Boolean>,
        val init: (Int, Inventory) -> AbstractContainerMenu,
        val displayName: Component,
        val customModelData: CustomModelData,
    )
    
    constructor(dataSupplier: () -> Data) : this(dataSupplier())

    fun open(player: ServerPlayer) {
        openMenus[player.openMenu(object : MenuProvider {
            override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
                val guiDisplayStack = ItemStack(Items.POISONOUS_POTATO)
                guiDisplayStack.set(DataComponents.CUSTOM_MODEL_DATA, data.customModelData)
                val menu = data.init.invoke(i, inventory)
                menu.setItem(menu.slots.size - 1, menu.incrementStateId(), guiDisplayStack)
                return menu
            }
            override fun getDisplayName(): Component = data.displayName
        }).orElseThrow()] = this
    }

    companion object {
        val openMenus = HashMap<Int, IEMenu>()
        
        fun register() {
            MenuClickCallback.EVENT.register inner@ { player, packet ->
                val iemenu = openMenus[packet.containerId] ?: return@inner InteractionResult.PASS
                for (changed in packet.changedSlots) {
                    val slotData = iemenu.data.slots[changed.key] ?: return@inner InteractionResult.FAIL
                    if(slotData(changed.value))
                        return@inner InteractionResult.SUCCESS
                    return@inner InteractionResult.FAIL
                }
                InteractionResult.PASS
            }
            
            MenuCloseCallback.EVENT.register { player, packet ->
                openMenus.remove(packet.containerId)
                InteractionResult.PASS
            }
        }
    }
}