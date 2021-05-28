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
public class CarbonDioxideParticle extends Particle{
    
    private int maxLifetime = 500;
    
    public CarbonDioxideParticle(double x, double y, double xVel, double yVel){
        super(x, y, xVel, yVel);
        lifetime = maxLifetime;
        size = 0.005 + getRand().nextDouble()*0.01;
    }
    
    public void simulate(Graphics2D g){
        lifetime -= 1.0;//getSimulationPanel().getSpeed();
        if(lifetime > 0){
            x += xVel;
            y += yVel;
            double cameraX = getSimulationPanel().getCameraX();
            double cameraY = getSimulationPanel().getCameraY();
            double scale = getSimulationPanel().getScale();

            double[] newPos = newPositions(x, y, cameraX, cameraY, scale);
            double rad = scale*size;
            int alpha = (int) (255*(1.0 - FastMath.abs(lifetime - maxLifetime*0.5)/(maxLifetime*0.5)));
            g.setColor(new Color(255,255,255, alpha));
            g.drawOval((int)(newPos[0] - rad), (int)(newPos[1] - rad), (int)(rad*2), (int)(rad*2));
        }
    }
    
    public double getLifeTime(){
        return lifetime;
    }
}
