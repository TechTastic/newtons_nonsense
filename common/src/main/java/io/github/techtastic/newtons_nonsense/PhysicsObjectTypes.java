package io.github.techtastic.newtons_nonsense;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import io.github.techtastic.newtons_nonsense.physics.object.box.BoxPhysicsObject;
import io.github.techtastic.newtons_nonsense.physics.object.box.BoxPhysicsObjectVisual;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PhysicsObjectTypes {
    private static final DeferredRegister<PhysicsObjectType<?>> PHYSICS_OBJECT_TYPES =
            DeferredRegister.create(NewtonsNonsense.MOD_ID, PhysicsObjectType.REGISTRY_KEY);

    public static Supplier<PhysicsObjectType<?>> BOX_OBJECT_TYPE =
            registerType("box", () -> new PhysicsObjectType<>(BoxPhysicsObject::new, BoxPhysicsObjectVisual::new));

    private static Supplier<PhysicsObjectType<?>> registerType(String id, Supplier<PhysicsObjectType<?>> type) {
        if (Platform.isFabric())
            return PHYSICS_OBJECT_TYPES.register(id, type);
        return type;
    }

    public static void register() {
        PHYSICS_OBJECT_TYPES.register();
    }

    public static void registerNeoForge(BiConsumer<String, Supplier<PhysicsObjectType<?>>> register) {
        register.accept("box", BOX_OBJECT_TYPE);
    }
}
