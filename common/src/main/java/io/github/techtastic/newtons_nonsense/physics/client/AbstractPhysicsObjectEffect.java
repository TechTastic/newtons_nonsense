package io.github.techtastic.newtons_nonsense.physics.client;

import dev.engine_room.flywheel.api.visual.Effect;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbstractPhysicsObjectEffect<T extends AbstractPhysicsObject> implements Effect {
    private final ClientLevel level;
    private T object;
    private @Nullable T previousObject;

    private AbstractPhysicsObjectVisual<? extends AbstractPhysicsObject> visual;

    public AbstractPhysicsObjectEffect(ClientLevel level, T object, @Nullable T previousObject) {
        this.level = level;
        this.object = object;
        this.previousObject = previousObject;
    }

    public AbstractPhysicsObjectEffect<T> update(AbstractPhysicsObject object, AbstractPhysicsObject previousObject) {
        this.object = (T) object;
        this.previousObject = (T) previousObject;
        return this;
    }

    @Override
    public @NotNull LevelAccessor level() {
        return this.level;
    }

    @Override
    public @NotNull AbstractPhysicsObjectVisual<? extends AbstractPhysicsObject> visualize(VisualizationContext visualizationContext, float v) {
        if (this.visual == null)
            this.visual = this.object.getType().createVisual(this.level, this.object, this.previousObject, visualizationContext);
        else
            this.visual.update(v);
        return visual;
    }
}
