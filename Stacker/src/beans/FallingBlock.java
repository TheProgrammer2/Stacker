/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

/**
 *
 * @author Lukas
 */
public class FallingBlock {

    public int leftX;
    public int topY;
    public boolean done = false;

    public FallingBlock(int leftX, int topY) {
        this.leftX = leftX;
        this.topY = topY;
    }
}
