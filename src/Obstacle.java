public class Obstacle {
    private double x, y;
    private int gapHeight;
    private int gapY;
    private static final int WIDTH = 80;
    private static final double SPEED = 3.0;
    private static final int MIN_GAP = 150;
    private static final int MAX_GAP = 200;
    private static final int NARROW_GAP = 120;

    public Obstacle(int startX) {
        this(startX, -1, false);
    }

    public Obstacle(int startX, int forcedY, boolean narrow) {
        this.x = startX;
        this.gapHeight = narrow ? NARROW_GAP : MIN_GAP + (int)(Math.random() * (MAX_GAP - MIN_GAP));
        if (forcedY < 0) {
            this.gapY = 50 + (int)(Math.random() * (GamePanel.getPanelHeight() - gapHeight - 100));
        } else {
            this.gapY = Math.min(Math.max(50, forcedY), GamePanel.getPanelHeight() - gapHeight - 50);
        }
    }

    public void update() {
        x -= SPEED;
    }

    public void render(java.awt.Graphics2D g2d) {
        // Draw the neon outline effect
        g2d.setColor(java.awt.Color.MAGENTA);
        // Top obstacle
        g2d.drawRect((int)x, 0, WIDTH, gapY);
        // Bottom obstacle
        g2d.drawRect((int)x, gapY + gapHeight, WIDTH, GamePanel.getPanelHeight() - (gapY + gapHeight));

        // Fill the obstacles with a darker color
        g2d.setColor(new java.awt.Color(50, 0, 50));
        g2d.fillRect((int)x + 1, 0, WIDTH - 1, gapY - 1);
        g2d.fillRect((int)x + 1, gapY + gapHeight + 1, WIDTH - 1, GamePanel.getPanelHeight() - (gapY + gapHeight) - 1);
    }

    public boolean isOffScreen() {
        return x + WIDTH < 0;
    }

    public boolean collidesWith(Player player) {
        java.awt.Rectangle playerBounds = player.getBounds();
        java.awt.Rectangle topObstacle = new java.awt.Rectangle((int)x, 0, WIDTH, gapY);
        java.awt.Rectangle bottomObstacle = new java.awt.Rectangle(
            (int)x, gapY + gapHeight,
            WIDTH, GamePanel.getPanelHeight() - (gapY + gapHeight)
        );

        return playerBounds.intersects(topObstacle) || playerBounds.intersects(bottomObstacle);
    }
}
