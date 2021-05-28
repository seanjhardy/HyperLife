/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts;

import Physics.Point;
import static Physics.Point.constrainToAngle;
import static Physics.Point.constrainToLength;
import CellParts.Proteins.Gametangium;
import static GUI.GUIManager.brightness;
import static GUI.GUIManager.getSimulationPanel;
import static GUI.GUIManager.interpolate;
import static GUI.SimulationPanel.FRICTION;
import GeneticAlgorithm.Creature;
import static HyperLife.HyperLife.getGA;
import static HyperLife.HyperLife.getRand;
import Physics.Signal;
import QuadTree.QuadNode;
import QuadTree.QuadTree;
import java.awt.BasicStroke;
import java.awt.Color;
import static java.awt.Color.WHITE;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import net.jafama.FastMath;
import static GeneticAlgorithm.Creature.getBuildScale;
import static Physics.PhysicsManager.newBlueprintPositions;
import static Physics.PhysicsManager.newPositions;
import static Physics.PhysicsManager.rotatePoint;

/**
 *
 * @author seanjhardy
 */
public class SegmentInstance extends CellPartInstance{
    
    private ArrayList<CellPartInstance> children = new ArrayList<>();
    private ArrayList<Signal> signals = new ArrayList<>();
    private Point endPoint;
    private boolean centred = false, detatched = false, dead = false;
    private double myelin = 1.0, muscleCycle = 0;
    private double minX, minY, maxX, maxY;
    
    public SegmentInstance(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        this.endPoint = new Point();
        if(parent != null){
            double[] point = getRotatedPoint();
            startPoint.setX(point[0]);
            startPoint.setY(point[1]);
            double length = (((SegmentType)(cellData.getType())).getLength())*size;
            double parentAngle = parent.getRealAngle();
            realAngle = parentAngle + radAngle;
            endPoint.setX(startPoint.getX() + FastMath.cos(realAngle)*length);
            endPoint.setY(startPoint.getY() + FastMath.sin(realAngle)*length);
        }else{
            double length = (((SegmentType)(cellData.getType())).getLength())*size;
            startPoint.setX(creature.getX());
            startPoint.setY(creature.getY());
            realAngle = getRand().nextDouble()*FastMath.PI*2;
            endPoint.setX(startPoint.getX() + FastMath.cos(realAngle)*length);
            endPoint.setY(startPoint.getY() + FastMath.sin(realAngle)*length);
        }
        startPoint.setLastX(startPoint.getX());
        startPoint.setLastY(startPoint.getY());
        endPoint.setLastX(endPoint.getX());
        endPoint.setLastY(endPoint.getY());
        if(parent == null){
            int index = 0;
            getGA().addSegment(index, this);
        }else{
            int index = FastMath.max(FastMath.min(getGA().getSegments().indexOf(parent), getGA().getSegments().size()), 0);
            getGA().addSegment(index, this);
        }
        if(((SegmentType)cellData.getType()).getMuscle()){
            creature.addOutput(this);
        }
        getSimulationPanel().getQuadTree().insert((QuadNode)this);
    }
    
    @Override
    public void simulate(){
        super.simulate();
        calculatePosition();
        for(CellPartInstance child : children){
            if(child instanceof ProteinInstance){
                child.simulate();
            }
        }
        calculateSignal();
        x = (startPoint.getX() + endPoint.getX())*0.5;
        y = (startPoint.getY() + endPoint.getY())*0.5;
        if(!dead){
            getSimulationPanel().getQuadTree().recalculatePosition((QuadNode)this);
        }
        setBounds(startPoint.getX(), startPoint.getY());
        setBounds(endPoint.getX(), endPoint.getY());
        double cellLength = ((SegmentType)cellData.getType()).getLength()*size;
        double cellWidth = ((SegmentType)cellData.getType()).getWidth()*size;
        if(creature != null){
            creature.removeEnergy(0.001*cellLength*cellWidth*getSimulationPanel().getSpeed());
        }
    }
    
