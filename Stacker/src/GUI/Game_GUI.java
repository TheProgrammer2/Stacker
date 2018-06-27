package GUI;

import Loader.ImageLoader;
import audio.AudioPlayer;
import beans.FallingBlock;
import beans.LeaderboardEntry;
import beans.LeaderboardResponse;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;
import net.Leaderboards;

/**
 *
 * @author Lukas
 */
public class Game_GUI extends javax.swing.JFrame {

    int[][] fixedBlocks;
    int[] movingBlocks;

    int leftMoving;
    long movingSpeed;
    boolean moveRight;
    int topMoving;
    boolean doesMove;

    int blockWidth = 50;
    int boundary;
    int borderLeft;
    int numberOfBlocks;

    int musicStep;

    int score;
    boolean isGameOver;
    long gameOverStart;
    LeaderboardResponse res;
    boolean askedForUpload;

    int fallingspeed = 40;
    LinkedList<FallingBlock> fallingBlocks;

    BufferedImage blueBlock = ImageLoader.loadImage("blueblock.png");
    BufferedImage redBlock = ImageLoader.loadImage("redblock.png");
    BufferedImage grayBlock = ImageLoader.loadImage("grayblock.png");
    BufferedImage background = ImageLoader.loadImage("bg.png");

    public void reset() {
        fixedBlocks = new int[10][10];
        movingBlocks = new int[10];
        leftMoving = 0;
        movingSpeed = 90;
        moveRight = true;
        topMoving = pnlScreen.getHeight() - blockWidth * 3;
        doesMove = true;
        musicStep = 1;
        score = 0;
        isGameOver = false;
        gameOverStart = 0;
        fallingBlocks = new LinkedList<>();
        res = null;
        askedForUpload = false;
        for (int i = 0; i < fixedBlocks[0].length; i++) {
            fixedBlocks[0][i] = 1;
        }
        AudioPlayer.playLoopAsync("bgm1");
    }

    public Game_GUI() {
        initComponents();
        reset();

        this.setExtendedState(MAXIMIZED_BOTH);
        borderLeft = (pnlScreen.getWidth() - fixedBlocks[0].length * blockWidth) / 2;
        boundary = (pnlScreen.getWidth() - (pnlScreen.getWidth() / blockWidth) * blockWidth) / 2;
        numberOfBlocks = pnlScreen.getWidth() / blockWidth;

        Timer updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, 0, 5);

