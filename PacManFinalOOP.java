import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
// main class - handles the game window and all
public class PacManFinalOOP extends JFrame {
    private GameBoard gameBoard;
    private GameEngine gameEngine;
    private JLabel statusLabel, levelLabel, scoreLabel, powerUpLabel;
    private Timer gameTimer;
    private StartScreen startScreen;
    private JPanel mainPanel;
    
    public PacManFinalOOP() {
        setTitle("Pac-Man Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // using CardLayout so i can switch between start screen and game
        mainPanel = new JPanel(new CardLayout());
        gameEngine = new GameEngine();
        gameBoard = new GameBoard(gameEngine);
        
        JPanel gamePanel = new JPanel(new BorderLayout());
        createMenuBar();
        gamePanel.add(gameBoard, BorderLayout.CENTER);
        gamePanel.add(createStatusPanel(), BorderLayout.SOUTH);
        
        startScreen = new StartScreen(this);
        mainPanel.add(startScreen, "START");
        mainPanel.add(gamePanel, "GAME");
        add(mainPanel);
        
        
        addKeyListener(new GameKeyListener(this));
        setFocusable(true);
        gameTimer = new Timer(350, e -> updateGame());
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    // switches to game screen
    public void showGame() {
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, "GAME");
        newGame();
        requestFocus();
    }
    
    public void showStartScreen() {
        gameTimer.stop();
        startScreen.restartAnimation();
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, "START");
    }
    // creates menu at top part
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(e -> newGame());
        JMenuItem mainMenuItem = new JMenuItem("Main Menu");
        mainMenuItem.addActionListener(e -> showStartScreen());
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(e -> System.exit(0));
        gameMenu.add(newGameItem);
        gameMenu.add(mainMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }
    
    // status panel shows level, score etc at bottom
    private JPanel createStatusPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 5));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(Color.BLACK);
        levelLabel = new JLabel("Level: 1");
        levelLabel.setForeground(Color.YELLOW);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.YELLOW);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel = new JLabel("Use WASD to move");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        powerUpLabel = new JLabel("");
        powerUpLabel.setForeground(Color.CYAN);
        powerUpLabel.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(levelLabel);
        p.add(scoreLabel);
        p.add(statusLabel);
        p.add(powerUpLabel);
        return p;
    }
    
    // start new game - resets everything
    public void newGame() {
        gameEngine.initializeGame();
        gameBoard.repaint();
        updateStatus();
        gameTimer.start();
        requestFocus();
    }
    // main game loop - runs every 350ms
    private void updateGame() {
        if (gameEngine.isGameOver()) {
            gameTimer.stop();
            return;
        }
        gameEngine.moveGhosts();
        gameEngine.checkCollisions();
        gameEngine.updatePowerUp();
        gameEngine.spawnRandomPowerUp();
        gameBoard.repaint();
        updateStatus();
        if (gameEngine.isGameOver()) {
            gameTimer.stop();
            handleGameOver();
        } else if (gameEngine.isLevelComplete()) {
            handleLevelComplete();
        }
    }
    // handles player movement from keyboard- processes movement
    public void processMove(char dir) {
        if (gameEngine.isGameOver()) return;
        if (!gameEngine.movePacMan(dir)) return;
        gameBoard.repaint();
        updateStatus();
        if (gameEngine.isGameOver()) {
            gameTimer.stop();
            handleGameOver();
        } else if (gameEngine.isLevelComplete()) {
            handleLevelComplete();
        }
    }
    // updates all the labels at bottom
    private void updateStatus() {
        levelLabel.setText("Level: " + gameEngine.getLevel());
        scoreLabel.setText("Score: " + gameEngine.getScore());
        if (gameEngine.hasPowerUp()) {
            powerUpLabel.setText("INVINCIBLE! Timer: " + gameEngine.getPowerUpTimer());
        } else {
            powerUpLabel.setText("");
        }
        statusLabel.setText("Pellets: " + gameEngine.getRemainingPellets());
    }
    // shows game over dialog
    private void handleGameOver() {
        String msg;
        String title;
        if (gameEngine.isVictory()) {
            msg = "VICTORY! You are the GOAT!\nFinal Score: " + gameEngine.getScore();
            title = "Congrats!";
        } else {
            msg = "Game Over!\nFinal Score: " + gameEngine.getScore();
            title = "Game Over";
        }
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
        int choice = JOptionPane.showConfirmDialog(this, "You failed lol!, Give it one more try?", "New Game?", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            newGame();
        } else {
            showStartScreen();
        }
    }
    // moves to next level
    private void handleLevelComplete() {
        gameEngine.advanceLevel();
        gameBoard.repaint();
        updateStatus();
        String msg = "Level " + gameEngine.getLevel() + " started!";
        if (gameEngine.getLevel() == 3) {
            msg += " FINAL LEVEL!";
        }
        statusLabel.setText(msg);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PacManFinalOOP game = new PacManFinalOOP();
            game.setVisible(true);
        });
    }
}
// start screen panel with buttons
class StartScreen extends JPanel {
    private PacManFinalOOP gameFrame;
    private Timer animTimer;
    private int blink = 0;

