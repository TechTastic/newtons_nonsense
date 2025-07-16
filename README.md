# Newton's Nonsense
A Minecraft physics mod heavily inspired by Valkyrien Skies 2 utilizing PhysX via [PhysX-JNI](https://github.com/fabmax/physx-jni)!

## Layout Breakdown
Once the server comes online, the `Orchard` is created for the server. The `Orchard` handles setting up PhysX and a variety of helper methods.
It will also create, store, and manage the per-level physics islands (`AppleTree`s). `AppleTree`s, as stated before, as per-level physics island 
instances which create, store, and handle the per-level physics objects (`Apple`s) and they also handle chunk collision physics bodies. `Apple`s
are the physics objects themselves, whether that be a `StructureApple` (think VS2 Ships) or an `EntityApple`, and store any relevant physics information
like center of mass, orientation, transform matrices, etc.

`Orchard` (physics manager) -> `AppleTree` (physics island) -> `Apple` (physics object)

## To Do List
- [ ] Setup `Orchard`
  - [ ] Material Registry
  - [ ] BlockState-based Shape/Mass/Material Registry
- [ ] Setup `AppleTree`
  - [ ] Setup Chunk physics bodies
- [ ] Setup generic `Apple`
- [ ] Setup S2C sync and rendering
  - [ ] Position
  - [ ] Orientation
  - [ ] Matrices
- [ ] Setup Physics commands
- [ ] Setup `StructureApple`