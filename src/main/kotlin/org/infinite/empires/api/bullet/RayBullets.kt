package org.infinitempires.api.bullet

import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Rotations
import net.minecraft.core.Vec3i
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSources
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class Snipper(source: CommandSourceStack) : RayCast(source) {
    private val size = Vec3(.2, .2, .2)
    override fun impact() {
        level.getEntities(shooter, AABB(position.subtract(size), position.add(size))).forEach {
            it.hurt(DamageSources(level.registryAccess()).arrow(Arrow(EntityType.ARROW, level), shooter), 35f)
        }
        level.playSound(null, BlockPos(position.x.toInt(), position.y.toInt(), position.z.toInt()), SoundEvents.GOAT_RAM_IMPACT, SoundSource.HOSTILE)
    }
}

class Bullet(val damage: Float, source: CommandSourceStack) : RayCast(source) {
    private val size = Vec3(.02, .02, .02)
    override fun impact() {
        level.getEntities(shooter, AABB(position.subtract(size), position.add(size))).forEach {
            it.hurt(DamageSources(level.registryAccess()).arrow(Arrow(EntityType.ARROW, level), shooter), damage)
        }
        level.playSound(null, BlockPos(position.x.toInt(), position.y.toInt(), position.z.toInt()), SoundEvents.CALCITE_HIT, SoundSource.HOSTILE)
    }
}


