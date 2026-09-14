package com.shine.autobridge;

import net.fabricmc.api.ClientModInitializer;

public class AutoBridgeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("Auto Bridge loaded!");
    }
}