        Timer moveTimer = new Timer();
        moveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (doesMove) {
                    if (moveRight) {
                        leftMoving++;
                        if (leftMoving + movingBlocks.length >= numberOfBlocks) {
                            moveRight = false;
                        }
                    } else {
                        leftMoving--;
                        if (leftMoving <= 0) {
                            moveRight = true;
                        }
                    }
                }
            }
        }, movingSpeed, movingSpeed);

        Timer fall = new Timer();
        fall.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean hasSettled = false;
                int settleCount = 0;
                boolean passedSettleRow = false;

                LinkedList<FallingBlock> toRemove = new LinkedList<>();
                for (FallingBlock f : fallingBlocks) {
                    if (topMoving + blockWidth == f.topY) { // is in the row where it should settle
                        passedSettleRow = true;
                        f.done = true; //means that this block has now passed the settle-row and is either falling until it reaches the end or getting part of fixedBlocks
                        // is in the range of the columns 0 - 9 of fixedBlocks
                        if (borderLeft <= f.leftX && f.leftX < borderLeft + fixedBlocks[0].length * blockWidth) {
                            int zeile = (pnlScreen.getHeight() - (topMoving + 2 * blockWidth)) / blockWidth;
                            int spalte = (f.leftX - borderLeft) / blockWidth;
                            // there is a fixed block underneath so it now is a fixed block and can be removed as falling block
                            if (fixedBlocks[zeile - 1][spalte] == 1) {
                                fixedBlocks[zeile][spalte] = 1;
                                AudioPlayer.playAsync("blockplace");
                                score++;
                                toRemove.add(f);
                                hasSettled = true;
                                settleCount++;
                                continue;
                            }
                        }
                    } else {
                        f.topY += 5;
                    }
                    if (f.topY >= pnlScreen.getHeight()) { // passes the bottom of the window and gets removed
                        toRemove.add(f);
                    } else {
                        f.topY += 5;
                    }
                }

                if (score >= 40 * musicStep && musicStep < 6) {
                    musicStep++;
                    AudioPlayer.playLoopAsync("bgm" + musicStep);
                }

                if (passedSettleRow && settleCount == 0) {
                    isGameOver = true;
                    gameOverStart = System.currentTimeMillis();
                    AudioPlayer.hardLoopEnd();
                    AudioPlayer.playAsync("gameover");
                } else if (passedSettleRow) {
                    movingBlocks = new int[settleCount];
                }
                if (hasSettled) {
                    doesMove = true;
                    topMoving -= blockWidth;
                }

                for (FallingBlock f : toRemove) {
                    fallingBlocks.remove(f);
                }

                if (hasBlockInFirstRow()) {
                    topMoving += blockWidth;
                    for (int z = 0; z < fixedBlocks.length - 1; z++) {
                        for (int s = 0; s < fixedBlocks[0].length; s++) {
                            fixedBlocks[z][s] = fixedBlocks[z + 1][s];
                        }
                    }
                    for (int s = 0; s < fixedBlocks[0].length; s++) {
                        fixedBlocks[fixedBlocks.length - 1][s] = 0;
                    }
                }
            }
        }, 0, fallingspeed);
    }

    public boolean hasBlockInFirstRow() {
        for (int i = 0; i < fixedBlocks[fixedBlocks.length - 1].length; i++) {
            if (fixedBlocks[fixedBlocks.length - 1][i] == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void paint(Graphics g) {
        BufferedImage img = (BufferedImage) createImage(pnlScreen.getWidth(), pnlScreen.getWidth());
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.drawImage(background, 0, 0, pnlScreen);

        //painting falling blocks
        try {
            for (FallingBlock f : fallingBlocks) {
                g2d.drawImage(blueBlock, f.leftX, f.topY, pnlScreen);
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("Modified while printing!");
            return;
        }

        // painting the fixed blocks
        for (int z = 0; z < fixedBlocks.length; z++) {
            for (int s = 0; s < fixedBlocks[0].length; s++) {
                if (fixedBlocks[z][s] == 1) {
                    g2d.drawImage(grayBlock, borderLeft + s * blockWidth, pnlScreen.getHeight() - z * blockWidth - blockWidth, pnlScreen);
                }
            }
        }

        // painting the moving blocks
        if (doesMove) {
            for (int i = 0; i < movingBlocks.length; i++) {
                g2d.drawImage(redBlock, boundary + blockWidth * (leftMoving + i), topMoving, pnlScreen);
            }
        }

        //print score
        g2d.setFont(new Font("Arial", Font.BOLD, 25));
        g2d.setColor(Color.white);
        g2d.drawString("Score: " + score, 20, 50);

        if (isGameOver) {
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.setColor(Color.red);
            g2d.drawString("Game Over!", (pnlScreen.getWidth() - getFontMetrics(g2d.getFont()).stringWidth("Game Over!")) / 2, 100);
            if (System.currentTimeMillis() >= gameOverStart + 1500) {
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
                if (res == null) {
                    res = Leaderboards.getLeaderboards();
                }
                if (res.getResponseCode() != 0) {
                    g2d.setFont(new Font("Arial", Font.BOLD, 40));
                    g2d.setColor(Color.red);
                    g2d.drawString("Could not connect to Leaderboards Server!", (pnlScreen.getWidth() - getFontMetrics(g2d.getFont()).stringWidth("Could not connect to Leaderboards Server!")) / 2, this.getHeight() / 2 - 20);
                } else {
                    int y = this.getHeight() / 2 - 250;
                    g2d.setFont(new Font("Arial", Font.BOLD, 40));
                    g2d.setColor(Color.white);
                    for (LeaderboardEntry entry : res.getEntries()) {
                        g2d.drawString(entry.toString(), (pnlScreen.getWidth() - getFontMetrics(g2d.getFont()).stringWidth(entry.toString())) / 2, y);
                        y += 50;
                    }
                    if(!askedForUpload) {
                        askedForUpload = true;
                        if(JOptionPane.showConfirmDialog(this, "Do you want to upload your score to the online leaderboards?", "Leaderboards", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            String name = JOptionPane.showInputDialog("Please enter your name: (leave blank to abort upload)");
                            if(name.equals(""))
                                return;
                            while(name.contains(";") || name.length() > 16) {
                                name = JOptionPane.showInputDialog("Please enter your name: (leave blank to abort upload)\nNames cannot contain semicolons (;) and can only be up to 16 characters long.");
                                if(name.equals(""))
                                    return;
                            }
                            int result = Leaderboards.addEntry(new LeaderboardEntry(name, score));
                            System.out.println(result);
                            if(result != 0)
                                JOptionPane.showMessageDialog(this, "Sorry! Your score could not be uploaded. Please check your connection or message the developers.");
                            else {
                                JOptionPane.showMessageDialog(this, "Your score has successfully been uploaded!");
                                res = Leaderboards.getLeaderboards();
                            }
                        }
                    }
                }
            }
        }

        g2d = (Graphics2D) pnlScreen.getGraphics();
        g2d.drawImage(img, 0, 0, pnlScreen);
    }

    public void drop() {
        doesMove = false;
        for (int i = 0; i < movingBlocks.length; i++) {
            fallingBlocks.add(new FallingBlock(boundary + blockWidth * (leftMoving + i), topMoving));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlScreen = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                onResize(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                onKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlScreenLayout = new javax.swing.GroupLayout(pnlScreen);
        pnlScreen.setLayout(pnlScreenLayout);
        pnlScreenLayout.setHorizontalGroup(
            pnlScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        pnlScreenLayout.setVerticalGroup(
            pnlScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        getContentPane().add(pnlScreen, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void onResize(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_onResize
        borderLeft = (pnlScreen.getWidth() - fixedBlocks[0].length * blockWidth) / 2;
        numberOfBlocks = pnlScreen.getWidth() / blockWidth;
        boundary = (pnlScreen.getWidth() - (pnlScreen.getWidth() / blockWidth) * blockWidth) / 2;
        topMoving = pnlScreen.getHeight() - blockWidth * 3;
    }//GEN-LAST:event_onResize

    private void onKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_onKeyPressed
        if (!isGameOver) {
            if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                drop();
            }
        } else {
            if (evt.getKeyCode() == KeyEvent.VK_R) {
                reset();
            }
        }
    }//GEN-LAST:event_onKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Game_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Game_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Game_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Game_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Game_GUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlScreen;
    // End of variables declaration//GEN-END:variables
}
