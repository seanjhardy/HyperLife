/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

/**
 *
 * @author s-hardy
 */
public class ButtonVariable {
    private boolean value, lastState;
    
    public ButtonVariable(boolean b){
        value = b;
    }
    
    public void setValue(boolean b){
        lastState = value;
        value = b;
    }
    public boolean getValue(){
        return value;
    }
    
    public void setLastState(boolean b){
        lastState = b;
    }
    public boolean getLastState(){
        return lastState;
    }
}
