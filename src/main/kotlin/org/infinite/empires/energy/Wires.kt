package org.infinite.empires.energy

import org.infinite.empires.util.Reloadable
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player

interface WireNode<T : Reloadable> : Reloadable {
    val connectedWith: HashSet<T>
    
    fun updateConnections() {
        connectedWith.removeIf {
            if (it.reload()) {
                it.unload()
                return@removeIf false
            }
            return@removeIf true
        }
    }
}

/** Incoming Wires */
interface WireReceptor : WireNode<WireProjector> {
    fun use(player: Player): InteractionResult {
        if (isHoldingWire(player)) {
            wireHolders[player]?.connectTo(this)
            return InteractionResult.FAIL
        }
        return InteractionResult.PASS
    }
    
    fun requestingPower(): Boolean
}

/** Outgoing Wires */
interface WireProjector : WireNode<WireReceptor> {
    fun connectTo(node: WireReceptor) {
        connectedWith.add(node)
        node.connectedWith.add(this)
    }
    
    fun use(player: Player): InteractionResult {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) {
            if (isHoldingWire(player)) {
                if (this is WireReceptor)
                    return (this as WireReceptor).use(player)
                return InteractionResult.PASS
            }
            wireHolders[player] = this
            return InteractionResult.FAIL
        }
        return InteractionResult.PASS
    }
    
    fun requestPower(max: Int): Int {
        updateConnections()
        var power = 0
        connectedWith.forEach {
            if (power >= max) return@forEach
            if (it.requestingPower()) power++
        }
        return power
    }
}

private val wireHolders = HashMap<Player, WireProjector>()
fun isHoldingWire(player: Player) = wireHolders.contains(player)
