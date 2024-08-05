package org.infinitempires.api.bullet

import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.Position
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

abstract class RayCast(private val source: CommandSourceStack, val range: Int = 99999, val color: Vector3f = Vector3f(.3f)) : BaseBullet {
    val level: ServerLevel = source.level
    val shooter = source.entity
    var position: Vec3 = source.position

    private val rotationVector = Vec3.directionFromRotation(source.rotation)

    override fun process(): Boolean {
        var aabb: AABB
        var oldPos: Position
        for (i in 1..range) {
            oldPos = Vec3(position.x, position.y, position.z)
            position = position.add(rotationVector)
            level.sendParticles(shooter as ServerPlayer, DustParticleOptions(color, 1f), false,  position.x, position.y, position.z, 1, .0, .0, .0, 1.0)
            aabb = AABB(oldPos, position)
            var hit: Boolean = level.getEntities(shooter, aabb).size > 0
            if (!hit) {
                level.getBlockCollisions(null, aabb).forEach { _ -> hit = true }
            }
            if (hit) {
                break
            }
        }
        return true
    }
}
