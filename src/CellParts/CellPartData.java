/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts;

import java.io.Serializable;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class CellPartData{
    private final CellPart type;
    private final int buildPriority;
    private final int angleOnBody, angleFromBody;
    private double percentAlongBody;
    private final boolean flipped;
    private boolean built = false;
    
    public CellPartData(CellPart cellType, boolean flipped, int buildPriority, int angleOnBody, int angleFromBody){
        this.type = cellType;
        this.buildPriority = buildPriority;
        this.angleOnBody = angleOnBody;
        this.angleFromBody = angleFromBody;
        this.flipped = flipped;
        percentAlongBody = (1.0-FastMath.cos(FastMath.toRadians(angleOnBody)))*0.5;
    }
    
    public final CellPart getType(){
        return type;
    }
    
    public final int getBuildPriority(){
        return buildPriority;
    }
    
    public final int getAngleOnBody(){
        return angleOnBody;
    }
    
    public final int getAngleFromBody(){
        return angleFromBody;
    }
    public final boolean isFlipped(){
        return flipped;
    }
    public final boolean isBuilt(){
        return built;
    }
    
    public double getPercentAlongBody(){
        return percentAlongBody;
    }
    
    public void setBuilt(boolean built){
        this.built = built;
    }
}
