# Newton's Nonsense
A Minecraft physics mod heavily inspired by Valkyrien Skies 2 utilizing PhysX via [PhysX-JNI](https://github.com/fabmax/physx-jni)!


### Explanation:
There are a few server-wide settings (backstage) and each `ServerLevel` holds its own `PxScene` (stage).
Each physics world holds all actors (Mimics) and joints.

Mimics (our equivalent of Ships) will be physically stored in 32x32 chunks at or beyond 30 million from (0, 0), be projected with the proper rotations and translations, and have all blocks on it interactable!

Each `BlockState` for each `Block` is given a mass, material, shape, and a type.
Initial mass, material, and type are derived from JSON and will have a way to modify the final result.
Initial shape is based on the `VoxelShape` of a block at the given state.


### To Do:
- [ ] Setup `Backstage` with methods to create scenes and necessary helper methods
- [ ] Setup `Stage` and attach it to `ServerLevel`
  - [ ] Properly handle ground mesh per level
  - [ ] Properly handle creating, updating, and deleting Mimics
  - [ ] Properly handle chunk loading, unloading
  - [ ] Properly handle server level ticking, server level loading, server level unloading
- [ ] Setup `Mimic`s
  - [ ] Properly handle rendering `Mimic`s (packets and rendering shenanigans)