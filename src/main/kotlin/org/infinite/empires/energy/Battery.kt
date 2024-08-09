package org.infinite.empires.energy

import net.minecraft.ChatFormatting
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.literal
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.crafting.*
import org.infinite.empires.InfiniteEmpires
import org.infinite.empires.util.IEItem

class Battery(override var power: Int = 0) : IEItem, Chargeable {
    override val maxPower: Int = MAX_POWER

    companion object {
        const val POWER_KEY = "power"
        const val MAX_POWER = 100
        val RECIPE: CraftingRecipe

        init {
            IEItem.Defaults["battery"] = Battery().create()
            IEItem.Defaults["battery_full"] = Battery(MAX_POWER).create()
            RECIPE = ShapedRecipe("ie:battery", CraftingBookCategory.EQUIPMENT, ShapedRecipePattern.of(
                mapOf(Pair('c', Ingredient.of(Items.COPPER_INGOT))),
                "ccc", "c c", "ccc"
            ), Battery().create())
        }
    }

    override fun getRepr(): ItemStack {
        val stack = ItemStack(Items.WARPED_FUNGUS_ON_A_STICK)
        stack.set(DataComponents.ITEM_NAME, literal("Battery"))
        stack.set(DataComponents.DAMAGE, MAX_POWER - power)
        stack.set(DataComponents.LORE, ItemLore(listOf(
            literal("Power: ").append(literal("$power").withStyle(ChatFormatting.AQUA)).append(literal("/$MAX_POWER").withStyle(ChatFormatting.GRAY))),
        ))
        return stack
    }

    override fun load(data: CompoundTag) {
        power = data.getInt(POWER_KEY)
    }

    override fun save(to: CompoundTag) {
        to.putInt(POWER_KEY, power.coerceAtLeast(0).coerceAtMost(MAX_POWER))
    }
}