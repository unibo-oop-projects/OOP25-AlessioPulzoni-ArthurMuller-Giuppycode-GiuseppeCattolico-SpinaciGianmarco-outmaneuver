package outmaneuver.view.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import outmaneuver.util.assets.AssetStore;
import outmaneuver.util.assets.SpriteId;
import outmaneuver.view.EntityRenderData;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;
import outmaneuver.view.swing.hud.IHudView;

public final class SwingGameView extends JPanel implements GameView {

    private static final Color SKY_COLOR = new Color(180, 225, 245); // azzurrino chiaro (cielo)
    private static final int EXPLOSION_FRAMES = 12;
    private static final double EXPLOSION_SIZE = 80.0;
    private static final SpriteId[] CLOUD_SPRITES = {
        SpriteId.CLOUD_1, SpriteId.CLOUD_2, SpriteId.CLOUD_3
    };
    private static final int CLOUD_TARGET = 30;
    private static final double CLOUD_FAR = 600.0;
    private static final double CLOUD_MARGIN = 250.0;
    private static final Random RAND = new Random();

    private final KeyListener keyListener;
    private final IHudView hudView;
    /** Fornisce gli sprite gia' caricati in memoria; iniettato dall'esterno (Dependency Inversion). */
    private final AssetStore assets;
    private volatile RenderState latestState;
    private final List<Cloud> clouds;

