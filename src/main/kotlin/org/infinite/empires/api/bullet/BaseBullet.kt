package org.infinite.empires.api.bullet

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

interface BaseBullet {
    fun process(): Boolean
    fun impact()
}

object Bullets {
    var list = ArrayList<BaseBullet>()

    fun register() {
        ServerTickEvents.END_SERVER_TICK.register {
            list.removeIf {
                if (it.process()) {
                    it.impact()
                    return@removeIf true
                }
                return@removeIf false
            }
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
            dispatcher.register((((((literal<CommandSourceStack?>("deploymissile")
                .requires { it.hasPermission(2) })
                .then(literal<CommandSourceStack?>("snipper").executes {
                    if (list.add(Snipper(it.source))) { 0 } else { -1 }
                }))
                .then(literal<CommandSourceStack?>("bullet").then(Commands.argument("damage", FloatArgumentType.floatArg()).executes {
                    val damage = FloatArgumentType.getFloat(it, "damage")
                    if (list.add(Bullet(damage, it.source))) { 0 } else { -1 }
                }))).then(literal<CommandSourceStack?>("grenade").then(Commands.argument("range", DoubleArgumentType.doubleArg()).executes {
                    val range = DoubleArgumentType.getDouble(it, "range")
                    if (list.add(Grenade(range, it.source))) { 0 } else { -1 }
                }))))
            )
        }
    }
}
