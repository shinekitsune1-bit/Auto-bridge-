package com.shine.autobridge;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import org.lwjgl.glfw.GLFW;

public class AutoBridgeClient implements ClientModInitializer {

    private static boolean enabled = false;
    private static KeyMapping toggleKey;
    private static int cooldown = 0;

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
                            Component.literal(
                                    "Auto Bridge: " +
                                            (enabled ? "ON" : "OFF")
                            ),
                            true
                    );
                }
            }

            if (!enabled || client.player == null) {
                return;
            }

            if (cooldown > 0) {
                cooldown--;
                return;
            }

            placeBridge(client);
        });
    }

    private static void placeBridge(Minecraft client) {

        if (client.level == null || client.gameMode == null) {
            return;
        }

        // Player ki movement direction
        double x = client.player.getDeltaMovement().x;
        double z = client.player.getDeltaMovement().z;

        if (Math.abs(x) < 0.01 && Math.abs(z) < 0.01) {
            return;
        }

        int slot = findAllowedBlock(client);

        if (slot == -1) {
            return;
        }

        /*
         * Player ke feet se ek block aage.
         * Y ko player ke feet ke level par rakha gaya hai,
         * taake air me bhi bridge continue ho.
         */
        int dx = 0;
        int dz = 0;

        if (Math.abs(x) > Math.abs(z)) {
            dx = x > 0 ? 1 : -1;
        } else {
            dz = z > 0 ? 1 : -1;
        }

        BlockPos target = BlockPos.containing(
                client.player.getX() + dx,
                client.player.getY() - 1.0,
                client.player.getZ() + dz
        );

        // Agar target already filled hai to kuch nahi karna
        if (!client.level.getBlockState(target).isAir()) {
            return;
        }

        /*
         * Target ke neeche support check.
         * Agar support hai to normal placement.
         */
        BlockPos support = target.below();

        if (!client.level.getBlockState(support).isAir()) {

            client.player.getInventory().setSelectedSlot(slot);

            client.gameMode.useItemOn(
                    client.player,
                    InteractionHand.MAIN_HAND,
                    new net.minecraft.world.phys.BlockHitResult(
                            support.getCenter(),
                            Direction.UP,
                            support,
                            false
                    )
            );

            cooldown = 2;
        }
    }

    private static int findAllowedBlock(Minecraft client) {

        for (int i = 0; i < 9; i++) {

            ItemStack stack =
                    client.player.getInventory().getItem(i);

            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                continue;
            }

            Block block = blockItem.getBlock();

            if (isAllowedBlock(block)) {
                return i;
            }
        }

        return -1;
    }

    private static boolean isAllowedBlock(Block block) {

        return block == Blocks.COBBLESTONE

                || block == Blocks.WHITE_WOOL
                || block == Blocks.ORANGE_WOOL
                || block == Blocks.MAGENTA_WOOL
                || block == Blocks.LIGHT_BLUE_WOOL
                || block == Blocks.YELLOW_WOOL
                || block == Blocks.LIME_WOOL
                || block == Blocks.PINK_WOOL
                || block == Blocks.GRAY_WOOL
                || block == Blocks.LIGHT_GRAY_WOOL
                || block == Blocks.CYAN_WOOL
                || block == Blocks.PURPLE_WOOL
                || block == Blocks.BLUE_WOOL
                || block == Blocks.BROWN_WOOL
                || block == Blocks.GREEN_WOOL
                || block == Blocks.RED_WOOL
                || block == Blocks.BLACK_WOOL;
    }
    }