    public StartScreen(PacManFinalOOP frame) {
        this.gameFrame = frame;
        setPreferredSize(new Dimension(600, 550));
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.BLACK);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("PAC-MAN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 65));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subLabel = new JLabel("Use WASD or Arrow Keys to move");
        subLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subLabel.setForeground(Color.CYAN);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(10));
        titlePanel.add(subLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Button panel
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.BLACK);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JButton startBtn = createButton("START GAME", new Color(0, 130, 0));
        startBtn.addActionListener(e -> startGame());
        JButton quitBtn = createButton("QUIT", new Color(130, 0, 0));
        quitBtn.addActionListener(e -> System.exit(0));
        
        btnPanel.add(startBtn);
        btnPanel.add(Box.createHorizontalStrut(30));
        btnPanel.add(quitBtn);
        add(btnPanel, BorderLayout.SOUTH);
        
        // animation timer only used for blinking text now
        animTimer = new Timer(400, e -> {
            blink = (blink + 1) % 30;
            repaint();
        });
        animTimer.start();
        
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start");
        getActionMap().put("start", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
    }
    
    public void restartAnimation() {
        animTimer.start();
    }
    // makes fancy buttons with colors
    private JButton createButton(String text, Color baseColor) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 18));
        b.setForeground(Color.WHITE);
        b.setBackground(baseColor);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(baseColor.brighter(), 2),
            BorderFactory.createEmptyBorder(12, 35, 12, 35)
        ));
        b.setFocusPainted(false);
        // hover effect thingy
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(baseColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                b.setBackground(baseColor);
            }
        });
        return b;
    }
    
    private void startGame() {
        animTimer.stop();
        gameFrame.showGame();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // move the info area up to fill the space where the animation was blah blah
        int gy = 160;
        g2.setColor(new Color(20, 20, 40));
        g2.fillRoundRect(50, gy, getWidth() - 100, 260, 12, 12);
        g2.setColor(new Color(60, 60, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(50, gy, getWidth() - 100, 260, 12, 12);
        
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        String howTo = "HOW TO PLAY";
        int htw = g2.getFontMetrics().stringWidth(howTo);
        g2.drawString(howTo, (getWidth() - htw) / 2, gy + 28);
        
        int row1 = gy + 55;
        int row2 = gy + 115;
        int col1 = 80;
        int col2 = 330;
        
        // game info
        g2.setColor(Color.YELLOW);
        g2.fillArc(col1, row1 - 12, 28, 28, 45, 270);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString("You - Catch em all!", col1 + 38, row1 + 5);
        
        g2.setColor(Color.WHITE);
        g2.fillOval(col2 + 8, row1 - 5, 12, 12);
        g2.drawString("Pellet - 10 points each", col2 + 38, row1 + 5);
        
        // Row 2: Ghost and Power-up
        drawGhost(g2, col1, row2 - 12, Color.RED);
        g2.setColor(Color.WHITE);
        g2.drawString("Ghost - Ghost them!(get it ;|)", col1 + 38, row2 + 5);
        
        g2.setColor(new Color(255, 215, 0));
        g2.fillOval(col2 + 5, row2 - 8, 20, 20);
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("*", col2 + 12, row2 + 7);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString("Power-up - Devour ghosts!", col2 + 38, row2 + 5);
        
        // Controls
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.setColor(Color.CYAN);
        String ctrl = "Controls:  W/Up  S/Down  A/Left  D/Right";
        int cw = g2.getFontMetrics().stringWidth(ctrl);
        g2.drawString(ctrl, (getWidth() - cw) / 2, gy + 220);
        
        // Blinking text
        if (blink < 20) {
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.setColor(Color.YELLOW);
            String txt = "Press ENTER or click START GAME";
            int tw = g2.getFontMetrics().stringWidth(txt);
            g2.drawString(txt, (getWidth() - tw) / 2, gy + 250);
        }
    }
    // darws a ghost shape at pos and colour (kept for static legend)
    private void drawGhost(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.fillArc(x, y, 32, 32, 0, 180);
        g.fillRect(x, y + 16, 32, 14);
        int[] xp = {x, x + 8, x + 16, x + 24, x + 32};
        int[] yp = {y + 30, y + 24, y + 30, y + 24, y + 30};
        g.fillPolygon(xp, yp, 5);
        g.setColor(Color.WHITE);
        g.fillOval(x + 5, y + 10, 9, 9);
        g.fillOval(x + 18, y + 10, 9, 9);
        g.setColor(Color.BLACK);
        g.fillOval(x + 7, y + 12, 5, 5);
        g.fillOval(x + 20, y + 12, 5, 5);
    }
}
// keyboard input
class GameKeyListener extends KeyAdapter {
    private PacManFinalOOP game;
    
    public GameKeyListener(PacManFinalOOP g) {
        game = g;
    }
    //WASD and arrow keys work
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            game.processMove('W');
        } else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            game.processMove('A');
        } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            game.processMove('S');
        } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            game.processMove('D');
        }
    }
}

