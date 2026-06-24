- abstract renderframe assember initialize in bootstrapper

- standardize time reference in planeImpl
- properly split updateEntity concerns PlaneImpl

- ! remove event state holding from HudController
  - hudcontroller should build a hudsnapshot much like renderdata and use that for drwaing
  - holding event state there to feed the renderStateAssembler is bad
  - use session to get time for HUD

- Collision engine extend internalEventListener MasterController sets it to EventController to handle all events there
  capire perche tutti i controller estendono internalEventListener mentre CollisionEngine lo inizializza al suo interno e mastercontrollerimpl lo implementa

# TASK 25 GIUNGO LAST DAY

- decommentare tutto e scrivere le javadoc da capo
- sistemare nome dei package
- finire la relazione
- prima di consegnare guardare le regole di consegna
- dentro SwingGameView eliminare lo switch case per la tipologia dei missili, utilizzare la stessa logica dei collectibles e plane per disegnare lo sprite corrispondente al tipo di missile