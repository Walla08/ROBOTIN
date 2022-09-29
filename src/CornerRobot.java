
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import robocode.*;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Walter
 */
public class CornerRobot extends TeamRobot {
    private static Map<String, Point2D.Double> teamPositions = new HashMap<>();
    Point2D.Double[] Esquinas = new Point2D.Double[4];
    Point2D.Double p;
    
    @Override
    public void run() {
        setBodyColor(Color.darkGray);
        setGunColor(Color.black);
        setRadarColor(Color.GREEN);
        setBulletColor(Color.ORANGE);
        setScanColor(Color.green);
        
        Esquinas[0] = new Point2D.Double(0, 0);                                             // left,bot
        Esquinas[1] = new Point2D.Double(0, getBattleFieldHeight());                        // left,top
        Esquinas[2] = new Point2D.Double(getBattleFieldWidth(), getBattleFieldHeight());    // right,top
        Esquinas[3] = new Point2D.Double(getBattleFieldWidth(), 0);                         // right,bot
        
        double x = getX();
        double y = getY();
        
        p = new Point2D.Double(x,y);
        teamPositions.put(getName(), p);
        out.println("Coord. inicial: "+p.x + " " + p.y);
        out.println(getName());
        out.println(Arrays.toString(getTeammates()));
        
        //out.println("Current team positions list: "+teamPositions);
        try {
            this.broadcastMessage(p);
        } catch (IOException ex) {
            Logger.getLogger(CornerRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        out.println("Mi esquina: "+calculaEsquinaCercana());
        
        while(teamPositions.size() != 5){
            doNothing();
        }
        
        Point2D.Double robotEsquina = Esquinas[calculaEsquinaCercana()];    // Esquina a la que se dirigirá el robot
    }
    
    public int calculaEsquinaCercana(){
        int indexOfEsquina = -1;
        for(int i = 0; i < 4; ++i){
            double minDistance = 1000000;
            String robotName = "";
            for (Entry<String, Point2D.Double> e:teamPositions.entrySet()){
                double auxDistance = Point2D.Double.distance(e.getValue().x, e.getValue().y, Esquinas[i].x, Esquinas[i].y);
                if(auxDistance < minDistance){
                    minDistance = auxDistance;
                    robotName = e.getKey();
                }
            }
            teamPositions.remove(robotName);    // se descarta al robot que ya esté asociado a una esquina
            if(robotName.equals(getName())){
                indexOfEsquina = i;
                break;
            }
        }
        return indexOfEsquina;
    }
    
    public void onMessageRecieved(MessageEvent e){
        out.println("Message recieved from: "+e.getSender());
        out.println("Message content: "+e.getMessage());
        String sender = e.getSender();
        Point2D.Double p = (Point2D.Double) e.getMessage();
        teamPositions.put(sender, p);
        out.println("Current team positions list: "+teamPositions);
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
