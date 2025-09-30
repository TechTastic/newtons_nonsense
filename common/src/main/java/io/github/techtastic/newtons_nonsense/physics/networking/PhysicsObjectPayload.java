package io.github.techtastic.newtons_nonsense.physics.networking;

import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import io.github.techtastic.newtons_nonsense.platform.RegistryUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record PhysicsObjectPayload<T extends AbstractPhysicsObject>(T object) implements CustomPacketPayload {

    public static final Type<PhysicsObjectPayload<? extends AbstractPhysicsObject>> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NewtonsNonsense.MOD_ID, "physics_object_update"));

    public static final StreamCodec<ByteBuf, PhysicsObjectPayload<? extends AbstractPhysicsObject>> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            (payload) -> RegistryUtils.getPhysicsObjectTypesRegistry().getKey(payload.object.getType()).toString(),
            ByteBufCodecs.COMPOUND_TAG,
            (payload) -> payload.object.getType().toTag(payload.object()),
            (type, nbt) -> new PhysicsObjectPayload<>(RegistryUtils.getPhysicsObjectTypesRegistry().get(ResourceLocation.tryParse(type)).fromTag(nbt))
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
