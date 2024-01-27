/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris.own;


/**
 *
 * @author Jan
 */
public class Settings {
    public static final int SIZE = 30;
    public static final int WIDTH = 12;
    public static final int HEIGHT = 20;
    public static int score = 0;
    public static int HIGTH_SCORE; //cte ze souboru HightScore.txt
    
    //public static boolean rightAndLeftDisabled = false; // nastavi se na true po padu ctverce a na false pri dalsim vytvoreni
    
    public static boolean konecHry = false;
    public static boolean pokracovani = false;
    public static Controller controller;
    public static Shape [] vsechnyTvary = {Shape.DOT,Shape.I,Shape.J,Shape.L,Shape.S, Shape.T,Shape.Z};
}
