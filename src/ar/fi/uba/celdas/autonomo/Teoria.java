package ar.fi.uba.celdas.autonomo;

import java.util.Random;

import ontology.Types.ACTIONS;
import tools.Vector2d;

public class Teoria {

	private static final int MASK = (-1) >>> 1; // all ones except the sign bit

	private static ACTIONS[] POSIBLES = {ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT,
		ACTIONS.ACTION_UP, ACTIONS.ACTION_USE, ACTIONS.ACTION_NIL};
	private char[][] nivel;
	private Vector2d posicionAgente;

	private char[][] condicionSupuesta;
	private ACTIONS accion;
	private char[][] efectoPredicho;
	private int cantidadUtilizada;
	private int cantidadExito;

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
		?: no importa que hay
	 *
	 * */

	public Teoria(char[][] nivel, Vector2d posicionAgente) {
		this.nivel = nivel;
		this.posicionAgente = posicionAgente;

		// Por ahora solo voy a ver lo que tengo inmediatamente alrededor
		condicionSupuesta = new char[3][3];

		Random rn = new Random();
		int i = (rn.nextInt() & MASK) % POSIBLES.length;
		accion = POSIBLES[i];

		efectoPredicho = new char[3][3];

		this.cantidadUtilizada = 0;
		this.cantidadExito = 0;

		cargarCondicionSupuesta();
	}

	private void cargarCondicionSupuesta() {
		int lowerX = (int) (posicionAgente.x - 1);
		int upperX = (int) (posicionAgente.x + 1);
		int lowerY = (int) (posicionAgente.y - 1);
		int upperY = (int) (posicionAgente.y + 1);
		int posFila = 0;
		int posCol = 0;
		for (int fila = lowerX; fila <= upperX; fila++) {
			for (int col = lowerY; col <= upperY; col++) {
				condicionSupuesta[posFila][posCol] = this.nivel[fila][col];
				posCol++;
			}
			posFila++;
			posCol = 0;
		}
	}

	/**
	 * La nueva Teoria esta en el paso N
	 * This es la vieja teoria que se ejecuto en el paso N-1, tengo que ver que
	 * es lo que hizo la accion que ejecute y ver que efecto tuvo
	 * */
	public void setEfecto(Teoria nuevaTeoria) {
		for (int f = 0; f < condicionSupuesta.length; f++) {
			for (int c = 0; c < condicionSupuesta[f].length; c++) {
				this.efectoPredicho[f][c] = nuevaTeoria.condicionSupuesta[f][c];
			}
		}
	}

	public ACTIONS getAccionTeoria() {
		return accion;
	}



	public boolean esMasGenericaQue(Teoria otra) {
		int estaTeoria = 0;
		int otraTeoria = 0;
		for (int f = 0; f < condicionSupuesta.length; f++) {
			for (int c = 0; c < condicionSupuesta[f].length; c++) {
				estaTeoria += (condicionSupuesta[f][c] == '?') ? 1 : 0;
				otraTeoria += (otra.condicionSupuesta[f][c] == '?') ? 1 : 0;
			}
		}
		return estaTeoria > otraTeoria;
	}

	/**
	 * Falta determinar que es mas similar
	 * */
	public boolean esSimilar(Teoria otra) {
		int estaTeoria = 0;
		int otraTeoria = 0;
		for (int f = 0; f < efectoPredicho.length; f++) {
			for (int c = 0; c < efectoPredicho[f].length; c++) {
				estaTeoria += (efectoPredicho[f][c] == '?') ? 1 : 0;
				otraTeoria += (otra.efectoPredicho[f][c] == '?') ? 1 : 0;
			}
		}
		return estaTeoria < otraTeoria;
	}

	public boolean mismasCondiciones(Teoria teoriaPrevia) {
		for (int f = 0; f < condicionSupuesta.length; f++) {
			for (int c = 0; c < condicionSupuesta[f].length; c++) {
				if (condicionSupuesta[f][c] != teoriaPrevia.condicionSupuesta[f][c]) {
					return false;
				}
			}
		}
		for (int f = 0; f < efectoPredicho.length; f++) {
			for (int c = 0; c < efectoPredicho[f].length; c++) {
				if (efectoPredicho[f][c] != teoriaPrevia.efectoPredicho[f][c]) {
					return false;
				}
			}
		}
		if (accion != teoriaPrevia.accion) {
			return false;
		}
		return true;
	}

	public void reforzarTeoria() {
		this.cantidadUtilizada++;
		this.cantidadExito++;
	}
	/*
	public String toString(){
		StringBuilder sb = new StringBuilder("");
		if(nivel!=null){
			for(int i=0;i< nivel.length; i++){
				for(int j=0;j<  nivel[i].length; j++){
					sb.append(nivel[i][j]);
				}
				sb.append("\n");
			}
		}
		sb.append("\n");
		for(int i=0;i< condicionSupuesta.length; i++){
			for(int j=0;j<  condicionSupuesta[i].length; j++){
				sb.append(condicionSupuesta[i][j]);
			}
			sb.append("\n");
		}
		sb.append(accionToString());
		return sb.toString();
	}
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder("");
		for(int i=0;i< condicionSupuesta.length; i++){
			for(int j=0;j<  condicionSupuesta[i].length; j++){
				sb.append(condicionSupuesta[i][j]);
			}
			if (i == 1) {
				sb.append("\t\t");
				sb.append(accionToString());
				sb.append("\t\t");
			} else {
				sb.append("\t\t\t\t");
			}

			for (int j = 0; j < efectoPredicho.length; j++) {
				sb.append(efectoPredicho[i][j]);
			}
			sb.append("\n");
		}
		sb.append("\n");
		return sb.toString();
	}

	private String accionToString() {
		if (ACTIONS.ACTION_DOWN.equals(accion)) {
			return "DOWN";
		} else if (ACTIONS.ACTION_LEFT.equals(accion)) {
			return "LEFT";
		} else if (ACTIONS.ACTION_RIGHT.equals(accion)) {
			return "RIGT";
		} else if (ACTIONS.ACTION_UP.equals(accion)) {
			return "UPPP";
		} else if (ACTIONS.ACTION_USE.equals(accion)) {
			return "USEE";
		} else if (ACTIONS.ACTION_NIL.equals(accion)) {
			return "NOTH";
		}
		return "";
	}

}
