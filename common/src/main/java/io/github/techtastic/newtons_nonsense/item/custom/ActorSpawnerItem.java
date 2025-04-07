package io.github.techtastic.newtons_nonsense.item.custom;

import io.github.techtastic.newtons_nonsense.physics.Stage;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ActorSpawnerItem extends Item {
    public ActorSpawnerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
        // Spawn Actor at Clicked Position

        Level level = useOnContext.getLevel();
        if (!(level instanceof ServerLevel sLevel))
            return InteractionResult.SUCCESS;

        BlockPos pos = useOnContext.getClickedPos().relative(useOnContext.getClickedFace());
        Stage.getOrCreateStage(sLevel).addActor(Backstage.createDefaultBox(pos.getX(), pos.getY(), pos.getZ()));

        return InteractionResult.SUCCESS;
    }
}
