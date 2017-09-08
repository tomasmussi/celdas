package ar.fi.uba.celdas;
/**
 * @author  Juan Manuel Rodriguez
 * @date 21/10/2016
 * */

import java.util.ArrayList;

import ontology.Types.ACTIONS;
import tools.Vector2d;
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
	private Vector2d agentPosition;
	private Vector2d agentOrientation;
	private boolean agentIsFacingSpider;

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
					case "07": this.level[j][i] = 'A';
					agentPosition = new Vector2d(j,i);
					agentOrientation = new Vector2d(stateObs.getAvatarOrientation());
					System.out.println("Pos: " + agentPosition);
					System.out.println("Orient: " + stateObs.getAvatarOrientation());
					break;
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

	/**
	 * Evaluates if a Spider is in the next 4 blocks: Up, Down, Left or Right
	 * Diagonals dont count since you cant attack in diagonal
	 * */
	public boolean isSpiderNear() {
		return spiderIsRight() || spiderIsLeft() || spiderIsDown() || spiderIsUp();

		/*System.out.println(agentPosition);
			System.out.println(level[(int)agentPosition.x][(int)agentPosition.y]);
			System.out.println(level[(int)agentPosition.x + 1][(int)agentPosition.y]);*/
	}

	private boolean spiderIsRight() {
		// Right
		if (level[(int)agentPosition.x][(int)agentPosition.y + 1] == '2') {
			System.out.println("Right");
			if (agentOrientation.x == 1) {
				agentIsFacingSpider = true;
			} else {
				agentIsFacingSpider = false;
			}
			return true;
		}
		return false;
	}

	private boolean spiderIsLeft() {
		// Left
		if (level[(int)agentPosition.x][(int)agentPosition.y - 1] == '2') {
			if (agentOrientation.x == -1) {
				agentIsFacingSpider = true;
			} else {
				agentIsFacingSpider = false;
			}
			System.out.println("Left");
			return true;
		}
		return false;
	}

	private boolean spiderIsDown() {
		// Down
		if (level[(int)agentPosition.x + 1][(int)agentPosition.y] == '2') {
			if (agentOrientation.y == 1) {
				agentIsFacingSpider = true;
			} else {
				agentIsFacingSpider = false;
			}
			System.out.println("Down");
			return true;
		}
		return false;
	}

	private boolean spiderIsUp() {
		// Up
		if (level[(int)agentPosition.x - 1][(int)agentPosition.y - 1] == '2') {
			if (agentOrientation.y == -1) {
				agentIsFacingSpider = true;
			} else {
				agentIsFacingSpider = false;
			}
			System.out.println("Down");
			return true;
		}
		return false;
	}

	public boolean canKillSpider() {
		if (agentIsFacingSpider) {
			System.out.println("Facing Spider!!!");
		}
		return agentIsFacingSpider;
	}

	public ACTIONS faceSpider() {
		if (spiderIsDown()){
			return ACTIONS.ACTION_DOWN;
		}
		if (spiderIsLeft()){
			return ACTIONS.ACTION_LEFT;
		}
		if (spiderIsRight()) {
			return ACTIONS.ACTION_RIGHT;
		}
		if (spiderIsUp()) {
			return ACTIONS.ACTION_UP;
		}
		return ACTIONS.ACTION_USE;
	}

}