    public void calculatePosition(){
        double speed = getSimulationPanel().getSpeed();
        if(detatched){
            setSize(size*getSimulationPanel().getDecaySpeed());
            if(size < CellPartInstance.initialSize){
                size = CellPartInstance.initialSize;
                dead = true;
                getGA().removeSegment(this);
            }
        }else if(size < 1){
            double newSize = FastMath.min(size*getSimulationPanel().getGrowthSpeed(),1.0);
            double growthEnergyCost = cellData.getType().getBuildCost()*(newSize - size);
            if(creature.getEnergy() > growthEnergyCost){
                setSize(newSize);
                creature.removeEnergy(growthEnergyCost);
            }
        }
        
        double width = ((SegmentType)cellData.getType()).getWidth()*size;
        double length = ((SegmentType)cellData.getType()).getLength()*size;
        double strength = FastMath.min(width/length, length/width);
        double stiffness = (0.1 + ((SegmentType)cellData.getType()).getBoneDensity()*0.3)/FRICTION;
        stiffness = FastMath.min(stiffness, 0.99);
        if(parent != null){
            startPoint.simulate();
            endPoint.simulate();
            int updateTimes = (int) FastMath.min(FastMath.max(1, 0.2*speed), 5);
            for(int i = 0; i < updateTimes; i++){
              double[] point = getRotatedPoint();
              startPoint.constrainToPos(parent.getStartPoint(), parent.getEndPoint(), point);
              constrainToLength(startPoint, endPoint, length);

              /*double parentLength1 = FastMath.hypot(parent.getStartPoint().getX() - point[0], parent.getStartPoint().getY() - point[1]);
              constrainToLength(parent.getStartPoint(), startPoint, parentLength1);

              double parentLength2 = FastMath.hypot(parent.getEndPoint().getX() - point[0], parent.getEndPoint().getY() - point[1]);
              constrainToLength(parent.getEndPoint(), startPoint, parentLength2);*/

              double targetAngle = parent.getRealAngle() + radAngle;
              constrainToAngle(startPoint, endPoint, length, targetAngle, stiffness);
            }
            startPoint.constrainToMap();
            endPoint.constrainToMap();
        }else{
            startPoint.simulate();
            endPoint.simulate();
            constrainToLength(startPoint, endPoint, length);
            startPoint.constrainToMap();
            endPoint.constrainToMap();
        }
        realAngle = FastMath.atan2(startPoint.getY()-endPoint.getY(), startPoint.getX() - endPoint.getX());
    }
    
    public final double[] getPointAtAngle(double angle){
        double width = (((SegmentType)(cellData.getType())).getWidth()/2)*size;
        double length = (((SegmentType)(cellData.getType())).getLength()/2)*size;
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
        double[] point = rotatePoint(xPoint, yPoint, realAngle);
        point[0] += startPoint.getX();
        point[1] += startPoint.getY();
        return point;
    }
    
    public final double[] getBlueprintPointAtAngle(double angle){
        double width = (((SegmentType)(cellData.getType())).getWidth()/2);
        double length = (((SegmentType)(cellData.getType())).getLength()/2);
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
        double[] point = rotatePoint(xPoint, yPoint, 0,0, blueprintAngle + FastMath.PI);
        point[0] += blueprintStartX;
        point[1] += blueprintStartY;
        return point;
    }
    
