package io.github.techtastic.newtons_nonsense.physics.networking;

import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import io.github.techtastic.newtons_nonsense.platform.RegistryUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PhysicsObjectPayload(UUID id, AbstractPhysicsObject object)
        implements CustomPacketPayload {

    public static final Type<PhysicsObjectPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NewtonsNonsense.MOD_ID, "physics_object_update"));

    public static final StreamCodec<ByteBuf, PhysicsObjectPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            payload -> payload.id.toString(),
            ByteBufCodecs.COMPOUND_TAG,
            (payload) -> {
                CompoundTag tag = payload.object.serializeNBT();
                tag.putString("key", RegistryUtils.getPhysicsObjectTypesRegistry().getKey(payload.object.getType()).toString());
                return tag;
            },
            (strId, nbt) -> new PhysicsObjectPayload(UUID.fromString(strId), RegistryUtils.getPhysicsObjectTypesRegistry()
                    .get(ResourceLocation.tryParse(nbt.getString("key"))).create(nbt))
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
