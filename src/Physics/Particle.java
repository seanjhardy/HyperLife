/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Physics;

import static GUI.GUIManager.getSimulationPanel;
import static GUI.SimulationPanel.FRICTION;
import static HyperLife.HyperLife.getRand;
import java.awt.Graphics2D;

/**
 *
 * @author seanjhardy
 */
public class Particle {
    
    protected double lifetime;
    protected double x, y, xVel, yVel, size;
    
    public Particle(double x, double y, double xVel, double yVel){
        this.x = x;
        this.y = y;
        this.xVel = xVel;
        this.yVel = yVel;
    }
    
    public void simulate(Graphics2D g){
        lifetime -= getSimulationPanel().getSpeed();
        xVel *= FRICTION;
        yVel *= FRICTION;
        x += xVel;
        y += yVel;
        double rad = getSimulationPanel().getScale()*size;
        g.drawOval((int)(x - rad), (int)(y - rad), (int)(rad*2), (int)(rad*2));
    }
    
    public double getLifeTime(){
        return lifetime;
    }
}
