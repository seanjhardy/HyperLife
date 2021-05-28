/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts;

import Physics.Point;
import GeneticAlgorithm.Creature;
import QuadTree.QuadNode;
import java.awt.Graphics2D;
import java.io.Serializable;
import net.jafama.FastMath;
import static Physics.PhysicsManager.rotatePoint;

/**
 *
 * @author seanjhardy
 */
public class CellPartInstance extends QuadNode{
  
    protected Creature creature;
    protected CellPartData cellData;
    protected SegmentInstance parent;
    protected Point startPoint;
    protected static double initialSize = 0.2;
    protected double lastSize = initialSize, size = initialSize, realAngle = 0.0, radAngle = 0;
    protected double lastAngle, lastX, lastY;
    protected double[] pointOnParent = {0,0}, rotatedPoint = {0,0};
    protected double blueprintAngle = 0, blueprintStartX = 0, blueprintStartY = 0;
    protected int depth = 0;
    protected boolean flipped = false;
    
    public CellPartInstance(Creature creature, CellPartData type, SegmentInstance parent){
        this.creature = creature;
        this.parent = parent;
        this.cellData = type;
        this.startPoint = new Point();
        this.flipped = type.isFlipped();
        if(parent == null){
            depth = 0;
        }else{
            depth = parent.depth + 1;
            if(parent.flipped){
                this.flipped = parent.flipped;
            }
        }
        updatePointOnParent(getAdjustedAngleOnBody());
        radAngle = FastMath.toRadians(getAdjustedAngleOnBody() + getAdjustedAngleFromBody());
        creature.addCellPartInstance(this);
    }
    
    public void simulate(){
        if(FastMath.abs(realAngle - lastAngle) > 0.005 ||
            FastMath.abs(startPoint.getX() - lastX) > 0.005 ||
            FastMath.abs(startPoint.getY() - lastY) > 0.005){
            lastX = startPoint.getX();
            lastY = startPoint.getY();
            lastAngle = realAngle;
        }
        if(parent != null){
            if(parent.getSize() != parent.getLastSize()){
                updatePointOnParent(getAdjustedAngleOnBody());
            }
        }
    }
    
    public void activateOutput(double potentialValue){}
    
    public void draw(Graphics2D g){}
    
    public void drawBlueprint(Graphics2D g, double cameraX, double cameraY, double scale){}
    
    public void calibrateBlueprint(Creature blueprintCreature){}
    
    public void setSize(double size){
        lastSize = this.size;
        this.size = size;
    }
    
    public double getEnergyContent(){
        return 0.0;
    }
    
    public final void updatePointOnParent(double angle){
        if(parent != null){
            double width = (((SegmentType)(parent.cellData.getType())).getWidth()/2)*parent.size;
            double length = (((SegmentType)(parent.cellData.getType())).getLength()/2)*parent.size;
            //calculate correct position of point
            double radAngleOnBody = FastMath.toRadians(angle % 360);
            double tanAngle = FastMath.tan(radAngleOnBody);
            //equation for point on circumference of ellipse
            double xPoint = (width*length)/(FastMath.hypot(width, (length*tanAngle)));
            double yPoint = xPoint*tanAngle;
            if(radAngleOnBody > FastMath.PI/2 && radAngleOnBody <= 3*FastMath.PI/2){
                xPoint *= -1;
                yPoint *= -1;
            }
            xPoint -= length;
            pointOnParent[0] = xPoint;
            pointOnParent[1] = yPoint;
        }
    }
    
    public final double[] getRotatedPoint(){
        if(FastMath.abs(parent.realAngle - parent.lastAngle) > 0.005 ||
            FastMath.abs(parent.startPoint.getX() - parent.lastX) > 0.005 ||
            FastMath.abs(parent.startPoint.getY() - parent.lastY) > 0.005 || 
            (rotatedPoint[0] == 0 && rotatedPoint[1] == 0) || 
            parent.getSize() != parent.getLastSize()){
                rotatedPoint = rotatePoint(pointOnParent[0], pointOnParent[1], parent.realAngle);
                rotatedPoint[0] += parent.startPoint.getX();
                rotatedPoint[1] += parent.startPoint.getY();
        }
        return rotatedPoint;
    }
    
    public final CellPartData getCellData(){
        return cellData;
    }
    public final CellPartInstance getParent(){
        return parent;
    }
    public Creature getCreature(){
        return creature;
    }
    public int getDepth(){
        return depth;
    }
    public static double getInitialSize(){
        return initialSize;
    }
    public double getSize(){
        return size;
    }
    public double getLastSize(){
        return lastSize;
    }
    public double getRealAngle(){
        return realAngle;
    }
    public double getBlueprintAngle(){
        return blueprintAngle;
    }
    public Point getStartPoint(){
        return startPoint;
    }
    
    public final double getAdjustedAngleFromBody(){
        return cellData.getAngleFromBody()*((flipped && !cellData.isFlipped())? -1.0 : 1.0);
    }
    public final double getAdjustedAngleOnBody(){
        return cellData.getAngleOnBody()*((flipped && !cellData.isFlipped())? -1.0 : 1.0) + ((flipped && !cellData.isFlipped())? 360 : 0);
    }
}
