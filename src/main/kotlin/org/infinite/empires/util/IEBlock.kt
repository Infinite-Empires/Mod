package org.infinite.empires.util

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Display.ItemDisplay
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.phys.AABB
import java.util.function.Consumer

interface IEBlock {
    fun getRepr(entity: ItemDisplay): BlockState
    fun use(player: Player): InteractionResult = InteractionResult.PASS
    fun drops(): LootTable

    fun save(to: CompoundTag)
    fun load(from: CompoundTag)
    
    fun unload(pos: BlockPos, level: Level) {
        val data = level.levelData as DataGetter
        val compound = data.get(pos)
        save(compound)
        if (compound.hasUUID(REPR_KEY))
            getRepr(getDisplay(level, pos, compound))
        data.set(pos, compound)
    }
    
    fun create(pos: BlockPos, level: Level) {
        val entity = EntityType.ITEM_DISPLAY.spawn(level as ServerLevel, pos, MobSpawnType.MOB_SUMMONED)!!
        val compound = CompoundTag()
        compound.putString(TYPE_KEY, this.javaClass.name)
        level.setBlock(pos, getRepr(entity), Block.UPDATE_ALL)
        if (entity.isAlive)
            compound.putUUID(REPR_KEY, entity.uuid)
        (level.levelData as DataGetter).set(pos, compound)
    }
    
    fun getDisplay(level: Level, pos: BlockPos, compound: CompoundTag): ItemDisplay {
        return level.getEntitiesOfClass(ItemDisplay::class.java, AABB.encapsulatingFullBlocks(pos, pos)) { it.uuid.equals(compound.getUUID(REPR_KEY)) }[0]
    }
    
    companion object {
        const val TYPE_KEY = "Type"
        const val REPR_KEY = "ItemDisplayID"
        
        fun isIE(pos: BlockPos, level: Level) = (level.levelData as DataGetter).has(pos)
        fun ifIsIE(pos: BlockPos, level: Level, consumer: Consumer<IEBlock>) {
            if (isIE(pos, level)) {
                val ie = load(pos, level)
                consumer.accept(ie)
                ie.unload(pos, level)
            }
        }
        
        fun load(pos: BlockPos, level: Level): IEBlock {
            val data = (level.levelData as DataGetter).get(pos)
            val block = Class.forName(data.getString(TYPE_KEY)).getConstructor().newInstance() as IEBlock
            if (block is CachedIEBlock) {
                block.position = pos
                block.level = level
            }
            block.load(data)
            return block
        }
    
        inline fun <reified T : IEBlock> create(entity: ItemDisplay): IEBlock {
            val level = entity.level()
            val pos = BlockPos(entity.position().x.toInt(), entity.position().z.toInt(), entity.position().y.toInt())
            val t = T::class.java.getConstructor().newInstance()
            t.create(pos, level)
            return t
        }
    }
    
    interface DataGetter {
        var blockData: CompoundTag
    }
}

interface Reloadable {
    fun reload(): Boolean
    fun unload(): Boolean
}

abstract class CachedIEBlock : IEBlock, Reloadable {
    var position: BlockPos? = null
    var level: Level? = null
    val hasCache get() = position != null && level != null
    
    override fun reload(): Boolean {
        if (!hasCache) return false
        val data = level!!.levelData as IEBlock.DataGetter
        if (!data.has(position!!)) return false
        load(data.get(position!!))
        return true
    }
    
    override fun unload(): Boolean {
        if (!hasCache) return false
        unload(position!!, level!!)
        return true
    }
    
    override fun create(pos: BlockPos, level: Level) {
        this.position = pos
        this.level = level
        super.create(pos, level)
    }
}

fun IEBlock.DataGetter.has(blockPos: BlockPos): Boolean {
    return blockData.contains(blockPos.toShortString(), Tag.TAG_COMPOUND.toInt())
}

fun IEBlock.DataGetter.get(blockPos: BlockPos): CompoundTag {
    return blockData.getCompound(blockPos.toShortString())
}

fun IEBlock.DataGetter.set(blockPos: BlockPos, data: CompoundTag) {
    blockData.put(blockPos.toShortString(), data)
}
