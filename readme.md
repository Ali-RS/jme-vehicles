WIP: jme-vehicles
===

An advanced implementation of vehicles for jMonkeyEngine.

This is a work in progress of creating individual parts for a vehicle. Currently implemented (but may not be fully implemented) are:

- Engine
- Brakes
- Automatic GearBox
- Wheel
- Tyres
- Suspension
- Speedometer
- Tachometer (rev counter)
- Skid marks
- Tyre Smoke
- Engine Audio
- Vehicle Editor

Probably a few more I can't remember.

There are 3 cars to play with (see main class) and a test playground.

The tyres use the pacejka formula. Currently only latitudinal forces are applied, but longitudinal code is there.

Controls:

- WASD : forward, left, brake, right
- T : Start Vehicle
- R : Reset Vehicle
- H : Horn
- E : Reverse
- F5 : Change View

![screenshot](https://i.ibb.co/JyPHdv8/image.png)