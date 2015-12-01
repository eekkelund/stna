/*
 * eetz1s License such cool so wow
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stna;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *
 * @author eetz1
 */
public class Stna extends JFrame implements ActionListener {

    private Arena arena = new Arena();
    private TowerEngineController contr = new TowerEngineController(arena);
    private JLabel selite;
    private JButton kasvatusp, nollausp;
    private ActionListener actionL;
    private JButton button;
    private JPanel panel;
    public boolean first = false;
    private int bsize;
    private int width = 700;
    private int height = 400;
    private Timer timer;
    private Graphics buffer;
    private Image dbImage;
    private static double fps = 60.0;
    private int pSec = 15;

    public Stna() {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Look and feel -asetus epäonnistui.");
        }

        alusta();

        arena.setTower(5, 5, "tower");
        arena.setTower(4, 3, "tower2");
        arena.setTower(2, 10, "tower");
        //arena.spawnEnemy();
        //start();
        game.run();

    }

    public void alusta() {
        setTitle("Karta");
        setSize(width, height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        panel = new JPanel();
        //selite = new JLabel();
        //actionL = new ButtonListener();
        button = new JButton("move");

        //setContentPane(panel);
        panel.add(button);
        //button.addActionListener(actionL);

        add(panel, BorderLayout.SOUTH);

        setVisible(true);
        bsize = arena.getBsize();
    }

    //  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        //if (e.getSource()==button) {
        move();

    }

    public void move() {
        for (int i = 0; i < arena.getEnemies().size(); i++) {
            ModelEnemy enemy = arena.getEnemies().get(i);
            contr.move(enemy);
            //repaint();
        }
    }
    private double spawnTime = 1 * (double) (fps);
    private double spawnFrame = spawnTime - fps;
    private int spawnCounter = 0;
    private boolean isFirst = true;
   
    private boolean sPause = false;
    private double pauseFrame = 1;
    private double pauseTime = pSec * (double) (fps);
    

    public void enemySpawner() {

        if (spawnFrame >= spawnTime && spawnCounter < arena.getSpawnWave() && !sPause) {

            arena.spawnEnemy();
            spawnFrame = 1;//-= spawnTime;
            spawnCounter++;
            isFirst = false;

        } else {
            spawnFrame++;
        }

    }
    

    
    Thread game = new Thread(new Runnable() {
        public void run() {
            long lastTime = System.nanoTime();
            long timer = System.currentTimeMillis();
            final double ns = 1000000000.0 / fps;
            double delta = 0;
            int updates = 0, frames = 0;

            while (true) {

                long now = System.nanoTime();
                delta += (now - lastTime) / ns;
                lastTime = now;

                // Update 60 times a second
                while (delta >= 1) {
                    
                    //update();
                    if(!sPause){
                        enemySpawner();
                        
                    }
                    if (arena.getEnemies().isEmpty() && !isFirst) {                        
                        if(pauseFrame >=pauseTime){
                            arena.setLevel();
                            spawnCounter = 0;
                            pauseFrame=1;
                            sPause=false;
                        }else{
                            pauseFrame++;
                            sPause =true;
                        }

                    }
                    

                    updates++;

                    

                    move();
                    delta--;

                }
                

                repaint();
                frames++;
                if (System.currentTimeMillis() - timer >= 1000) {
                    timer += 1000;
                    setTitle(" | ups: " + updates + " | fps: " + frames + "| Time: "+Math.round(Math.abs((pauseFrame/fps)-pSec)));
                    updates = 0;
                    frames = 0;
                }
            }
        }
    });

    /*public void start() {
     timer = new Timer(10, this);
     timer.start();
     }*/
    public void paint(Graphics g) {
        dbImage = createImage(width, height);
        buffer = dbImage.getGraphics();
        paintComponent(buffer);
        g.drawImage(dbImage, 0, 0, this);

    }
        private double shootFrame = fps;
        private double shootTime = 1* (int) fps/3;

    public void paintComponent(Graphics g) {

        ModelBlock[][] grid = arena.getArena();
        super.paint(g);
        
        BufferedImage img;
        for (int y = 0; y < grid.length; y++) {//DRAWS MAP
            int h = y;
            h = h * bsize;
            for (int x = 0; x < grid[0].length; x++) {
                try {
                    int w = x;
                    w = w * bsize;
                    img = ImageIO.read(new File(grid[y][x].getImg()));
                    g.drawImage(img, w, h, bsize, bsize, this);

                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }

        }

        for (ModelTower tower : arena.getTowers()) {//For each tower dis is gonna check if there is enemy to shoot
            try {

                ModelEnemy enemy;//enemy what to shoot
                if("tower".equals(tower.getid())){//if  1. level tower is shooting tower
                    if(shootFrame>=shootTime){//it has to cool down 
                        
                        if(shootFrame<=shootTime*2){
                            shootFrame++;
                            enemy = contr.shoot(tower);
                            
                        }else{
                            shootFrame=1;
                            enemy=null;
                        }
                    }else{
                        shootFrame++;
                        enemy=null;
                    }
                }else {
                    enemy = contr.shoot(tower);
                }


                g.setColor(tower.getClr());
                g.drawLine(tower.getX() * bsize + (bsize / 2), tower.getY() * bsize + (bsize / 2), enemy.getMoveX() + (bsize / 2), enemy.getMoveY() + (bsize / 2));
                
            } catch (Exception e) {
                //System.out.print(e);
                //}
            }

            //}
            drawEnemy(g);
            drawTower(g);
        }
    }

    public void drawEnemy(Graphics g) {

        BufferedImage img;
        try {//DRAWS ENEMYS

            for (int i = 0; i < arena.getEnemies().size(); i++) {
                ModelEnemy enemy = arena.getEnemies().get(i);
                img = ImageIO.read(new File(enemy.getImg()));
                g.drawImage(img, enemy.getMoveX(), enemy.getMoveY(), bsize, bsize, this);
            }
        } catch (IOException ex) {
            System.out.print(ex);
        }

    }

    public void drawTower(Graphics g) {
        BufferedImage img;
        try {
            for (ModelTower tower : arena.getTowers()) {
                img = ImageIO.read(new File(tower.getImg()));
                g.drawImage(img, tower.getX() * bsize, tower.getY() * bsize, bsize, bsize, this);
            }
        } catch (IOException ex) {
            System.out.print(ex);
        }
    }

    /*for (int x=0;x<grid.length; x++){
     int w =x;
     w =w*32;
     for(int y=0; y<grid[0].length;y++){
     int h =y;
     h =h*32;
     if(grid[x][y].getid().equals("start")){
     arena.setEnemy(x,y,"enemy");
     //g.setColor(Color.blue);
     //g.fillRect(h, w, 20, 20);
     BufferedImage img; 
     try {
     img = ImageIO.read(new File("images/img.png"));
     g.drawImage(img, h, w, this);
     } catch (IOException ex) {
     System.out.print(ex);
     }
                    
                            
     }
     }
     }*/
    public static void main(String args[]) {
        new Stna();

    }

}
