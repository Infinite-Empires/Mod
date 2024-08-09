package org.infinite.empires.util

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.sun.jdi.connect.Connector.StringArgument
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import java.util.Optional
import java.util.function.Consumer

interface IEItem {
    /** gets a vanilla representation of the item, purely visual, don't store data */
    fun getRepr(): ItemStack

    /** logic performed when you right-click */
    fun use(player: Player): InteractionResult = InteractionResult.SUCCESS_NO_ITEM_USED

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

    companion object {
        protected const val DATA_KEY = "ie:item_data"
        protected const val TYPE_KEY = "type"

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
        }
    }
}
