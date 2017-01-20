# PlayWithJS

The Template controller for JavaScript extension of Play controllers is defined in the nashornPlay project. The sample project is called jsPlay. 

How to use the Play JavaScipt extension:

1. build the  nashornPlay jar
 - cd nashornPlay
 - play dependencies
 - play bm
   specify 1.2 as the minimum play version requrement
 - now copy the jar named nashorPlay.jar to your target project's lib

2. run a sample project
 - create new Play project with "play new"
 - copy the jar named nashorPlay.jar from nashornPlay project lib directory to your target project's lib. 
 - create an empty controller to dispatch javascript requests. The controller contains no method, but it must extend the controllers.nashornplay.NashornController
 - put an entry in the conf/route:  * /js/{_module}/{_method}  
 - create a directory js/etc

See the jsPlay projec for an example


