package com.shine.autobridge;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.lwjgl.glfw.GLFW;

public class AutoBridgeClient implements ClientModInitializer {

    private static boolean enabled = false;
    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {

        toggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.auto-bridge.toggle",
                        GLFW.GLFW_KEY_RIGHT_CONTROL,
                        "category.auto-bridge"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (toggleKey.consumeClick()) {
                enabled = !enabled;

                if (client.player != null) {
                    client.player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "Auto Bridge: " + (enabled ? "ON" : "OFF")
                            ),
                            true
                    );
                }
            }

            if (enabled) {
                placeBridgeBlock(client);
            }
        });
    }

    private static void placeBridgeBlock(Minecraft client) {

        if (client.player == null ||
                client.level == null ||
                client.gameMode == null) {
            return;
        }

        BlockPos playerPos = client.player.blockPosition();

        // Block directly below the player's feet
        BlockPos targetPos = playerPos.below();

        // Only place if the position is empty
        if (!client.level.getBlockState(targetPos).isAir()) {
            return;
        }

        int slot = findAllowedBlock(client);

        if (slot == -1) {
            return;
        }

        client.player.getInventory().setSelectedSlot(slot);

        client.gameMode.useItemOn(
                client.player,
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        client.player.position(),
                        Direction.UP,
                        targetPos,
                        false
                )
        );
    }

    private static int findAllowedBlock(Minecraft client) {

        for (int i = 0; i < 9; i++) {

            ItemStack stack =
                    client.player.getInventory().getItem(i);

            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                continue;
            }

            BlockState state =
                    blockItem.getBlock().defaultBlockState();

            if (isAllowedBlock(state)) {
                return i;
            }
        }

        return -1;
    }

    private static boolean isAllowedBlock(BlockState state) {

        return state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.WHITE_WOOL)
                || state.is(Blocks.ORANGE_WOOL)
                || state.is(Blocks.MAGENTA_WOOL)
                || state.is(Blocks.LIGHT_BLUE_WOOL)
                || state.is(Blocks.YELLOW_WOOL)
                || state.is(Blocks.LIME_WOOL)
                || state.is(Blocks.PINK_WOOL)
                || state.is(Blocks.GRAY_WOOL)
                || state.is(Blocks.LIGHT_GRAY_WOOL)
                || state.is(Blocks.CYAN_WOOL)
                || state.is(Blocks.PURPLE_WOOL)
                || state.is(Blocks.BLUE_WOOL)
                || state.is(Blocks.BROWN_WOOL)
                || state.is(Blocks.GREEN_WOOL)
                || state.is(Blocks.RED_WOOL)
                || state.is(Blocks.BLACK_WOOL);
    }
                    }
