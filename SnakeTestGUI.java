import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * SnakeTestGUI class that shows the use of GameManager in practice.
 */
public class SnakeTestGUI implements ActionListener, KeyListener{

    //Variable Declarations
    private int timerDelay;
    private LinkedList<Color> colors;
    private LinkedList<Integer> buffer;
    private JPanel gamePanel, grid[][], header;
    private JButton pauseBtn;
    private JLabel scoreLbl;
    private boolean keyDown, triggered, paused;
    private Integer currentDirection, intScore, counter, smallestSide;
    private double boardRatio, screenRatio;
    private GameManager game1;
    private Dimension screenSize;
    private long keyEvent;
    private JFrame window;
    private Outcome state;
    private Timer timer;
    private Random rand;
    private final Integer MIN_HEIGHT;

    /**
     * Constructor for the SnakeTestGUI class that takes a text file
     * level configuration file.
     * @param file Level configuration text file.
     */
    public SnakeTestGUI(String file) {
        this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.window = new JFrame("Snake");
        this.header = new JPanel(new FlowLayout());
        this.scoreLbl = new JLabel("Score: 0");
        this.game1 = new GameManager(file);
        this.rand = new Random(System.currentTimeMillis());
        this.buffer = new LinkedList<>();
        this.colors = new LinkedList<>();
        this.keyDown = false;
        this.paused = true;
        this.currentDirection = -1;
        this.timerDelay = 500;
        this.intScore = game1.getFood();
        this.counter = 0;
        this.boardRatio = (double)game1.getCols() / (double)game1.getRows();
        this.screenRatio = (double)screenSize.getWidth() / (double)screenSize.getHeight();
        this.MIN_HEIGHT = (int)(Math.min((int)(screenSize.getWidth()), (int)(screenSize.getHeight()))*.8);
        this.smallestSide = MIN_HEIGHT;
        game1.createFood();
        game1.setFoodRate(1);
        scoreLbl.setForeground(Color.WHITE);


        this.grid = new JPanel[game1.getRows()][ game1.getCols()];
        this.gamePanel = new JPanel(new GridLayout(game1.getRows(), game1.getCols(),1,1)) {
            private static final long serialVersionUID = 1L;
            @Override   
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(window.getWidth() > window.getHeight()) {smallestSide = (int)(window.getHeight());}
                else {smallestSide = (int)(window.getWidth());}

                if(boardRatio > screenRatio) {
                    smallestSide = (int)(MIN_HEIGHT*((screenRatio/boardRatio)));
                }

                //Set the window size according to the smallest side of the screen size.
                if(boardRatio > 1) {
                    window.setSize((int)(smallestSide*boardRatio), smallestSide);
                }else {
                    window.setSize(smallestSide, (int)(smallestSide*boardRatio));
                }
            }
        };;
        
