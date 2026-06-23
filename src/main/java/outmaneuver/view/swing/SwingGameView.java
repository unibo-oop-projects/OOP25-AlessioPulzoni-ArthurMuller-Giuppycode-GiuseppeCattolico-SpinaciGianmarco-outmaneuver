package outmaneuver.view.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import outmaneuver.util.assets.AssetStore;
import outmaneuver.util.assets.SpriteId;
import outmaneuver.view.EntityRenderData;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;
import outmaneuver.view.swing.hud.IHudView;

public final class SwingGameView extends JPanel implements GameView {

    // Raggio "logico" dell'aereo: usato per scalare lo sprite alla misura dell'hitbox.
    private static final int PLANE_RADIUS = 20;
    // Raggio "logico" dei collectible: stessa idea, per scalare il loro sprite.
    private static final int COLLECTIBLE_RADIUS = 10;

    private final KeyListener keyListener;
    private final IHudView hudView;
    /** Fornisce gli sprite gia' caricati in memoria; iniettato dall'esterno (Dependency Inversion). */
    private final AssetStore assets;
    private volatile RenderState latestState;

    public SwingGameView(final KeyListener keyListener, final IHudView hudView,
            final AssetStore assets) {
        this.keyListener = Objects.requireNonNull(keyListener, "keyListener must not be null");
        this.hudView = Objects.requireNonNull(hudView, "hudView must not be null");
        this.assets = Objects.requireNonNull(assets, "assets must not be null");
        this.latestState = null;
    }

    public void init() {
        setFocusable(true);
        addKeyListener(keyListener);
    }

    @Override
    public void renderFrame(final RenderState state) {
        this.latestState = state;
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final var g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(180, 225, 245)); // azzurrino chiaro (cielo)
        g2d.fillRect(0, 0, getWidth(), getHeight());

