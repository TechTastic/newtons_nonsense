package io.github.techtastic.newtons_nonsense.physics.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.VoxelShape;
import physx.physics.PxMaterial;

import static io.github.techtastic.newtons_nonsense.NewtonsNonsense.MOD_ID;

public class NNBlockPhysics {
    public static final ResourceKey<Registry<BlockInfo>> BLOCK_INFO_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "blockstates"));

    public record BlockInfo(VoxelShape shape, double mass, PxMaterial material) {
        //public static final Codec<BlockInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        //        Vec3.CODEC.optionalFieldOf("gravity", new Vec3(0, -9.81, 0)).forGetter(BlockInfo::gravity)
        //).apply(instance, BlockInfo::new));
    }
}
