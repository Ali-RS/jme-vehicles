[The More Advanced Vehicles Project][mav] demonstrates vehicle simulation using
[the jMonkeyEngine (JME) game engine][jme].

<a name="toc"/>

## Contents of this document

+ [Important features](#features)
+ [How to download and run a pre-built release of More Advanced Vehicles](#prebuilt)
+ [How to build and run More Advanced Vehicles from source](#build)
+ [Controls](#controls)
+ [Wish list](#wishlist)
+ [Licensing](#licensing)
+ [Conventions](#conventions)
+ [Acknowledgments](#acks)

<a name="features"/>

## Important features

+ 2 environments:
  + racetrack
  + vehicle playground
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
  + buttons and animated menus
  + compass
  + speedometer
  + tachometer
  + steering-wheel indicator
  + automatic-transmission mode indicator
  + edit vehicle parameters
+ special effects:
  + skid marks
  + tire smoke
  + engine/horn sounds

[Jump to table of contents](#toc)

<img height="400" src="https://i.imgur.com/TsWukzO.png">

<a name="prebuilt"/>

## How to download and run a pre-built release of More Advanced Vehicles

1. Install [Java], if you don't already have it.
2. Browse to https://github.com/stephengold/jme-vehicles/releases/latest
3. Follow the "jme-vehicles.zip" link.
4. Save the ZIP file.
5. Extract the contents of the saved ZIP file.
  + using Bash:  `unzip jme-vehicles.zip`
6. `cd` to the extracted "bin" directory/folder
  + using Bash:  `cd jme-vehicles/bin`
7. Run the shell script.
  + using Bash:  `./jme-vehicles`

[Jump to table of contents](#toc)

<a name="build"/>

## How to build and run More Advanced Vehicles from source

 1. Install a [Java Development Kit (JDK)][openJDK],
    if you don't already have one.
 2. Download and extract the More Advanced Vehicles source code from GitHub:
   + using Git:
     + `git clone https://github.com/stephengold/jme-vehicles.git`
     + `cd jme-vehicles`
     + `git checkout -b latest v1.1.0`
   + using a web browser:
     + browse to https://github.com/stephengold/jme-vehicles/releases/latest
     + follow the "Source code (zip)" link
     + save the ZIP file
     + extract the contents of the saved ZIP file
     + `cd` to the extracted directory/folder
 3. Set the `JAVA_HOME` environment variable:
   + using Bash:  `export JAVA_HOME="` *path to your JDK* `"`
   + using Windows Command Prompt:  `set JAVA_HOME="` *path to your JDK* `"`
 4. Build the application:
   + using Bash:  `./gradlew build`
   + using Windows Command Prompt:  `.\gradlew build`
 5. Run the application:
   + using Bash:  `./gradlew run`
   + using Windows Command Prompt:  `.\gradlew run`

[Jump to table of contents](#toc)

<a name="controls"/>

## Controls

When a vehicle is loaded:

+ F5 : toggle viewpoints between dash camera and chase camera
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
+ Numpad9 or wheel up : zoom in (narrow the field of view)
+ Numpad3 or wheel down : zoom out (widen the field of view)
+ Numpad6 : reset the field of view (to 90 degrees vertical angle)
+ F12 : capture a screenshot to the current working directory
+ Esc : return to the Main Menu

Additional controls when the chase camera is active:

 + Numpad7 : dolly forward (toward the vehicle)
 + Numpad1 : dolly back (away from the vehicle)
 + Numpad8 or drag upward with MMB : orbit upward (to look down on the vehicle)
 + Numpad2 or drag downward with MMB : orbit downward (to look up at the vehicle)
 + Numpad5 or RMB : reset the viewpoint's position relative to the vehicle

[Jump to table of contents](#toc)

[adi]: https://github.com/scenemax3d "Adi Barda"
[atryder]: https://github.com/ATryder "Adam T. Ryder"
[github]: https://github.com "GitHub"
[gradle]: https://gradle.org "Gradle Project"
[java]: https://java.com "Java"
[jme]: http://jmonkeyengine.org  "jMonkeyEngine Project"
[lemur]: https://github.com/jMonkeyEngine-Contributions/Lemur "Lemur UI Toolkit"
[mav]: https://github.com/stephengold/jme-vehicles "More Advanced Vehicles Project"
[openJDK]: https://openjdk.java.net "OpenJDK Project"
[pspeed]: https://github.com/pspeed42 "Paul Speed"
[sergej]: https://hdrihaven.com/hdris/?a=Sergej%20Majboroda "HDRIs by Sergej Majboroda"
[tgt]: https://www.tgthorne.com/contact "Thomas Glenn Thorne"
[zampaoli]: https://sketchfab.com/mauro.zampaoli "Mauro Zampaoli"

<a name="wishlist"/>

## Wish list

This project is a work in progress.  Some ideas for future development:

+ More alternatives for:
  + Environments, such as: off-road, urban, and parking garage
  + Surface conditions, such as: wet, dirt, grass
  + Propulsion, such as: four-wheel drive, front-wheel drive, and jets
  + Vehicle physics, such as that used in RallyGame
  + Vehicle types, such as: hovertanks, motorcycles, motorized tricycles, speedboats, airplanes, and helicopters
  + Viewpoints, such as: FlyCam and plan view
+ More obstacles:
  + Other vehicles (parked or AI-controlled)
  + Animated non-vehicles, such as: gates, drawbridges, deer, and pedestrians
  + Passive non-vehicles, such as: traffic cones, portable barricades, and loose tires
+ More vehicle equipment:
  + Artificial horizon
  + Clock
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
  + Night driving
  + Player-vs-player over a network
  + Time trial
  + Career mode
+ More details:
  + Scoring for stunts
  + Simulate damage and tire wear
  + Sound effects for crashes and squealing tires

See also
[the project's issue tracker](https://github.com/stephengold/jme-vehicles/issues).

[Jump to table of contents](#toc)

<a name="licensing"/>

## Licensing

The source code has
[a BSD 3-Clause license](https://github.com/stephengold/jme-vehicles/blob/master/license.txt).

Resources/assets/media:

+ The [Droid font](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Interface/Fonts)
  has an Apache License, Version 2.0.
+ The [GT model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/GT)
  has a CC Attribution-NonCommercial-ShareAlike license.
+ The [gtr_nismo model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/gtr_nismo)
  has a CC Attribution-NonCommercial-ShareAlike license.
+ The [ford_ranger model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/ford_ranger)
  has a CC Attribution license.
+ The [racetrack model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/race1)
  has an Unlicense and a Creative Commons Zero v1.0 Universal license.
+ The [vehicle-playground model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/vehicle-playground)
  has a CC Zero Universal license.
+ The [marble_01 textures](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Textures/Ground/Marble)
  have a Creative Commons CC0 License.
+ The [quarry_03 texture](https://github.com/stephengold/jme-vehicles/blob/master/src/main/resources/Textures/Sky/quarry_03_4k.jpg)
  has a CC0 1.0 Universal license.
+ The [exit texture](https://github.com/stephengold/jme-vehicles/blob/master/src/main/resources/Textures/exit.png)
  has a CC0 1.0 Universal license.
+ The following 15 textures were generated procedurally by
  [the Georg Project](https://github.com/stephengold/Georg), which has a BSD 3-Clause license:
  + compass.png
  + horn-silent.png
  + horn-sound.png
  + loading.png
  + mute.png
  + pause.png
  + power-off.png
  + power-on.png
  + run.png
  + single-step.png
  + sound.png
  + speedo_bg_2.png
  + speedo_needle_2.png
  + steering.png
  + tachometer_bg.png

[Jump to table of contents](#toc)

<a name="conventions"/>

## Conventions

Package names begin with `com.jayfella.jme.vehicle`

The source code is compatible with JDK 8.

The world (and physics-space) coordinate system is:

 + the `+X` axis points toward the northern horizon
 + the `+Y` axis points up (toward the zenith)
 + the `+Z` axis points toward the eastern horizon

The world (and physics-space) units of distance are *meters*.

[Jump to table of contents](#toc)

<a name="acks"/>

## Acknowledgments

+ James Khan (aka "jayfella") initiated the Advanced Vehicles Project,
  on which this project is based.
+ [Paul Speed (aka "pspeed42")][pspeed] created the Lemur libraries.
+ [Adam T. Ryder (aka "ATryder")][atryder] created the jME-TTF library.
+ Lennart Demes created the marble_01 textures.
+ [Sergej Majboroda][sergej] created the quarry_03 texture.
+ [Thomas Glenn Thorne (aka "systmh")][tgt] created the GT model.
+ "isteven" created the gtr_nismo model.
+ [Mauro Zampaoli][zampaoli] created the ford_ranger model.
+ [Adi Barda (aka "adi.barda")][adi] created the racetrack model.

I am grateful to [Github] and Imgur
for providing free hosting for this project
and many other open-source projects.

If I've misattributed anything or left anyone out, please let me know so I can
correct the situation: sgold@sonic.net

[Jump to table of contents](#toc)
