package ar.fi.uba.celdas;
/**
 * @author  Juan Manuel Rodriguez
 * @date 21/10/2016
 * */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ontology.Types.ACTIONS;
import tools.Vector2d;
import tools.pathfinder.Node;
import tools.pathfinder.PathFinder;
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
		s: scorpion
		m: murcielago
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
	private PathFinder pathFinder;
	private Vector2d nextItem;
	private Vector2d keyItemPosition;
	private Vector2d doorItemPosition;
	private Vector2d someEnemyPosition;
	private Vector2d unknownObject;


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

		Set<Integer> obstacleTypes = new HashSet<Integer>();



		this.level = new char[levelHeight][levelWidth];
		for(int i=0;i< levelWidth; i++){
			for(int j=0;j< levelHeight; j++){
				observationList = (grid[i][j]);
				if(!observationList.isEmpty()){
					o = observationList.get(observationList.size()-1);
					String element =  o.category+""+o.itype;

					switch (element) {
					case "40": this.level[j][i] = 'w';
					obstacleTypes.add(o.itype);
					break;
					case "44": this.level[j][i] = '+';
					// nextItem = new Vector2d(j,i);
					keyItemPosition = new Vector2d(j,i);
					break;
					case "08":
					case "07": this.level[j][i] = 'A';
					agentPosition = new Vector2d(j,i);
					agentOrientation = new Vector2d(stateObs.getAvatarOrientation());
					// pathFinder = new PathFinder();
					//System.out.println("Pos: " + agentPosition);
					//System.out.println("Orient: " + stateObs.getAvatarOrientation());
					break;

					case "311": this.level[j][i] = '2';
					obstacleTypes.add(o.itype);
					someEnemyPosition = new Vector2d(j,i);
					break;
					case "312": this.level[j][i] = 's';
					obstacleTypes.add(o.itype);
					someEnemyPosition = new Vector2d(j,i);
					break;
					case "310": this.level[j][i] = 'm';
					obstacleTypes.add(o.itype);
					someEnemyPosition = new Vector2d(j,i);
					break;
					case "23": this.level[j][i] = 'g';
					doorItemPosition = new Vector2d(j,i);
					break;

					case "55": this.level[j][i] = 'X'; break;
					default: this.level[j][i] = '?';
					unknownObject = new Vector2d(j,i);
					//System.out.println("Que hay en: " + i + "," +j);
					//System.out.println(o.category + "," + o.itype);
					break;

					}
				}else{
					this.level[j][i] = '.';
				}
			}
		}
		if (keyItemPosition == null || agentPosition.equals(keyItemPosition)) {
			//System.out.println("door");
			nextItem = doorItemPosition;
		}
		if (keyItemPosition != null && !agentPosition.equals(keyItemPosition)) {
			//System.out.println("key");
			nextItem = keyItemPosition;
		}
		if (nextItem == null && doorItemPosition != null) {
			//System.out.println("door 2");
			nextItem = doorItemPosition;
		}
		if (nextItem == null) {
			//System.out.println("enemy");
			nextItem = someEnemyPosition;
		}
		if (nextItem == null) {
			//System.out.println("no pude encontrar nada :(");
			nextItem = agentPosition;
		}
		/*for (String key : tipos.keySet()) {
			System.out.println("Key " + key + " : " + tipos.get(key));
		}*/
		//System.out.println("\n\n");
		if (agentPosition == null) {
			/*System.out.println("Agent position en null!!!!");
			System.out.println(stateObs.getAvatarPosition());*/
			agentPosition = unknownObject;
			agentOrientation = new Vector2d(stateObs.getAvatarOrientation());
		}
		pathFinder = new PathFinder(new ArrayList<Integer>(obstacleTypes));
		pathFinder.run(stateObs);
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


	public boolean spiderIsRight() {
		// Right
		if (level[(int)agentPosition.x][(int)agentPosition.y + 1] == '2'
				|| level[(int)agentPosition.x][(int)agentPosition.y + 1] == 'm'
				|| level[(int)agentPosition.x][(int)agentPosition.y + 1] == 's'
				) {
			// System.out.println("Right");
			if (agentOrientation.x == 1) {
				agentIsFacingSpider = true;
			} else {
				agentIsFacingSpider = false;
			}
			return true;
		}
		return false;
	}

	public boolean spiderIsLeft() {
		// Left
		if (level[(int)agentPosition.x][(int)agentPosition.y - 1] == '2'
				|| level[(int)agentPosition.x][(int)agentPosition.y + 1] == 'm'
				|| level[(int)agentPosition.x][(int)agentPosition.y + 1] == 's') {
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

	public boolean spiderIsDown() {
		// Down
		if (level[(int)agentPosition.x + 1][(int)agentPosition.y] == '2'
				|| level[(int)agentPosition.x][(int)agentPosition.y + 1] == 'm'
				|| level[(int)agentPosition.x][(int)agentPosition.y + 1] == 's') {
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

	public boolean spiderIsUp() {
		// Up
		if (level[(int)agentPosition.x - 1][(int)agentPosition.y - 1] == '2'
				|| level[(int)agentPosition.x][(int)agentPosition.y + 1] == 'm'
				|| level[(int)agentPosition.x][(int)agentPosition.y + 1] == 's') {
			if (agentOrientation.y == -1) {
				agentIsFacingSpider = true;
			} else {
				agentIsFacingSpider = false;
			}
			//System.out.println("Down");
			return true;
		}
		return false;
	}

	public boolean canKillSpider() {
		if (agentIsFacingSpider) {
			// System.out.println("Facing Spider!!!");
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

	public ACTIONS getNextMove() {
		ArrayList<Node> path = pathFinder.getPath(new Vector2d(agentPosition.y, agentPosition.x), new Vector2d(nextItem.y, nextItem.x));
		Vector2d correctAgentPosition = new Vector2d(agentPosition.y, agentPosition.x);
		//System.out.println("Player: " + correctAgentPosition);
		if (path != null && !path.isEmpty()) {
			Node nextBox = path.get(0);
			//System.out.println("Next box: " + nextBox.position);
			if (nextBox.position.x != correctAgentPosition.x) {
				if (nextBox.position.x > correctAgentPosition.x) {
					//System.out.println("Go Right");
					return ACTIONS.ACTION_RIGHT;
				} else {
					//System.out.println("Go Left");
					return ACTIONS.ACTION_LEFT;
				}
			} else if (nextBox.position.y != correctAgentPosition.y) {
				if (nextBox.position.y > correctAgentPosition.y) {
					//System.out.println("Go Down");
					return ACTIONS.ACTION_DOWN;
				} else {
					//System.out.println("Go Up");
					return ACTIONS.ACTION_UP;
				}
			}
			// System.out.println(agentPosition);
		}
		return ACTIONS.ACTION_RIGHT;
	}

	public boolean shouldGoRight() {
		return getNextMove() == ACTIONS.ACTION_RIGHT;
	}

	public boolean shouldGoLeft() {
		return getNextMove() == ACTIONS.ACTION_LEFT;
	}

	public boolean shouldGoDown() {
		return getNextMove() == ACTIONS.ACTION_DOWN;
	}

	public boolean shouldGoUp() {
		return getNextMove() == ACTIONS.ACTION_UP;
	}

	public Vector2d getAgentPosition() {
		return agentPosition;
	}

}