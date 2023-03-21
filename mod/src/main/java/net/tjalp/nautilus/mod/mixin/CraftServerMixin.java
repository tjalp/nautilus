package net.tjalp.nautilus.mod.mixin;

import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.Logger;

@Mixin(CraftServer.class)
public abstract class CraftServerMixin {

    @Shadow(remap = false) public abstract Logger getLogger();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(CallbackInfo info) {
        this.getLogger().info("Loaded Nautilus mod!");
    }
}
