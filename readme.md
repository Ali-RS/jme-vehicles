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
+ [External links](#links)
+ [Acknowledgments](#acks)

<a name="features"/>

## Important features

+ 3 worlds:
  + endless plain
  + racetrack
  + vehicle playground
+ 7 vehicle models:
  + Grand Tourer
  + GTR Nismo
  + pickup truck
  + hatchback
  + dune buggy
  + Rotator
  + HoverTank
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
     + `git checkout -b latest v1.2.0`
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

General controls:

+ Numpad9 or wheel up : zoom in (narrow the field of view)
+ Numpad3 or wheel down : zoom out (widen the field of view)
+ Numpad6 : reset the field of view (to 90 degrees vertical angle)
+ F12 : capture a screenshot to the current working directory

When driving a vehicle:

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
+ Esc : return to the Main Menu

Additional controls when the chase camera or orbit camera is active:

 + Numpad7 : dolly forward (toward the vehicle)
 + Numpad1 : dolly back (away from the vehicle)
 + Numpad8 or drag upward with MMB : orbit upward (to look down on the vehicle)
 + Numpad2 or drag downward with MMB : orbit downward (to look up at the vehicle)
 + Numpad5 or RMB : reset the viewpoint's position relative to the vehicle

Additional controls when the orbit camera is active:

 + Drag left with MMB : orbit leftward
 + Drag right with MMB : orbit rightward

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

More Advanced Vehicles is a work in progress.  Some ideas for future development:

+ More alternatives for:
  + Worlds, such as: block world, drag strip, off-road, parking garage, parking lot, showroom, and urban grid
  + Surface conditions, such as: wet, dirt, and grass
  + Propulsion, such as: jets and propellers
  + Skies, such as TehLeo's SevenSky
  + User interface, such as: joystick and NiftyGUI
  + Vehicle dynamics, such as that used in Murph9's RallyGame
  + Vehicle types, such as: buses, tanks, golf carts, hovertanks, motorcycles, palanquins, rickshaws, aerial trams, snowmobiles, speedboats, airplanes, and helicopters
  + Viewpoints, such as: FlyCam and plan view
+ More obstacles:
  + Other vehicles (parked or AI-controlled)
  + Animated non-vehicles, such as: gates, drawbridges, deer, and pedestrians
  + Passive non-vehicles, such as: traffic cones, portable barricades, and loose tires
+ More vehicle equipment:
  + Anti-lock braking
  + Artificial horizon
  + Brake lights and turn signals
  + Clock/stopwatch/timer
  + Cruise control
  + Fuel gauge
  + Headlamps
  + Manual transmission with clutch
  + Maps
  + Mirrors and backup assist
  + Nitrous oxide
  + Odometer
  + Oil-temperature gauge
  + Operable doors, hood, and trunk
  + Passengers
  + Sirens
  + Speed limiter
  + Starter motor
  + Trailers
  + Weaponry
  + Windshield wipers
+ More scenarios:
  + Crazy taxi
  + Demolition derby
  + Night driving
  + Performance tests, such as: braking distance, turning radius, and zero-to-60
  + Player-vs-player over a network
  + Time trial
  + Career mode
+ More details:
  + Scoring for stunts
  + Simulate damage, brake wear, and tire wear
  + Sound effects for crashes, squealing tires, and wind

See also
[the project's issue tracker](https://github.com/stephengold/jme-vehicles/issues).

[Jump to table of contents](#toc)

<a name="licensing"/>

## Licensing

The source code has
[a BSD 3-Clause license](https://github.com/stephengold/jme-vehicles/blob/master/license.txt).

Resources/assets/media:

+ The [Droid Serif font](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Interface/Fonts)
  has an Apache License, Version 2.0.
+ The [Opel GT Retopo model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/GT)
  has a CC Attribution-NonCommercial-ShareAlike license.
+ The [HoverTank model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/Tank)
  has a BSD 3-Clause license.
+ The [Ford Ranger model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/ford_ranger)
  has a CC Attribution license.
+ The [Nissan GT-R model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/gtr_nismo)
  has a CC Attribution-NonCommercial-ShareAlike license.
+ The [HCR2 Buggy model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/hcr2_buggy)
  has a CC Attribution license.
+ The [HCR2 Rotator model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/hcr2_rotator)
  has a CC Attribution license.
+ The [Modern Hatchback - Low Poly model model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/modern_hatchback)
  has a CC Attribution license.
+ The [Race Track model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/race1)
  has an Unlicense license.
+ The [terrain textures](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Textures/Terrain)
  has a BSD 3-Clause license.
+ The [Vehicle Playground model](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Models/vehicle-playground)
  has a CC0 license.
+ The [marble_01 textures](https://github.com/stephengold/jme-vehicles/tree/master/src/main/resources/Textures/Ground/Marble)
  have a CC0 license.
+ The [Quarry 03 texture](https://github.com/stephengold/jme-vehicles/blob/master/src/main/resources/Textures/Sky/quarry_03)
  has a CC0 license.
+ The [Car-door Exit Button texture](https://github.com/stephengold/jme-vehicles/blob/master/src/main/resources/Textures/sgold)
  has a CC0 license.
+ The [Lunar libration with phase Oct 2007 texture](https://github.com/stephengold/jme-vehicles/blob/master/src/main/resources/Textures/sgold)
  has a PD-self license.
+ The [Georg textures](https://github.com/stephengold/jme-vehicles/blob/master/src/main/resources/Textures/Georg)
  were generated procedurally using [the Georg Project](https://github.com/stephengold/Georg),
  which has a BSD 3-Clause license.

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


<a name="links"/>

## External links

+ January 2021 [walkthru video](https://www.youtube.com/watch?v=RnfEhB3xOys)

[Jump to table of contents](#toc)


<a name="acks"/>

## Acknowledgments

+ James Khan (aka "jayfella") initiated
  [the Advanced Vehicles Project](https://github.com/jMonkeyEngine-archive/jme-vehicles-jayfella-github),
  on which this project is based.
+ [Paul Speed (aka "pspeed42")][pspeed] created the Lemur libraries.
+ [Adam T. Ryder (aka "ATryder")][atryder] created the jME-TTF library.
+ Rémy Bouquet (aka "Nehon") created the "Jaime" and "Enhanced HoverTank" models.
+ [Adi Barda (aka "adi.barda")][adi] created the racetrack model.
+ Rob Tuytel created the "marble_01" textures.
+ [Sergej Majboroda][sergej] created the "Quarry 03" texture.
+ Tom Ruen created the "Lunar libration with phase Oct 2007" animation.

### CC Attribution

+ This work is based on "Opel GT Retopo"
  (https://sketchfab.com/3d-models/opel-gt-retopo-badcab3c8a3d42359c8416db8a7427fe)
  by [Thomas Glenn Thorne (aka "systmh")][tgt]
  licensed under CC-BY-NC-SA.
+ This work is based on "Ford Ranger"
  (https://sketchfab.com/3d-models/ford-ranger-dade78dc96e34f1a8cbcf14dd47d84de)
  by mauro.zampaoli (https://sketchfab.com/mauro.zampaoli)
  licensed under CC-BY-4.0 (http://creativecommons.org/licenses/by/4.0/).
+ This work is based on "Nissan GT-R"
  (https://sketchfab.com/3d-models/nissan-gt-r-5f5781614c6f4ff4b7cb1d3cff9d931c)
  by iSteven
  licensed under CC-BY-NC-SA.
+ This work is based on "HCR2 Buggy"
  (https://sketchfab.com/3d-models/hcr2-buggy-a65fe5c27464448cbce7fe61c49159ef)
  by oakar258
  licensed under CC-BY-4.0 (http://creativecommons.org/licenses/by/4.0/).
+ This work is based on "HCR2 Rotator"
  (https://sketchfab.com/3d-models/hcr2-rotator-f03e95525b4c48cfb659064a76d8cd53)
  by oakar258 (https://sketchfab.com/oakar258)
  licensed under CC-BY-4.0 (http://creativecommons.org/licenses/by/4.0/).
+ This work is based on "Modern Hatchback - Low Poly model"
  (https://sketchfab.com/3d-models/modern-hatchback-low-poly-model-055ff8a21b8d4d279debca089e2fafcd)
  by Daniel Zhabotinsky (https://sketchfab.com/DanielZhabotinsky)
  licensed under CC-BY-4.0 (http://creativecommons.org/licenses/by/4.0/).

### Hosting

I am grateful to [GitHub], Imgur, and YouTube
for providing free hosting for this project
and many other open-source projects.

If I've misattributed anything or left anyone out, please let me know so I can
correct the situation: sgold@sonic.net

[Jump to table of contents](#toc)
