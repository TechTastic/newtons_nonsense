package io.github.techtastic.newtons_nonsense.registries;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import io.github.techtastic.newtons_nonsense.physics.client.RenderWithPhysicsContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PhysicsObjectType<T extends AbstractPhysicsObject> implements Codec<T> {
    public static final ResourceKey<Registry<PhysicsObjectType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(NewtonsNonsense.MOD_ID, "physics_object_types"));

    private final Function<CompoundTag, T> factory;
    private final RenderWithPhysicsContext renderer;
    private final Codec<T> codec;

    public PhysicsObjectType(Function<CompoundTag, T> factory, RenderWithPhysicsContext renderer) {
        this.factory = factory;
        this.renderer = renderer;
        this.codec = this.buildCodec();
    }

    public T create(CompoundTag nbt) {
        return factory.apply(nbt);
    }

    public void renderObject(ClientLevel level, AbstractPhysicsObject object, @Nullable AbstractPhysicsObject previousObject, VisualizationContext visualizationContext, DynamicVisual.Context dynamicContext) {
        this.renderer.render(level, object, previousObject, visualizationContext, dynamicContext);
    }

    @SuppressWarnings("unchecked")
    private Codec<T> buildCodec() {
        return RecordCodecBuilder.create(instance ->
                instance.group(
                        CompoundTag.CODEC.fieldOf("data").forGetter(AbstractPhysicsObject::serializeNBT)
                ).apply(instance, factory)
        );
    }

    @Override
    public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> ops, U input) {
        return codec.decode(ops, input);
    }

    @Override
    public <U> DataResult<U> encode(T input, DynamicOps<U> ops, U prefix) {
        return codec.encode(input, ops, prefix);
    }
}
