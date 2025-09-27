package io.github.techtastic.newtons_nonsense.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.Backend;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import physx.physics.PxMaterial;

public class Material {
    public static final ResourceKey<Registry<PxMaterial>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(NewtonsNonsense.MOD_ID, "materials"));

    public static final Codec<PxMaterial> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("dynamicFriction", 0.5f).forGetter(PxMaterial::getDynamicFriction),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("staticFriction", 0.5f).forGetter(PxMaterial::getStaticFriction),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("restitution", 0.5f).forGetter(PxMaterial::getRestitution)
    ).apply(instance, Backend.PHYSICS::createMaterial));

    public static void bootstrap(BootstrapContext<PxMaterial> context) {
    }
}
