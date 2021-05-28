/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts.Proteins;

import CellParts.CellPartData;
import CellParts.ProteinInstance;
import CellParts.ProteinType;
import CellParts.SegmentInstance;
import static GUI.GUIManager.getSimulationPanel;
import GeneticAlgorithm.Creature;
import static GeneticAlgorithm.Creature.getBuildScale;
import static HyperLife.HyperLife.getRand;
import Physics.BubbleParticle;
import static Physics.PhysicsManager.newPositions;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class Jet extends ProteinInstance{ 
    
    public int maxCooldown = 200, cooldown = maxCooldown, lastSent = 0;
    
    public Jet(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        creature.addOutput(this);
    } 
    
    @Override
    public void simulate(){
        super.simulate();
        double[] point = getRotatedPoint();
        startPoint.setX(point[0]);
        startPoint.setY(point[1]);
        if(cooldown > 0){
            cooldown -= getSimulationPanel().getSpeed();
        }
        if(lastSent >= 0){
            lastSent -= getSimulationPanel().getStep();
            double newAngle = parent.getRealAngle() + radAngle;
            //force
            double magnitude = 0.01;
            double forceXVel = -magnitude*FastMath.cos(newAngle)*getSimulationPanel().getSpeed();
            double forceYVel = -magnitude*FastMath.sin(newAngle)*getSimulationPanel().getSpeed();
            double[] force = {forceXVel,forceYVel};
            double dist = FastMath.sqrt(FastMath.pow(startPoint.getX() - parent.getX(), 2) +
                            FastMath.pow(startPoint.getY() - parent.getY(), 2));
            double xChange = FastMath.cos(realAngle)*(dist);
            double yChange = FastMath.sin(realAngle)*(dist);
            parent.addForce(force, new double[]{xChange, yChange});
            cooldown = maxCooldown;
            
            //particles
            if(getSimulationPanel().getScale() > 25){
                for(int i = 0; i < 5; i++){
                    double randAngle = newAngle + FastMath.PI*0.2*(getRand().nextDouble()-0.5);

                    double speed = getRand().nextDouble()*0.005 + 0.001;
                    double xVel = FastMath.cos(randAngle)*speed + (parent.getStartPoint().getX() - parent.getStartPoint().getLastX());
                    double yVel = FastMath.sin(randAngle)*speed + (parent.getStartPoint().getY() - parent.getStartPoint().getLastY());
                    BubbleParticle newParticle = new BubbleParticle(startPoint.getX(), startPoint.getY(), xVel, yVel, randAngle);
                    getSimulationPanel().addParticle(newParticle);
                }
            }
        }
        realAngle = parent.getRealAngle() + radAngle;
    }
    
    @Override
    public void activateOutput(double potentialValue){
        if(potentialValue > 0 && cooldown <= 0){
            lastSent = 10;
        }
    }
    
    public double getBuildCost(){
        return getBuildScale()*0.3*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getEnergyContent(){
        return getBuildScale()*0.3*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    @Override
    public void draw(Graphics2D g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();

        double angle = realAngle;
        double[] newPositions = parent.getPointAtAngle(getAdjustedAngleOnBody());
        newPositions = newPositions(newPositions[0], newPositions[1], cameraX, cameraY, scale);

        double w = (0.1+0.2*getAdjustedSize())*scale;
        double l = (0.1+0.6*getAdjustedSize())*scale;

        RoundRectangle2D  shape = new RoundRectangle2D.Double(-l/2,-w/2,l,w, w*0.5, w*0.5);
        AffineTransform tx = new AffineTransform();
        tx.rotate(angle);
        Shape newShape = tx.createTransformedShape(shape);

        AffineTransform at = new AffineTransform();
        at.translate(newPositions[0], newPositions[1]);
        newShape = at.createTransformedShape(newShape);

        g.setColor(((ProteinType)cellData.getType()).getColour());
        g.fill(newShape);
    }
}