class GameEngine {
    private static final int SIZE = 15;
    private static final int INIT_GHOSTS = 3;
    private static final int POWER_DUR = 20;  // changed from 15, was too short
    
    private Grid grid;
    private PacMan pacMan;
    private List<Ghost> ghosts;
    private PowerUp powerUp;
    private int score;
    private int level;
    private boolean gameOver;
    private boolean victory;
    private Random rand;
    
    public GameEngine() {
        rand = new Random();
        ghosts = new ArrayList<>();
    }
    // resets everything for new game
    public void initializeGame() {
        level = 1;
        score = 0;
        gameOver = false;
        victory = false;
        ghosts.clear();
        initLevel();
    }
    
    private void initLevel() {
        grid = new Grid(SIZE);
        grid.placeObstacles(level);
        
        // had to place pellets as was working before
        grid.placeAllPellets();
        
        // find spot for pacman
        int[] pp = grid.findEmpty(rand);
        pacMan = new PacMan(pp[0], pp[1]);
        
        // remove pellet from Pacman's starting pos
        grid.removePellet(pp[0], pp[1]);
        
        // Spawn ghosts away
        ghosts.clear();
        int numGhosts = INIT_GHOSTS + level;
        int minDist = (level == 1) ? 6 : 4; // less distance on harder levels
        for (int i = 0; i < numGhosts; i++) {
            int[] p = grid.findEmptyFar(rand, pacMan.getRow(), pacMan.getCol(), minDist);
            ghosts.add(new Ghost(p[0], p[1]));
            // Remove pellet from ghost starting position
            grid.removePellet(p[0], p[1]);
        }
        powerUp = null;
    }
    
