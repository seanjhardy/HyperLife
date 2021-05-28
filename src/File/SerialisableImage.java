/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package File;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author seanjhardy
 */
public class SerialisableImage implements Serializable{
    
    public BufferedImage image = null;
    
    public SerialisableImage(){
    }
    public SerialisableImage(BufferedImage i){
        image = i;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        image = null;
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
    
}
