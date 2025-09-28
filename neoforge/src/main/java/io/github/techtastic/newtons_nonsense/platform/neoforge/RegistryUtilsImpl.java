package io.github.techtastic.newtons_nonsense.platform.neoforge;

import io.github.techtastic.newtons_nonsense.neoforge.NewtonsNonsenseNeoForge;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.minecraft.core.Registry;

public class RegistryUtilsImpl {
    public static Registry<PhysicsObjectType<?>> getPhysicsObjectTypesRegistry() {
        return NewtonsNonsenseNeoForge.PHYSICS_OBJECT_TYPES_REGISTRY;
    }
}
