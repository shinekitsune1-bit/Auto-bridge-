package com.shine.autobridge;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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

            if (!enabled || client.player == null ||
                    client.level == null || client.gameMode == null) {
                return;
            }

            if (cooldown > 0) {
                cooldown--;
                return;
            }

            scaffoldPlace(client);
        });
    }

    private static void scaffoldPlace(Minecraft client) {

        double motionX = client.player.getDeltaMovement().x;
        double motionZ = client.player.getDeltaMovement().z;

        if (Math.abs(motionX) < 0.005 &&
                Math.abs(motionZ) < 0.005) {
            return;
        }

        int dx;
        int dz;

        if (Math.abs(motionX) > Math.abs(motionZ)) {
            dx = motionX > 0 ? 1 : -1;
            dz = 0;
        } else {
            dx = 0;
            dz = motionZ > 0 ? 1 : -1;
        }

        BlockPos target = BlockPos.containing(
                client.player.getX() + dx,
                client.player.getY() - 1.0,
                client.player.getZ() + dz
        );

        if (!client.level.getBlockState(target).isAir()) {
            return;
        }

        int slot = findAllowedBlock(client);

        if (slot == -1) {
            return;
        }

        /*
         * Target ke neeche/side me nearby block dhoondo.
         * Isi existing block ki face par normal Minecraft
         * placement request bheji jayegi.
         */
        Placement placement = findPlacement(
                client,
                target
        );

        if (placement == null) {
            return;
        }

        client.player.getInventory().setSelectedSlot(slot);

        client.gameMode.useItemOn(
                client.player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        placement.hitPos,
                        placement.face,
                        placement.support,
                        false
                )
        );

        cooldown = 1;
    }

    private static Placement findPlacement(
            Minecraft client,
            BlockPos target
    ) {

        Direction[] directions = {
                Direction.DOWN,
                Direction.UP,
                Direction.NORTH,
                Direction.SOUTH,
                Direction.WEST,
                Direction.EAST
        };

        for (Direction direction : directions) {

            BlockPos support = target.relative(direction.getOpposite());

            BlockState state =
                    client.level.getBlockState(support);

            if (!state.isAir() && state.getCollisionShape(
                    client.level,
                    support
            ).isEmpty() == false) {

                return new Placement(
                        support,
                        direction,
                        support.getCenter()
                );
            }
        }

        return null;
    }

    private static int findAllowedBlock(Minecraft client) {

        for (int i = 0; i < 9; i++) {

            ItemStack stack =
                    client.player.getInventory().getItem(i);

            if (!(stack.getItem() instanceof BlockItem item)) {
                continue;
            }

            if (isAllowedBlock(item.getBlock())) {
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

    private record Placement(
            BlockPos support,
            Direction face,
            net.minecraft.world.phys.Vec3 hitPos
    ) {}
}