    public void calculateSignal(){
        if(((SegmentType)this.getCellData().getType()).getNerve()){
            Iterator<Signal> it = signals.iterator();
            while(it.hasNext()){
                Signal signal = it.next();
                if(signal.getPotentialValue() != 0){
                    double lastSignalPercent = signal.getSignalPercent();
                    double length = ((SegmentType)(((CellPartData)cellData).getType())).getLength();
                    double signalSpeed = (myelin/length)*getSimulationPanel().getSpeed()*0.01;
                    signalSpeed = signalSpeed * (signal.getDirection() ? -1 : 1);
                    signal.addSignalPercent(signalSpeed);
                    signal.setSignalPercent(FastMath.min(FastMath.max(signal.getSignalPercent(),0.0), 1.0));
                    //signal.setPotentialValue(signal.getPotentialValue()*0.997);
                    CellPartInstance target = signal.getNextTarget();
                    if(target == null){
                        activateOutput(signal.getPotentialValue());
                    }else if(children.contains(target)){
                        double percentAlongBody = target.getCellData().getPercentAlongBody();
                        if((percentAlongBody <= signal.getSignalPercent() && percentAlongBody >= lastSignalPercent) ||
                           (percentAlongBody >= signal.getSignalPercent() && percentAlongBody <= lastSignalPercent)){
                            if(target instanceof SegmentInstance){
                                if(((SegmentType)((SegmentInstance)target).getCellData().getType()).getNerve()){
                                    signal.removeTarget();
                                    ((SegmentInstance)target).addSignal(
                                        new Signal(signal.getPotentialValue(), 
                                                   signal.getDirection(),
                                                   signal.getTargets()));
                                }
                            }else{
                                target.activateOutput(signal.getPotentialValue());
                            }
                        }
                    }
                    signal.setPotentialValue(FastMath.max(FastMath.min(signal.getPotentialValue(), 1.0), -1.0));
                    if(signal.getSignalPercent() >= 1.0 || signal.getSignalPercent() <= 0.0){
                        it.remove();
                    }
                }
            }
        }
    }
    
    public void activateOutput(double potentialValue){
        if(((SegmentType)this.getCellData().getType()).getMuscle() && !detatched){
            double length = FastMath.hypot(endPoint.getY() - startPoint.getY(), 
                                            endPoint.getX() - startPoint.getX());
            double width = ((SegmentType)cellData.getType()).getWidth();
            double muscleStrength = 0.002*((SegmentType)cellData.getType()).getMuscleStrength()*potentialValue*(flipped ? -1 : 1)*getSimulationPanel().getSpeed();
            double energyCost = 0.05*FastMath.abs(muscleStrength)*size*length*((creature.getSize()-width)/creature.getSize());
            double oldAngle = realAngle + FastMath.PI; 
            double newAngle = oldAngle + muscleStrength;
            if(energyCost < creature.getEnergy()){
                creature.removeEnergy(energyCost);
                endPoint.setX(startPoint.getX() + FastMath.cos(newAngle)*length);
                endPoint.setY(startPoint.getY() + FastMath.sin(newAngle)*length);

                //add force
                double magnitude = -size*length*0.02*FastMath.abs(muscleStrength);
                double[] force = {FastMath.cos((newAngle + oldAngle)*0.5)*magnitude, FastMath.sin((newAngle + oldAngle)*0.5)*magnitude};
                double[] coords = {startPoint.getX()-creature.getX(), startPoint.getY()-creature.getY()};
                //creature.addForce(force, coords);
            }
        }
    }
    
