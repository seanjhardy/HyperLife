/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package File;

import static HyperLife.HyperLife.getRand;
import java.util.HashSet;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class NameGenerator{
    
    private static final String lexicon = "abcdefghijklmnopqrstuvwxyz";
    private static final HashSet<String> identifiers = new HashSet<>();
    private static final double mutationChance = 0.008;

    public static String getRandomName() {
        StringBuilder builder = new StringBuilder();
        while(builder.toString().length() == 0) {
            int length = getRand().nextInt(5)+5;
            for(int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(getRand().nextInt(lexicon.length())));
            }
            if(identifiers.contains(builder.toString())) {
                builder = new StringBuilder();
            }
        }
        return builder.toString();
    }
    
    public static final String mutateName(String name){
        String newName = "";
        for(int i = 0; i < name.length(); i++){
            String letter = name.substring(i,i+1);
            double random = getRand().nextDouble();
            double insert = getRand().nextDouble();
            double delete = getRand().nextDouble();
            if(insert < mutationChance && name.length() < 10){
                newName += lexicon.charAt(getRand().nextInt(lexicon.length()));
            }
            if(delete > mutationChance || name.length() < 5){
                if(random < mutationChance){
                    newName += lexicon.charAt(getRand().nextInt(lexicon.length()));
                }else{
                    newName += letter;
                }
            }
        }
        return newName;
    }
    
    public static final String crossoverName(String name1, String name2){
        String newName = "";
        int randomLength;
        if(name1.length() == name2.length()){
            randomLength = name1.length();
        }else{
            randomLength = getRand().nextInt(FastMath.abs(name1.length() - name2.length())) + FastMath.min(name1.length(), name2.length());
        
        }
        for(int i = 0; i < randomLength; i++){
            if(i < name1.length() && i >= name2.length()){
                newName += name1.substring(i,i+1);
            }else if(i < name2.length() && i >= name1.length()){
                newName += name2.substring(i,i+1);
            }else{
                double random = getRand().nextDouble();
                String letter1 = name1.substring(i,i+1);
                String letter2 = name2.substring(i,i+1);
                if(random < 0.5){
                    newName += letter1;
                }else{
                    newName += letter2;
                }
            }
        }
        return newName;
    }
}