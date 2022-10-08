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
 * @author Walter y Henok
 */
public class CornerRobot extends TeamRobot {
    private static final Map<String, Point2D.Double> teamPositions = new HashMap<>();
    Point2D.Double[] Esquinas = new Point2D.Double[4];
    int indexOfEsquina = -1;
    enum EstadosCorner {irEsquina, Patrulla};
    enum EstadosKamikaze {scan, chase};
    EstadosKamikaze kamikazeState;
    EstadosCorner cornerState;
    boolean arrivedToCorner = false;
    
    @Override
    /**
     * Funcion principal del programa. Aqui es donde se toman las decisiones iniciales tales como
     * el color del robot. Tambien se hace un calculo de esquinas de manera local donde el robot
     * sabrá que esquina le tocará.
     * Tambien se les asigna las posiciones de las esquinas.
     */
    
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
            //El robot espera que le lleguen todas las posiciones de los robots del equipo
            doNothing();
        }
        
        
        indexOfEsquina = calculaEsquinaCercana();
        out.println("Mi esquina: "+indexOfEsquina);
        if(indexOfEsquina == -1){
            //Si el robot recibe la esquina -1 se convierte en el kamikaze
            setRadarColor(Color.RED);
            while(true){
                kamikazeState = EstadosKamikaze.scan;
                turnRadarLeft(360);
            }
        }
        
        Point2D.Double robotEsquina = Esquinas[indexOfEsquina];    // Esquina a la que se dirigirá el robot
        cornerState = EstadosCorner.irEsquina;
                
        while(true){
            if(!onMyCorner())
                cornerState = EstadosCorner.irEsquina;
            switch(cornerState){
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
                    cornerState = EstadosCorner.Patrulla;
                    //arrivedToCorner = true;
                    break;
                case Patrulla:
                    setAdjustGunForRobotTurn(true);
                    //turnGunLeft(90);
                    //turnGunRight(90);
                    setTurnGunRightRadians(Double.POSITIVE_INFINITY);
                    centinela();
                    break;
            }
            
        }
    }
    
    /**
     * Función encargada de verificar si el robot se encuentra en su esquina
     * calculando la posicion actual del robot con el de su esquina asignada.
     * @return onMyCorner
     */
    public boolean onMyCorner(){
        boolean posX = Esquinas[indexOfEsquina].x-50 < getX() && getX() < Esquinas[indexOfEsquina].x+50;
        boolean posY = Esquinas[indexOfEsquina].y-50 < getY() && getY() < Esquinas[indexOfEsquina].y+50;
        if(posX && posY)
            arrivedToCorner = true;
        return posX && posY || arrivedToCorner;
    }
    
    /**
     * La funcion devuelve el índice a los robos más cercanos a las esquinas
     * y el robot que se quede sin indice se convertirá en el kamikaze
     *
     * @return esquina
    */
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
            out.println("Robot: "+robotName+" a la esquina "+indexOfEsquina);
            teamPositions.remove(robotName);    // se descarta al robot que ya esté asociado a una esquina
            if(robotName.equals(getName())){
                indexOfEsquina = i;
                break;
            }
        }
        return indexOfEsquina;
    }
    
    /**
     * Función que se encarga del movimiento de patrulla en las esquinas.
     * Permite que el robot situado en su esquina tengo un movimiento unicamente cuando
     * se encuentre en su esquina. De esta manera trata de esquivar balas.
     */
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

    /*public void kamikazeFullScan(){
            if(kamikazeState == EstadosKamikaze.scan)
            turnRadarLeft(360);
    }*/
    
    /**
    *Esta funcion se encarga de gestionar los mensajes recibidos por broadcast.
    * Se utiliza para almacenar la posición del resto de robots del equipo.
    *
     * @param event
    */
    @Override
    public void onMessageReceived(MessageEvent event) {
        out.println("Message recieved from: "+event.getSender());
        out.println("Message content: "+event.getMessage());
        String sender = event.getSender();
        Point2D.Double p = (Point2D.Double) event.getMessage();
        teamPositions.put(sender, p);
        out.println("Current team positions list: "+teamPositions);
    }

    /**
    *Esta función gestiona el evento de escanear un robot.
    * El robot solo disparará si el robot escaneado es un enemigo.
    * Si el robot es el kamikaze tendrá un comportamiento especial.
    *
     * @param event
    */
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        if(!isTeammate(event.getName())){
            out.println("ENEMY SCANNED");
            if(indexOfEsquina != -1){
                double absoluteBearing = getHeading() + event.getBearing();
                double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
                if (Math.abs(bearingFromGun) <= 3) {
                    turnGunRight(bearingFromGun);
                    if (getGunHeat() == 0) {
                        fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
                   }
                }
                else {
                    turnGunRight(bearingFromGun);
               }
                
            }
            else if(indexOfEsquina == -1){
                kamikazeState = EstadosKamikaze.chase;
                chaseRobot(event);
            }
        }
    }
    
    /**
    *Esta funcion es exclusiva del kamikaze.
    * Se encargará de seguir al robot enemigo escaneado
    * y tratará de acercar al kamikaze todo lo posible mientras dispara.
    *
     * @param e
    */
    
    public void chaseRobot(ScannedRobotEvent e){

        double gunTurnAmt = Utils.normalRelativeAngle(e.getBearing() + (getHeading() - getRadarHeading()));

        setTurnGunRight(gunTurnAmt);
        setTurnRight(e.getBearing());

        setAhead(e.getDistance());

        double absoluteBearing = getHeading() + e.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
        if (Math.abs(bearingFromGun) <= 3) {
            turnGunRight(bearingFromGun);
            if (getGunHeat() == 0) {
                fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
           }
        }
        else
            turnGunRight(bearingFromGun);

        return;
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
