package io.github.techtastic.newtons_nonsense.neoforge;

import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import net.neoforged.fml.common.Mod;

@Mod(NewtonsNonsense.MOD_ID)
public final class NewtonsNonsenseNeoForge {
    public NewtonsNonsenseNeoForge() {
        // Run our common setup.
        NewtonsNonsense.init();
    }
}
