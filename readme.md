The More Advanced Vehicles Project demonstrates
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
  + menus and buttons
  + speedometer
  + tachometer
  + steering-wheel indicator
  + edit vehicle parameters
+ effects:
  + skid marks
  + tire smoke
  + engine/horn sounds

### Keyboard controls

When a vehicle is loaded:

+ F5 : toggle viewpoints between hood camera and chase camera
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
+ Esc : return to the Main Menu

<img height="150" src="https://i.imgur.com/n7iyCF8.png">

[atryder]: https://github.com/ATryder "Adam T. Ryder"
[jme]: http://jmonkeyengine.org  "jMonkeyEngine Project"
[lemur]: https://github.com/jMonkeyEngine-Contributions/Lemur "Lemur UI Toolkit"
[pspeed]: https://github.com/pspeed42 "Paul Speed"
[sergej]: https://hdrihaven.com/hdris/?a=Sergej%20Majboroda "HDRIs by Sergej Majboroda"
[tgt]: https://www.tgthorne.com/contact "Thomas Glenn Thorne"

### Wish list

+ Eliminate the initial drop
+ More alternatives for:
  + Environments, such as: off-road, urban, and parking garage
  + Propulsion, such as: four-wheel drive, front-wheel drive, and jets
  + Vehicle physics, such as that used in RallyGame
  + Vehicle types, such as: hovertanks, motorcycles, speedboats, airplanes, and helicopters
  + Viewpoints, such as: FlyCam and plan view
+ More obstacles:
  + Other vehicles (parked or AI-controlled)
  + Animated non-vehicles, such as: gates, drawbridges, deer, and pedestrians
  + Passive non-vehicles, such as: traffic cones, portable barricades, and loose tires
+ More vehicle equipment:
  + Artificial horizon
  + Clock
  + Compass
  + Cruise control
  + Fuel gauge
  + Headlamps
  + Manual transmission with clutch
  + Maps
  + Mirrors and backup assist
  + Nitrous oxide
  + Odometer
  + Oil-temperature gauge
  + Sirens
  + Trailers
  + Weaponry
+ More scenarios:
  + Crazy taxi
  + Demolition derby
  + Night driving with headlamps
  + Player-vs-player over a network
  + Time trial
+ More details:
  + Scoring for stunts
  + Simulate damage and tire wear
  + Sound effects for crashes

See also
[the project's issue tracker](https://github.com/stephengold/jme-vehicles/issues).

### Licensing

The source code has
[a BSD 3-Clause license](https://github.com/stephengold/jme-vehicles/blob/master/license.txt).

Resources/assets:

+ The [Droid font](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Interface/Fonts)
  has an Apache License, Version 2.0.
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
+ [Paul Speed (aka "pspeed42")][pspeed] created the Lemur libraries.
+ Lennart Demes created the marble_01 textures.
+ [Sergej Majboroda][sergej] created the quarry_03 texture.
+ [Thomas Glenn Thorne (aka "systmh")][tgt] created the GT model.
+ "isteven" created the gtr_nismo model.
+ [Adam T. Ryder (aka "ATryder")][atryder] created the jME-TTF library.
