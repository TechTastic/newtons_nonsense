package io.github.techtastic.newtons_nonsense.fabric;

import net.fabricmc.api.ModInitializer;

import io.github.techtastic.newtons_nonsense.NewtonsNonsense;

public final class NewtonsNonsenseFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        NewtonsNonsense.init();
    }
}
