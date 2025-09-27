package io.github.techtastic.newtons_nonsense;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.techtastic.newtons_nonsense.physics.object.BoxPhysicsObject;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;

public class PhysicsObjectTypes {
    public static final DeferredRegister<PhysicsObjectType<?>> PHYSICS_OBJECT_TYPES =
            DeferredRegister.create(NewtonsNonsense.MOD_ID, PhysicsObjectType.REGISTRY_KEY);

    public static final RegistrySupplier<PhysicsObjectType<?>> BOX_OBJECT_TYPE =
            PHYSICS_OBJECT_TYPES.register("box", () -> new PhysicsObjectType<>(BoxPhysicsObject::new));

    public static void register() {
        PHYSICS_OBJECT_TYPES.register();
    }
}
