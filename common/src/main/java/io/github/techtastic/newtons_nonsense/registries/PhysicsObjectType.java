package io.github.techtastic.newtons_nonsense.registries;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import io.github.techtastic.newtons_nonsense.physics.client.AbstractPhysicsObjectVisual;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PhysicsObjectType<T extends AbstractPhysicsObject> {
    public static final ResourceKey<Registry<PhysicsObjectType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(NewtonsNonsense.MOD_ID, "physics_object_types"));

    private final Function<CompoundTag, T> factory;
    private final PropertyDispatch.QuadFunction<ClientLevel, T, T, VisualizationContext, AbstractPhysicsObjectVisual<T>> visual;

    public PhysicsObjectType(Function<CompoundTag, T> factory, PropertyDispatch.QuadFunction<ClientLevel, T, T, VisualizationContext, AbstractPhysicsObjectVisual<T>> visual) {
        this.factory = factory;
        this.visual = visual;
    }

    public T create(CompoundTag nbt) {
        return factory.apply(nbt);
    }

    public CompoundTag toTag(AbstractPhysicsObject object) {
        return object.serializeNBT();
    }

    public AbstractPhysicsObjectVisual<T> createVisual(ClientLevel level, AbstractPhysicsObject object, @Nullable AbstractPhysicsObject previousObject, VisualizationContext context) {
        return this.visual.apply(level, (T) object, (T) previousObject, context);
    }
}
