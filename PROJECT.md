# OutManeuver — PROJECT.md

> **Progetto Universitario OOP** · Java 21 · Gradle Kotlin DSL · Swing  
> Data di consegna: **25/06/2026**  
> Repository di riferimento: `OOP26-outmaneuver`

---

## Indice

1. [Team](#1-team)
2. [Descrizione del Progetto](#2-descrizione-del-progetto)
3. [Requisiti](#3-requisiti)
4. [Modello del Dominio](#4-modello-del-dominio)
5. [Architettura — Pattern MVC](#5-architettura--pattern-mvc)
6. [Struttura dei Package](#6-struttura-dei-package)
7. [Catalogo delle Classi e Interfacce](#7-catalogo-delle-classi-e-interfacce)
8. [Catalogo degli Eventi (GameEventBus)](#8-catalogo-degli-eventi-gameventbus)
9. [Interfacce Condivise](#9-interfacce-condivise)
10. [Suddivisione del Lavoro](#10-suddivisione-del-lavoro)
11. [Regole di Integrazione](#11-regole-di-integrazione)
12. [Principi e Pattern Applicati](#12-principi-e-pattern-applicati)
13. [Checklist di Avvio](#13-checklist-di-avvio)
14. [Funzionalità Obbligatorie e Opzionali](#14-funzionalità-obbligatorie-e-opzionali)
15. [Challenge Tecniche](#15-challenge-tecniche)

---

## 1. Team

| Nome | Email | Matricola |
|---|---|---|
| Muller Arthur 
| Cattolico Giuseppe 
| Pulzoni Alessio |
| Spinaci Gianmarco 

---

## 2. Descrizione del Progetto

**OutManeuver** è un videogioco arcade 2D in Java ispirato al titolo mobile *Missiles*. Il giocatore pilota un velivolo in uno spazio aperto con l'unico obiettivo di sopravvivere il più a lungo possibile, schivando una quantità crescente di **missili a ricerca automatica** e inducendoli a collidere tra loro.

Per *missile a ricerca automatica* si intende un proiettile dotato di un algoritmo di inseguimento autonomo (steering behavior) che aggiorna continuamente la propria traiettoria in direzione del velivolo del giocatore.

Durante la partita compaiono **entità collezionabili** di tipologie distinte che possono aiutare il giocatore. È possibile acquistare **velivoli potenziati** tramite un sistema di negozio in-game. La difficoltà aumenta progressivamente nel tempo con un numero crescente di missili contemporaneamente a schermo. La partita termina quando un missile colpisce il velivolo in assenza di uno scudo attivo.

---

## 3. Requisiti

### 3.1 Requisiti Funzionali

| ID | Requisito |
|---|---|
| RF-01 | Il sistema genera missili ai bordi dell'area di gioco con frequenza crescente nel tempo, ciascuno dei quali insegue autonomamente il velivolo del giocatore. |
| RF-02 | Il sistema rileva i contatti tra entità di gioco (missile-velivolo, missile-missile, velivolo-collezionabile). |
| RF-03 | Il sistema presenta un menù principale con le opzioni di avvio partita e di uscita dall'applicazione. |
| RF-04 | Il sistema fornisce un HUD che mostra il punteggio aggiornato in tempo reale e una schermata di Game Over al termine della partita. |
| RF-05 | Il sistema genera a schermo entità collezionabili a intervalli di tempo. |
| RF-06 | Il sistema prevede più tipologie di missili con comportamenti distinti. |
| RF-07 | Il sistema salva e visualizza una classifica dei punteggi massimi conseguiti. |

### 3.2 Requisiti Non Funzionali

| ID | Requisito |
|---|---|
| RNF-01 | Il sistema garantisce una risposta fluida e reattiva agli input del giocatore in ogni condizione di gioco. |
| RNF-02 | Il sistema è eseguibile sui principali sistemi operativi desktop (Windows, macOS, Linux). |
| RNF-03 | Il sistema rende immediatamente comprensibili le proprie meccaniche risultando intuitivo a qualsiasi giocatore. |
| RNF-04 | Il sistema adatta dinamicamente la propria interfaccia e l'area di gioco alle dimensioni della finestra. |

---

## 4. Modello del Dominio

```
┌─────────────────────────────────────────────────────┐
│                   AREA DI GIOCO                      │
│                                                      │
│   ┌──────────┐      insegue      ┌─────────────┐    │
│   │ VELIVOLO │ ◄──────────────── │   MISSILE   │    │
│   │          │                   │  (N tipologie)│   │
│   └────┬─────┘                   └──────┬──────┘    │
│        │ raccoglie                       │ collide   │
│        ▼                                ▼           │
│   ┌──────────────────┐       ┌──────────────────┐   │
│   │   COLLEZIONABILE │       │  altro MISSILE   │   │
│   │  SpeedBoost      │       │  → distruzione   │   │
│   │  Stella          │       │    reciproca     │   │
│   │  Scudo           │       └──────────────────┘   │
│   │  EMP             │                              │
│   └──────────────────┘                              │
└─────────────────────────────────────────────────────┘
         │
         ▼
┌──────────────────┐     ┌─────────────┐     ┌────────┐
│  SESSIONE GIOCO  │────▶│  CLASSIFICA │     │ NEGOZIO│
│  score, timer    │     │  top-N      │     │ shop   │
│  GameState       │     │  ScoreEntry │     │ planes │
└──────────────────┘     └─────────────┘     └────────┘
```

### Entità del dominio

**Velivolo** — entità centrale controllata dal giocatore. Si muove continuamente nell'area. Può essere in stato *vulnerabile* o *protetto* (scudo attivo). Versioni potenziate acquistabili nel negozio con caratteristiche superiori (velocità, virata, durata power-up).

**Missile** — entità autonoma che insegue il Velivolo aggiornando continuamente la direzione. Esistono tipologie distinte con velocità e manovrabilità diverse. Può collidere con il Velivolo (Game Over se no scudo) o con un altro Missile (distruzione reciproca).

**Collezionabile** — appare a intervalli nell'area di gioco. Quattro tipologie:
- **SpeedBoost** — aumenta temporaneamente la velocità del Velivolo
- **Stella** — incrementa il punteggio del giocatore
- **Scudo** — conferisce protezione aggiuntiva al Velivolo
- **EMP** — disattiva istantaneamente tutti i missili a schermo

**Sessione di Gioco** — mantiene punteggio corrente, tempo di sopravvivenza e stato della partita (GameState). Produce un risultato al termine.

**Classifica** — conserva i migliori risultati delle sessioni precedenti su file locale.

**Negozio** — catalogo dei velivoli acquistabili con il punteggio accumulato.

---

## 5. Architettura — Pattern MVC

```
┌─────────────────────────────────────────────────┐
│                    VIEW                         │
│  UIManager · SwingGameView · MainMenuView       │
│  GameOverView · GameView · HudSnapshot          │
│  Riceve dati già pronti (RenderState/DTO)       │
│  Non interroga MAI il Model direttamente        │
└────────────────────┬────────────────────────────┘
                     │ eventi / callback
┌────────────────────▼────────────────────────────┐
│                 CONTROLLER                      │
│  MasterController · InputController             │
│  EntityController · HudController               │
│  GameEventBus · AppBootstrapper                 │
│  Orchestrazione, traduzione input→azione        │
└────────────────────┬────────────────────────────┘
                     │ interfacce
┌────────────────────▼────────────────────────────┐
│                   MODEL                         │
│  IGameSession · Plane · DifficultyConfig        │
│  GameState · ScoreEntry                         │
│  Solo stato di dominio e regole                 │
│  ZERO dipendenze da View o Controller           │
└─────────────────────────────────────────────────┘
```

### Regola fondamentale delle dipendenze

```
View  →  Controller  →  Model
```

Il flusso è **unidirezionale e irreversibile**. Il Model non conosce né View né Controller. La View non interroga mai il Model direttamente — riceve snapshot immutabili dal Controller.

---

## 6. Struttura dei Package

```
src/main/java/outmaneuver/
│
├── Main.java
├── AppBootstrapper.java
│
├── model/
│   ├── area/
│   │   ├── Plane.java                  (interfaccia condivisa — owner: Muller)
│   │   ├── PlaneImpl.java
│   │   ├── PlaneStats.java             (interfaccia)
│   │   ├── StandardStats.java
│   │   └── TurnState.java              (enum)
│   │
│   ├── session/
│   │   ├── IGameSession.java           (interfaccia condivisa — owner: Cattolico)
│   │   ├── GameSession.java
│   │   ├── GameState.java              (enum)
│   │   └── ScoreEntry.java             (record)
│   │
│   ├── powerup/
│   │   ├── Collectible.java            (@FunctionalInterface — owner: Cattolico)
│   │   ├── SpeedBoost.java
│   │   ├── StarCollectible.java
│   │   ├── ShieldPowerUp.java
│   │   └── EmpPowerUp.java             (richiede IGameArea — da impl.)
│   │
│   ├── leaderboard/
│   │   ├── ILeaderboardRepository.java
│   │   ├── JsonLeaderboardRepository.java
│   │   └── Leaderboard.java
│   │
│   └── DifficultyConfig.java           (record immutabile — owner: Cattolico)
│
├── controller/
│   ├── EntityController.java           (interfaccia — owner: Muller)
│   ├── HudController.java              (interfaccia — owner: Cattolico)
│   ├── InputController.java            (interfaccia — owner: Muller)
│   ├── MasterController.java           (interfaccia — owner: Muller)
│   ├── InternalEvent.java              (enum)
│   ├── OutmaneuverEvent.java           (enum)
│   │
│   ├── event/
│   │   ├── GameEventBus.java           (interfaccia condivisa — owner: Spinaci, stub TODO)
│   │   └── InternalEventListener.java  (@FunctionalInterface)
│   │
│   └── impl/
│       ├── EntityControllerImpl.java
│       ├── HudControllerImpl.java
│       ├── InputControllerImpl.java
│       └── MasterControllerImpl.java
│
├── util/
│   └── Vector2.java
│
└── view/
    ├── BankState.java                  (enum)
    ├── EntityRenderData.java
    ├── GameView.java                   (interfaccia condivisa — owner: Muller)
    ├── HudSnapshot.java                (record immutabile — owner: Cattolico)
    ├── RenderState.java
    │
    └── swing/
        ├── GameKeyListener.java
        ├── SwingGameView.java
        ├── UIManager.java
        │
        ├── gameover/
        │   └── GameOverView.java
        │
        └── menu/
            └── MainMenuView.java
```

---

## 7. Catalogo delle Classi e Interfacce

### 7.1 Model

| Classe / Interfaccia | Tipo | Owner | Responsabilità |
|---|---|---|---|
| `IGameSession` | Interface | Cattolico | Contratto pubblico della sessione: `getScore()`, `getElapsedTimeMillis()`, `getGameState()`, `equipPlane(Plane)`, `incrementScore(int)`, `transitionTo(GameState)`. |
| `GameSession` | Class | Cattolico | Implementa `IGameSession`. Score mutabile, timer che conta solo i ms in stato `PLAYING` (accumula attraverso pause), macchina a stati con transizioni validate. Nessuna dipendenza da `GameEventBus`. |
| `GameState` | Enum | Cattolico | `MENU`, `PLAYING`, `PAUSED`, `GAME_OVER`. Usato da `MasterControllerImpl` e `UIManager`. |
| `ScoreEntry` | Record | Cattolico | `int score`, `String playerName`, `LocalDate date`. Implementa `Comparable<ScoreEntry>` per ordinamento decrescente. |
| `DifficultyConfig` | Record | Cattolico | `double initialSpawnRate`, `double spawnRateIncrement`, `int maxMissilesOnScreen`. Immutabile. Include factory method `defaultConfig()`. |
| `Plane` | Interface | Muller | Contratto del Velivolo. `getPosition()`, `getDirection()`, `activateShield()`, `deactivateShield()`, `isShieldActive()`, `applySpeedMultiplier()`, `getEffectiveSpeed()`. |
| `PlaneImpl` | Class | Muller | Implementa `Plane`. Gestisce posizione, direzione, scudo e moltiplicatori di velocità temporanei. |
| `PlaneStats` | Interface | Muller | Parametri statici del velivolo: `getId()`, `getBaseSpeed()`, `getTurnRate()`, `getHitboxRadius()`, `getSpriteId()`. |
| `StandardStats` | Class | Muller | Implementa `PlaneStats`. Costanti per il velivolo standard: speed=200, turnRate=3, radius=20. |
| `TurnState` | Enum | Muller | `NONE`, `LEFT`, `RIGHT`. Stato di virata corrente del velivolo. |
| `IMissile` | Interface | Pulzoni | *(da implementare)* Contratto del Missile: `update(float dt)`, `getPosition()`, `deactivate()`, `getType()`. |
| `SteeringBehavior` | Interface | Pulzoni | *(da implementare)* Pattern Strategy per il comportamento di inseguimento. |
| `MissileType` | Enum | Pulzoni | *(da implementare)* Tipologie di missile con parametri distinti di velocità e manovrabilità. |
| `Collectible` | Interface | Cattolico | `@FunctionalInterface` — `void apply(Plane plane, IGameSession session)`. Contratto Strategy per tutti i collezionabili. |
| `SpeedBoost` | Class | Cattolico | Implementa `Collectible`. Chiama `plane.applySpeedMultiplier(factor, durationMs)`. |
| `StarCollectible` | Class | Cattolico | Implementa `Collectible`. Chiama `session.incrementScore(scoreValue)`. |
| `ShieldPowerUp` | Class | Cattolico | Implementa `Collectible`. Attiva lo scudo e lo disattiva su virtual thread dopo `durationMs`. |
| `EmpPowerUp` | Class | Cattolico | *(da implementare — richiede `IGameArea`)* Disattiva tutti i missili attivi. |
| `ILeaderboardRepository` | Interface | Cattolico | `List<ScoreEntry> load()`, `void persist(List<ScoreEntry>)`. Isola I/O da file. |
| `JsonLeaderboardRepository` | Class | Cattolico | Implementa `ILeaderboardRepository` con Gson. Adapter `LocalDate` custom, silent load su file assente. |
| `Leaderboard` | Class | Cattolico | Top-N punteggi ordinati. `save(int, String)` e `getTopScores()`. Lista risultato non modificabile. |
| `IGameArea` | Interface | Condiviso | *(da implementare)* Contenitore stato dinamico partita: `getMissiles()`, `getCollectibles()`, `getPlane()`, `getMissileCount()`. |
| `Shop` | Class | Cattolico | *(da implementare)* Catalogo velivoli acquistabili. |

### 7.2 Controller

| Classe / Interfaccia | Tipo | Owner | Responsabilità |
|---|---|---|---|
| `MasterController` | Interface | Muller | `handleEvent(OutmaneuverEvent)`, `attachView(GameView)`, `start()`, `stop()`, `shutdown()`. Orchestratore principale. |
| `MasterControllerImpl` | Class | Muller | Implementa `MasterController` e `InternalEventListener`. Contiene il game loop (`ScheduledExecutorService`), gestisce pausa/resume, notifica le views ogni tick. Delega aggiornamento entità a `EntityController`. |
| `EntityController` | Interface | Muller | `updateEntities(long deltaMs)`, `clearAll()`, `getPlane()`. Gestisce lo stato delle entità di gioco. |
| `EntityControllerImpl` | Class | Muller | Implementa `EntityController`. Aggiorna posizione e direzione del velivolo applicando `InputController.getTurnDirection()`. |
| `InputController` | Interface | Muller | `onKeyPressed(int)`, `onKeyReleased(int)`, `getTurnDirection()`, `isThrustActive()`. Astrazione input tastiera. |
| `InputControllerImpl` | Class | Muller | Implementa `InputController`. Traduce key codes Swing in direzioni astratte. |
| `HudController` | Interface | Cattolico | Estende `InternalEventListener`. `buildSnapshot(Plane, boolean)`, `reset()`. Produce `HudSnapshot` per la View. |
| `HudControllerImpl` | Class | Cattolico | Implementa `HudController`. Accumula tempo trascorso, conteggio stelle. Produce `HudSnapshot` ogni frame. |
| `InternalEvent` | Enum | — | `STAR_COLLECTED`. Eventi interni tra controller senza passare per il bus esterno. |
| `OutmaneuverEvent` | Enum | — | `TOGGLE_PAUSE`, `QUIT_APPLICATION`. Azioni UI che `MasterController.handleEvent()` gestisce. |
| `GameEventBus` | Interface | Spinaci | *(stub TODO)* Bus eventi centralizzato per comunicazione tra moduli. |
| `InternalEventListener` | Interface | — | `@FunctionalInterface` — `void onInternalEvent(InternalEvent evt, Object data)`. Rompe la dipendenza circolare tra `MasterControllerImpl` ed `EntityControllerImpl`. |
| `ICollidable` | Interface | Spinaci | *(da implementare)* Contratto per entità collidibili. |
| `CollisionEngine` | Class | Spinaci | *(da implementare)* Rileva collisioni missile-velivolo e missile-missile. |
| `EntityRemover` | Class | Spinaci | *(da implementare)* Rimuove entità distrutte da `IGameArea`. |
| `MissileSpawner` | Class | Pulzoni | *(da implementare)* Genera missili ai bordi dell'area secondo `DifficultyConfig`. |
| `CollectibleSpawner` | Class | Cattolico | *(da implementare)* Genera collezionabili a intervalli. |
| `EntityFactory` | Interface | Muller | *(da implementare)* Factory per creazione di `IMissile` e collezionabili. |
| `AppBootstrapper` | Class | — | Costruisce il `JFrame`, wiring di tutti i componenti, navigazione tra schermate. |

### 7.3 View

| Classe / Interfaccia | Tipo | Owner | Responsabilità |
|---|---|---|---|
| `GameView` | Interface | Muller | `void renderFrame(RenderState state)`. Contratto per qualsiasi view che riceve aggiornamenti dal game loop. |
| `SwingGameView` | Class | Muller | Implementa `GameView`. Pannello Swing con inner class `GamePanel`. Riceve `RenderState` e ridisegna velivolo + HUD via `SwingUtilities.invokeLater`. |
| `RenderState` | Class | Muller | Snapshot immutabile del frame corrente. Contiene `EntityRenderData` per il velivolo e `HudSnapshot`. Costruito via Builder. |
| `EntityRenderData` | Class | Muller | DTO con `x`, `y`, `directionRad`, `spriteId`. Dati di rendering per una singola entità. |
| `HudSnapshot` | Record | Cattolico | `long elapsedMs`, `double speed`, `boolean shieldActive`, `boolean paused`, `int stars`. Snapshot immutabile HUD passato alla View ogni frame. |
| `BankState` | Enum | Muller | `NONE`, `LEFT`, `RIGHT`. Stato visivo di virata per animazione sprite. |
| `UIManager` | Class | Cattolico | `JPanel` con `CardLayout`. Registra una schermata per ogni `GameState` e la mostra via `showScreen(GameState)`. |
| `GameKeyListener` | Class | Muller | Implementa `KeyListener`. Delega `keyPressed`/`keyReleased` a `InputController` e `OutmaneuverEvent` a `MasterController`. |
| `MainMenuView` | Class | Cattolico | `JPanel` con bottoni Start/Exit. Riceve due `Runnable` nel costruttore (DIP). |
| `GameOverView` | Class | Cattolico | `JPanel` schermata Game Over. *(da completare)* |
| `HUDView` | Class | Cattolico | *(da implementare)* Overlay in-game separato. Logica HUD attualmente inline in `SwingGameView`. |
| `PlaneRenderer` | Class | Muller | *(da implementare)* Renderer dedicato sprite velivolo. |
| `MissileRenderer` | Class | Pulzoni | *(da implementare)* Renderer dedicato sprite missili. |
| `CollectibleRenderer` | Class | Cattolico | *(da implementare)* Renderer icone collezionabili. |
| `ParticleSystem` | Class | Spinaci | *(da implementare)* Effetti visivi esplosioni. |

---

## 8. Catalogo degli Eventi

### 8.1 OutmaneuverEvent (enum) — eventi UI → Controller

Gestiti da `MasterController.handleEvent()`. Originati da `GameKeyListener`.

| Evento | Publisher | Subscriber |
|---|---|---|
| `TOGGLE_PAUSE` | `GameKeyListener` | `MasterControllerImpl` → toglie/mette pausa al game loop |
| `QUIT_APPLICATION` | `GameKeyListener` | `MasterControllerImpl` → `shutdown()` + `System.exit(0)` |

### 8.2 InternalEvent (enum) — eventi interni Controller

Transitano via `InternalEventListener` senza passare per `GameEventBus`.

| Evento | Publisher | Subscriber |
|---|---|---|
| `STAR_COLLECTED` | `EntityControllerImpl` | `HudControllerImpl` → incrementa contatore stelle |

### 8.3 GameEventBus — eventi di dominio (da completare — owner: Spinaci)

`GameEventBus` è attualmente uno stub vuoto. Gli eventi pianificati una volta completato il bus:

| Evento previsto | Payload | Publisher | Subscriber(s) |
|---|---|---|---|
| `MISSILE_MISSILE_COLLISION` | posizione, idA, idB | CollisionEngine | EntityRemover, GameSession (score++), ParticleSystem |
| `MISSILE_PLANE_COLLISION` | CollisionEvent | CollisionEngine | GameSession (→ GAME_OVER se no shield) |
| `GAME_OVER` | ScorePayload | GameSession | MasterControllerImpl (stop loop), UIManager |
| `SCORE_CHANGED` | `delta: int` | StarCollectible | GameSession, HUDView |
| `COLLECTIBLE_COLLECTED` | CollectibleType, planeId | CollisionEngine | Cattolico (`Collectible.apply()`) |
| `MISSILE_DESTROYED` | missileId | EntityRemover | MissileSpawner (aggiorna contatore) |
| `ENTITY_REMOVE_REQUEST` | entityId | CollisionEngine | EntityRemover |

---

## 9. Interfacce Condivise

Le interfacce condivise sono il punto di integrazione del team. Qualsiasi modifica richiede accordo esplicito tra tutti gli owner coinvolti.

### Implementate

| Interfaccia | Package | Owner | Consumatori |
|---|---|---|---|
| `Plane` | `model.area` | Muller | `EntityControllerImpl` (aggiornamento posizione), `HudController` (speed snapshot), Pulzoni (steering target), Cattolico (PowerUp.apply) |
| `PlaneStats` | `model.area` | Muller | `StandardStats` implementa; `RenderState.Builder` legge `getSpriteId()` |
| `IGameSession` | `model.session` | Cattolico | `MasterControllerImpl`, `CollisionEngine` (da implementare) |
| `GameView` | `view` | Muller | `SwingGameView` implementa; `MasterControllerImpl` notifica ogni tick |
| `GameEventBus` | `controller.event` | Spinaci | Tutti i moduli (stub, da completare) |
| `InternalEventListener` | `controller.event` | — | `MasterControllerImpl` implementa; `HudControllerImpl` estende; `EntityControllerImpl` chiama |
| `MasterController` | `controller` | Muller | `AppBootstrapper` — punto di avvio |
| `EntityController` | `controller` | Muller | `MasterControllerImpl` delega `updateEntities()` |
| `InputController` | `controller` | Muller | `EntityControllerImpl` legge `getTurnDirection()` |
| `HudController` | `controller` | Cattolico | `MasterControllerImpl` chiama `buildSnapshot()` ogni tick |

### Da implementare

| Interfaccia | Package | Owner | Note |
|---|---|---|---|
| `IMissile` | `model.missile` | Pulzoni | Contratto Missile: `update`, `getPosition`, `deactivate`, `getType` |
| `SteeringBehavior` | `model.missile` | Pulzoni | Strategy per algoritmo di inseguimento |
| `ICollidable` | `controller.collision` | Spinaci | Contratto entità collidibili con hitbox |
| `IGameArea` | `model.area` | Condiviso | Contenitore entità attive (missili, collezionabili, velivolo) |
| `Collectible` | `model.powerup` | Cattolico | Strategy per effetti collezionabili (`SpeedBoost`, `StarCollectible`, `ShieldPowerUp` ✓ — `EmpPowerUp` da impl.) |
| `EntityFactory` | `controller` | Muller | Factory per `IMissile` e collezionabili |
| `ILeaderboardRepository` | `model.leaderboard` | Cattolico | Isola I/O JSON da logica classifica ✓ |

---

## 10. Suddivisione del Lavoro

### Muller Arthur
**Model:** `Plane`, `PlaneImpl`, `PlaneStats`, `StandardStats`, `TurnState`  
**Controller:** `MasterController`, `MasterControllerImpl` (game loop integrato), `InputController`, `InputControllerImpl`, `EntityController`, `EntityControllerImpl`, `EntityFactory` *(da impl.)*, `AppBootstrapper`  
**View:** `GameView`, `GameKeyListener`, `SwingGameView`, `RenderState`, `EntityRenderData`, `BankState`, `PlaneRenderer` *(da impl.)*

### Pulzoni Alessio
**Model:** `IMissile` *(da impl.)*, `HomingMissile` *(da impl.)*, `FastMissile` *(da impl.)*, `HeavyMissile` *(da impl.)*, `MissileType` *(da impl.)*, `SteeringBehavior` *(da impl.)*  
**Controller:** `MissileSpawner` *(da impl.)*  
**View:** `MissileRenderer` *(da impl.)*

### Spinaci Gianmarco
**Controller:** `GameEventBus` (stub), `GameEventBusImpl` *(da impl.)*, `ICollidable` *(da impl.)*, `CollisionEngine` *(da impl.)*, `EntityRemover` *(da impl.)*  
**View:** `ParticleSystem` *(da impl.)*, `AudioManager` *(da impl.)*

### Cattolico Giuseppe
**Model:** `IGameSession` ✓, `GameSession` ✓, `GameState` ✓, `ScoreEntry` ✓, `DifficultyConfig` ✓, `Collectible` ✓, `SpeedBoost` ✓, `StarCollectible` ✓, `ShieldPowerUp` ✓, `EmpPowerUp` *(da impl. — richiede `IGameArea`)*, `ILeaderboardRepository` ✓, `JsonLeaderboardRepository` ✓, `Leaderboard` ✓, `Shop` *(da impl.)*  
**Controller:** `HudController` ✓, `HudControllerImpl` ✓, `CollectibleSpawner` *(da impl.)*  
**View:** `UIManager` ✓, `HudSnapshot` ✓, `MainMenuView` ✓, `GameOverView` *(da completare)*, `HUDView` *(da impl.)*, `ShopView` *(da impl.)*, `CollectibleRenderer` *(da impl.)*

---

## 11. Regole di Integrazione

| Scenario di rischio | Regola di risoluzione |
|---|---|
| Pulzoni ha bisogno della posizione del Velivolo | Legge `Plane.getPosition()` — mai accede a `PlaneImpl` direttamente |
| Spinaci deve rimuovere un Missile dopo la collisione | Pubblica `ENTITY_REMOVE_REQUEST` su `GameEventBus`; `EntityRemover` chiama `IGameArea.remove()` |
| Cattolico deve aggiornare il punteggio dopo una collisione | Si iscrive a `MISSILE_MISSILE_COLLISION` via `GameEventBus`; Spinaci pubblica, Cattolico ascolta |
| Muller vuole sapere se è Game Over | `GameSession` (Cattolico) pubblica `GAME_OVER` via bus; `MasterControllerImpl` (Muller) si iscrive e ferma il loop |
| Pulzoni vuole sapere la difficoltà corrente | Legge `DifficultyConfig` in sola lettura; non chiama metodi di `IGameSession` |
| Cattolico deve mostrare quanti missili ci sono | Legge `IGameArea.getMissileCount()` — interfaccia condivisa, nessuna dipendenza circolare |
| `MasterControllerImpl` ed `EntityControllerImpl` si dipendono reciprocamente | `MasterControllerImpl` imposta `EntityController` via `setEntityController()` dopo la costruzione; implementa `InternalEventListener` e la passa a `HudControllerImpl` |
| Due membri vogliono modificare un'interfaccia condivisa | Pull Request condivisa — nessun merge unilaterale |
| Un metodo di un'interfaccia condivisa deve cambiare firma | Notifica sul canale team + accordo tra tutti gli owner coinvolti prima del commit |

---

## 12. Principi e Pattern Applicati

### Principi SOLID e altri

| Principio | Applicazione nel progetto |
|---|---|
| **SRP** | Ogni classe ha una sola responsabilità. `HudControllerImpl` gestisce solo il tempo/stelle HUD; `InputControllerImpl` solo l'input tastiera; `MasterControllerImpl` solo orchestrazione tick. |
| **OCP** | Nuovi tipi di missile si aggiungono implementando `IMissile` + `SteeringBehavior`, senza toccare codice esistente. Nuovi velivoli implementano `PlaneStats`. |
| **LSP** | `StandardStats` è sostituibile ovunque si usa `PlaneStats`. Ogni futuro `IMissile` è sostituibile in `CollisionEngine` e `MissileSpawner`. |
| **ISP** | `IGameSession` espone solo i metodi necessari al consumatore. `EntityController` non espone dettagli di rendering. `HudController` ha solo i metodi necessari a costruire snapshot. |
| **DIP** | Tutte le dipendenze sono su interfacce. `MasterControllerImpl` dipende da `EntityController`, `HudController`, `GameView` — mai da implementazioni concrete. |
| **DRY** | `RenderState.Builder` centralizza la proiezione `Plane → EntityRenderData`. `HudControllerImpl` è l'unica sorgente del tempo di gioco. |
| **KISS** | `UIManager` usa `CardLayout` Swing — nessuna libreria di navigazione esterna. Il game loop usa `ScheduledExecutorService` standard Java. |

### Pattern GoF applicati

| Pattern | Dove |
|---|---|
| **Observer** | `GameEventBus` *(da completare)* — comunicazione publish/subscribe tra moduli; `InternalEventListener` — notifiche interne tra controller |
| **Strategy** | `SteeringBehavior` *(da impl.)* — algoritmo inseguimento intercambiabile; `Collectible` — effetto intercambiabile per ogni tipo di collezionabile |
| **Builder** | `RenderState.Builder` — costruzione step-by-step dello snapshot di rendering |
| **Factory Method** | `EntityFactory` *(da impl.)* — creazione di `IMissile` e collezionabili |
| **State** | `GameState` + `UIManager` — `CardLayout` mostra schermata diversa per MENU/PLAYING/PAUSED/GAME_OVER |
| **Repository** | `ILeaderboardRepository` *(da impl.)* — isola la persistenza JSON dalla logica di classifica |
| **DTO** | `RenderState`, `HudSnapshot`, `EntityRenderData` — snapshot immutabili che separano Model da View |

---

## 13. Checklist di Avvio

Prima che ciascun membro inizi a scrivere implementazioni:

- [x] `Plane`, `PlaneStats`, `PlaneImpl`, `StandardStats`, `TurnState` — model velivolo (Muller) ✓
- [x] `Plane`, `PlaneStats`, `PlaneImpl`, `StandardStats`, `TurnState` — model velivolo (Muller) ✓
- [x] `IGameSession`, `GameSession`, `GameState`, `ScoreEntry`, `DifficultyConfig` — model sessione (Cattolico) ✓
- [x] `Collectible`, `SpeedBoost`, `StarCollectible`, `ShieldPowerUp` — collezionabili (Cattolico) ✓
- [x] `ILeaderboardRepository`, `JsonLeaderboardRepository`, `Leaderboard` — classifica (Cattolico) ✓
- [x] `MasterController`, `EntityController`, `InputController`, `HudController` — interfacce controller (Muller/Cattolico) ✓
- [x] `InternalEventListener`, `InternalEvent`, `OutmaneuverEvent` — eventi interni (Muller) ✓
- [x] `GameView`, `RenderState`, `HudSnapshot`, `EntityRenderData` — layer view base ✓
- [x] `UIManager`, `SwingGameView`, `MainMenuView` — schermate Swing ✓
- [x] Game loop funzionante in `MasterControllerImpl` (Muller) ✓
- [ ] `GameEventBus` implementazione completa (Spinaci) — tutti lo dipendono per missili/collisioni
- [ ] `IMissile`, `SteeringBehavior`, tipi missile (Pulzoni)
- [ ] `ICollidable`, `CollisionEngine`, `EntityRemover` (Spinaci)
- [ ] `IGameArea` e integrazione in `EntityControllerImpl` (Condiviso)
- [ ] `EmpPowerUp` (Cattolico — aspetta `IGameArea`)
- [ ] `EntityFactory`, `MissileSpawner`, `CollectibleSpawner` (Muller/Pulzoni/Cattolico)
- [ ] `GameOverView`, `HUDView`, `ShopView`, `CollectibleRenderer` (Cattolico)
- [ ] `Shop` (Cattolico)


---

## 14. Funzionalità Obbligatorie e Opzionali

### Obbligatorie (60–70% del tempo)

- **Movimento base** — virata continua destra/sinistra tramite tastiera
- **Spawn e Homing** — generazione casuale missili ai bordi + algoritmo steering base
- **Collision Detection** — missile-velivolo (Game Over) e missile-missile (score++)
- **Flusso di gioco base** — Menu principale, HUD con punteggio, schermata Game Over

### Opzionali (completamento al 100%)

- **Sistema Power-up** — Scudo, EMP, SpeedBoost, Stella
- **Varietà nemici** — missili con steering behavior e parametri distinti
- **Leaderboard** — salvataggio high-score su file JSON locale
- **Particelle e audio** — effetti visivi esplosioni, feedback sonori

---

## 15. Challenge Tecniche

| Challenge | Approccio |
|---|---|
| **Steering behaviors fluidi** | `SteeringBehavior` come Strategy iniettata nel costruttore del missile. Parametri (`turnSpeed`, `maxSpeed`) in `DifficultyConfig` e per tipo missile. Bilanciamento manuale tramite playtest. |
| **Ottimizzazione collisioni** | `CollisionEngine` itera solo sulle entità attive in `IGameArea`. Hitbox semplici (cerchi). Structural sharing via interfacce per evitare cast. |
| **Gestione memoria / GC** | Object pooling per missili — `MissileSpawner` riusa istanze invece di crearne di nuove. `EntityRemover` rimuove subito le entità distrutte dalla lista attiva. |
| **Dipendenza circolare Controller** | `InternalEventListener` rompe il ciclo `MasterControllerImpl` ↔ `EntityControllerImpl`. |
| **Sincronizzazione Swing** | Tutte le operazioni UI tramite `SwingUtilities.invokeLater()`. `SwingGameView.renderFrame()` salva lo state `volatile` e chiama `repaint()` — mai modifica diretta dei componenti fuori dall'EDT. |