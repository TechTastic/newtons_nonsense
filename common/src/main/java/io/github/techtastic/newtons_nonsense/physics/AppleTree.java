package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.physx.PxSceneManager;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.UUID;

public class AppleTree {
    private final PxSceneManager physicsManager;
    private boolean paused = false;

    private final HashMap<UUID, Apple> objects = new HashMap<>();

    protected AppleTree(ServerLevel level) {
        this.physicsManager = new PxSceneManager(level);
    }

    public void pause(boolean pause) {
        this.paused = pause;
    }

    public void tryAndTick() {
        if (!paused)
            this.physicsManager.tick(1 / 20f);
    }
}
