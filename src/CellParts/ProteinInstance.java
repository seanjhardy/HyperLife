/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts;

import CellParts.Proteins.Chloroplast;
import CellParts.Proteins.ColourSensor;
import CellParts.Proteins.Gametangium;
import CellParts.Proteins.Jaws;
import CellParts.Proteins.Jet;
import CellParts.Proteins.Myelin;
import CellParts.Proteins.RigidTouchSensor;
import CellParts.Proteins.SoftTouchSensor;
import CellParts.Proteins.Spike;
import GeneticAlgorithm.Creature;
import static GeneticAlgorithm.Creature.getBuildScale;

/**
 *
 * @author seanjhardy
 */
public class ProteinInstance extends CellPartInstance{
    
    public ProteinInstance(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        double[] point = parent.getPointAtAngle(getAdjustedAngleOnBody());
        startPoint.setX(point[0]);
        startPoint.setY(point[1]);
        startPoint.setLastX(startPoint.getX());
        startPoint.setLastY(startPoint.getY());
    }
    
    public static ProteinInstance createProteinInstance(Creature creature, CellPartData cellData, SegmentInstance parent, int type){
        while(type > 8){
            type -= 8;
        }
        switch (type) {
            case 0:
                return new RigidTouchSensor(creature, cellData, parent);
            case 1:
                return new SoftTouchSensor(creature, cellData, parent);
            case 2:
                return new ColourSensor(creature, cellData, parent);
            case 3:
                return new Chloroplast(creature, cellData, parent);
            case 4:
                return new Spike(creature, cellData, parent);
            case 5:
                return new Gametangium(creature, cellData, parent);
            case 6:
                return new Myelin(creature, cellData, parent);
            case 7:
                return new Jaws(creature, cellData, parent);
            case 8:
                return new Jet(creature, cellData, parent);
            default:
                return new RigidTouchSensor(creature, cellData, parent);
        }
    }
    
    public final double getAdjustedSize(){
        //size of body in growth * max size of protein type * parent size
        return size*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getBuildCost(){
        return getBuildScale()*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getEnergyContent(){
        return getBuildScale()*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getInput(){
        return 0.0;
    }
    
}
