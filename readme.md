The Advanced Vehicles Project demonstrates
vehicle simulation using [the jMonkeyEngine (JME)][jme] game engine.

### Features

+ Vehicle Playground environment
+ 5 vehicle models:
  + Grand Tourer
  + GTR Nismo
  + pickup truck
  + hatchback
  + dune buggy
+ vehicle customization:
  + engine
  + brakes
  + automatic gearbox
  + wheels
  + tires with Pacejka model for friction (only the latitudinal forces are applied)
  + suspension
+ graphical user interface using [the Lemur UI Toolkit][lemur]:
  + menus
  + speedometer
  + tachometer
  + edit vehicle parameters
+ effects:
  + skid marks
  + tire smoke
  + engine/horn sounds

### Keyboard controls

+ F5 : toggle viewpoints between driver and chase camera
+ Y : toggle the engine on/off
+ W : accelerate forward
+ S : apply brakes
+ Space : apply handbrake
+ E : accelerate in reverse
+ A : steer left
+ D : steer right
+ H : sound the horn
+ R : reset the vehicle to a parked state
+ Pause or . : toggle the simulation paused/running
+ F12 : capture a screenshot to the current working directory

![screenshot](https://i.ibb.co/JyPHdv8/image.png)

[jme]: http://jmonkeyengine.org  "jMonkeyEngine Project"
[lemur]: https://github.com/jMonkeyEngine-Contributions/Lemur "Lemur UI Toolkit"
[sergej]: https://hdrihaven.com/hdris/?a=Sergej%20Majboroda "HDRIs by Sergej Majboroda"

### Licensing

The source code has
[a BSD 3-Clause license](https://github.com/stephengold/jme-vehicles/blob/master/license.txt).

Resources/assets:

+ The [GT model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/GT)
  has a CC Attribution-NonCommercial-ShareAlike license.
+ The [gtr_nismo model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/gtr_nismo)
  has a CC Attribution-NoDerivs license.
+ The [vehicle-playground model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/vehicle-playground)
  has a CC Zero Universal license.
+ The [marble_01 textures](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Textures/Ground/Marble)
  have a Creative Commons CC0 License.
+ The [quarry_03 texture](https://github.com/stephengold/jme-vehicles/blob/master/src/main/resources/Textures/Sky/quarry_03_4k.jpg)
  has a CC0 1.0 Universal license.

### Acknowledgments

+ James Khan (aka "jayfella") initiated the Advanced Vehicles Project.
+ Lennart Demes created the marble_01 textures.
+ [Sergej Majboroda][sergej] created the quarry_03 texture.
