/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Physics;

import static GUI.GUIManager.getSimulationPanel;
import static HyperLife.HyperLife.getRand;
import static Physics.PhysicsManager.newPositions;
import java.awt.Color;
import java.awt.Graphics2D;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class BubbleParticle extends Particle{
    
    private int maxLifetime = 500;
    private double yAngleChange, xAngleChange, speedChange;
    
    public BubbleParticle(double x, double y, double xVel, double yVel, double angle){
        super(x, y, xVel, yVel);
        lifetime = maxLifetime;
        size = 0.01 + getRand().nextDouble()*0.05;
        speedChange = 0.01;
        xAngleChange = angle;
        yAngleChange = angle;
    }
    
    public void simulate(Graphics2D g){
        lifetime -= getSimulationPanel().getSpeed();
        if(lifetime > 0){
            speedChange += (getRand().nextDouble()-0.5)*0.05;
            yAngleChange += (getRand().nextDouble()-0.5)*speedChange;
            xAngleChange += (getRand().nextDouble()-0.5)*speedChange;
            xVel += FastMath.cos(yAngleChange)*0.00001;
            yVel += FastMath.sin(xAngleChange)*0.00001;
            xVel *= 0.99;
            yVel *= 0.99;
            x += xVel;
            y += yVel;
            double cameraX = getSimulationPanel().getCameraX();
            double cameraY = getSimulationPanel().getCameraY();
            double scale = getSimulationPanel().getScale();

            double[] newPos = newPositions(x, y, cameraX, cameraY, scale);
            double rad = scale*size;
            double miniRad = rad*0.2;
            int newX = (int) (newPos[0] - rad*0.3);
            int newY = (int) (newPos[1] - rad*0.3);
            g.setColor(new Color(255,255,255, (int)(((double)lifetime/maxLifetime)*255)));
            g.drawOval((int)(newPos[0] - rad), (int)(newPos[1] - rad), (int)(rad*2), (int)(rad*2));
            g.fillOval(newX - (int) (miniRad), newY-(int) (miniRad), (int) (miniRad*2), (int) (miniRad*2));
        }
    }
    
    public double getLifeTime(){
        return lifetime;
    }
}
