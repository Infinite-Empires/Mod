package org.infinite.empires.util

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import org.infinite.empires.InfiniteEmpires
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Unit
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
        val customModelData: CustomModelData? = null,
        val tick: ((IEMenu) -> Unit)? = null
    )
    
    constructor(dataSupplier: () -> Data) : this(dataSupplier())

    fun open(player: Player) {
        openMenus[player.openMenu(object : MenuProvider {
            override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
                val guiDisplayStack = ItemStack(Items.POISONOUS_POTATO)
                data.customModelData?.let { guiDisplayStack.set(DataComponents.CUSTOM_MODEL_DATA, it) }
                guiDisplayStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE)
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
                InfiniteEmpires.logger.info("${player.name} clicked in menu")
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
                InfiniteEmpires.logger.info("${player.name} closed menu")
                openMenus.remove(packet.containerId)
                InteractionResult.PASS
            }
            
            ServerTickEvents.START_SERVER_TICK.register {
                openMenus.values.forEach {
                    it.data.tick?.invoke(it)
                }
            }
        }
    }
}