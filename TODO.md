- move plane stats to json at loadtime
  - build json system for base entity settings instead of file per based settings aproach
- divide area package for missiles, collectibles, plane
- decide weather to keep bank state for sprite "animation"

- missile interface definition
- split missile implementation form abstract class
- utilize renderState for rendering and collectibles
- use entityController for missiles and collectibles

- validate mock tests with junit instead of mockito

- ? separate frame timing and fsm logic from masterController

- ? how to get event from game event to the hud

- remove eventBus use internalEvent and listener to route events accordingly then use mastercontroller to route message how needed 