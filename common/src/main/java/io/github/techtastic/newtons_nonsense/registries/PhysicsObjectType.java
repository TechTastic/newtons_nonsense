package io.github.techtastic.newtons_nonsense.registries;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class PhysicsObjectType<T extends AbstractPhysicsObject> implements Codec<T> {
    public static final ResourceKey<Registry<PhysicsObjectType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(NewtonsNonsense.MOD_ID, "physics_object_types"));

    private final Function<CompoundTag, T> factory;
    private final Codec<T> codec;

    public PhysicsObjectType(Function<CompoundTag, T> factory) {
        this.factory = factory;
        this.codec = this.buildCodec();
    }

    public T create(CompoundTag nbt) {
        return factory.apply(nbt);
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
