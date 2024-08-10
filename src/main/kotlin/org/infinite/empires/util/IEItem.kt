package org.infinite.empires.util

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.commands.Commands
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Display.ItemDisplay
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.entries.DynamicLoot
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
import net.minecraft.world.level.storage.loot.functions.LootItemFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import java.util.function.Consumer

interface IEItem {
    /** gets a vanilla representation of the item, purely visual, don't store data */
    fun getRepr(): ItemStack

    /** logic performed when you right-click */
    fun use(player: Player) = InteractionResult.FAIL

    fun load(data: CompoundTag)
    fun save(to: CompoundTag)

    fun unload(stack: ItemStack) {
        val repr = getRepr()
        for (componentType in BuiltInRegistries.DATA_COMPONENT_TYPE) {
            if (repr.has(componentType)) {
                @Suppress("UNCHECKED_CAST")
                stack.set(componentType as DataComponentType<Any>, repr.get(componentType))
                continue
            }
            if (stack.has(componentType))
                stack.remove(componentType)
        }
        stack.update(DataComponents.CUSTOM_DATA, CustomData.of(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag())) { customData ->
            customData.update { tag ->
                val data = tag.getCompound(DATA_KEY).copy()
                data.putString(TYPE_KEY, this.javaClass.name)
                save(data)
                tag.put(DATA_KEY, data)
            }
        }
    }

    fun create(): ItemStack {
        val stack = getRepr()
        unload(stack)
        return stack
    }
    
    private class LootIEItem(val ieItem: IEItem, weight: Int, quality: Int, conditions: MutableList<LootItemCondition>, functions: MutableList<LootItemFunction>)
        : LootPoolSingletonContainer(weight, quality, conditions, functions) {
        override fun getType(): LootPoolEntryType = LootPoolEntries.DYNAMIC
    
        override fun createItemStack(stackConsumer: Consumer<ItemStack>, lootContext: LootContext) {
            stackConsumer.accept(ieItem.create())
        }
        
        companion object {
            fun loot(ieItem: IEItem) = simpleBuilder {
                    weight, quality, conditions, functions ->
                LootIEItem(ieItem, weight, quality, conditions, functions)
            }
        }
    }
    
    fun lootEntry(): LootPoolEntryContainer = LootIEItem.loot(this).build()

    companion object {
        protected const val DATA_KEY = "ie:item_data"
        protected const val TYPE_KEY = "type"
        const val BLOCK_ITEM_KEY = "block"

        val Defaults = HashMap<String, ItemStack>()

        fun isIE(item: ItemStack): Boolean {
            val data = item.get(DataComponents.CUSTOM_DATA) ?: return false
            return data.copyTag().contains(DATA_KEY)
        }
        fun ifIsIE(item: ItemStack, consumer: Consumer<IEItem>) {
            if (isIE(item)) {
                val ie = load(item)
                consumer.accept(ie)
                ie.unload(item)
            }
        }

        fun load(item: ItemStack): IEItem {
            val data = item.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getCompound(DATA_KEY)!!
            val instance = Class.forName(data.getString(TYPE_KEY)).getConstructor().newInstance() as IEItem
            instance.load(data)
            return instance
        }
        
        fun register() {
            UseItemCallback.EVENT.register click@ { player, _, hand ->
                val item = player.getItemInHand(hand)
                var result: InteractionResult? = null
                ifIsIE(item) {
                    result = it.use(player)
                }
                if (result == null)
                    return@click InteractionResultHolder.pass(item)
                return@click InteractionResultHolder(result!!, item)
            }

            CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
                val command = "iegive"
                dispatcher.register(Commands.literal(command).then(
                    Commands.argument("item", StringArgumentType.word())
                        .suggests { context, builder ->
                            Defaults.keys.forEach { builder.suggest(it) }
                            return@suggests builder.buildFuture()
                        }
                        .executes {
                            val key = StringArgumentType.getString(it, "item")
                            if (!Defaults.contains(key)) {
                                it.source.sendFailure(Component.literal("IE Item '$key' not found!"))
                                return@executes -1
                            }
                            if (it.source.playerOrException.inventory.add(Defaults[StringArgumentType.getString(it, "item")]!!)) return@executes 1
                            it.source.sendFailure(Component.literal("Failed to give the item"))
                            return@executes -1
                        }
                    )
                )
            }
            
            PlayerBlockBreakEvents.BEFORE.register { level, player, pos, state, entity ->
                var item = player.getItemInHand(InteractionHand.MAIN_HAND)
                if (item.isEmpty)
                    item = player.getItemInHand(InteractionHand.OFF_HAND)
                ifIsIE(item) {
                    if (it !is BlockItem) return@ifIsIE
                    it.block.create(pos, level)
                }
                true
            }
        }
    }
    
    class BlockItem(val block: IEBlock) : IEItem {
        override fun getRepr(): ItemStack {
            val display = ItemDisplay(EntityType.ITEM_DISPLAY, null as Level?)
            try {
                block.getRepr(display)
            } catch (_: NullPointerException) { }
            return display.getSlot(0).get().copy()
        }
        
        override fun load(data: CompoundTag) { }
        override fun save(to: CompoundTag) {
            to.putString(BLOCK_ITEM_KEY, block.javaClass.name)
        }
        
        override fun use(player: Player) = InteractionResult.PASS
    }
}
