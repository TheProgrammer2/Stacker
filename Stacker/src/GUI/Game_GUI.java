/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import audio.AudioPlayer;
import beans.FallingBlock;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Lukas
 */
public class Game_GUI extends javax.swing.JFrame {

    int[][] fixedBlocks = new int[10][10];
    int[] movingBlocks = new int[10];

    int leftMoving = 0;
    long movingSpeed = 100;
    boolean moveRight = true;
    int topMoving;
    boolean doesMove = true;

    int blockWidth = 50;
    int boundary;
    int borderLeft;
    int numberOfBlocks;

    boolean isGameOver = false;

    int fallingspeed = 25;
    LinkedList<FallingBlock> fallingBlocks = new LinkedList<>();
    int score = 0;

    public Game_GUI() {
        initComponents();
        AudioPlayer.playLoopAsync("bgm6");
        this.setExtendedState(MAXIMIZED_BOTH);
        borderLeft = (pnlScreen.getWidth() - fixedBlocks[0].length * blockWidth) / 2;
        boundary = (pnlScreen.getWidth() - (pnlScreen.getWidth() / blockWidth) * blockWidth) / 2;
        numberOfBlocks = pnlScreen.getWidth() / blockWidth;
        topMoving = pnlScreen.getHeight() - blockWidth * 3; //change to the upper block later

        // init the base row
        for (int i = 0; i < fixedBlocks[0].length; i++) {
            fixedBlocks[0][i] = 1;
        }

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
                if (passedSettleRow && settleCount == 0) {
                    System.out.println("Game Over");
                    isGameOver = true;
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
            }
        }, 0, fallingspeed);
    }

    @Override
    public void paint(Graphics g) {
        BufferedImage img = (BufferedImage) createImage(pnlScreen.getWidth(), pnlScreen.getWidth());
        Graphics2D g2d = (Graphics2D) img.getGraphics();

        //painting falling blocks
        for (FallingBlock f : fallingBlocks) {
            g2d.fillRect(f.leftX, f.topY, blockWidth, blockWidth);
        }

        // painting the fixed blocks
        for (int z = 0; z < fixedBlocks.length; z++) {
            for (int s = 0; s < fixedBlocks[0].length; s++) {
                if (fixedBlocks[z][s] == 1) {
                    g2d.fillRect(borderLeft + s * blockWidth, pnlScreen.getHeight() - z * blockWidth - blockWidth, blockWidth, blockWidth);
                    g2d.setColor(Color.yellow);
                    g2d.drawRect(borderLeft + s * blockWidth, pnlScreen.getHeight() - z * blockWidth - blockWidth, blockWidth, blockWidth);
                    g2d.setColor(Color.black);
                }
            }
        }

        // painting the moving blocks
        if (doesMove) {
            for (int i = 0; i < movingBlocks.length; i++) {
                g2d.fillRect(boundary + blockWidth * (leftMoving + i), topMoving, blockWidth, blockWidth);
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
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            drop();
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
