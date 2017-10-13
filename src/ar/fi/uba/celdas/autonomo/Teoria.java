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

	private Teoria() {
		this.nivel = null;
		this.posicionAgente = null;

		// Por ahora solo voy a ver lo que tengo inmediatamente alrededor
		condicionSupuesta = new char[3][3];
		efectoPredicho = new char[3][3];
		this.cantidadUtilizada = 0;
		this.cantidadExito = 0;
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
	 * Una teoria es similar a otra si:
	 * 1) tienen misma accion y:
	 * 		a) tienen mismos efectos predichos
	 * 		o
	 * 		b) tienen mismas condiciones supuestas
	 *
	 * */
	public boolean esSimilar(Teoria otra) {
		if (this.accion != otra.accion) {
			return false;
		}
		int csDiferencias = 0;
		for (int f = 0; f < condicionSupuesta.length; f++) {
			for (int c = 0; c < condicionSupuesta[f].length; c++) {
				if (this.condicionSupuesta[f][c] != otra.condicionSupuesta[f][c]) {
					csDiferencias++;
				}
			}
		}
		if (csDiferencias == 1) {
			// Si se diferencian en solo 1 bloque de los aledanios, son similares y
			// se puede aplicar heuristica EXCLUSION
			return true;
		}
		int epSimilares = 0;
		for (int f = 0; f < efectoPredicho.length; f++) {
			for (int c = 0; c < efectoPredicho[f].length; c++) {
				if (this.efectoPredicho[f][c] != otra.efectoPredicho[f][c]) {
					epSimilares++;
				}
			}
		}
		if (epSimilares == 1) {
			// Si se diferencian en solo 1 bloque de los aledanios, son similares y
			// se puede aplicar heuristica EXCLUSION
			return true;
		}
		// Si tienen misma accion, pero no hubo match de cs o ep, no son similares
		return false;
	}


	public Teoria generalizarCon(Teoria teoriaIteracionAnterior) {
		if (!this.esSimilar(teoriaIteracionAnterior)) {
			System.err.println("Se quizo generalizar con teorias que no son similares");
			return null;
		}
		Teoria mutante = new Teoria();
		mutante.accion = this.accion;

		for (int f = 0; f < this.condicionSupuesta.length; f++) {
			for (int c = 0; c < this.condicionSupuesta[f].length; c++) {
				// Copio condiciones supuestas y efectos predichos
				mutante.condicionSupuesta[f][c] = this.condicionSupuesta[f][c];
				mutante.efectoPredicho[f][c] = this.efectoPredicho[f][c];
			}
		}
		// Se que se diferencian en CS o EP
		boolean cs = false;
		boolean ep = false;
		for (int f = 0; f < this.condicionSupuesta.length; f++) {
			for (int c = 0; c < this.condicionSupuesta[f].length; c++) {
				if (this.condicionSupuesta[f][c] != teoriaIteracionAnterior.condicionSupuesta[f][c]) {
					mutante.condicionSupuesta[f][c] = '?';
					cs = true;
				}
			}
		}
		for (int f = 0; f < this.efectoPredicho.length; f++) {
			for (int c = 0; c < this.efectoPredicho[f].length; c++) {
				if (this.efectoPredicho[f][c] != teoriaIteracionAnterior.efectoPredicho[f][c]) {
					mutante.efectoPredicho[f][c] = '?';
					ep = true;
				}
			}
		}
		if (cs && ep) {
			System.err.println("Se modifico el efecto predicho y la condicion supuesta");
		}

		return mutante;

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