    public boolean movePacMan(char d) {
        int nr = pacMan.getRow();
        int nc = pacMan.getCol();
        
        if (d == 'W') nr--;
        else if (d == 'A') nc--;
        else if (d == 'S') nr++;
        else if (d == 'D') nc++;
        else return false;

        // cant move into walls
        if (!grid.isValid(nr, nc)) return false;
        
        // check if pacman gets powerup
        if (powerUp != null && powerUp.isAt(nr, nc)) {
            pacMan.activate(POWER_DUR);
            powerUp = null;
        }
        
        pacMan.moveTo(nr, nc);
        
        if (grid.hasPellet(nr, nc)) {
            grid.removePellet(nr, nc);
            score += 10;
        }
        return true;
    }
    // AI for ghost movement
    public void moveGhosts() {
        int chase = (level == 1) ? 40 : 70;
        for (Ghost g : ghosts) {
            if (!g.isActive()) continue;
            int[] np;
            // sometimes chase, sometimes random
            if (rand.nextInt(100) < chase) {
                np = bestMove(g);
            } else {
                np = randMove(g);
            }
            if (grid.isValid(np[0], np[1])) {
                g.moveTo(np[0], np[1]);
            }
        }
    }
     // finds best move for ghost to chase pacman
    private int[] bestMove(Ghost g) {
        int[][] m = {
            {g.getRow() - 1, g.getCol()},
            {g.getRow() + 1, g.getCol()},
            {g.getRow(), g.getCol() - 1},
            {g.getRow(), g.getCol() + 1}
        };
        int br = g.getRow();
        int bc = g.getCol();
        int md = Integer.MAX_VALUE;
        for (int[] mv : m) {
            if (grid.isValid(mv[0], mv[1])) {
                int d = Math.abs(mv[0] - pacMan.getRow()) + Math.abs(mv[1] - pacMan.getCol());
                if (d < md) {
                    md = d;
                    br = mv[0];
                    bc = mv[1];
                }
            }
        }
        return new int[]{br, bc};
    }
    
    private int[] randMove(Ghost g) {
        int[][] m = {
            {g.getRow() - 1, g.getCol()},
            {g.getRow() + 1, g.getCol()},
            {g.getRow(), g.getCol() - 1},
            {g.getRow(), g.getCol() + 1}
        };
        int d = rand.nextInt(4);
        if (grid.isValid(m[d][0], m[d][1])) {
            return m[d];
        }
        return bestMove(g);
    }
    // checks for collisions between pacman and ghosts
    public void checkCollisions() {
        for (Ghost g : ghosts) {
            if (g.isActive() && g.isAt(pacMan.getRow(), pacMan.getCol())) {
                if (pacMan.hasPowerUp()) {
                    g.setActive(false);
                    score += 50;
                    if (level == 3 && isLevelComplete() && allGhostsEaten()) {
                        victory = true;
                        gameOver = true;
                    }
                } else {
                    gameOver = true;
                    victory = false;
                }
            }
        }
    }
    // updates powerup timer
    public void updatePowerUp() {
        if (pacMan.hasPowerUp()) {
            pacMan.decTimer();
        }
    }
    // spawns powerup randomly
    public void spawnRandomPowerUp() {
        if (powerUp != null || pacMan.hasPowerUp()) return;
        
        if (level == 3 && isLevelComplete() && !allGhostsEaten()) {
            int[] p = grid.findEmpty(rand);
            powerUp = new PowerUp(p[0], p[1]);
            return;
        }
        
        int chance = (level == 1) ? 15 : 10;
        if (rand.nextInt(chance) == 0) {
            int[] p = grid.findEmpty(rand);
            powerUp = new PowerUp(p[0], p[1]);
        }
    }
   // checks if all pellets eaten 
    public boolean isLevelComplete() {
        return grid.getPellets() == 0;
    }
    // moves to next level
    public void advanceLevel() {
        if (level < 3) {
            level++;
            initLevel();
        }
    }
    // checks if all ghosts are dead
    private boolean allGhostsEaten() {
        for (Ghost g : ghosts) {
            if (g.isActive()) return false;
        }
        return true;
    }
    // getters
    public Grid getGrid() { return grid; }
    public PacMan getPacMan() { return pacMan; }
    public List<Ghost> getGhosts() { return ghosts; }
    public PowerUp getPowerUp() { return powerUp; }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public boolean isGameOver() { return gameOver; }
    public boolean isVictory() { return victory; }
    public int getRemainingPellets() { return grid.getPellets(); }
    public boolean hasPowerUp() { return pacMan.hasPowerUp(); }
    public int getPowerUpTimer() { return pacMan.getTimer(); }
    public int getGridSize() { return SIZE; }
}
// base class for all game entities
abstract class Entity {
    protected int row;
    protected int col;
    
