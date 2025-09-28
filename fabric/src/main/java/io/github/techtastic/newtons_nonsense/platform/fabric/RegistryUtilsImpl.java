package io.github.techtastic.newtons_nonsense.platform.fabric;

import io.github.techtastic.newtons_nonsense.fabric.NewtonsNonsenseFabric;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.minecraft.core.Registry;

public class RegistryUtilsImpl {
    public static Registry<PhysicsObjectType<?>> getPhysicsObjectTypesRegistry() {
        return NewtonsNonsenseFabric.PHYSICS_OBJECT_TYPES_REGISTRY;
    }
}
