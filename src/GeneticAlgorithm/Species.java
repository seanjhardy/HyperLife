

package GeneticAlgorithm;

import static File.NameGenerator.getRandomName;
import static File.NameGenerator.mutateName;
import static GUI.GUIManager.getSimulationPanel;
import static HyperLife.HyperLife.getGA;
import static HyperLife.HyperLife.getRand;
import java.awt.BasicStroke;
import java.awt.Color;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import net.jafama.FastMath;

public class Species implements Serializable{
    
    protected static int nodeRadius = 15;
    protected Species parent;
    protected ArrayList<Species> leftChildren = new ArrayList<>();
    protected ArrayList<Species> rightChildren = new ArrayList<>();
    protected ArrayList<Creature> members = new ArrayList<>();
    protected Creature mascot;
    protected String name;
         
    protected boolean sideOnParent = false;
    protected int totalLeft, totalRight, totalChildren;
    protected int originTime, deathTime = -1;
    protected int populationSize = 0;

    public Species(Species parent, Creature mascot) {
        this.parent = parent;
        this.originTime = (int)getSimulationPanel().getStep();
        this.mascot = mascot;
        sideOnParent = getRand().nextBoolean();
        parent.addChildSpecies(this, sideOnParent);
        addCreature(mascot);
        name = mutateName(parent.getName());
    }
    
    public Species(Creature mascot) {
        this.originTime = (int)getSimulationPanel().getStep();
        this.mascot = mascot;
        addCreature(mascot);
        name = getRandomName();
    }
    
    public void draw(Graphics2D g, double x1, double y1,double x2, double height){
        double divisionSize = (x2-x1)/(totalChildren + 1.0);
        double currentX = x1;
        g.setColor(WHITE);
        //g.drawLine((int)x1,(int)y1,(int)x2,(int)y1);
        for(Species left : leftChildren){
            //yVal is the top of the child block
            double yVal = y1+ ((left.getOriginTime() - originTime)/getSimulationPanel().getStep())*height;
            double leftTotalChildren = left.getTotalChildren()+1;
            //draw line from current node at yVal, to child node start
            double splitPoint = (left.getLeftChildren()+0.5)*divisionSize;
            g.drawLine((int)(x1 + divisionSize*(totalLeft+0.5)),
                       (int)yVal, (int)(currentX + splitPoint), (int)yVal);
            left.draw(g, currentX, yVal, currentX+divisionSize*leftTotalChildren, height);
            
            currentX += divisionSize*leftTotalChildren;
        }
        
        int xPos = (int)((currentX + divisionSize/2));
        g.setColor(BLACK);
        g.setStroke(new BasicStroke(3));
        g.fillOval(xPos-nodeRadius, (int)(y1 - nodeRadius), nodeRadius*2, nodeRadius*2);
        g.setStroke(new BasicStroke(1));
        g.setColor(WHITE);
        g.drawOval(xPos-nodeRadius, (int)(y1 - nodeRadius), nodeRadius*2, nodeRadius*2);
        int yPos = (int) (y1 + height*(deathTime - originTime)/getSimulationPanel().getStep());
        if(deathTime == -1){
            yPos = (int) (y1 + height*(getSimulationPanel().getStep() - originTime)/getSimulationPanel().getStep());
        }
        g.drawLine(xPos, (int)(y1), xPos, yPos);
        currentX += divisionSize;
        
        int mouseX = MouseInfo.getPointerInfo().getLocation().x;
        int mouseY = MouseInfo.getPointerInfo().getLocation().y;
        int dist = (int) FastMath.sqrt(FastMath.pow2(mouseX - (x1 + x2)*0.5) + FastMath.pow2(mouseY - y1));
        if(dist <= nodeRadius){
          BufferedImage image = mascot.getLastIcon();
          if(image == null){
            image = mascot.createIcon();
          }
          g.drawImage(image, null, mouseX, mouseY);
        }
        for(Species right : rightChildren){
            double yVal = y1+ ((right.getOriginTime() - originTime)/getSimulationPanel().getStep())*height;
            double rightTotalChildren = right.getTotalChildren()+1;
            double splitPoint = (right.getLeftChildren()+0.5)*divisionSize;
            g.drawLine((int)(x1 + divisionSize*(totalLeft+0.5)),
                       (int)yVal, (int)(currentX + splitPoint), (int)yVal);
            right.draw(g, currentX, yVal, currentX+divisionSize*rightTotalChildren, height);
            currentX += divisionSize*rightTotalChildren;
        }
    }
    
    public final void addChildSpecies(Species child, boolean sideOnParent){
        if(sideOnParent){
            rightChildren.add(0, child);
        }else{
            leftChildren.add(child);
        }
        incrementTotalChildren(sideOnParent);
    }
 
    public final void addCreature(Creature creature){
        members.add(creature);
        populationSize += 1;
        if(creature.getSpecies() != null){
            creature.getSpecies().removeCreature(creature);
        }
        creature.setSpecies(this);
    }
    
    public final void removeCreature(Creature creature){
        populationSize -= 1;
        if(populationSize <= 0){
            deathTime = (int) getSimulationPanel().getStep();
            getGA().removeSpecies();
        }
    }
    
    public final void incrementTotalChildren(boolean side){
        totalChildren += 1;
        if(side){
            totalRight += 1;
        }else{
            totalLeft += 1;
        }
        
        if(parent != null){
            parent.incrementTotalChildren(sideOnParent);
        }
    }
    
    
    public int getOriginTime(){
        return originTime;
    }
    public String getName(){
        return name;
    }
    public int getTotalChildren(){
        return totalChildren;
    }
    public int getRightChildren(){
        return totalRight;
    }
    public int getLeftChildren(){
        return totalLeft;
    }
}