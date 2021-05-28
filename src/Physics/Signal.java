/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Physics;

import CellParts.CellPartInstance;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Queue;

/**
 *
 * @author seanjhardy
 */
public class Signal{
    
    private double potentialValue;
    private double signalPercent;
    private boolean direction = false;
    private Queue<CellPartInstance> targets;
    
    public Signal(double potentialValue, boolean direction, Queue<CellPartInstance> targets){
        this.potentialValue = potentialValue;
        this.direction = direction;
        if(direction){
            signalPercent = 1.0;
        }else{
            signalPercent = 0.0;
        }
        this.targets = targets;
    }
    
    public double getPotentialValue(){
        return potentialValue;
    }
    
    public double getSignalPercent(){
        return signalPercent;
    }
    
    public boolean getDirection(){
        return direction;
    }
    
    public Queue<CellPartInstance> getTargets(){
        return targets;
    }
    
    public CellPartInstance getNextTarget(){
        return targets.peek();
    }
    
    public void removeTarget(){
        targets.poll();
    }
    
    public void setPotentialValue(double p){
        this.potentialValue = p;
    }
    
    public void addSignalPercent(double s){
        this.signalPercent += s;
    }
    
    public void setSignalPercent(double s){
        this.signalPercent = s;
    }
    
}
