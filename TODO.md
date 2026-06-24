- abstract renderframe assember initialize in bootstrapper
- remove hud dependency
- standardize time reference in planeImpl
- properly split updateEntity concerns PlaneImpl

- ! fix GameSession.score data race

- ? abstract game view from entities to be separate

- remove hud plane dependency
  - refactor implementation to standard
    sistemare hud che non richieda il plane --> ho bisogno che spinac mi gestisca l'internal event in modo tale da triggerarmi l'evento di passare lo stato di shield e di velocità nell'hud senza avere metodi del tipo plane.getEffectiveSpeed(),
    plane.isShieldActive(),(in HudController.java)

- ? check missile-missile collision for score
  inoltre ho bisogno che spinac mi passi un internal event anche per la collissione missile-missile, ne ho bisogno per calcolare il punteggio che è dato da: tempo di vita, star collezionate e missili fatti scontrare. (per ora lo fa con l'internalevent listener delle star collected)

- ?
  capire perche tutti i controller estendono internalEventListener mentre CollisionEngine lo inizializza al suo interno e mastercontrollerimpl lo implementa
