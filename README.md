# Newton's Nonsense
A Minecraft physics mod heavily inspired by Valkyrien Skies 2 utilizing PhysX via [PhysX-JNI](https://github.com/fabmax/physx-jni)!


### Explanation:
There are a few server-wide settings (backstage) and each `ServerLevel` holds its own `PxScene` (stage).
Each physics world holds all actors (Mimics) and joints.

Mimics (our equivalent of Ships) will be physically stored in 32x32 chunks at or beyond 30 million from (0, 0), be projected with the proper rotations and translations, and have all blocks on it interactable!

Each `BlockState` for each `Block` is given a mass, material, shape, and a type.
Initial mass, material, and type are derived from JSON and will have a way to modify the final result.
Initial shape is based on the `VoxelShape` of a block at the given state.


### Commands:
There are a few commands I will create for debugging
- `sim` base command
  - `pause` pause the simulation tick
  - `resume` resume the simulation tick
  - `step <elapsedTime>` step the simulation forward in time, ignores pause/resume
  - `materials` lists all materials available at runtime
  - `actor`
    - `remove all` removes and frees all actors in the scene
    - `add`
      - `static` adds a new static actor at your position with `newtons_nonsense:default` material
        - `<pos>` sets the new actor's position, optional
          - `<rot>` rotates the new actor on XYZ axis', optional
            - `<material>` sets the new actor's shape material, optional
              - `<size>` sets the new actor's shape size in half-lengths, optional
      - `dynamic` adds a new dynamic actor at your position with `newtons_nonsense:default` material
        - `<pos>` sets the new actor's position, optional
          - `<rot>` rotates the new actor on XYZ axis', optional
            - `<material>` sets the new actor's shape material, optional
              - `<size>` sets the new actor's shape size in half-lengths, optional

### To Do:
- [X] Setup `Backstage` with methods to create scenes and necessary helper methods
- [ ] Setup `Stage` and attach it to `ServerLevel`
  - [X] Properly handle ground mesh per level
  - [ ] Properly handle creating, updating, and deleting Mimics
  - [ ] Properly handle chunk loading, unloading
  - [X] Properly handle server level ticking, server level loading, server level unloading
- [ ] Setup `Mimic`s
  - [ ] Properly handle rendering `Mimic`s (packets and rendering shenanigans)