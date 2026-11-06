package dev.fastpvp;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class FastPvpClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientEntityEvents.ENTITY_LOAD.register(FastCrystal::onEntityLoad);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("fastpvp")
                        .then(toggle("crystal", () -> FastPvpConfig.fastCrystal, v -> FastPvpConfig.fastCrystal = v))
                        .then(toggle("trigger", () -> FastPvpConfig.crystalTrigger, v -> FastPvpConfig.crystalTrigger = v))
                        .then(toggle("anchor", () -> FastPvpConfig.fastAnchor, v -> FastPvpConfig.fastAnchor = v))
                        .then(toggle("detonate", () -> FastPvpConfig.anchorAutoDetonate, v -> FastPvpConfig.anchorAutoDetonate = v))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> toggle(
            String name, BooleanSupplier getter, Consumer<Boolean> setter) {
        return literal(name).executes(ctx -> {
            boolean value = !getter.getAsBoolean();
            setter.accept(value);
            ctx.getSource().sendFeedback(Text.literal("FastPvP " + name + ": " + (value ? "ON" : "OFF")));
            return 1;
        });
    }
}
