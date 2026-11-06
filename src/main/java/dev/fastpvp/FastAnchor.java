package dev.fastpvp;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FastAnchor {
    private static final int MAX_CHARGES = 4;

    private FastAnchor() {}

    private static Hand glowstoneHand(ClientPlayerEntity p) {
        if (p.getMainHandStack().isOf(Items.GLOWSTONE)) return Hand.MAIN_HAND;
        if (p.getOffHandStack().isOf(Items.GLOWSTONE)) return Hand.OFF_HAND;
        return null;
    }

    /**
     * Runs after vanilla doItemUse. Vanilla has already performed the first click (charge or detonate).
     * @return true if an anchor is targeted, so the caller should clear the use cooldown
     */
    public static boolean afterUse(MinecraftClient client) {
        if (!FastPvpConfig.fastAnchor) return false;

        ClientPlayerEntity p = client.player;
        ClientWorld w = client.world;
        ClientPlayerInteractionManager im = client.interactionManager;
        if (p == null || w == null || im == null) return false;
        if (!(client.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return false;

        BlockPos pos = hit.getBlockPos();
        BlockState state = w.getBlockState(pos);
        if (!state.isOf(Blocks.RESPAWN_ANCHOR)) return false;

        // 1) Rapid charge: top up to 4 in the same tick. Each call uses the sequenced packet path and
        //    predicts the new state client-side, so the loop condition always reads fresh data.
        Hand hand = glowstoneHand(p);
        if (hand != null) {
            for (int i = 0; i < MAX_CHARGES; i++) {
                state = w.getBlockState(pos);
                // Never click glowstone on a full anchor: outside the Nether that would detonate it.
                if (!state.isOf(Blocks.RESPAWN_ANCHOR) || state.get(RespawnAnchorBlock.CHARGES) >= MAX_CHARGES) break;
                if (!p.getStackInHand(hand).isOf(Items.GLOWSTONE)) break;
                ActionResult result = im.interactBlock(p, hand, hit);
                if (!result.isAccepted()) break;
            }
        }

        // 2) Optional instant detonation (never in the Nether, where anchors set spawn instead).
        state = w.getBlockState(pos);
        if (FastPvpConfig.anchorAutoDetonate
                && w.getRegistryKey() != World.NETHER
                && state.isOf(Blocks.RESPAWN_ANCHOR)
                && state.get(RespawnAnchorBlock.CHARGES) >= 1) {
            int slot = findDetonatorSlot(p);
            if (slot >= 0) {
                p.getInventory().setSelectedSlot(slot); // interactBlock syncs the selected slot before the click
                im.interactBlock(p, Hand.MAIN_HAND, hit);
                p.swingHand(Hand.MAIN_HAND);
            }
        }
        return true;
    }

    /** Prefers a Totem of Undying; never returns a glowstone slot. */
    private static int findDetonatorSlot(ClientPlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }
}
