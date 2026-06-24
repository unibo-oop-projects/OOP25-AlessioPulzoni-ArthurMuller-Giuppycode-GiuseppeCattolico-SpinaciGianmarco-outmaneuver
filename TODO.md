- abstract renderframe assember initialize in bootstrapper
- standardize time reference in planeImpl
- properly split updateEntity concerns PlaneImpl

- ! remove event state holding from HudController
  - hudcontroller should build a hudsnapshot much like renderdata and use that for drwaing
  - holding event state there to feed the renderStateAssembler is bad

- ? fix GameSession.score data race

- ?
  capire perche tutti i controller estendono internalEventListener mentre CollisionEngine lo inizializza al suo interno e mastercontrollerimpl lo implementa
