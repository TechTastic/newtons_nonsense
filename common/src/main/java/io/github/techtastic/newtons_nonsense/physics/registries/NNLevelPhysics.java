package io.github.techtastic.newtons_nonsense.physics.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import static io.github.techtastic.newtons_nonsense.NewtonsNonsense.MOD_ID;

public class NNLevelPhysics {
    public static final ResourceKey<Registry<LevelInfo>> LEVEL_INFO_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "levels"));

    public record LevelInfo(Vec3 gravity) {
        public static final Codec<LevelInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.optionalFieldOf("gravity", new Vec3(0, -9.81, 0)).forGetter(LevelInfo::gravity)
        ).apply(instance, LevelInfo::new));
    }
}
