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
public class CornerRobot2 extends TeamRobot {
    private static final Map<String, Point2D.Double> teamPositions = new HashMap<>();
    Point2D.Double[] Esquinas = new Point2D.Double[4];
    int indexOfEsquina = -1;
    enum EstadosCorner {irEsquina, Patrulla};
    enum EstadosKamikaze {scan, chase};
    EstadosKamikaze kamikazeState;
    EstadosCorner cornerState;
    double oldEnemyHeading = 0;
    
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
        setRadarColor(Color.BLUE);
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
            setRadarColor(Color.WHITE);
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
                    break;
                case Patrulla:
                    setAdjustGunForRobotTurn(true);
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
        return posX && posY;
    }
    
    /**
     * La funcion devuelve el índice a los robos más cercanos a las esquinas
     * y el robot que se quede sin indice se convertirá en el kamikaze
     *
     * @return esquina
    */
    public int calculaEsquinaCercana(){
        for(int i = 0; i < 3; ++i){
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
     * Permite que el robot situado en su esquina tenga un movimiento unicamente cuando
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
    }
    
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
    * Se encargará de perseguir al robot enemigo escaneado
    * mientras le dispara.
    *
     * @param e
    */
    
    public void chaseRobot(ScannedRobotEvent e){

        double bulletPower = Math.min(3.0,getEnergy());
        double myX = getX();
        double myY = getY();
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        //opertura entre el canyo i el radar
        double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
        double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        double enemyVelocity = e.getVelocity();
        oldEnemyHeading = enemyHeading;


        double deltaTime = 0;
        double battleFieldHeight = getBattleFieldHeight(), 
        battleFieldWidth = getBattleFieldWidth();
        double predictedX = enemyX, predictedY = enemyY;
        while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
            Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
                predictedX += Math.sin(enemyHeading) * enemyVelocity;
                predictedY += Math.cos(enemyHeading) * enemyVelocity;
                enemyHeading += enemyHeadingChange;
                if(	predictedX < 18.0 
                        || predictedY < 18.0
                        || predictedX > battleFieldWidth - 18.0
                        || predictedY > battleFieldHeight - 18.0){

                        predictedX = Math.min(Math.max(18.0, predictedX), 
                            battleFieldWidth - 18.0);	
                        predictedY = Math.min(Math.max(18.0, predictedY), 
                            battleFieldHeight - 18.0);
                        break;
                }
        }
        double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));

        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));

         if (e.getDistance() > 160){
            //pre: Si el tanc enemic esta a menys que 160px
            //post: Disparam amb una potencia 0, girem el tanc en direccio del radar (mov. horari)
            //post: Movem el tanc 100 px mes aprop del enemic
            fire(2);
            setTurnRightRadians(getGunHeadingRadians() - getHeadingRadians());
            setAhead((e.getDistance() + 100));
        }else{
            //pre: donat el cas que estem a menys o a 160px
            //post: disparam amb mes intensitat i girem al voltant del altre tanc apuntant amb el canyo
            //* i el radar 90º cap al enemic (mov. horari)
            //post: llavors retrocedim la distancia fins a l'enemic + 100px
            fire(5);
            setTurnRightRadians(getGunHeadingRadians() - getHeadingRadians() + 1.5);
            setBack((e.getDistance() + 100));
        }
    }
    
    /**
     * Función exclusiva del kamikaze,
     * en caso de recibir una bala, el tanque modificará su trayectoria ligeramente
     * @param event 
     */
    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        if(indexOfEsquina == -1){
            setTurnLeftRadians(getGunHeadingRadians() - getHeadingRadians());
            setBack((100));
        }
    }
    
    /**
     * Función exclusica de kamikaze,
     * en caso de impacto con un robot, el tanque modificará su trayectoria ligeramente
     * @param event 
     */
    @Override
    public void onHitRobot(HitRobotEvent event){
        if(indexOfEsquina == -1){
            back(50);
            turnRight(45);
        }
    }
    
    /**
     * Función exclusica de kamikaze,
     * en caso de impacto con una pared, el tanque modificará su trayectoria ligeramente
     * @param event 
     */
    @Override
    public void onHitWall(HitWallEvent event){
        if(indexOfEsquina == -1){
            back(50);
            turnRight(45);
        }
    }
     
}
