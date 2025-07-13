package io.github.techtastic.newtons_nonsense.neoforge;

import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(NewtonsNonsense.MOD_ID)
public final class NewtonsNonsenseNeoForge {
    public NewtonsNonsenseNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Run our common setup.
        NewtonsNonsense.init();
    }
}
