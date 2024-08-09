package org.infinite.empires.pollution

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Display.BlockDisplay
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.infinite.empires.InfiniteEmpires

object Pollution {
    const val TAG = "_ie_mod__pollution_cloud"
    const val TAG_INTENSITY = "_ie_mod__pollution_cloud"
    val DATA_INTENSITY = SynchedEntityData.defineId(Display::class.java, EntityDataSerializers.INT)
    private val size = Vec3(.5, .5, .5)

    fun spawn(level: ServerLevel, position: Vec3, intensity: Int): BlockDisplay {
        InfiniteEmpires.logger.info(position.toString())
        val out = object : BlockDisplay(EntityType.BLOCK_DISPLAY, level) {
            override fun defineSynchedData(builder: SynchedEntityData.Builder) {
                builder.define(DATA_INTENSITY, intensity)
                builder.define(SynchedEntityData.defineId(BlockDisplay::class.java, EntityDataSerializers.BLOCK_STATE), Blocks.GRAY_CONCRETE.defaultBlockState())
            }
        }
        out.setPos(position)
        out.addTag(TAG)
        level.addFreshEntity(out)
        return out
    }

    fun register() {
        ServerTickEvents.START_SERVER_TICK.register { server ->
            server.allLevels.forEach { level ->
                level.getEntities(EntityType.BLOCK_DISPLAY) { it.tags.contains(TAG) }.forEach process@ { entity ->
                    InfiniteEmpires.logger.info(entity.toString())
                    var position = entity.position().add(Vec3(.0, .1, .0))
                    if (position.y > 200) {
                        InfiniteEmpires.logger.info("killed")
                        entity.kill()
                        return@process
                    }
                    var blocked = false
                    level.getBlockCollisions(null, AABB(position.subtract(size), position.add(size))).forEach { blocked = true }
                    if (blocked) {
                        InfiniteEmpires.logger.info("blocked")
                        position = Vec3(position.x, position.y.toInt().toDouble(), position.z)
                    }
                    entity.setPos(position)
                }
            }
        }
        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
            dispatcher.register(LiteralArgumentBuilder.literal<CommandSourceStack?>("pollute")
                .then(Commands.argument("location", Vec3Argument.vec3())
                    .then(Commands.argument("intensity", IntegerArgumentType.integer()).executes {
                        spawn(it.source.level, Vec3Argument.getVec3(it, "location"), IntegerArgumentType.getInteger(it, "intensity"));0
                    })))
        }
    }
}
