
import java.awt.Color;
import java.io.Serializable;
import robocode.AdvancedRobot;
import robocode.* ;
import robocode.TeamRobot;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Walter
 */
public class CornerRobot extends AdvancedRobot {
     @Override
     public void run() {
        setBodyColor(Color.darkGray);
	setGunColor(Color.black);
	setRadarColor(Color.GREEN);
	setBulletColor(Color.ORANGE);
	setScanColor(Color.green);
        
         double x = getX();
         double y = getY();
         //broadcasMessage(x);
         
     }

    public void disparo(double Distancia){
        if (Distancia < 150){
            fire(1);
        }
        if(Distancia < 300){
            fire(2);
        }
        else{
            fire(3);
        }
        
    }

     
}
