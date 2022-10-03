package CornerTeam;

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
import robocode.util.Utils;
import static robocode.util.Utils.normalRelativeAngleDegrees;


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
    private static final Map<String, Point2D.Double> teamPositions = new HashMap<>();
    Point2D.Double[] Esquinas = new Point2D.Double[4];
    Point2D.Double p;
    int indexOfEsquina = -1;
    
    @Override
    public void run() {
        execute();
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
        //teamPositions.put(getName(), Esquinas[0]);
        out.println("Coord. inicial: "+p.x + " " + p.y);
        out.println("My name: "+getName());
        out.println("Current teammates: "+Arrays.toString(getTeammates()));
        
        //out.println("Current team positions list: "+teamPositions);
        try {
            this.broadcastMessage(p);
            out.println("Message sent");
        } catch (IOException ex) {
            Logger.getLogger(CornerRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        while(teamPositions.size() != 5){
            doNothing();
        }
        
        indexOfEsquina = calculaEsquinaCercana();
        if(indexOfEsquina == 0)
            kamikaze();
        out.println("Mi esquina: "+(int)(indexOfEsquina+1));
        Point2D.Double robotEsquina = Esquinas[indexOfEsquina];    // Esquina a la que se dirigirá el robot
        //this.turnRight(90);
        goToCorner(robotEsquina);
        setAdjustGunForRobotTurn(true);
        while(true)
            centinela();
    }
    
    public int calculaEsquinaCercana(){
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
    
    public void goToCorner(Point2D.Double esquina){
        turnRight(normalRelativeAngleDegrees(indexOfEsquina*90 - getHeading()-90));
        out.println("Angulo: "+getHeading());
        if(getHeading() == 0){
            ahead(800 - p.y-20);
            turnLeft(90);
            ahead(p.x-20);
        }
        else if(getHeading() == 90){
            ahead(1000 - p.x-20);
            turnLeft(90);
            ahead(800 - p.y-20);
        }
        else if(getHeading() == 180){
            ahead(p.y-20);
            turnLeft(90);
            ahead(1000 - p.x-20);
        }
        else if(getHeading() == 270){
            ahead(p.x-20);
            turnLeft(90);
            ahead(p.y-20);
        }
        turnLeft(90);
    }
    
    public void centinela(){
        ahead(100);
        ahead(-100);
        turnLeft(90);
        ahead(100);
        ahead(-100);
        turnRight(90);
    }

    public void kamikaze(){
        
    }
    
    @Override
    public void onMessageReceived(MessageEvent event) {
        out.println("Message recieved from: "+event.getSender());
        out.println("Message content: "+event.getMessage());
        String sender = event.getSender();
        Point2D.Double p = (Point2D.Double) event.getMessage();
        teamPositions.put(sender, p);
        out.println("Current team positions list: "+teamPositions);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        if(!isTeammate(event.getName())){
            stop();
            out.print("DETECTED ENEMY: "+event.getName());
            disparo(event.getDistance());
            scan();
            resume();
            //goToCorner(Esquinas[indexOfEsquina]);
        }
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
