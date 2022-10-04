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
    int indexOfEsquina = -1;
    enum Estados {irEsquina, Patrulla};
    Estados states;
    boolean arrivedToCorner = false;
    
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
        
        teamPositions.put(getName(), new Point2D.Double(getX(), getY()));
        //teamPositions.put(getName(), Esquinas[0]);
        out.println("Coord. inicial: "+getX() + " " + getY());
        out.println("My name: "+getName());
        out.println("Current teammates: "+Arrays.toString(getTeammates()));
        
        //out.println("Current team positions list: "+teamPositions);
        try {
            this.broadcastMessage(new Point2D.Double(getX(), getY()));
            out.println("Message sent");
        } catch (IOException ex) {
            Logger.getLogger(CornerRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        while(teamPositions.size() != 5){
            doNothing();
        }
        
        
        indexOfEsquina = calculaEsquinaCercana();
        out.println("Mi esquina: "+indexOfEsquina);
        if(indexOfEsquina == -1){
            kamikaze();
            return;
        }
        Point2D.Double robotEsquina = Esquinas[indexOfEsquina];    // Esquina a la que se dirigirá el robot
        
        states = states.irEsquina;
        
        //goToCorner(robotEsquina);
        
        while(true){
            if(!onMyCorner())
                states = states.irEsquina;
            switch(states){
                case irEsquina:
                    turnRight(indexOfEsquina*90 - getHeading()-90);
                    if(getHeading() == 0){
                        ahead(getBattleFieldHeight() - getY()-20);
                        turnLeft(90);
                        ahead(getX()-20);
                    }
                    else if(getHeading() == 90){
                        ahead(getBattleFieldWidth() - getX()-20);
                        turnLeft(90);
                        ahead(getBattleFieldHeight() - getY()-20);
                    }
                    else if(getHeading() == 180){
                        ahead(getY()-20);
                        turnLeft(90);
                        ahead(getBattleFieldWidth() - getX()-20);
                    }
                    else if(getHeading() == 270){
                        ahead(getX()-20);
                        turnLeft(90);
                        ahead(getY()-20);
                    }
                    turnLeft(90);
                    states = states.Patrulla;
                    //arrivedToCorner = true;
                    break;
                case Patrulla:
                    setAdjustGunForRobotTurn(true);
                    turnGunLeft(90);
                    turnGunRight(90);
                    centinela();
                    break;
            }
        }
    }
    
    public boolean onMyCorner(){
        boolean posX = Esquinas[indexOfEsquina].x-50 < getX() && getX() < Esquinas[indexOfEsquina].x+50;
        boolean posY = Esquinas[indexOfEsquina].y-50 < getY() && getY() < Esquinas[indexOfEsquina].y+50;
        if(posX && posY)
            arrivedToCorner = true;
        return posX && posY || arrivedToCorner;
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
        out.println("Angulo: "+getHeading());
        if(getHeading() == 0){
            ahead(getBattleFieldHeight() - getY()-20);
            turnLeft(90);
            ahead(getX()-20);
        }
        else if(getHeading() == 90){
            ahead(getBattleFieldWidth() - getX()-20);
            turnLeft(90);
            ahead(getBattleFieldHeight() - getY()-20);
        }
        else if(getHeading() == 180){
            ahead(getY()-20);
            turnLeft(90);
            ahead(getBattleFieldWidth() - getX()-20);
        }
        else if(getHeading() == 270){
            ahead(getX()-20);
            turnLeft(90);
            ahead(getY()-20);
        }
        turnLeft(90);
    }
    
    public void centinela(){
        if(onMyCorner()){
            ahead(100);
            ahead(-100);
            turnLeft(90);
            ahead(100);
            ahead(-100);
            turnRight(90);
        }
        else{
            arrivedToCorner = false;
        }
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
             double absoluteBearing = getHeading() + event.getBearing();
             double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
             if (Math.abs(bearingFromGun) <= 3) {
                 if(states == states.Patrulla)
                     turnGunRight(bearingFromGun);
                 if (getGunHeat() == 0) {
                        fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
                }
             }
             else {
                 if(states == states.Patrulla)
                     turnGunRight(bearingFromGun);
            }
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
