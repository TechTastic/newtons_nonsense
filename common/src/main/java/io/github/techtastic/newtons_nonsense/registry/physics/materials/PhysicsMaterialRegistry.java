package io.github.techtastic.newtons_nonsense.registry.physics.materials;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import physx.physics.PxMaterial;

import static io.github.techtastic.newtons_nonsense.NewtonsNonsense.MOD_ID;

public class PhysicsMaterialRegistry {
    public static final Codec<PxMaterial> MATERIAL_CODEC;

    public static final ResourceKey<Registry<PxMaterial>> MATERIAL_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "materials"));

    public static final ResourceKey<PxMaterial> DEFAULT_MATERIAL =
            ResourceKey.create(MATERIAL_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(MOD_ID, "default"));

    static {
        MATERIAL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("staticFriction").forGetter(PxMaterial::getStaticFriction),
                Codec.FLOAT.fieldOf("dynamicFriction").forGetter(PxMaterial::getDynamicFriction),
                Codec.FLOAT.fieldOf("restitution").forGetter(PxMaterial::getRestitution)
        ).apply(instance, Backstage.physics::createMaterial));
    }
}