    public Entity(int r, int c) {
        row = r;
        col = c;
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
    public void moveTo(int r, int c) { row = r; col = c; }
    public boolean isAt(int r, int c) { return row == r && col == c; }
}

class PacMan extends Entity {
    private boolean powered;
    private int timer;
    
    public PacMan(int r, int c) {
        super(r, c);
        powered = false;
        timer = 0;
    }
    // activates powerup
    public void activate(int d) {
        powered = true;
        timer = d;
    }
    // decreases powerup timer
    public void decTimer() {
        if (powered) {
            timer--;
            if (timer <= 0) {
                powered = false;
            }
        }
    }
    
    public boolean hasPowerUp() { return powered; }
    public int getTimer() { return timer; }
}
// ghost class
class Ghost extends Entity {
    private boolean active;
    
    public Ghost(int r, int c) {
        super(r, c);
        active = true;
    }
    // checks if ghost is there
    public boolean isActive() { return active; }
    public void setActive(boolean a) { active = a; }
}
// makes pacman invincible
class PowerUp extends Entity {
    public PowerUp(int r, int c) {
        super(r, c);
    }
}
//  handles maze grid layout, pellets and all
class Grid {
    private int size;
    private char[][] cells;
    private boolean[][] pellets;
    
    public Grid(int s) {
        size = s;
        cells = new char[s][s];
        pellets = new boolean[s][s];
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                cells[i][j] = '_';
                pellets[i][j] = false;
            }
        }
    }
    
    public void placeObstacles(int lvl) {
        // fill everything with walls first
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = '#';
            }
        }
        
        // males out the paths (black corridors)
        for (int i = 1; i < size - 1; i++) {
            cells[1][i] = '_';           // Top row
            cells[size - 2][i] = '_';    // Bottom row
            cells[i][1] = '_';           // Left column
            cells[i][size - 2] = '_';    // Right column
        }
        
        // horizontal corridors
        for (int i = 1; i < size - 1; i++) {
            cells[4][i] = '_';   // Upper middle
            cells[7][i] = '_';   // Center
            cells[10][i] = '_';  // Lower middle
        }
        
        // verticall corridors
        for (int i = 1; i < size - 2; i++) {
            cells[i][4] = '_';   // Left-center
            cells[i][7] = '_';   // Center
            cells[i][10] = '_';  // Right-center
        }
        
        // more paths for connections, maze layout
        cells[2][2] = '_'; cells[3][2] = '_';
        cells[2][12] = '_'; cells[3][12] = '_';
        cells[11][2] = '_'; cells[12][2] = '_';
        cells[11][12] = '_'; cells[12][12] = '_';
        
        //  center area - make it accessible
        cells[6][6] = '_'; cells[6][7] = '_'; cells[6][8] = '_';
        cells[7][6] = '_'; cells[7][7] = '_'; cells[7][8] = '_';
        cells[8][6] = '_'; cells[8][7] = '_'; cells[8][8] = '_';
        
        // etxra paths connecting to center
        cells[5][7] = '_'; cells[9][7] = '_';
        cells[7][5] = '_'; cells[7][9] = '_';
        
        //  some internal walls for maze structure
        cells[2][4] = '#'; cells[2][5] = '#'; cells[2][6] = '#';
        cells[2][8] = '#'; cells[2][9] = '#'; cells[2][10] = '#';
        
        cells[5][2] = '#'; cells[6][2] = '#';
        cells[5][12] = '#'; cells[6][12] = '#';
        cells[8][2] = '#'; cells[9][2] = '#';
        cells[8][12] = '#'; cells[9][12] = '#';
        
        cells[5][4] = '#'; cells[6][4] = '#';
        cells[5][10] = '#'; cells[6][10] = '#';
        cells[8][4] = '#'; cells[9][4] = '#';
        cells[8][10] = '#'; cells[9][10] = '#';
        
        cells[12][4] = '#'; cells[12][5] = '#'; cells[12][6] = '#';
        cells[12][8] = '#'; cells[12][9] = '#'; cells[12][10] = '#';
        
        // Level 2: more walls
        if (lvl >= 2) {
            cells[3][5] = '#'; cells[3][6] = '#';
            cells[3][8] = '#'; cells[3][9] = '#';
            cells[11][5] = '#'; cells[11][6] = '#';
            cells[11][8] = '#'; cells[11][9] = '#';
        }
        
        // Level 3: more more walls
        if (lvl >= 3) {
            cells[5][5] = '#'; cells[5][9] = '#';
            cells[9][5] = '#'; cells[9][9] = '#';
            cells[4][3] = '#'; cells[4][11] = '#';
            cells[10][3] = '#'; cells[10][11] = '#';
        }
    }
    
    public void placePellet(int r, int c) {
        if (inBounds(r, c) && cells[r][c] == '_' && !pellets[r][c]) {
            pellets[r][c] = true;
        }
    }
    // put pellets in corridors
    public void placeAllPellets() {
        // 40% seemed good, not too many not too few, goldilocks
        Random r = new Random();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (cells[row][col] == '_' && r.nextInt(100) < 40) {
                    pellets[row][col] = true;
                }
            }
        }
    }
    // removes pellet from pos
    public void removePellet(int r, int c) {
        pellets[r][c] = false;
    }
    // checks if pellet is there
    public boolean hasPellet(int r, int c) {
        return inBounds(r, c) && pellets[r][c];
    }
    
    public int getPellets() {
        int n = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (pellets[i][j]) n++;
            }
        }
        return n;
    }
    // checks if cell is valid (in bounds and not wall)
    public boolean isValid(int r, int c) {
        return inBounds(r, c) && cells[r][c] != '#';
    }
    
    public boolean inBounds(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }
    // checks if cell is wall
    public boolean isObstacle(int r, int c) {
        return inBounds(r, c) && cells[r][c] == '#';
    }
    
    public int[] findEmpty(Random rnd) {
        while (true) {
            int r = rnd.nextInt(size);
            int c = rnd.nextInt(size);
            if (cells[r][c] == '_') {
                return new int[]{r, c};
            }
        }
    }
    // finds empty cell 
    public int[] findEmpty(Random rnd, int avoidR, int avoidC) {
        while (true) {
            int r = rnd.nextInt(size);
            int c = rnd.nextInt(size);
            if (cells[r][c] == '_' && !(r == avoidR && c == avoidC)) {
                return new int[]{r, c};
            }
        }
    }
    
    public int[] findEmptyFar(Random rnd, int tr, int tc, int minDist) {
        while (true) {
            int r = rnd.nextInt(size);
            int c = rnd.nextInt(size);
            int dist = Math.abs(r - tr) + Math.abs(c - tc);
            if (cells[r][c] == '_' && dist > minDist) {
                return new int[]{r, c};
            }
        }
    }
    // gets grid size
    public int getSize() {
        return size;
    }
}
//
class GameBoard extends JPanel {
    private static final int CS = 40;
    private GameEngine ge;
    // draws everything
    public GameBoard(GameEngine e) {
        ge = e;
        int boardSize = e.getGridSize() * CS;
        setPreferredSize(new Dimension(boardSize, boardSize));
        setBackground(Color.BLACK);
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Grid gr = ge.getGrid();
        
        // draws blue background first
        g2.setColor(new Color(0, 0, 255));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // drawing grid
        for (int r = 0; r < gr.getSize(); r++) {
            for (int c = 0; c < gr.getSize(); c++) {
                int x = c * CS;
                int y = r * CS;
                
                if (!gr.isObstacle(r, c)) {
                    // black corridor space
                    g2.setColor(Color.BLACK);
                    g2.fillRect(x, y, CS, CS);
                    
                    // draw small dot in empty spaces (grid pattern)
                    g2.setColor(new Color(50, 50, 50));
                    g2.fillOval(x + CS / 2 - 2, y + CS / 2 - 2, 4, 4);
                }
                
                if (gr.hasPellet(r, c)) {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x + CS / 2 - 5, y + CS / 2 - 5, 10, 10);
                }
            }
        }
        
