import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Random;
import robocode.*;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Walter
 */
public class robotprueba1 extends AdvancedRobot{
    double oldEnemyHeading = 0;
    @Override
     /**
    *Funcio principal aquest es la funcio que executa el constantment si no
    *hi ha cap event
    * <p>
    * La funcio consisteix en primerament aplicar alguns param al tanc (color,adjust,etc)
    * i seguidament tenim el bucle infinit que executam si no hi ha cap event
    * on tenim el radar constanment girant a la dreta
    */
    public void run() {
        // -- Customitzar el Tanc
        setColors(Color.black, Color.black, Color.red, Color.red, Color.red); // Creepy

        setAdjustGunForRobotTurn(true);
        //El canyo gira independentment de la direccio que el vehicle
       
        while(true){
            //post: gira el radar a la dreta quan no esta escanejant cap enemic,
            //fins a trobar un i saltar a la funcio onScannedRobot
            setTurnGunRightRadians(Double.POSITIVE_INFINITY);
            execute();
        }
    }
   
    /**
    *Aquesta funcio es cridad quan el radar detecta un enemic
    *<p>
    * La funcio consisteix en disparar al enemic amb una potencia determinada
    * segons la proximitat d'aquest.
    *
    */
    public void onScannedRobot(ScannedRobotEvent e) {
                
        
        
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
        double theta = Utils.normalAbsoluteAngle(Math.atan2(
            predictedX - getX(), predictedY - getY()));

        setTurnRadarRightRadians(Utils.normalRelativeAngle(
            absoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(
            theta - getGunHeadingRadians()));
        
         if (e.getDistance() > 160){
            //pre: Si el tanc enemic esta a menys que 160px
            //post: Disparam amb una potencia 0, girem el tanc en direccio del radar (mov. horari)
            //post: Movem el tanc 100 px mes aprop del enemic
            disparaMulticol(2);
            setTurnRightRadians(getGunHeadingRadians() - getHeadingRadians());
            setAhead((e.getDistance() + 100));
        }else{
            //pre: donat el cas que estem a menys o a 160px
            //post: disparam amb mes intensitat i girem al voltant del altre tanc apuntant amb el canyo
            //* i el radar 90º cap al enemic (mov. horari)
            //post: llavors retrocedim la distancia fins a l'enemic + 100px
            disparaMulticol(5);
            setTurnRightRadians(getGunHeadingRadians() - getHeadingRadians() + 1.5);
            setBack((e.getDistance() + 100));
        }
            
      
    }
    
    
    
    public void onHitByBullet(HitByBulletEvent e) { 
        setTurnLeftRadians(getGunHeadingRadians() - getHeadingRadians());
        setBack((100));
    }   
    
    /**
    *Aquesta funcio es cridad quan volem crear un dispar multicolor
    *<p>
    *
    * La funcio consisteix en crear un color random i despres
    * utilitzar el metode setBulletColor per modificar el color de la bala
    * La potencia del dispar per l'unic parametre de la funcio
    * 
    * @param n potencia de dispar del robot
    */
    public void disparaMulticol(int n){
       
        Random rand = new Random();

        //generem les diferents parts del color de manera random
       
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();

        Color randomColor = new Color(r, g, b);

        setBulletColor(randomColor);

        fire(n);
    }
    
    /**
    *En el moment que l'event HitWall s'activa cridem aquesta funcio
    *<p>
    *Al xocar contra una paret en movem primerament endavant,
    *si encara aixi seguim en onHitWall anem endarrere
    */
    public void onHitWall(HitWallEvent e){
        setAhead(50);
        setBack(50);
    }
    
    /**
    * En el moment que l'event WinEvent s'activa cridem aquesta funcio
    * <p>
    * Al guanyar el tanc canvia totalment al color negre
    */
    public void onWin(WinEvent e) {
        setColors(Color.black, Color.black, Color.black, Color.black, Color.black);
    }
}
