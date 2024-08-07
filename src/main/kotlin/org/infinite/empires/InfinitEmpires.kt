package org.infinitempires

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.resources.ResourceLocation
import org.infinitempires.api.InitRightClick
import org.infinitempires.api.bullet.Bullets
import org.infinitempires.pollution.Pollution
import org.slf4j.LoggerFactory

object InfinitEmpires : ModInitializer {
	val logger = LoggerFactory.getLogger("infinitempires")
    val apiNamespace = "ie_api"
	var init = false

	fun apiResourceLocation(str: String): ResourceLocation {
		return ResourceLocation.fromNamespaceAndPath(apiNamespace, str)
	}

	override fun onInitialize() {
		Bullets.register()
		Pollution.register()
		ServerTickEvents.END_SERVER_TICK.register {
			if (init) return@register
			init = true
			InitRightClick(it)
		}
	}
}