        // draw power-up
        PowerUp pu = ge.getPowerUp();
        if (pu != null) {
            int x = pu.getCol() * CS;
            int y = pu.getRow() * CS;
            g2.setColor(new Color(255, 215, 0, 150));
            g2.fillOval(x + 4, y + 4, CS - 8, CS - 8);
            g2.setColor(new Color(255, 215, 0));
            g2.fillOval(x + 8, y + 8, CS - 16, CS - 16);
        }
        
        // draw ghosts
        Color[] ghostColors = {Color.RED, Color.MAGENTA, new Color(255, 150, 0), Color.CYAN, Color.GREEN};
        int colorIdx = 0;
        for (Ghost gh : ge.getGhosts()) {
            if (!gh.isActive()) {
                colorIdx++;
                continue;
            }
            int x = gh.getCol() * CS;
            int y = gh.getRow() * CS;
            
            Color gc;
            if (ge.hasPowerUp()) {
                gc = Color.BLUE;
            } else {
                gc = ghostColors[colorIdx % ghostColors.length];
            }
            
            // making Ghost body
            g2.setColor(gc);
            g2.fillArc(x + 4, y + 2, CS - 8, CS - 8, 0, 180);
            g2.fillRect(x + 4, y + CS / 2 - 2, CS - 8, CS / 2 - 4);
            
            // wavy butt design
            int waveW = (CS - 8) / 4;
            for (int i = 0; i < 4; i++) {
                int wx = x + 4 + i * waveW;
                g2.fillArc(wx, y + CS - 12, waveW, 10, 180, 180);
            }
            
            // Making Eyes
            g2.setColor(Color.WHITE);
            g2.fillOval(x + 10, y + 10, 8, 8);
            g2.fillOval(x + CS - 18, y + 10, 8, 8);
            g2.setColor(Color.BLACK);
            g2.fillOval(x + 12, y + 12, 4, 4);
            g2.fillOval(x + CS - 16, y + 12, 4, 4);
            
            colorIdx++;
        }
        
        // Had to Draw Pac-Man
        PacMan pm = ge.getPacMan();
        int px = pm.getCol() * CS;
        int py = pm.getRow() * CS;
        
        if (ge.hasPowerUp()) {
            g2.setColor(new Color(255, 255, 0, 80));
            g2.fillOval(px - 2, py - 2, CS + 4, CS + 4);
        }
        
        g2.setColor(Color.YELLOW);
        g2.fillArc(px + 4, py + 4, CS - 8, CS - 8, 35, 290);
        
        // Eye creation
        g2.setColor(Color.BLACK);
        g2.fillOval(px + CS / 2 - 2, py + 10, 5, 5);
    }
}