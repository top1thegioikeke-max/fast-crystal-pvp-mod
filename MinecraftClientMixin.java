package dev.fastpvp.mixin;

import dev.fastpvp.FastAnchor;
import dev.fastpvp.FastCrystal;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow private int itemUseCooldown;

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void fastpvp$beforeUse(CallbackInfo ci) {
        FastCrystal.onPlacementAttempt((MinecraftClient) (Object) this);
    }

    // RETURN (not TAIL): doItemUse has several early returns, and TAIL only matches the last one.
    @Inject(method = "doItemUse", at = @At("RETURN"))
    private void fastpvp$afterUse(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        boolean anchor = FastAnchor.afterUse(client);
        boolean crystal = FastCrystal.shouldResetCooldown(client);
        if (anchor || crystal) {
            this.itemUseCooldown = 0;
        }
    }
}
