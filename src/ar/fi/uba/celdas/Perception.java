package ar.fi.uba.celdas;
/**
 * @author  Juan Manuel Rodriguez
 * @date 21/10/2016
 * */

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;

public class Perception {

	
	/*Legend:
	 *  w: WALL
		A: Agent A
		+: llave
		X: espada
		2: arania
		g: puerta
		.: empty space
	 * 
	 * */
	private char[][] level = null;
	private int sizeWorldWidthInPixels;
	private int sizeWorldHeightInPixels;
	private int levelWidth;
	private int levelHeight;
	private int spriteSizeWidthInPixels;
	private int spriteSizeHeightInPixels;

	
	public Perception(StateObservation stateObs){
		 	ArrayList<Observation>[][] grid = stateObs.getObservationGrid();
	        ArrayList<Observation> observationList;
	        Observation o;
	        
	        
	        this.sizeWorldWidthInPixels= stateObs.getWorldDimension().width;
	        this.sizeWorldHeightInPixels= stateObs.getWorldDimension().height;
	        this.levelWidth = stateObs.getObservationGrid().length;
	        this.levelHeight = stateObs.getObservationGrid()[0].length;
	        this.spriteSizeWidthInPixels =  stateObs.getWorldDimension().width / levelWidth;
	        this.spriteSizeHeightInPixels =  stateObs.getWorldDimension().height / levelHeight;

	        this.level = new char[levelHeight][levelWidth];
	        for(int i=0;i< levelWidth; i++){
	        	for(int j=0;j< levelHeight; j++){
	        		observationList = (grid[i][j]);	        		
	        		if(!observationList.isEmpty()){
	        			o = observationList.get(observationList.size()-1);
	        			String element =  o.category+""+o.itype;
	        			switch (element) {
							case "40": this.level[j][i] = 'w'; break;
							case "44": this.level[j][i] = '+'; break;
							case "07": this.level[j][i] = 'A'; break;
							case "311": this.level[j][i] = '2'; break;
							case "23": this.level[j][i] = 'g'; break;
							case "55": this.level[j][i] = 'X'; break;
							default: this.level[j][i] = '?'; break;
								
						}
	        		}else{
	        			 this.level[j][i] = '.';
	        		}
	        	}      	
	        }
	}
	
	public char getAt(int i, int j){
		return level[i][j];
	}
	
	
	public char[][] getLevel(){
		return level;
	}
	
	public int getSizeWorldWidthInPixels() {
		return sizeWorldWidthInPixels;
	}
	
	public int getSizeWorldHeightInPixels() {
		return sizeWorldHeightInPixels;
	}

	
	public int getLevelWidth() {
		return levelWidth;
	}

	
	public int getLevelHeight() {
		return levelHeight;
	}

	public int getSpriteSizeWidthInPixels() {
		return spriteSizeWidthInPixels;
	}

	
	public int getSpriteSizeHeightInPixels() {
		return spriteSizeHeightInPixels;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder("");
		if(level!=null){
			 for(int i=0;i< level.length; i++){
		        	for(int j=0;j<  level[i].length; j++){
		        		sb.append(level[i][j]);
		        	}
		        	sb.append("\n");
			 }
		}
		return sb.toString();
	}
	
}