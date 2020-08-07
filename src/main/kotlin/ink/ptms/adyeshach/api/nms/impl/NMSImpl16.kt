package ink.ptms.adyeshach.api.nms.impl

import com.google.common.base.Enums
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import ink.ptms.adyeshach.api.nms.NMS
import ink.ptms.adyeshach.common.bukkit.BukkitDirection
import ink.ptms.adyeshach.common.bukkit.BukkitPaintings
import ink.ptms.adyeshach.common.bukkit.BukkitParticles
import io.izzel.taboolib.module.lite.SimpleEquip
import io.izzel.taboolib.module.lite.SimpleReflection
import net.minecraft.server.v1_16_R1.*
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import java.util.*


/**
 * @author Arasple
 * @date 2020/8/3 21:51
 */
class NMSImpl16 : NMS() {

    override fun spawnEntity(player: Player, entityType: Any, entityId: Int, uuid: UUID, location: Location) {
        sendPacket(
            player,
            PacketPlayOutSpawnEntity(),
            Pair("a", entityId),
            Pair("b", uuid),
            Pair("c", location.x),
            Pair("d", location.y),
            Pair("e", location.z),
            Pair("f", (location.yaw * 256.0f / 360.0f).toInt().toByte()),
            Pair("g", (location.pitch * 256.0f / 360.0f).toInt().toByte()),
            Pair("k", entityType)
        )
    }

    override fun spawnEntityLiving(player: Player, entityType: Any, entityId: Int, uuid: UUID, location: Location) {
        sendPacket(
            player,
            PacketPlayOutSpawnEntityLiving(),
            Pair("a", entityId),
            Pair("b", uuid),
            Pair("c", IRegistry.ENTITY_TYPE.a(entityType as EntityTypes<*>)),
            Pair("d", location.x),
            Pair("e", location.y),
            Pair("f", location.z),
            Pair("g", 0),
            Pair("h", 0),
            Pair("i", 0),
            Pair("j", (location.yaw * 256.0f / 360.0f).toInt().toByte()),
            Pair("k", (location.pitch * 256.0f / 360.0f).toInt().toByte()),
            Pair("l", (location.yaw * 256.0f / 360.0f).toInt().toByte())
        )
    }

    override fun spawnNamedEntity(player: Player, entityType: Any, entityId: Int, uuid: UUID, location: Location) {
        sendPacket(
            player,
            PacketPlayOutNamedEntitySpawn(),
            Pair("a", entityId),
            Pair("b", uuid),
            Pair("c", location.x),
            Pair("d", location.y),
            Pair("e", location.z),
            Pair("f", (location.yaw * 256 / 360).toInt().toByte()),
            Pair("g", (location.pitch * 256 / 360).toInt().toByte())
        )
    }

    override fun spawnEntityFallingBlock(player: Player, entityId: Int, uuid: UUID, location: Location, material: Material, data: Byte) {
        val block = (SimpleReflection.getFieldValueChecked(Blocks::class.java, null, material.name, true) ?: Blocks.STONE) as Block
        val id = Block.getCombinedId(block.blockData)

        sendPacket(
            player,
            PacketPlayOutSpawnEntity(),
            Pair("a", entityId),
            Pair("b", uuid),
            Pair("c", location.x),
            Pair("d", location.y),
            Pair("e", location.z),
            Pair("f", (location.yaw * 256.0f / 360.0f).toInt().toByte()),
            Pair("g", (location.pitch * 256.0f / 360.0f).toInt().toByte()),
            Pair("k", getEntityTypeNMS(ink.ptms.adyeshach.common.entity.type.EntityTypes.FALLING_BLOCK)),
            Pair("l", id)
        )
    }

    override fun spawnEntityExperienceOrb(player: Player, entityId: Int, location: Location, amount: Int) {
        sendPacket(
            player,
            PacketPlayOutSpawnEntityExperienceOrb(),
            Pair("a", entityId),
            Pair("b", location.x),
            Pair("c", location.y),
            Pair("d", location.z),
            Pair("e", amount),
        )
    }

    override fun spawnEntityPainting(player: Player, entityId: Int, uuid: UUID, location: Location, direction: BukkitDirection, painting: BukkitPaintings) {
        sendPacket(
            player,
            PacketPlayOutSpawnEntityPainting(),
            Pair("a", entityId),
            Pair("b", uuid),
            Pair("c", getBlockPositionNMS(location)),
            Pair("d", EnumDirection.valueOf(direction.name)),
            Pair("e", IRegistry.MOTIVE.a(getPaintingNMS(painting) as Paintings?))
        )
    }