        this.pauseBtn = new JButton("Start");
        pauseBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(pauseBtn.getText().equals("Start")) {
                    paused = false;
                    pauseBtn.setText("Pause");
                    timer.start();
                }else {
                    paused = true;
                    pauseBtn.setText("Start");
                    timer.stop();
                }
                window.requestFocus();
            }
        });
        
        timer = new Timer((int)timerDelay, this);
        timer.setCoalesce(true);
        setColorList();
        initializeBoard();
        header.setBackground(Color.BLACK);
        header.add(scoreLbl);
        header.add(pauseBtn);
        if(boardRatio > 1) {
            window.setSize((int)(MIN_HEIGHT*boardRatio), MIN_HEIGHT);
        }else {
            window.setSize(MIN_HEIGHT, (int)(MIN_HEIGHT*boardRatio));
        }
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setFocusable(true);
        window.getContentPane().add(header, BorderLayout.PAGE_START);
        window.getContentPane().add(gamePanel, BorderLayout.CENTER);
        window.addKeyListener(this);
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setVisible(true);
    }
    
    public void keyPressed(KeyEvent e) {
        if(paused == true) {
            return;
        }
        keyDown = true;
        if(keyDown && triggered) {
            triggered = false;
            keyEvent = System.currentTimeMillis();
        }
        if(System.currentTimeMillis() > keyEvent + 150) {
            timer.setDelay((int)(timerDelay/2));
        }
        if(currentDirection == -1) {timer.start();}
        if(KeyEvent.getKeyText(e.getKeyCode()).equals("Up")) {
            if(currentDirection == -1) {currentDirection = 0;}
            addBuffer(0);
        }else if(KeyEvent.getKeyText(e.getKeyCode()).equals("Right")) {
            if(currentDirection == -1) {currentDirection = 1;}
            addBuffer(1);
        }else if(KeyEvent.getKeyText(e.getKeyCode()).equals("Down")) {
            if(currentDirection == -1) {currentDirection = 2;}
            addBuffer(2);
        }else if(KeyEvent.getKeyText(e.getKeyCode()).equals("Left")) {
            if(currentDirection == -1) {currentDirection = 3;}
            addBuffer(3);
        }if(KeyEvent.getKeyText(e.getKeyCode()).equals("R")) {
            reset();
        }
        if(KeyEvent.getKeyText(e.getKeyCode()).equals("NumPad +") ||
            KeyEvent.getKeyText(e.getKeyCode()).equals("Equals")) {
            if(timerDelay >= 100) {
                timer.setDelay((int)(timerDelay = timerDelay / 2));
            }
        }
        if(KeyEvent.getKeyText(e.getKeyCode()).equals("NumPad -") ||
            KeyEvent.getKeyText(e.getKeyCode()).equals("Minus")) {
            if(timerDelay < 1000) {
                timer.setDelay((int)(timerDelay = timerDelay * 2));
            }
        }
    }
    
    public void keyReleased(KeyEvent e) {
        keyDown = false;
        triggered = true;
        timer.setDelay((int)timerDelay);
    }
    public void keyTyped(KeyEvent e) {}

    public void actionPerformed(ActionEvent e) {
        if(currentDirection == -1) {
            drawBoard();
            return;
        }
        if(buffer.isEmpty()) {
            state = game1.move(currentDirection);
        }
        else if(currentDirection == getOppositeDirection(buffer.peekFirst())) {
            state = game1.move(currentDirection);
            buffer.removeFirst();
        }else {
            currentDirection = buffer.peekFirst();
            state = game1.move(buffer.getFirst());
            buffer.removeFirst();
        }
        if(state == Outcome.FOOD_COLLISION) {
            intScore += game1.getFoodRate();
            game1.createFood();
        }else if (state == Outcome.WALL_COLLISION || state == Outcome.SNAKE_COLLISION) {
            timer.stop();
            JOptionPane.showMessageDialog(null, "Game Over..", "Snake", 0);
            reset();
        }
        scoreLbl.setText("Score: "+ intScore);
        Toolkit.getDefaultToolkit().sync();
        drawBoard();
    }

    private void addBuffer(int keyEvent) {
        if(buffer.size() <= 2 && !buffer.contains(keyEvent)) {
            buffer.add(keyEvent);
        }
    }
    private int getOppositeDirection(int d) {
        switch(d) {
            case 0: {return 2;}
            case 1: {return 3;}
            case 2: {return 0;}
            case 3: {return 1;}
            default: {return -1;}
        }
    }
    private void setColorList() {
        for (int r=0; r<100; r+=1) colors.add(new Color(r*255/100,       255,         0));
        for (int g=100; g>0; g-=1) colors.add(new Color(      255, g*255/100,         0));
        for (int b=0; b<100; b+=1) colors.add(new Color(      255,         0, b*255/100));
        for (int r=100; r>0; r-=1) colors.add(new Color(r*255/100,         0,       255));
        for (int g=0; g<100; g+=1) colors.add(new Color(        0, g*255/100,       255));
        for (int b=100; b>0; b-=1) colors.add(new Color(        0,       255, b*255/100));
        colors.add(new Color(        0,       255,         0));
    }
    private  void initializeBoard() {
        for(int r = 0; r < game1.getRows(); r++) {
            for(int c = 0; c < game1.getCols(); c++) {
                grid[r][c] = new JPanel();
                grid[r][c].setBackground(game1.getSquare(r,c).getColor());
                if(r == game1.getHead().getX() && c == game1.getHead().getY()) {
                    grid[r][c].setBackground(colors.get(counter));
                }
                gamePanel.add(grid[r][c]);
            }
        }
    }
    private void drawBoard() {
        for(int r = 0; r < game1.getRows(); r++) {
            for(int c = 0; c < game1.getCols(); c++) {
                if(counter >= colors.size()) {
                    counter = 0;
                }
                if(game1.getSquare(r,c) != Square.SNAKE) {
                    grid[r][c].setBackground(game1.getSquare(r,c).getColor());
                    grid[r][c].setBorder(BorderFactory.createEmptyBorder());
                }
                if(game1.getSquare(r,c) == Square.SNAKE) {
                    grid[r][c].setBackground(colors.get(counter));
                    grid[r][c].setBorder(BorderFactory.createEmptyBorder());
                }
                if(r == game1.getHead().getX() && c == game1.getHead().getY()) {
                    grid[r][c].setBackground(colors.get(counter));
                    grid[r][c].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
                }
            }
        }counter++;
    }
    private void reset() {
        game1.createSnake();
        currentDirection = -1;
        keyEvent = -1;
        intScore = game1.getFood();
        scoreLbl.setText("Score: " +intScore);
        buffer.clear();
        timer.stop();
        paused = true;
        pauseBtn.setText("Start");
    }

    /**
     * Main method that creates an instance of the SnakeTestGUI class
     * passing a command line argument.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SnakeTestGUI g1;
                if(args.length == 0) {
                    g1 = new SnakeTestGUI("maze-cross.txt");
                }else {
                    for(String s : args) {
                        new SnakeTestGUI(s);
                    }
                }
            }
        }); 
    }
}