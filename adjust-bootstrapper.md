AppBootstrapper.java è il punto di inizio e configurazione dell'intera applicazione. È il "wiring" che collega tutti i componenti.

Fa principalmente questo:

Crea il frame principale (JFrame)

Carica le risorse:

Repository dei piani da planes.json
Profilo del giocatore da profile.json (o lo crea se è prima volta)
Istanzia i controller (input, hud, entity, master) e li collega tra loro

Crea la view del gioco (SwingGameView) con il key listener

Istanzia lo Shop con il catalogo dei piani

Crea tutte le schermate:

UsernameSetupView (setup username prima volta)
MainMenuView (menù principale)
SwingGameView (il gioco vero e proprio)
PauseView (schermata pausa)
GameOverView (game over)
ShopView (negozio)
LeaderboardView (leaderboard)
Configura i callback:

Cosa succede quando il gioco finisce (game over)
Cosa succede quando premi P (toggle pause/resume)
Cosa succede quando clicchi QUIT dalla pausa
Assembla tutto nell'UIManager (usando CardLayout) e decide quale schermata mostrare all'avvio:

Se è prima volta → SETUP (username)
Altrimenti → MENU
In breve: è il dependency injection e assembly point di tutta l'app. Se togli/aggiungi un componente, lo modifichi qui.

Sì, ha troppe responsabilità. Secondo il Single Responsibility Principle, dovrebbe solo orchestrare, non fare tutto.



sistemare hud che non richieda il plane
vedere perche non calcola il punteggio finale