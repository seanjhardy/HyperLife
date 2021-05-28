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
import Physics.CarbonDioxideParticle;
import static Physics.PhysicsManager.newPositions;
import java.awt.Color;
import java.awt.Graphics2D;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class Chloroplast extends ProteinInstance{ 
    
    private Color optimum = new Color(94, 176, 23);
    private double efficiency = 1.0;
    private double difference = 0.0;
    
    public Chloroplast(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        Color colour = ((ProteinType)cellData.getType()).getOriginalColour();
        difference = ((double)FastMath.abs(optimum.getRed()-colour.getRed())+
                FastMath.abs(optimum.getGreen()-colour.getGreen())+
                FastMath.abs(optimum.getBlue()-colour.getBlue()))/570;
        difference = FastMath.pow(1.0 - difference,2);
    } 
    
    @Override
    public void simulate(){
        super.simulate();
        efficiency -= getSimulationPanel().getEfficiencyDecrease()*getSimulationPanel().getSpeed();
        double[] point = getRotatedPoint();
        startPoint.setX(point[0]);
        startPoint.setY(point[1]);
        startPoint.setLastX(point[0]);
        startPoint.setLastY(point[1]);
        calculateEnergy();
    }
    
    public void calculateEnergy(){
        double energy = 0.005*size*
                parent.getSize()*
                ((ProteinType)cellData.getType()).getSize()*
                efficiency*
                difference*
                (1.0-creature.getOverlapPercent())*
                getSimulationPanel().getSpeed();
        creature.addEnergy(energy);
    }
    
    public double getBuildCost(){
        return getBuildScale()*1.5*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getEnergyContent(){
        return getBuildScale()*1.5*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    @Override
    public void draw(Graphics2D g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();
        
        double totalSize = 1*getAdjustedSize()*scale;
        if(totalSize > 1){
            double[] startPosition = newPositions(startPoint.getX(),startPoint.getY(),cameraX,cameraY,scale);
            Color colour = ((ProteinType)cellData.getType()).getColour();
            g.setColor(colour);
            if(scale > 50){
                g.fillOval((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
            }else{
                g.fillRect((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
            }
            //particles
            if(getSimulationPanel().getScale() > 200){
                if(getRand().nextDouble() < 0.002){
                    double randAngle = 4*FastMath.PI*(getRand().nextDouble()-0.5);
                    double d = getRand().nextDouble();
                    double particleX = startPoint.getX() + FastMath.cos(randAngle)*getAdjustedSize()*4*(d+0.25);
                    double particleY = startPoint.getY() + FastMath.sin(randAngle)*getAdjustedSize()*4*(d+0.25);
                    double speed = 0.0005;
                    double xVel = FastMath.cos(FastMath.PI + randAngle)*speed;
                    double yVel = FastMath.sin(FastMath.PI + randAngle)*speed;
                    CarbonDioxideParticle particle = new CarbonDioxideParticle(particleX, particleY, xVel, yVel);
                    getSimulationPanel().addParticle(particle);
                }
            }
        }
    }
    
}