    override fun addPlayerInfo(player: Player, uuid: UUID, name: String, ping: Int, gameMode: GameMode, texture: Array<String>) {
        val profile = GameProfile(uuid, name)
        val infoData = PacketPlayOutPlayerInfo()
        if (texture.size == 2) {
            profile.properties.put("textures", Property("textures", texture[0], texture[1]))
        }
        sendPacket(
            player,
            infoData,
            Pair("a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER),
            Pair(
                "b",
                listOf(
                    infoData.PlayerInfoData(
                        profile,
                        ping,
                        Enums.getIfPresent(EnumGamemode::class.java, gameMode.name).or(EnumGamemode.NOT_SET),
                        ChatComponentText(name)
                    )
                )
            )
        )
    }

    override fun removePlayerInfo(player: Player, uuid: UUID) {
        val infoData = PacketPlayOutPlayerInfo()
        sendPacket(
            player,
            infoData,
            Pair("a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER),
            Pair("b", listOf(infoData.PlayerInfoData(GameProfile(uuid, ""), -1, null, null)))
        )
    }

    override fun destroyEntity(player: Player, entityId: Int) = sendPacket(player, PacketPlayOutEntityDestroy(entityId))

    override fun teleportEntity(player: Player, entityId: Int, location: Location) {
        sendPacket(
            player,
            PacketPlayOutEntityTeleport(),
            Pair("a", entityId),
            Pair("b", location.x),
            Pair("c", location.y),
            Pair("d", location.z),
            Pair("e", (location.yaw * 256 / 360).toInt().toByte()),
            Pair("f", (location.pitch * 256 / 360).toInt().toByte()),
            Pair("g", false) // onGround
        )
    }

    override fun relMoveEntity(player: Player, entityId: Int, x: Double, y: Double, z: Double) {
        sendPacket(
            player,
            PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                entityId,
                (x * 4096).toInt().toShort(),
                (y * 4096).toInt().toShort(),
                (z * 4096).toInt().toShort(),
                true
            )
        )
    }

    override fun setHeadRotation(player: Player, entityId: Int, yaw: Float, pitch: Float) {
        sendPacket(
            player,
            PacketPlayOutEntityHeadRotation(),
            Pair("a", entityId),
            Pair("b", MathHelper.d(yaw * 256.0f / 360.0f).toByte())
        )
        sendPacket(
            player,
            PacketPlayOutEntity.PacketPlayOutEntityLook(
                entityId,
                MathHelper.d(yaw * 256.0f / 360.0f).toByte(),
                MathHelper.d(pitch * 256.0f / 360.0f).toByte(),
                true
            )
        )
    }

    override fun updateEquipment(player: Player, entityId: Int, slot: EquipmentSlot, itemStack: ItemStack) {
        sendPacket(player, PacketPlayOutEntityEquipment(entityId, listOf(com.mojang.datafixers.util.Pair(EnumItemSlot.fromName(SimpleEquip.fromBukkit(slot).nms), CraftItemStack.asNMSCopy(itemStack)))))
    }

    override fun updateEntityMetadata(player: Player, entityId: Int, vararg objects: Any) {
        sendPacket(player, PacketPlayOutEntityMetadata(), Pair("a", entityId), Pair("b", objects.map { it as DataWatcher.Item<*> }.toList()))
    }

    override fun getMetaEntityItemStack(itemStack: ItemStack): Any {
        TODO("Not yet implemented")
    }

    override fun getMetaEntityInt(index: Int, value: Int): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.b), value)
    }

    override fun getMetaEntityFloat(index: Int, value: Float): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.c), value)
    }

    override fun getMetaEntityString(index: Int, value: String): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.d), value)
    }

    override fun getMetaEntityBoolean(index: Int, value: Boolean): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.i), value)
    }

    override fun getMetaEntityParticle(index: Int, value: BukkitParticles): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.j), getParticleNMS(value) as ParticleParam?)
    }

    override fun getMetaEntityByte(index: Int, value: Byte): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.a), value)
    }

    override fun getMetaEntityVector(index: Int, value: EulerAngle): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.k), Vector3f(value.x.toFloat(), value.y.toFloat(), value.z.toFloat()))
    }

    override fun getMetaEntityChatBaseComponent(index: Int, name: String): Any {
        return DataWatcher.Item<Optional<IChatBaseComponent>>(DataWatcherObject(index, DataWatcherRegistry.f), Optional.of(ChatComponentText(name)))
    }

    override fun getEntityTypeNMS(entityTypes: ink.ptms.adyeshach.common.entity.type.EntityTypes): Any {
        return SimpleReflection.getFieldValueChecked(EntityTypes::class.java, null, entityTypes.name, true) ?: EntityTypes.ARMOR_STAND
    }

    override fun getBlockPositionNMS(location: Location): Any {
        return BlockPosition(location.blockX, location.blockY, location.blockZ)
    }

    override fun getPaintingNMS(bukkitPaintings: BukkitPaintings): Any {
        return SimpleReflection.getFieldValueChecked(Paintings::class.java, null, bukkitPaintings.index.toString(), true) ?: Paintings.a
    }

    override fun getParticleNMS(bukkitParticles: BukkitParticles): Any {
        return SimpleReflection.getFieldValueChecked(Particles::class.java, null, bukkitParticles.name, true) ?: Particles.FLAME
    }

}