- abstract renderframe assember initialize in bootstrapper
- remove hud dependency
- standardize time reference in planeImpl

- ! fix shield virtualThread possible race condition
  - reuse SpeedBoost solution if right

- ! fix GameSession.score data race

- separate frame timing and fsm logic from masterController

- proper game loop system with thread safety on long running sessions

sistemare hud che non richieda il plane --> ho bisogno che spinac mi gestisca l'internal event in modo tale da triggerarmi l'evento di passare lo stato di shield e di velocità nell'hud senza avere metodi del tipo plane.getEffectiveSpeed(),
plane.isShieldActive(),(in HudController.java)


- sistemare hitbox quando scalo la grandezza degli sprite







