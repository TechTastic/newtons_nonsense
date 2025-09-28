package io.github.techtastic.newtons_nonsense.physics.client;

import dev.engine_room.flywheel.api.visual.Effect;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbstractPhysicsObjectEffect<T extends AbstractPhysicsObject> implements Effect {
    private final ClientLevel level;
    private final T object;
    private final @Nullable T previousObject;

    public AbstractPhysicsObjectEffect(ClientLevel level, T object, @Nullable T previousObject) {
        this.level = level;
        this.object = object;
        this.previousObject = previousObject;
    }

    @Override
    public @NotNull LevelAccessor level() {
        return this.level;
    }

    @Override
    public @NotNull AbstractPhysicsObjectVisual<T> visualize(VisualizationContext visualizationContext, float v) {
        NewtonsNonsense.LOGGER.info("This is the visualize method!");
        return new AbstractPhysicsObjectVisual<>(this.level, this.object, this.previousObject, visualizationContext);
    }
}
