/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts.Proteins;

import CellParts.CellPartData;
import CellParts.CellPartInstance;
import CellParts.ProteinInstance;
import CellParts.ProteinType;
import CellParts.SegmentInstance;
import static GUI.GUIManager.brightness;
import static GUI.GUIManager.getSimulationPanel;
import GeneticAlgorithm.Creature;
import static GeneticAlgorithm.Creature.getBuildScale;
import static HyperLife.HyperLife.getGA;
import static HyperLife.HyperLife.getRand;
import static Physics.PhysicsManager.newPositions;
import QuadTree.QuadNode;
import QuadTree.QuadRect;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class Gametangium extends ProteinInstance{
    //asexual(0-0.5)/sexual(0.5-1);
    //energythreshold
    //child headID
    
    private final static int maxCooldown = 2000;
    private double cooldown = 0;
    private Color brightColour;
    
    public Gametangium(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        brightColour = brightness(cellData.getType().getColour(),1.5);
        getSimulationPanel().getQuadTree().insert((QuadNode)this);
        creature.addGametangium(this);
    } 
    
    @Override
    public void simulate(){
        super.simulate();
        double[] point = getRotatedPoint();
        startPoint.setX(point[0]);
        startPoint.setY(point[1]);
        startPoint.setLastX(point[0]);
        startPoint.setLastY(point[1]);
        startPoint.constrainToMap();
        x = startPoint.getX();
        y = startPoint.getY();
        realAngle = parent.getRealAngle() + radAngle;
        getSimulationPanel().getQuadTree().recalculatePosition(this);
        if(cooldown == 0){
            double energyThreshold = FastMath.max(((ProteinType)cellData.getType()).getParameter(1)*100, 5);
            if(creature.getEnergy() > energyThreshold){
                if(getSimulationPanel().getGlobalStep() % 10 == 0){
                    if(((ProteinType)cellData.getType()).getParameter(0) >= 0.5){
                        checkForOverlap();
                    }else{
                        ArrayList<Gametangium> gametangii = creature.getGametangii();
                        boolean found = false;
                        Gametangium partToCloneFrom = null;
                        while(!found){
                            partToCloneFrom = gametangii.get(getRand().nextInt(gametangii.size()));
                            found = (partToCloneFrom.getCooldown() == 0);
                        }
                        double angle = partToCloneFrom.getRealAngle();
                        getGA().cloneCreature(creature, partToCloneFrom.getX(), partToCloneFrom.getY(), angle, true);
                    }
                }
            }
        }else{
            cooldown -= getSimulationPanel().getSpeed();
            if(cooldown < 0){
                cooldown = 0;
            }
        }
    }
    
    public void checkForOverlap(){
        QuadRect range = new QuadRect(startPoint.getX(), startPoint.getY(), 0.5,0.5);
        ArrayList<QuadNode> segments = getSimulationPanel().getQuadTree().query(range, new ArrayList<>());
        for(QuadNode node : segments){
            if(node instanceof Gametangium){
                if(((CellPartInstance)node).getCreature() != this.getCreature()){
                    if(((CellPartInstance)node).getCreature().getSpecies() == getCreature().getSpecies()){
                        if(creature.getReproductionType() == ((Gametangium) node).getCreature().getReproductionType()){
                            double nodeX = ((CellPartInstance)node).getStartPoint().getX();
                            double nodeY = ((CellPartInstance)node).getStartPoint().getY();
                            double s = (0.05 + 0.3*getAdjustedSize())*2;
                            double dist = FastMath.hypot(startPoint.getX() - nodeX, startPoint.getY() - nodeY);
                            if(dist <= s){
                                cooldown = maxCooldown;
                                ((Gametangium)node).setCooldown(maxCooldown);
                                getGA().crossover(creature, ((CellPartInstance)node).getCreature());
                            }
                        }
                    }
                }
            }
        }
    }
    
    public double getBuildCost(){
        return getBuildScale()*3*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getEnergyContent(){
        return getBuildScale()*3*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getCooldown(){
        return cooldown;
    }
    
    @Override
    public void draw(Graphics2D g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();
        
        double totalSize = (0.05 + 0.4*getAdjustedSize())*scale;
        double[] startPosition = newPositions(startPoint.getX(),startPoint.getY(),cameraX,cameraY,scale);
        g.setColor(cellData.getType().getColour());
        if(totalSize > 10){
            g.fillOval((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        }else{
            g.fillRect((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        }
        g.setStroke(new BasicStroke((float) (totalSize*0.2)));
        g.setColor(brightColour);
        if(totalSize > 10){
            g.drawOval((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        }else{
            g.drawRect((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        }
        if(totalSize > 10){
            totalSize *= 0.5;
            g.drawOval((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
            g.setStroke(new BasicStroke(1));
        }
    }
    
    public void setCooldown(double cooldown){
        this.cooldown = cooldown;
    }
}