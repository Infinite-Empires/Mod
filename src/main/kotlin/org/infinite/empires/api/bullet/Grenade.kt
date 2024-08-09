@file:Suppress("MemberVisibilityCanBePrivate")

package org.infinite.empires.api.bullet

import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

class Grenade(range: Double, val source: CommandSourceStack) : BaseBullet {
    val entity: ArmorStand = ArmorStand(EntityType.ARMOR_STAND, source.level)
    var timer = 45

    init {
        entity.setPos(source.position)
        entity.deltaMovement = Vec3.directionFromRotation(source.rotation).multiply(range, range, range)
        entity.attributes.getInstance(Attributes.SCALE)?.baseValue = 0.1
        source.level.addFreshEntity(entity)
    }

    override fun process(): Boolean {
        timer--
        if (timer < 0) return true
        return false
    }

    override fun impact() {
        val position = entity.position()
        entity.kill()
        source.level.explode(source.entity, position.x, position.y, position.z, 5f, true, Level.ExplosionInteraction.MOB)
    }
}