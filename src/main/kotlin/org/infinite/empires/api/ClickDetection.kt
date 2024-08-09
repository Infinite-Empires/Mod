package org.infinite.empires.api

import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.commands.CommandSource
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.infinite.empires.InfiniteEmpires


fun InitRightClick(server: MinecraftServer) {
    val on_item_interact = server.functions.getTag(InfiniteEmpires.apiResourceLocation("on_item_interact"))
    val on_block_interact = server.functions.getTag(InfiniteEmpires.apiResourceLocation("on_block_interact"))
    val on_entity_interact = server.functions.getTag(InfiniteEmpires.apiResourceLocation("on_entity_interact"))

    fun onItem(player: Player, hand: InteractionHand) {
        val msg = "item interact -> " + player.name.string
        on_item_interact.forEach {
            server.functions.execute(it, CommandSourceStack(
                CommandSource.NULL,
                Vec3.ZERO,
                Vec2.ZERO,
                player.level() as ServerLevel,
                3,
                msg,
                Component.literal(msg),
                server,
                player
            )
            )
        }
    }

    fun onBlock(player: Player, hitResult: BlockHitResult) {
        val msg = "block interact -> " + player.name.string
        on_block_interact.forEach {
            server.functions.execute(it, CommandSourceStack(
                CommandSource.NULL,
                hitResult.blockPos.center,
                Vec2.ZERO,
                player.level() as ServerLevel,
                0,
                msg,
                Component.literal(msg),
                server,
                player
            )
            )
        }
    }

    fun onEntity(player: Player, entity: Entity) {
        val msg = "entity interact -> " + player.name.string
        on_entity_interact.forEach {
            server.functions.execute(it, CommandSourceStack(
                CommandSource.NULL,
                player.position(),
                Vec2.ZERO,
                player.level() as ServerLevel,
                3,
                msg,
                Component.literal(msg),
                server,
                entity
            )
            )
        }
    }


    UseItemCallback.EVENT.register { player, world, hand ->
        onItem(player, hand)
        InteractionResultHolder(InteractionResult.PASS, player.getItemInHand(hand))
    }

    UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
        onBlock(player, hitResult)
        InteractionResult.PASS
    }

    UseEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
        onEntity(player, entity)
        InteractionResult.PASS
    }
}