    public SwingGameView(final KeyListener keyListener, final IHudView hudView,
            final AssetStore assets) {
        this.keyListener = Objects.requireNonNull(keyListener, "keyListener must not be null");
        this.hudView = Objects.requireNonNull(hudView, "hudView must not be null");
        this.assets = Objects.requireNonNull(assets, "assets must not be null");
        this.latestState = null;
        this.clouds = new ArrayList<>(CLOUD_TARGET);
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

        g2d.setColor(SKY_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        final var state = latestState;
        if (state != null) {
            final var planeData = state.getPlane();
            final double cameraX = planeData.getX();
            final double cameraY = planeData.getY();

            updateClouds(cameraX, cameraY);
            drawClouds(g2d, cameraX, cameraY);
            drawMissiles(g2d, state.getMissiles(), cameraX, cameraY);
            drawPlane(g2d, planeData, cameraX, cameraY);

            for (final var col : state.getCollectibles()) {
                drawCollectible(g2d, col, cameraX, cameraY);
            }

            for (final var col : state.getCollisions()) {
                drawExplosion(g2d, col, cameraX, cameraY);
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
        // Scala = misura dell'hitbox (AbstractCollectible.HITBOX_RADIUS, via DTO): come aerei/missili.
        final double scale = 2.0 * data.getRadius() / sprite.getWidth();
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

    // [Alessio - asset loader] L'aereo ora e' disegnato con il suo sprite (plane_standard/fast/
    // heavy) invece del vecchio cerchio ciano. Lo spriteId testuale del DTO (es. "plane_standard")
    // viene tradotto nell'enum SpriteId e l'immagine arriva dall'AssetStore.
    private void drawPlane(final Graphics2D g2d, final EntityRenderData data,
            final double cameraX, final double cameraY) {
        final BufferedImage sprite = assets.getSprite(planeSprite(data.getSpriteId()));
        // Lo sprite "guarda in su" mentre l'angolo 0 del gioco punta a destra: +PI/2 li allinea.
        // Scala = misura dell'HITBOX (hitboxRadius dal JSON, via DTO): sprite e collisione allineati.
        final double scale = 2.0 * data.getRadius() / sprite.getWidth();
        drawSprite(g2d, sprite, data.getX(), data.getY(), cameraX, cameraY,
                data.getDirectionRad() + Math.PI / 2, scale);
    }

    // [Alessio - asset loader] Traduce lo spriteId dell'aereo (es. "plane_standard") nell'enum.
    // Se il JSON contenesse un nome senza voce corrispondente, fromFilename lancerebbe
    // IllegalArgumentException (crash): qui ripieghiamo sull'aereo standard, come lo switch
    // dei missili ha il suo default. Robusto ai dati sbagliati, niente crash.
    private SpriteId planeSprite(final String filename) {
        try {
            return SpriteId.fromFilename(filename);
        } catch (final IllegalArgumentException e) {
            return SpriteId.PLANE_STANDARD;
        }
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
    // ruotato verso la direzione di volo e scalato sulla misura del suo HITBOX (dal JSON, via DTO):
    // cosi' sprite e collisione restano sempre allineati.
    private void drawMissiles(final Graphics2D g2d,
            final List<EntityRenderData> missiles,
            final double cameraX, final double cameraY) {
        for (final EntityRenderData m : missiles) {
            final BufferedImage sprite = assets.getSprite(missileSprite(m.getSpriteId()));
            final double scale = 2.0 * m.getRadius() / sprite.getWidth();   // sprite = hitbox
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

    private void updateClouds(final double cameraX, final double cameraY) {
        final var hw = getWidth() / 2.0;
        final var hh = getHeight() / 2.0;
        clouds.removeIf(c -> {
            final var dx = Math.abs(c.worldX - cameraX);
            final var dy = Math.abs(c.worldY - cameraY);
            return dx > hw + CLOUD_FAR || dy > hh + CLOUD_FAR;
        });
        while (clouds.size() < CLOUD_TARGET) {
            final var sprite = CLOUD_SPRITES[RAND.nextInt(CLOUD_SPRITES.length)];
            final var scale = 2.0 + RAND.nextDouble() * 5.0;
            final var alpha = 0.15f + RAND.nextFloat() * 0.35f;
            final double worldX;
            final double worldY;
            switch (RAND.nextInt(4)) {
                case 0 -> { // top
                    worldX = cameraX + (RAND.nextDouble() * 2 - 1) * (hw + CLOUD_MARGIN);
                    worldY = cameraY - hh - CLOUD_MARGIN * (1 + RAND.nextDouble());
                }
                case 1 -> { // bottom
                    worldX = cameraX + (RAND.nextDouble() * 2 - 1) * (hw + CLOUD_MARGIN);
                    worldY = cameraY + hh + CLOUD_MARGIN * (1 + RAND.nextDouble());
                }
                case 2 -> { // left
                    worldX = cameraX - hw - CLOUD_MARGIN * (1 + RAND.nextDouble());
                    worldY = cameraY + (RAND.nextDouble() * 2 - 1) * (hh + CLOUD_MARGIN);
                }
                default -> { // right
                    worldX = cameraX + hw + CLOUD_MARGIN * (1 + RAND.nextDouble());
                    worldY = cameraY + (RAND.nextDouble() * 2 - 1) * (hh + CLOUD_MARGIN);
                }
            }
            clouds.add(new Cloud(worldX, worldY, sprite, scale, alpha));
        }
    }

    private void drawExplosion(final Graphics2D g2d, final EntityRenderData data,
            final double cameraX, final double cameraY) {
        final BufferedImage sheet = assets.getSprite(SpriteId.EXPLOSION);
        final int frameW = sheet.getWidth() / EXPLOSION_FRAMES;
        final int frame = (int) Math.round(data.getDirectionRad());
        final double screenX = data.getX() - cameraX + getWidth() / 2.0;
        final double screenY = data.getY() - cameraY + getHeight() / 2.0;
        final double scale = EXPLOSION_SIZE / frameW;
        final int drawW = (int) (frameW * scale);
        final int drawH = (int) (sheet.getHeight() * scale);
        g2d.drawImage(sheet,
                (int) (screenX - drawW / 2), (int) (screenY - drawH / 2),
                (int) (screenX + drawW / 2), (int) (screenY + drawH / 2),
                frame * frameW, 0,
                (frame + 1) * frameW, sheet.getHeight(),
                null);
    }

    private void drawClouds(final Graphics2D g2d, final double cameraX, final double cameraY) {
        for (final var cloud : clouds) {
            g2d.setComposite(AlphaComposite.SrcOver.derive(cloud.alpha));
            drawSprite(g2d, assets.getSprite(cloud.sprite), cloud.worldX, cloud.worldY,
                    cameraX, cameraY, 0, cloud.scale);
        }
        g2d.setComposite(AlphaComposite.SrcOver.derive(1.0f));
    }

    private record Cloud(double worldX, double worldY, SpriteId sprite, double scale, float alpha) {}
}
