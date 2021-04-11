# aerobrakes

To use this project, you can download one of the `.zip` files from the GitHub releases section.

It requires the following:
- JDK 1.8 is located at `JAVA_HOME`
- `java` can be found on your `PATH`

If those conditions are met, you can run OpenRocket using the scripts in the `bin` folder.

To add the simulation listener to one of your OpenRocket documents,
go to the "Simulation Options tab", press "Add Extension", then from the 
"User code" section select "Java". Enter the following classname: 
`me.akainth.aerobrakes.Aerobrakes`

At this time, the aerobrakes cannot be configured directly from the OpenRocket UI. 
To do so, you will have to follow the instructions in [`CONTRIBUTING.md`](CONTRIBUTING.md)