    @Override
    public void draw(Graphics2D g){
        for (CellPartInstance child : children) {
            if(child instanceof ProteinInstance){
                child.draw(g);
            }
        }
        double newAngle = realAngle + FastMath.PI;
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();
        
        double w = ((SegmentType)cellData.getType()).getWidth()*size*scale;
        double l = ((SegmentType)cellData.getType()).getLength()*size*scale;
        if(w > 1 && l > 1){
            double[] newPositions = newPositions(startPoint.getX(), startPoint.getY(), cameraX, cameraY, scale);

            Color cellColour = ((SegmentType)cellData.getType()).getColour();
            double lineWidth = FastMath.min(w,l)*0.2;
            //drawing bone under cell
            double boneDensity = ((SegmentType)cellData.getType()).getBoneDensity();
            if(boneDensity > 0 && parent != null){
                double[] lineA = getPointAtAngle(-40);
                lineA = newPositions(lineA[0], lineA[1], cameraX, cameraY, scale);
                double[] lineAEnd = parent.getPointAtAngle(getAdjustedAngleOnBody()+40);
                lineAEnd = newPositions(lineAEnd[0], lineAEnd[1], cameraX, cameraY, scale);

                double[] lineB = getPointAtAngle(40);
                lineB = newPositions(lineB[0], lineB[1], cameraX, cameraY, scale);
                double[] lineBEnd = parent.getPointAtAngle(getAdjustedAngleOnBody()-40);
                lineBEnd = newPositions(lineBEnd[0], lineBEnd[1], cameraX, cameraY, scale);

                g.setStroke(new BasicStroke((float)(lineWidth*boneDensity*0.4*size)));
                Color boneColour = WHITE;
                if(creature != null){
                    boneColour = brightness(boneColour, 1.0 - creature.getOverlapPercent());
                }
                g.setColor(boneColour);
                g.drawLine((int)(lineA[0]),(int)(lineA[1]),(int)(lineAEnd[0]),(int)(lineAEnd[1]));
                g.drawLine((int)(lineB[0]),(int)(lineB[1]),(int)(lineBEnd[0]),(int)(lineBEnd[1]));
            }
            
            Shape newShape;
            if(scale > 75){
                double s = FastMath.min(FastMath.max((scale-50.0)*1.5, 0), w);
                newShape = new RoundRectangle2D.Double(0,-w/2,l,w, s, s);
            }else{
                newShape = new Rectangle2D.Double(0,-w/2,l,w);
            }
            AffineTransform tx = new AffineTransform();
            tx.rotate(newAngle);
            newShape = tx.createTransformedShape(newShape);

            //body shape
            AffineTransform at = new AffineTransform();
            at.translate(newPositions[0], newPositions[1]);
            Shape mainShape = at.createTransformedShape(newShape);
            //g.setStroke(new BasicStroke(1));
            g.setColor(cellColour);
            g.fill(mainShape);
            //draw muscle over cell
            if(((SegmentType)cellData.getType()).getMuscle()){
                Color defulatMuscleColour = new Color(255, 125, 125);
                if(creature != null){
                    defulatMuscleColour = brightness(defulatMuscleColour, 1.0-creature.getOverlapPercent());
                }
                double a = newAngle + FastMath.PI/2;
                double xChange = FastMath.cos(a)*w/4;
                double yChange = FastMath.sin(a)*w/4;

                double percent = w/(l*2);
                double diffX = (endPoint.getX() - startPoint.getX());
                double diffY = (endPoint.getY() - startPoint.getY());
                double pointAX = startPoint.getX() + diffX*percent;
                double pointAY = startPoint.getY() + diffY*percent;
                double[] pointA = newPositions(pointAX, pointAY, cameraX, cameraY, scale);

                double pointBX = startPoint.getX() + diffX*(1-percent);
                double pointBY = startPoint.getY() + diffY*(1-percent);
                double[] pointB = newPositions(pointBX, pointBY, cameraX, cameraY, scale);

                double[] lineA = {pointA[0] + xChange, pointA[1] + yChange};
                double[] lineAEnd = {pointB[0] + xChange, pointB[1] + yChange};
                double[] lineB = {pointA[0] - xChange, pointA[1] - yChange};
                double[] lineBEnd = {pointB[0] - xChange, pointB[1] - yChange};
                
                if(scale > 50){
                    g.setStroke(new BasicStroke((float) (lineWidth*2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
                }else{
                    g.setStroke(new BasicStroke((float) (lineWidth*2)));
                }
                g.setColor(defulatMuscleColour);
                g.drawLine((int)(lineA[0]),(int)(lineA[1]),(int)(lineAEnd[0]),(int)(lineAEnd[1]));
                g.drawLine((int)(lineB[0]),(int)(lineB[1]),(int)(lineBEnd[0]),(int)(lineBEnd[1]));
            }
            
            if(((SegmentType)cellData.getType()).getNerve()){
                double[] lineStart = {0,0};
                double[] lineEnd = rotatePoint(0,l-lineWidth*0.5,0,0,newAngle - FastMath.PI/2);
                lineStart[0] += newPositions[0];lineStart[1] += newPositions[1];
                lineEnd[0] += newPositions[0];lineEnd[1] += newPositions[1];
                g.setStroke(new BasicStroke((float) (lineWidth*0.5)));
                Color nerveColour = brightness(cellColour, 0.8);
                if(creature != null){
                    nerveColour = brightness(nerveColour, 1.0-creature.getOverlapPercent());
                }
                g.setColor(nerveColour);
                       
                if(scale > 100){
                    g.drawLine((int)(lineStart[0]),(int)(lineStart[1]),(int)(lineEnd[0]),(int)(lineEnd[1]));
                
                    double diffX = (endPoint.getX() - startPoint.getX());
                    double diffY = (endPoint.getY() - startPoint.getY());

                    for(Signal signal : signals){
                        double percent = FastMath.min(FastMath.max(signal.getSignalPercent(),0.05),0.95); 
                        double pointX = startPoint.getX() + diffX*percent;
                        double pointY = startPoint.getY() + diffY*percent;
                        double[] point = newPositions(pointX, pointY, cameraX, cameraY, scale);

                        double pointChangeX = diffX*0.01*scale;
                        double pointChangeY = diffY*0.01*scale;
                        g.setStroke(new BasicStroke((float) (lineWidth*0.5)));
                        nerveColour = nerveColour(signal.getPotentialValue());
                        if(creature != null){
                            nerveColour = brightness(nerveColour, 1.0-creature.getOverlapPercent());
                        }
                        g.setColor(nerveColour);
                        g.drawLine((int)(point[0] - pointChangeX), (int)(point[1] - pointChangeY), 
                                   (int)(point[0] + pointChangeX), (int)(point[1] + pointChangeY));
                    }
                }else{
                    if(!signals.isEmpty()){
                        nerveColour = nerveColour(signals.get(0).getPotentialValue());
                        if(creature != null){
                            nerveColour = brightness(nerveColour, 1.0-creature.getOverlapPercent());
                        }
                    }
                    g.setColor(nerveColour);
                    g.drawLine((int)(lineStart[0]),(int)(lineStart[1]),(int)(lineEnd[0]),(int)(lineEnd[1]));
                    
                }
            }
            g.setStroke(new BasicStroke(1));
        }
    }
    
    @Override
    public void drawBlueprint(Graphics2D g, double cameraX, double cameraY, double scale){
        for (CellPartInstance child : children) {
            child.drawBlueprint(g, cameraX, cameraY, scale);
        }
        double w = ((SegmentType)cellData.getType()).getWidth()*scale;
        double l = ((SegmentType)cellData.getType()).getLength()*scale;
        double[] newPositions = newBlueprintPositions(blueprintStartX, blueprintStartY, cameraX, cameraY, scale);
        
        RoundRectangle2D  shape = new RoundRectangle2D.Double(0,-w/2,l,w, w, w);
        
        AffineTransform tx = new AffineTransform();
        tx.rotate(blueprintAngle);
        Shape newShape = tx.createTransformedShape(shape);
        
        AffineTransform at = new AffineTransform();
        at.translate(newPositions[0], newPositions[1]);
        newShape = at.createTransformedShape(newShape);
        // draw cell body
        Color cellColour = ((SegmentType)cellData.getType()).getColour();
        g.setColor(cellColour);
        g.setStroke(new BasicStroke((float) (2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{(float)(4)}, 0));
        g.draw(newShape);
        g.setStroke(new BasicStroke(1));
    }

    @Override
    public void calibrateBlueprint(Creature blueprintCreature){
        double[] startPos;
        if(parent == null){
            blueprintAngle = Math.PI;
            startPos = new double[]{0,0};
        }else{
            blueprintAngle = parent.getBlueprintAngle() + radAngle + FastMath.PI;
            startPos = parent.getBlueprintPointAtAngle((getAdjustedAngleOnBody()));
        }
        blueprintStartX = startPos[0];
        blueprintStartY = startPos[1];
        double length = (((SegmentType)(cellData.getType())).getLength());
        double endX = startPos[0] + FastMath.cos(blueprintAngle)*length;
        double endY = startPos[1] + FastMath.sin(blueprintAngle)*length;
        blueprintCreature.setIconBounds(startPos[0], startPos[1]);
        blueprintCreature.setIconBounds(endX, endY);
        for (CellPartInstance child : children) {
            child.calibrateBlueprint(blueprintCreature);
        }
    }
    
    public ArrayList<Object> getHighestPriorityPart(int currentPriority){
        ArrayList<Object> highestPriorityPart = null;
        for (CellPartInstance child : children){
            //if the part is not already created
            if(child instanceof SegmentInstance){
                ArrayList<Object> newPart = ((SegmentInstance)child).getHighestPriorityPart(currentPriority);
                //returns cell part data, then cell part instance
                if(newPart != null){
                    int newBuildPriority = ((CellPartData)newPart.get(0)).getBuildPriority()/(((int)newPart.get(2)));
                    if(newBuildPriority > currentPriority){
                        currentPriority = newBuildPriority;
                        highestPriorityPart = newPart;
                    }
                }
            }
        }
        for (CellPartData childData : ((SegmentType)getCellData().getType()).getChildren()){
            int buildPriority = childData.getBuildPriority();
            
            boolean childExists = false;
            for(CellPartInstance child : children){
                if(child.getCellData() == childData){
                    childExists = true;
                }
            }
            if(!childExists){
                if(buildPriority/(this.depth+1) > currentPriority && 
                   this.depth+1 < 8){
                    if(childData.isFlipped()){
                        if(centred){
                            currentPriority = buildPriority/(this.depth+1);
                            highestPriorityPart = new ArrayList<>();
                            highestPriorityPart.add(childData);
                            highestPriorityPart.add(this);
                            highestPriorityPart.add(depth+1);
                        }
                    }else{
                        currentPriority = buildPriority/(this.depth+1);
                        highestPriorityPart = new ArrayList<>();
                        highestPriorityPart.add(childData);
                        highestPriorityPart.add(this);
                        highestPriorityPart.add(depth+1);
                    }
                }
            }
        }
        return highestPriorityPart;
    }
    
    public boolean grow(){
        ArrayList<Object> partToGrow = getHighestPriorityPart(-1);
        if(partToGrow != null && !partToGrow.isEmpty()){
            SegmentInstance growFrom = (SegmentInstance)partToGrow.get(1);
            CellPartData cellPartData = (CellPartData)partToGrow.get(0);
            CellPart cellType = cellPartData.getType();

            if(cellType.isSegment()){
                double energyCost = cellType.getBuildCost()*CellPartInstance.getInitialSize();
                if(creature.getEnergy() > energyCost*(1+creature.getGrowthPriority())){
                    creature.removeEnergy(energyCost);
                    creature.addCurrentGrowthEnergy(energyCost);
                    SegmentInstance newPart = new SegmentInstance(creature, cellPartData, growFrom);
                    if(centred && ((newPart.getAdjustedAngleOnBody() == 180 && newPart.getAdjustedAngleFromBody() == 0) || 
                                   (newPart.getAdjustedAngleOnBody() == 0 && newPart.getAdjustedAngleFromBody() == 0))){
                        newPart.setCentred(true);
                    }
                    growFrom.addChild((CellPartInstance)newPart);
                    return true;
                }
            }else{
                ProteinInstance newPart = ProteinInstance.createProteinInstance(creature, cellPartData, growFrom, ((ProteinType)cellType).getType()); 
                double energyCost = newPart.getBuildCost()*CellPartInstance.getInitialSize();
                if(creature.getEnergy() > energyCost*(1+creature.getGrowthPriority())){
                    creature.removeEnergy(energyCost);
                    creature.addCurrentGrowthEnergy(energyCost);
                    growFrom.addChild((CellPartInstance)newPart);
                    return true;
                }
            }
        }
        return false;
    }
    
    public double getEnergyContent(){
        double length = ((SegmentType)cellData.getType()).getLength();
        double width = ((SegmentType)cellData.getType()).getWidth();
        boolean muscle = ((SegmentType)cellData.getType()).getMuscle();
        double energyContent = width*length*(1.0 + 0.5*(muscle?1.0:0.0));
        for(CellPartInstance child : children){
            if(child instanceof ProteinInstance){
                energyContent += child.getEnergyContent();
            }
        }
        energyContent *= getBuildScale();
        return energyContent;
    }
    
    public void removeFromQuadTree(){
        if(parentQuadTree != null){
            parentQuadTree.removeQuadNode(this);
        }
        for(CellPartInstance child : children){
            if(child instanceof SegmentInstance){
                ((SegmentInstance)child).removeFromQuadTree();
            }
            if(child instanceof Gametangium && child.getParentQuadTree() != null){
                child.getParentQuadTree().removeQuadNode(child);
            }
        }
    }
    
    public void insertIntoQuadTree(QuadTree quadTree){
        for(CellPartInstance child : children){
            if(child instanceof SegmentInstance){
                ((SegmentInstance) child).insertIntoQuadTree(quadTree);
            }
            if(child instanceof Gametangium){
                quadTree.insert(child);
            }
        }
        quadTree.insert(((QuadNode)this));
    }
    
    public void detatchFromCreature(){
        endPoint.setLastX(endPoint.getLastX() + (getRand().nextDouble()-0.5)*0.01);
        endPoint.setLastY(endPoint.getLastY() + (getRand().nextDouble()-0.5)*0.01);
        if(parent != null){
            parent.getChildren().remove(this);
        }
        parent = null;
        detach();
    }
    
    public void detach(){
        if(creature != null){
            creature.setCurrentGrowthEnergy(creature.getCurrentGrowthEnergy() - cellData.getType().getBuildCost()*creature.getRegenerationFraction());
        }
        creature = null;
        detatched = true;
        Iterator<CellPartInstance> it = children.iterator();
        while(it.hasNext()){
            CellPartInstance child = it.next();
            if(child instanceof SegmentInstance){
                ((SegmentInstance)child).detach();
            }
            if(child instanceof Gametangium){
                if(child.getParentQuadTree() != null){
                    child.getParentQuadTree().removeQuadNode(child);
                }
            }
        }
    }
    
    public void killSegment(){
        double speed = 0.0005;
        double newAngle = -realAngle + (getRand().nextDouble()-0.5)*0.4;
        double xChange = FastMath.cos(newAngle)*speed;
        double yChange = FastMath.sin(newAngle)*speed;
        startPoint.setLastX(startPoint.getLastX() + xChange);
        startPoint.setLastY(startPoint.getLastY() + yChange);
        endPoint.setLastX(endPoint.getLastX() + (getRand().nextDouble()-0.5)*0.01);
        endPoint.setLastY(endPoint.getLastY() + (getRand().nextDouble()-0.5)*0.01);
        creature.setCurrentGrowthEnergy(creature.getCurrentGrowthEnergy() - cellData.getType().getBuildCost()*creature.getRegenerationFraction());
        creature = null;
        detatched = true;
        parent = null;
        Iterator<CellPartInstance> it = children.iterator();
        while(it.hasNext()){
            CellPartInstance child = it.next();
            if(child instanceof SegmentInstance){
                ((SegmentInstance)child).killSegment();
            }
            if(child instanceof Gametangium){
                if(child.getParentQuadTree() != null){
                    child.getParentQuadTree().removeQuadNode(child);
                }
            }
            it.remove();
        }
    }
    
    public void setXPos(double xPos){
        endPoint.addX(xPos - endPoint.getX());
        endPoint.addLastX(xPos - endPoint.getLastX());
        startPoint.addLastX(xPos - startPoint.getX());
        startPoint.setX(xPos);
    }
    public void setYPos(double yPos){
        endPoint.addY(yPos - endPoint.getY());
        endPoint.addLastY(yPos - endPoint.getLastY());
        startPoint.addLastY(yPos - startPoint.getY());
        startPoint.setY(yPos);
    }
    public void addX(double x){
        startPoint.addX(x);
        endPoint.addX(x);
    }
    public void addY(double y){
        startPoint.addY(y);
        endPoint.addY(y);
    }
    public void addLastX(double x){
        startPoint.addLastX(x);
        endPoint.addLastX(x);
    }
    public void addLastY(double y){
        startPoint.addLastY(y);
        endPoint.addLastY(y);
    }
    public void addAngle(double angle){
        realAngle -= angle;
        double len = FastMath.hypot(startPoint.getY() - endPoint.getY(), startPoint.getX() - endPoint.getX())*0.5;
        double midX = (startPoint.getX() + endPoint.getX())*0.5;
        double midY = (startPoint.getY() + endPoint.getY())*0.5;
        double newX = midX + FastMath.cos(realAngle)*len;
        double newY = midY + FastMath.sin(realAngle)*len;
        double newX2 = midX - FastMath.cos(realAngle)*len;
        double newY2 = midY - FastMath.sin(realAngle)*len;
        startPoint.addX((newX - startPoint.getX()));
        startPoint.addY((newY - startPoint.getY()));
        endPoint.addX((newX2 - endPoint.getX()));
        endPoint.addY((newY2 - endPoint.getY()));
    }
    public void addForce(double[] force, double[] coords){
        double magnitude = FastMath.hypot(force[0],force[1]);
        double distance = FastMath.hypot(coords[0],coords[1]);

        double forceAngle = FastMath.atan2(force[1],force[0]);
        double coordAngle;
        if(distance != 0){
            coordAngle = FastMath.atan2(coords[1],coords[0]);
        }else{
            coordAngle = forceAngle;
        }
        //calculate rotational acceleration
        double angleYComp = FastMath.sin(forceAngle-coordAngle)*magnitude*100;
        addAngle(angleYComp*distance);

        //calculate linear acceleration
        double angleXComp = FastMath.cos(forceAngle-coordAngle)*magnitude;
        double linearX = FastMath.cos(coordAngle)*angleXComp;
        double linearY = FastMath.sin(coordAngle)*angleXComp;
        startPoint.addX(linearX);
        startPoint.addY(linearY);
    }
    public void addSignal(Signal signal){
        signal.setPotentialValue(FastMath.max(FastMath.min(signal.getPotentialValue(),1.0),-1.0));
        signals.add(signal);
    }
    public void setCentred(boolean centred){
        this.centred = centred;
    }
    public void addChild(CellPartInstance child) throws NullPointerException{
        if(child == null){
            throw new NullPointerException("Null Child");
        }
        children.add(child);
    }
    public void addMyelin(double m){
        myelin += m;
    }
    public void setBounds(double newX, double newY){
        minX = FastMath.min(minX, (newX - this.x) - 0.05);
        minY = FastMath.min(minY, (newY - this.y) - 0.05);
        maxX = FastMath.max(maxX, (newX - this.x) + 0.05);
        maxY = FastMath.max(maxY, (newY - this.y) + 0.05); 
    }
    public void setDead(boolean dead){
        this.dead = dead;
    }
    
    public ArrayList<CellPartInstance> getChildren(){
        return children;
    }
    public static Color nerveColour(double value){
        Color polarised = new Color(117, 214, 255);
        Color depolarised = new Color(255, 0, 0);
        return interpolate(depolarised, polarised, value/2 + 0.5);
    }
    public boolean isDetatched(){
        return detatched;
    }
    public boolean isDead(){
        return dead;
    }
    public Point getEndPoint(){
        return endPoint;
    }
    public int getDepth(){
        return depth;
    }
    public double getMinX(){
        return minX;
    }
    public double getMinY(){
        return minY;
    }
    public double getMaxX(){
        return maxX;
    }
    public double getMaxY(){
        return maxY;
    }
}
