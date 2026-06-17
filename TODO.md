- ! fix shield virtualThread possible race condition
  - reuse SpeedBoost solution if right

- ! fix GameSession.score data race

- separate frame timing and fsm logic from masterController
  - proper game loop system with thread safety on long running sessions

- missing collectible json
- decide weather to keep bank state for sprite "animation"

- abstract game view from entities to be separate

- missile interface definition
- split missile implementation form abstract class
- utilize renderState for rendering and collectibles
- use entityController for missiles and collectibles

sistemare hud che non richieda il plane --> ho bisogno che spinac mi gestisca l'internal event in modo tale da triggerarmi l'evento di passare lo stato di shield e di velocità nell'hud senza avere metodi del tipo plane.getEffectiveSpeed(),
plane.isShieldActive(),(in HudController.java)

No, il design attuale è corretto così com'è. Ci sono due ragioni:
Velocità e scudo sono stato volatile che cambia a ogni frame — non eventi discreti. getEffectiveSpeed() ha un timer interno (multiplierEndTime) che scade in PlaneImpl senza pubblicare eventi. isShieldActive() viene settato da ShieldPowerUp su un virtual thread. Dovresti aggiungere 4 nuovi InternalEvent (SHIELD_ON, SHIELD_OFF, SPEED_BOOST, SPEED_EXPIRED) solo per replicare quello che una lettura diretta già fa correttamente.

inoltre ho bisogno che spinac mi passi un internal event anche per la collissione missile-missile, ne ho bisogno per calcolare il punteggio che è dato da: tempo di vita, star collezionate e missili fatti scontrare. (per ora lo fa con l'internalevent listener delle star collected)

inoltre ragionare sul fatto se lasciare le chiamate di internaleventlistener per le collisioni dentro mastercontroller (ultime righe) oppure portarle all'interno di collision engine.

capire perche tutti i controller estendono internalEventListener mentre CollisionEngine lo inizializza al suo interno e mastercontrollerimpl lo implementa
