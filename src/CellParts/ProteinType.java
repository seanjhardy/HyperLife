/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts;

import GeneticAlgorithm.Creature;
import java.util.ArrayList;

/**
 *
 * @author seanjhardy
 */
public class ProteinType extends CellPart{
    
    public ArrayList<Double> parameters = new ArrayList<>();
    private int type;
    private double size;
            
    public ProteinType(Creature creature, int ID) {
        super(creature, ID, false);
    }
    
    public void setType(int type){
        this.type = type;
    }
    public void setSize(double size){
        this.size = size;
    }
    public void addParameter(double p){
        parameters.add(p);
    }
    
    public int getType(){
        return type;
    } 
    public double getSize(){
        return size;
    } 
    public double getParameter(int index){
        if(index < parameters.size()){
            return parameters.get(index);
        }else{
            return 0;
        }
    }
}
