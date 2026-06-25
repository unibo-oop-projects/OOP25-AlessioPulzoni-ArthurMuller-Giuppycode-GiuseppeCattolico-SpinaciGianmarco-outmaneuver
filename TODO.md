- standardize time reference in planeImpl
- properly split updateEntity concerns PlaneImpl

- Collision engine extend internalEventListener MasterController sets it to EventController to handle all events there
  capire perche tutti i controller estendono internalEventListener mentre CollisionEngine lo inizializza al suo interno e mastercontrollerimpl lo implementa

# TASK 25 GIUNGO LAST DAY

- decommentare tutto e scrivere le javadoc da capo
- sistemare nome dei package
- finire la relazione
- prima di consegnare guardare le regole di consegna
- dentro SwingGameView eliminare lo switch case per la tipologia dei missili, utilizzare la stessa logica dei collectibles e plane per disegnare lo sprite corrispondente al tipo di missile

# relation part division

spina
- entity controller -> collectible controller + Effect system
- collision -> internal Event
- rendering explosion / collectible

ale
- missiles def
- missiles spawn director
- asset loader

Giup
- screen page view + uiManager -> appBootstrapper + factory
- session / score / hudSnapshot
 
- shop / wallet
- player profile

Art
- plane + json loader + dto + resourceLoader
- gameloop EventController
- RenderState system