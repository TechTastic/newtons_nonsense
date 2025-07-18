package io.github.techtastic.newtons_nonsense.physics.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import physx.physics.PxMaterial;

import static io.github.techtastic.newtons_nonsense.NewtonsNonsense.MOD_ID;

public class NNMaterials {
    public static final ResourceKey<Registry<PxMaterial>> MATERIAL_KEY;

    public static final ResourceLocation DEFAULT;

    static {
        MATERIAL_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "materials"));

        DEFAULT = ResourceLocation.fromNamespaceAndPath(MOD_ID, "default");
    }
}
