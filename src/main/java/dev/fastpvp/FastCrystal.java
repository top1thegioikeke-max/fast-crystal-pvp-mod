package dev.fastpvp;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public final class FastCrystal {
    /** Ticks after a placement attempt during which a spawned crystal is considered "ours". */
    private static final long MATCH_WINDOW_TICKS = 20L;

    private static BlockPos pendingBase = null;
    private static long pendingTick = 0L;

    private FastCrystal() {}

    private static boolean holdingCrystal(ClientPlayerEntity p) {
        return p.getMainHandStack().isOf(Items.END_CRYSTAL) || p.getOffHandStack().isOf(Items.END_CRYSTAL);
    }

    /** Called at the start of MinecraftClient#doItemUse: remember where we are trying to place. */
    public static void onPlacementAttempt(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        ClientWorld w = client.world;
        if (!FastPvpConfig.crystalTrigger || p == null || w == null) return;
        if (!holdingCrystal(p)) return;
        if (!(client.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockState state = w.getBlockState(hit.getBlockPos());
        if (state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.BEDROCK)) {
            pendingBase = hit.getBlockPos().toImmutable();
            pendingTick = w.getTime();
        }
    }

    /** Called after doItemUse: should the use cooldown be cleared? */
    public static boolean shouldResetCooldown(MinecraftClient client) {
        return FastPvpConfig.fastCrystal && client.player != null && holdingCrystal(client.player);
    }

    /**
     * ClientEntityEvents.ENTITY_LOAD hook. Fires when the spawn packet has been applied, which is the earliest
     * moment the crystal exists client-side. If it spawned on the block we just placed on, attack it right away.
     */
    public static void onEntityLoad(Entity entity, ClientWorld world) {
        if (!FastPvpConfig.crystalTrigger || pendingBase == null) return;
        if (!(entity instanceof EndCrystalEntity)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity p = client.player;
        if (p == null || client.interactionManager == null) return;

        if (world.getTime() - pendingTick > MATCH_WINDOW_TICKS) {
            pendingBase = null;
            return;
        }
        if (!entity.getBlockPos().equals(pendingBase.up())) return;
        pendingBase = null;

        if (!p.canInteractWithEntity(entity, 0.0)) return; // avoid sending out-of-reach attacks the server would reject

        client.interactionManager.attackEntity(p, entity); // sends the vanilla attack packet
        p.swingHand(Hand.MAIN_HAND);
    }
}
