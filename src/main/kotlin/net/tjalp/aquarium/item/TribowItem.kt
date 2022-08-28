package net.tjalp.aquarium.item

import net.tjalp.aquarium.registry.CUSTOM_ITEM
import net.tjalp.aquarium.util.ItemBuilder
import net.tjalp.aquarium.util.mini
import org.bukkit.Material.BOW
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

object TribowItem : CustomItem() {

    override val identifier: String = "tribow"
    override val item: ItemStack
        get() = ItemBuilder(BOW).name(mini("Tribow")).customModelData(1).data(CUSTOM_ITEM, this.identifier).build()

    override fun onShoot(event: EntityShootBowEvent) {
        val entity = event.entity
        val projectile = event.projectile as Arrow
        val projVec = projectile.velocity
        val firstVec = projVec.clone().add(Vector(0.0, .5, 0.0))
        val secondVec = projVec.clone().subtract(Vector(0.0, .5, 0.0))
        
        fun copyData(from: Arrow, to: Arrow) {
            to.isCritical = from.isCritical
            to.color = from.color
            to.knockbackStrength = from.knockbackStrength
            to.damage = from.damage
            to.pierceLevel = from.pierceLevel
            to.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY // to prevent duplicating
            to.isShotFromCrossbow = from.isShotFromCrossbow
            to.setNoPhysics(from.hasNoPhysics())
            for (effect in from.customEffects) {
                to.addCustomEffect(effect, true)
            }
        }

        entity.launchProjectile(Arrow::class.java, firstVec).apply { copyData(projectile, this) }
        entity.launchProjectile(Arrow::class.java, secondVec).apply { copyData(projectile, this) }
    }
}