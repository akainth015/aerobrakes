# Contributing to aerobrakes

This repository is a standard Gradle project. You can `git clone` it,
then run it from source with `gradlew run`. The only current requirement 
is JDK 1.8, induced by OpenRocket`15. Once we can stably rely on OpenRocket 16,
this requirement can be lifted.

Code refactoring is currently in progress, so hold off on contributions until a
better project structure has been implemented. The following classes will likely be 
of particular interest.
~~~
me.akainth.aerobrakes.Aerobrakes
me.akainth.aerobrakes.ApogeePredictor
me.akainth.aerobrakes.FlightController
~~~

Although some of these are very unnecessary with regards to the simulation itself, 
they are likely implemented to make the logic here more similar to the one on our
aerobrakes' embedded circuits, where we need to implement more features like 
launch detection.
