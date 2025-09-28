package io.github.techtastic.newtons_nonsense.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.minecraft.core.Registry;

public class RegistryUtils {
    @ExpectPlatform
    public static Registry<PhysicsObjectType<?>> getPhysicsObjectTypesRegistry() {
        throw new AssertionError();
    }
}
