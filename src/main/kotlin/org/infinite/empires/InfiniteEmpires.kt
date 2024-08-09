package org.infinite.empires

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeHolder
import org.infinite.empires.api.InitRightClick
import org.infinite.empires.api.bullet.Bullets
import org.infinite.empires.energy.Battery
import org.infinite.empires.pollution.Pollution
import org.infinite.empires.util.IEItem
import org.infinite.empires.util.IEMenu
import org.slf4j.LoggerFactory

object InfiniteEmpires : ModInitializer {
	const val API_NAMESPACE = "ie_api"
	const val NAMESPACE = "ie"

	val logger = LoggerFactory.getLogger("infinite-empires")

	fun apiResourceLocation(str: String): ResourceLocation {
		return ResourceLocation.fromNamespaceAndPath(API_NAMESPACE, str)
	}

	fun resourceLocation(str: String): ResourceLocation {
		return ResourceLocation.fromNamespaceAndPath(NAMESPACE, str)
	}

	override fun onInitialize() {
		Bullets.register()
		Pollution.register()
		IEItem.register()
		IEMenu.register()
		ServerLifecycleEvents.SERVER_STARTING.register { server ->
			InitRightClick(server)
			val recipes = ArrayList<RecipeHolder<*>>()
			recipes.addAll(server.recipeManager.recipes)
			recipes.addAll(listOf(
				RecipeHolder(resourceLocation("battery"), Battery.RECIPE),
			))
			server.recipeManager.replaceRecipes(recipes)
		}
	}
}