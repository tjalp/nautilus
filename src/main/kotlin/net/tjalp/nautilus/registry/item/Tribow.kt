package net.tjalp.nautilus.registry.item

import net.tjalp.nautilus.item.NautilusItem
import org.bukkit.Material.BOW
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.util.Vector

object Tribow : NautilusItem() {

    override val identifier = "tribow"
    override val customModelData = 1
    override val preferredMaterial = BOW

    override fun onShoot(event: EntityShootBowEvent) {
        super.onShoot(event)

        val projectile = event.projectile

        if (projectile !is Projectile) return

        // TODO: Actually make this work
    }
}