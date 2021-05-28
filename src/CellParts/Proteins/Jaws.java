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
import CellParts.SegmentType;
import static GUI.GUIManager.getImage;
import static GUI.GUIManager.getSimulationPanel;
import static GUI.GUIManager.resize;
import static GUI.GUIManager.rotate;
import static GUI.GUIManager.tintImage;
import GeneticAlgorithm.Creature;
import static HyperLife.HyperLife.getGA;
import Physics.Point;
import QuadTree.QuadNode;
import QuadTree.QuadRect;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import net.jafama.FastMath;
import static GeneticAlgorithm.Creature.getBuildScale;
import File.SerialisableImage;
import static Physics.PhysicsManager.newPositions;
import static Physics.PhysicsManager.pointIntersectsSegment;
import java.awt.image.BufferedImage;
import static Physics.PhysicsManager.rotatePoint;

/**
 *
 * @author seanjhardy
 */
public class Jaws extends ProteinInstance{ 
    
    private Point endPoint;
    private SerialisableImage icon = new SerialisableImage(getImage("jawsOpen"));
    private Color lastColour;
    private double active = 0, lastActive = 0;
    private boolean mouthOpen = true, lastMouthOpen;
    
    public Jaws(Creature creature, CellPartData type, SegmentInstance parent){
        super(creature, type, parent);
        endPoint = new Point();
        creature.addOutput(this);
        lastColour = type.getType().getColour();
    } 
    
    @Override
    public void simulate(){
        super.simulate();
        double[] point = getRotatedPoint();
        startPoint.setLastX(startPoint.getX());
        startPoint.setLastY(startPoint.getY());
        startPoint.setX(point[0]);
        startPoint.setY(point[1]);
        double length = (0.2 + getAdjustedSize());
        double angle = parent.getRealAngle() + radAngle;
        double endX = startPoint.getX() + FastMath.cos(angle)*length;
        double endY = startPoint.getY() + FastMath.sin(angle)*length;
        endPoint.setX(endX);
        endPoint.setY(endY);
        if(active > 0){
            active -= getSimulationPanel().getSpeed();
            if((int)active <= lastActive - 25){
                lastActive = active;
                lastMouthOpen = mouthOpen;
                mouthOpen = !mouthOpen;
                if(!mouthOpen){
                    bite();
                }
            }
        }else{
            mouthOpen = true;
        }
    }
    
    public void activateOutput(double potentialValue){
        active = (int) (100*potentialValue);
        lastActive = active+100;
    }
    
    public void bite(){
        QuadRect range = new QuadRect(startPoint.getX(),startPoint.getY(), 1.25,1.25);
        ArrayList<QuadNode> segments = getSimulationPanel().getQuadTree().query(range, new ArrayList<>());
        for(QuadNode node : segments){
            if(node instanceof SegmentInstance){
                if(((SegmentInstance)node).getCreature() != this.getCreature()){
                    if(pointIntersectsSegment(endPoint.getX(), endPoint.getY(), (SegmentInstance)node)){
                        if(((SegmentInstance) node).getSize() > 0.2){
                            double nodeSize = ((SegmentInstance) node).getSize();
                            double sizeChange = FastMath.min(0.2, nodeSize-0.2);
                            double energyContent = ((SegmentInstance) node).getEnergyContent();
                            double energy;
                            if(((SegmentInstance) node).isDetatched()){
                                energy = sizeChange*nodeSize*energyContent;
                                ((SegmentInstance) node).setSize(nodeSize - sizeChange);
                            }else{
                                if(((SegmentInstance) node).getCreature() != null && ((SegmentInstance) node).getCreature().getHead() != node){
                                    ((SegmentInstance) node).detatchFromCreature();
                                }
                                energy = 0.5*sizeChange*nodeSize*energyContent;
                                ((SegmentInstance) node).setSize(nodeSize - sizeChange);
                            }
                            if(((SegmentInstance) node).getSize() <= 0.2){
                                if(((SegmentInstance) node).getCreature() != null && ((SegmentInstance) node).getCreature().getHead() == node){
                                    getGA().removeCreature(((SegmentInstance) node).getCreature());
                                    getGA().killCreature(((SegmentInstance) node).getCreature());
                                }else{
                                    ((SegmentInstance) node).setSize(0.2);
                                    getGA().removeSegment((SegmentInstance) node);
                                    ((SegmentInstance) node).setDead(true);
                                }
                            }
                            creature.addEnergy(energy);
                        }
                    }
                }
            }
        }
    }
    
    public double getBuildCost(){
        return getBuildScale()*0.5*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getEnergyContent(){
        return getBuildScale()*0.5*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    @Override
    public void draw(Graphics2D g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();

        double angle = parent.getRealAngle() + radAngle;
        double[] newPositions = parent.getPointAtAngle(getAdjustedAngleOnBody());
        newPositions = newPositions(newPositions[0], newPositions[1], cameraX, cameraY, scale);
        
        Color c = ((ProteinType)cellData.getType()).getColour();
        double a = ((SegmentType)parent.getCellData().getType()).getLength();
        double b = ((SegmentType)parent.getCellData().getType()).getWidth();
        double w,h;
        double jawSize = (0.05 + 0.6*(getAdjustedSize()*a*b))*scale;
        if(icon == null || 
           getSimulationPanel().getScale() != getSimulationPanel().getLastScale() ||
           FastMath.abs(c.getRed() - lastColour.getRed()) > 10 ||
           FastMath.abs(c.getGreen() - lastColour.getGreen()) > 10 ||
           FastMath.abs(c.getBlue() - lastColour.getBlue()) > 10 ||
           FastMath.abs(angle - lastAngle) > 0.05 ||
           mouthOpen != lastMouthOpen){
            BufferedImage image;
            if(mouthOpen){
                image = getImage("JawsOpen");
            }else{
                image = getImage("JawsClosed");
            }
            w = (int) (jawSize*image.getWidth()/10);
            h = (int) (jawSize*image.getHeight()/10);
            if(w > 5 && h > 5){
                lastColour = c;
                lastAngle = angle;
                image = tintImage(image, c.getRed(),c.getGreen(),c.getBlue(),255);
                image = rotate(image, angle);
                image = resize(image, w, h);
                icon = new SerialisableImage(image);
            }
            
        }else{
            w = (int) (jawSize*icon.image.getWidth()/10);
            h = (int) (jawSize*icon.image.getHeight()/10);
        }
        
        if(w > 5 && h > 5){   
            double[] point = rotatePoint(-w*0.3, 0, angle);
            double newW = icon.image.getWidth()/2;
            double newH = icon.image.getHeight()/2;
            g.drawImage(icon.image, (int)(newPositions[0] - (newW + point[0])), 
                    (int)(newPositions[1] - (newH + point[1])), null);
        }
        //newPositions
    }
}