        final var state = latestState;
        if (state != null) {
            final var planeData = state.getPlane();
            final double cameraX = planeData.getX();
            final double cameraY = planeData.getY();

            drawMissiles(g2d, state.getMissiles(), cameraX, cameraY);
            drawPlane(g2d, planeData, cameraX, cameraY);

            for (final var col : state.getCollectibles()) {
                drawCollectible(g2d, col, cameraX, cameraY);
            }

            if (state.getHud() != null) {
                hudView.render(g2d, state.getHud(), this);
            }
        }
        g2d.dispose();
    }

    // [Alessio - asset loader] Il collectible ora usa il suo sprite (star/speed/shield) al posto
    // del cerchio giallo. Nessuna rotazione: i collectible non hanno un orientamento.
    private void drawCollectible(final Graphics2D g2d, final EntityRenderData data,
            final double cameraX, final double cameraY) {
        final BufferedImage sprite = assets.getSprite(collectibleSprite(data.getSpriteId()));
        final double scale = 2.0 * collectibleRadius(data.getSpriteId()) / sprite.getWidth();
        drawSprite(g2d, sprite, data.getX(), data.getY(), cameraX, cameraY, 0, scale);
    }

    // Mappa tipo di collectible -> sprite (stessa logica di missileSprite per i missili).
    private SpriteId collectibleSprite(final String type) {
        return switch (type) {
            case "speed"  -> SpriteId.COLLECTIBLE_SPEED;
            case "shield" -> SpriteId.COLLECTIBLE_SHIELD;
            default       -> SpriteId.COLLECTIBLE_STAR;
        };
    }

    // Dimensione (raggio in px) per tipo di collectible: lo scudo e' un po' piu' grande.
    private int collectibleRadius(final String type) {
        return switch (type) {
            case "shield" -> 14;
            default -> COLLECTIBLE_RADIUS;
        };
    }

    // [Alessio - asset loader] L'aereo ora e' disegnato con il suo sprite (plane_standard/fast/
    // heavy) invece del vecchio cerchio ciano. Lo spriteId testuale del DTO (es. "plane_standard")
    // viene tradotto nell'enum SpriteId e l'immagine arriva dall'AssetStore.
    private void drawPlane(final Graphics2D g2d, final EntityRenderData data,
            final double cameraX, final double cameraY) {
        final BufferedImage sprite = assets.getSprite(SpriteId.fromFilename(data.getSpriteId()));
        // Lo sprite "guarda in su" mentre l'angolo 0 del gioco punta a destra: +PI/2 li allinea.
        // Scala = diametro voluto (2*PLANE_RADIUS) diviso la larghezza nativa dello sprite.
        final double scale = 2.0 * PLANE_RADIUS / sprite.getWidth();
        drawSprite(g2d, sprite, data.getX(), data.getY(), cameraX, cameraY,
                data.getDirectionRad() + Math.PI / 2, scale);
    }

    /**
     * Disegna uno sprite centrato sulla posizione di mondo {@code (worldX, worldY)}, tenendo
     * conto della camera (sempre centrata sull'aereo), ruotato di {@code angleRad} e scalato
     * di {@code scale} rispetto alle dimensioni native dell'immagine.
     *
     * <p>La trasformazione affine si legge dall'ultima riga alla prima: prima centra lo sprite
     * sul proprio centro, poi lo scala, poi lo ruota e infine lo porta sul punto di schermo.
     *
     * @param scale 1.0 = grandezza originale; minore di 1 rimpicciolisce, maggiore ingrandisce
     */
    private void drawSprite(final Graphics2D g2d, final BufferedImage img,
            final double worldX, final double worldY,
            final double cameraX, final double cameraY,
            final double angleRad, final double scale) {
        final double screenX = worldX - cameraX + getWidth() / 2.0;
        final double screenY = worldY - cameraY + getHeight() / 2.0;
        final AffineTransform at = new AffineTransform();
        at.translate(screenX, screenY);                              // 4. centro sullo schermo
        at.rotate(angleRad);                                         // 3. rotazione attorno al centro
        at.scale(scale, scale);                                      // 2. scala
        at.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0); // 1. centra lo sprite sull'origine
        g2d.drawImage(img, at, null);
    }

    // [Alessio - asset loader] Ogni tipo di missile usa il proprio sprite (vedi missileSprite),
    // ruotato verso la direzione di volo e scalato in base al tipo (missileRadius).
    private void drawMissiles(final Graphics2D g2d,
            final List<EntityRenderData> missiles,
            final double cameraX, final double cameraY) {
        for (final EntityRenderData m : missiles) {
            final BufferedImage sprite = assets.getSprite(missileSprite(m.getSpriteId()));
            final double scale = 2.0 * missileRadius(m.getSpriteId()) / sprite.getWidth();
            drawSprite(g2d, sprite, m.getX(), m.getY(), cameraX, cameraY,
                    m.getDirectionRad() + Math.PI / 2, scale);
        }
    }

    // Mappa tipo di missile -> sprite. Il default (basic) copre anche eventuali tipi nuovi
    // ancora senza sprite dedicato: nessun crash, restano sull'aspetto base.
    private SpriteId missileSprite(final String type) {
        return switch (type) {
            case "fast"   -> SpriteId.MISSILE_FAST;
            case "sniper" -> SpriteId.MISSILE_SNIPER;
            case "bounce" -> SpriteId.MISSILE_BOUNCE;
            case "shield" -> SpriteId.MISSILE_SHIELD;
            case "clock"  -> SpriteId.MISSILE_CLOCK;
            default       -> SpriteId.MISSILE_BASIC;
        };
    }

    // Dimensione (raggio in px) per tipo di missile: i tipi piu' "leggeri" sono piu' piccoli.
    private int missileRadius(final String type) {
        return switch (type) {
            case "fast" -> 8;
            case "sniper" -> 6;
            case "bounce", "shield" -> 11;
            case "clock" -> 12;
            default -> 10;
        };
    }
}
