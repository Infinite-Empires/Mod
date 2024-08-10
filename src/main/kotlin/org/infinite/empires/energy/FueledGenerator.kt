package org.infinite.empires.energy

import org.infinite.empires.CustomModelDatas
import org.infinite.empires.util.CachedIEBlock
import org.infinite.empires.util.IEItem
import org.infinite.empires.util.IEMenu
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable

class FueledGenerator(var heat: Int = 0, var fuel: ItemStack = ItemStack.EMPTY, var charging: ItemStack = ItemStack.EMPTY) : CachedIEBlock(), WireReceptor {
    override val connectedWith = HashSet<WireProjector>()
        get() {
            if (field.isEmpty()) {
                Block.popResource(level!!, position!!, charging)
                charging = ItemStack.EMPTY
            }
            updateConnections()
            return field
        }
    
    override fun getRepr(entity: Display.ItemDisplay): BlockState {
        if (entity.getSlot(0).get().isEmpty) {
            val item = ItemStack(Items.COBBLESTONE)
            item.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelDatas.GENERATOR.get())
            entity.getSlot(0).set(item)
        }
        return Blocks.COBBLESTONE.defaultBlockState()
    }
    
    override fun drops(): LootTable
        = LootTable.lootTable().withPool(LootPool.lootPool().with(IEItem.BlockItem(this).lootEntry())).build()
    
    override fun save(to: CompoundTag) {
        TODO("Not yet implemented")
    }
    
    override fun load(from: CompoundTag) {
        TODO("Not yet implemented")
    }
    
    override fun use(player: Player): InteractionResult {
        val result = super<WireReceptor>.use(player)
        if (result != InteractionResult.PASS) return result
        val menu = IEMenu(IEMenu.Data(
            mapOf(
                12 to { stack -> AbstractFurnaceBlockEntity.isFuel(stack).let { if (it) fuel = stack;it } },
                14 to battery@ { stack ->
                    if (connectedWith.isNotEmpty()) return@battery false
                    var output = false
                    IEItem.ifIsIE(stack) {
                        output = it is Chargeable
                    }
                    if (output)
                        charging = stack
                    output
                },
            ),
            { id, inv ->
                val menu = ChestMenu.fourRows(id, inv)
                val stateID = menu.incrementStateId()
                menu.setItem(12, stateID, fuel)
                menu.setItem(14, stateID, charging)
                //TODO: burn animation
                menu
            },
            Component.literal("Fueled Generator"),
            CustomModelDatas.GENERATOR.get()
        ))
        menu.open(player)
        return InteractionResult.FAIL
    }
    
    fun burnNext(): Boolean {
        if (heat > 0 || fuel.isEmpty)
            return false
        heat = AbstractFurnaceBlockEntity.getFuel()[fuel.item]!!
        return true
    }
    
    override fun requestingPower(): Boolean {
        if (heat > 0 || burnNext()) {
            heat--
            return true
        }
        return false
    }
}