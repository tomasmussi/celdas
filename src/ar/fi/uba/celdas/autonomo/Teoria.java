package ar.fi.uba.celdas.autonomo;

import java.util.Iterator;
import java.util.Random;

import ontology.Types.ACTIONS;

import org.json.JSONArray;
import org.json.JSONObject;

import tools.Vector2d;
import ar.fi.uba.celdas.Perception;

public class Teoria {

	private static final int MASK = (-1) >>> 1; // all ones except the sign bit

	private static ACTIONS[] POSIBLES = {ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT,
		ACTIONS.ACTION_UP, ACTIONS.ACTION_USE};
	private char[][] nivel;
	private Vector2d posicionAgente;
	
	private ACTIONS orientacion; // GUARDA CON ESTO

	private char[][] condicionSupuesta;
	private boolean tieneLlave;
	
	private ACTIONS accion;
	private char[][] efectoPredicho;
	
	// Esto es para usar como pesos en el grafo dirigido del planificador
	private int cantidadUtilizada;
	private int cantidadExito;
	
	
	private Integer id;

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

	
	public Teoria(Perception perception, Integer id) {
		this.nivel = perception.getLevel();
		this.posicionAgente = perception.getAgentPosition();
		this.orientacion = perception.getAgentOrientation();

		// Por ahora solo voy a ver lo que tengo inmediatamente alrededor
		condicionSupuesta = new char[3][3];
		this.tieneLlave = perception.tieneLlave();

		Random rn = new Random();
		int i = (rn.nextInt() & MASK) % POSIBLES.length;
		accion = POSIBLES[i];

		efectoPredicho = new char[3][3];

		this.cantidadUtilizada = 0;
		this.cantidadExito = 0;
		
		this.id = id;

		cargarCondicionSupuesta();
	}

	private Teoria(Integer id) {
		this.nivel = null;
		this.posicionAgente = null;
		this.orientacion = ACTIONS.ACTION_NIL;

		// Por ahora solo voy a ver lo que tengo inmediatamente alrededor
		condicionSupuesta = new char[3][3];
		efectoPredicho = new char[3][3];
		this.tieneLlave = false;
		this.cantidadUtilizada = 0;
		this.cantidadExito = 0;
		this.id = id;
	}
	
	public Teoria(JSONObject json) {
		this.nivel = null;
		this.posicionAgente = null;
		this.orientacion = ACTIONS.ACTION_NIL;
		this.tieneLlave = json.getBoolean("tieneLlave");
		this.cantidadUtilizada = json.getInt("cantidadUtilizada");
		this.cantidadExito = json.getInt("cantidadExito");
		this.accion = json.getEnum(ACTIONS.class, "accionTeoria");
		this.id = json.getInt("id");
		
		condicionSupuesta = new char[3][3];
		efectoPredicho = new char[3][3];
		loadCharArray(condicionSupuesta, json.getJSONArray("condicionSupuesta"));
		loadCharArray(efectoPredicho, json.getJSONArray("efectoPredicho"));	
	}
	
	private void loadCharArray(char[][] arr, JSONArray json) {
		int fila = 0;		
		Iterator<Object> it = json.iterator();		
		while (it.hasNext()) {
			int columna = 0;
			JSONArray row = (JSONArray) it.next();
			Iterator<Object> col = row.iterator();			
			while (col.hasNext()) {
				String c = (String) col.next();
				arr[fila][columna] = c.charAt(0);
				columna++;
			}
			fila++;
		}
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
	
	public void setEfecto(Perception perception) {
		if (posicionAgente == null) {
			return;
		}
		int lowerX = (int) (posicionAgente.x - 1);
		int upperX = (int) (posicionAgente.x + 1);
		int lowerY = (int) (posicionAgente.y - 1);
		int upperY = (int) (posicionAgente.y + 1);
		int posFila = 0;
		int posCol = 0;
		for (int fila = lowerX; fila <= upperX; fila++) {
			for (int col = lowerY; col <= upperY; col++) {
				efectoPredicho[posFila][posCol] = this.nivel[fila][col];
				posCol++;
			}
			posFila++;
			posCol = 0;
		}
	}

	public ACTIONS getAccionTeoria() {
		return accion;
	}

	private int compararArrays(char[][] teoria1, char[][] teoria2) {
		int dif = 0;
		for (int f = 0; f < teoria1.length; f++) {
			for (int c = 0; c < teoria1[f].length; c++) {
				if (teoria1[f][c] != teoria2[f][c]) {
					dif++;
				}
			}
		}
		return dif;
	}
	
	private int compararArraysEsquinas(char[][] teoria1, char[][] teoria2) {
		int dif = 0;
		for (int f = 0; f < teoria1.length; f++) {
			for (int c = 0; c < teoria1[f].length; c++) {
				if (f == 1 || c == 1) {
					if (teoria1[f][c] != teoria2[f][c]) {
						return 0;
					}
				}
				if (teoria1[f][c] != teoria2[f][c]) {
					dif++;
				}
			}
		}
		return dif;
	}
	
	/**
	 * Verifica diferencias en las condiciones supuestas
	 * Si hay un char diferente, o una tiene llave y la otra no, son condiciones distintas
	 * */
	private boolean mismasCondicionesSupuestas(Teoria otra) {
		int csDiferencias = compararArrays(this.condicionSupuesta, otra.condicionSupuesta);
		return csDiferencias == 0 && (this.tieneLlave == otra.tieneLlave);
	}
	
	public boolean distintosEfectos(Teoria otra) {
		return accion == otra.accion && mismasCondicionesSupuestas(otra) && !mismoEfectoPredicho(otra);
	}
	
	/**
	 * Verifica si hay diferencias en el efecto predicho de las teorias
	 * */
	private boolean mismoEfectoPredicho(Teoria otra) {
		int epDiferencias = compararArrays(this.efectoPredicho, otra.efectoPredicho);
		return epDiferencias == 0; 
	}
	
	public boolean mismasCondiciones(Teoria teoriaPrevia) {
		if (accion != teoriaPrevia.accion) {
			return false;
		}
		if (this.tieneLlave != teoriaPrevia.tieneLlave) {
			return false;
		}
		if (!mismasCondicionesSupuestas(teoriaPrevia)) {
			return false;
		}
		if (!mismoEfectoPredicho(teoriaPrevia)) {
			return false;
		}
		return true;
	}

	/**
	 * Una teoria es similar a otra si:
	 * 1) tienen misma accion y:
	 * 		a) tienen mismos efectos predichos
	 * 		o
	 * 		b) tienen mismas condiciones supuestas
	 * */
	public boolean esSimilar(Teoria otra) {
		if (this.accion != otra.accion) {
			return false;
		}
		if (this.tieneLlave != otra.tieneLlave) {
			// Por ahora no voy a generalizar con teorias que se diferencian en llave, son casos bastante distintos
			return false;
		}
		int csDiferencias = compararArraysEsquinas(this.condicionSupuesta, otra.condicionSupuesta);
		if (csDiferencias == 1 && mismoEfectoPredicho(otra)) {
			// Diferencia 1 bloque, son similares => heuristica EXCLUSION 
			return true;
		}
		int epSimilares = compararArraysEsquinas(this.efectoPredicho, otra.efectoPredicho);
		if (epSimilares == 1 && mismasCondicionesSupuestas(otra)) {
			// Diferencia 1 bloque, son similares => heuristica EXCLUSION 
			return true;
		}
		// Si tienen misma accion, pero no hubo match de cs o ep, no son similares
		return false;
	}


	public Teoria generalizarCon(Teoria teoriaIteracionAnterior, Integer nextId) {
		if (!this.esSimilar(teoriaIteracionAnterior)) {
			System.err.println("Se quiso generalizar con teorias que no son similares");
			return null;
		}
		Teoria mutante = new Teoria(nextId);
		mutante.accion = this.accion;
		mutante.accion = this.accion;
		mutante.tieneLlave = this.tieneLlave;
		mutante.cantidadExito = this.cantidadExito;
		mutante.cantidadUtilizada = this.cantidadUtilizada;
		mutante.orientacion = this.orientacion;

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
		boolean isNotCs = false;
		for (int f = 0; f < this.condicionSupuesta.length; f++) {
			for (int c = 0; c < this.condicionSupuesta[f].length; c++) {
				if (f == 1 || c == 1) {
					if (this.condicionSupuesta[f][c] != teoriaIteracionAnterior.condicionSupuesta[f][c]) {
						isNotCs = true;
					}
				}
				if (this.condicionSupuesta[f][c] != teoriaIteracionAnterior.condicionSupuesta[f][c] && !isNotCs) {
					mutante.condicionSupuesta[f][c] = '?';
					cs = true;
				}
			}
		}
		if (isNotCs) {
			for (int f = 0; f < this.condicionSupuesta.length; f++) {
				for (int c = 0; c < this.condicionSupuesta[f].length; c++) {
					mutante.condicionSupuesta[f][c] = this.condicionSupuesta[f][c];
				}
			}
		}
		if (cs) {
			return mutante;
		}
		for (int f = 0; f < this.efectoPredicho.length; f++) {
			for (int c = 0; c < this.efectoPredicho[f].length; c++) {
				if (f == 1 || c == 1) {
					continue;
				}
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

	public void reforzarUsos() {
		this.cantidadUtilizada++;
	}
	
	public void reforzarExitos() {
		this.cantidadExito++;
	}
	
	/**
	 * Evalua utilidad de esta teoria:
	 * 100 => tengo llave y entro a la puerta
	 * 90 => agarro llave
	 * 50 => mato arania
	 * 40 => escapo de arania (FALTA)
	 * 10 => utilidad por defecto
	 * 0 => muero
	 * */
	public int utilidad() {
		switch (this.accion) {
		case ACTION_DOWN:
			if (this.tieneLlave && condicionSupuesta[2][1] == 'g') {
				return 100;
			}
			if (!this.tieneLlave && condicionSupuesta[2][1] == '+') {
				return 90;
			}
			if (condicionSupuesta[2][1] == '2' || condicionSupuesta[2][1] == 's' || condicionSupuesta[2][1] == 'm') {
				return 0;
			}
			if (condicionSupuesta[0][1] == '2' || condicionSupuesta[0][1] == 's' || condicionSupuesta[0][1] == 'm') {
				return 40;
			}
			break;
		case ACTION_UP:
			if (this.tieneLlave && condicionSupuesta[0][1] == 'g') {
				return 100;
			}
			if (!this.tieneLlave && condicionSupuesta[0][1] == '+') {
				return 90;
			}
			if (condicionSupuesta[0][1] == '2' || condicionSupuesta[0][1] == 's' || condicionSupuesta[0][1] == 'm') {
				return 0;
			}
			if (condicionSupuesta[2][1] == '2' || condicionSupuesta[2][1] == 's' || condicionSupuesta[2][1] == 'm') {
				return 40;
			}
			break;
		case ACTION_LEFT:
			if (this.tieneLlave && condicionSupuesta[1][0] == 'g') {
				return 100;
			}
			if (!this.tieneLlave && condicionSupuesta[1][0] == '+') {
				return 90;
			}
			if (condicionSupuesta[1][0] == '2' || condicionSupuesta[1][0] == 's' || condicionSupuesta[1][0] == 'm') {
				return 0;
			}
			if (condicionSupuesta[1][2] == '2' || condicionSupuesta[1][2] == 's' || condicionSupuesta[1][2] == 'm') {
				return 40;
			}
			break;
		case ACTION_RIGHT:
			if (this.tieneLlave && condicionSupuesta[1][2] == 'g') {
				return 100;
			}
			if (!this.tieneLlave && condicionSupuesta[1][2] == '+') {
				return 90;
			}
			if (condicionSupuesta[1][2] == '2' || condicionSupuesta[1][2] == 's' || condicionSupuesta[1][2] == 'm') {
				return 0;
			}
			if (condicionSupuesta[1][0] == '2' || condicionSupuesta[1][0] == 's' || condicionSupuesta[1][0] == 'm') {
				return 40;
			}
			break;
		case ACTION_USE:
			switch (orientacion) {
			case ACTION_DOWN:
				if (condicionSupuesta[2][1] == '2' || condicionSupuesta[2][1] == 's' || condicionSupuesta[2][1] == 'm') {
					return 50;
				}
				break;
			case ACTION_UP:
				if (condicionSupuesta[0][1] == '2' || condicionSupuesta[0][1] == 's' || condicionSupuesta[0][1] == 'm') {
					return 50;
				}
				break;
			case ACTION_LEFT:
				if (condicionSupuesta[1][0] == '2' || condicionSupuesta[1][0] == 's' || condicionSupuesta[1][0] == 'm') {
					return 50;
				}
				break;
			case ACTION_RIGHT:
				if (condicionSupuesta[1][2] == '2' || condicionSupuesta[1][2] == 's' || condicionSupuesta[1][2] == 'm') {
					return 50;
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		return 10;
	}

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
	

	public char[][] getCondicionSupuesta() {
		return condicionSupuesta;
	}
	
	public char[][] getEfectoPredicho() {
		return efectoPredicho;
	}
	
	public boolean getTieneLlave() {
		return tieneLlave;
	}
	
	public int getCantidadUtilizada() {
		return cantidadUtilizada;
	}
	
	public int getCantidadExito() {
		return cantidadExito;
	}

	public void copiarUsos(Teoria teoria) {
		this.cantidadUtilizada = teoria.cantidadUtilizada;
	}

	/**
	 * this efecto == eslabon condicion supuesta
	 * */
	public boolean tieneComoEfecto(Teoria eslabon) {	
		for (int f = 0; f < this.condicionSupuesta.length; f++) {
			for (int c = 0; c < this.condicionSupuesta[f].length; c++) {
				if (condicionSupuesta[f][c] != '?' && eslabon.efectoPredicho[f][c] != '?') {
					if (condicionSupuesta[f][c] != eslabon.efectoPredicho[f][c]) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean siguientePaso(Teoria eslabon) {	
		for (int f = 0; f < this.efectoPredicho.length; f++) {
			for (int c = 0; c < this.efectoPredicho[f].length; c++) {
				if (this.efectoPredicho[f][c] != '?' && eslabon.condicionSupuesta[f][c] != '?') {
					if (this.efectoPredicho[f][c] != eslabon.condicionSupuesta[f][c]) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean tieneCondicionSupuesta(char[][] condicionActual, boolean tieneLlave2) {
		if (this.tieneLlave != tieneLlave2) {
			return false;
		}
		for (int f = 0; f < this.condicionSupuesta.length; f++) {
			for (int c = 0; c < this.condicionSupuesta[f].length; c++) {
				if (condicionSupuesta[f][c] != '?' && condicionActual[f][c] != '?') {
					if (condicionSupuesta[f][c] != condicionActual[f][c]) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public Integer getId() {
		return id;
	}
	
	public int cociente() {
		if (cantidadUtilizada == 0) {
			return 0;
		}
		return (100 * cantidadExito) / cantidadUtilizada;
	}

	public boolean efectoNulo(Perception perception) {
		if (this.accion == ACTIONS.ACTION_USE) {
			return false;
		}
		int lowerX = (int) (perception.getAgentPosition().x - 1);
		int upperX = (int) (perception.getAgentPosition().x + 1);
		int lowerY = (int) (perception.getAgentPosition().y - 1);
		int upperY = (int) (perception.getAgentPosition().y + 1);
		int posFila = 0;
		int posCol = 0;
		for (int fila = lowerX; fila <= upperX; fila++) {
			for (int col = lowerY; col <= upperY; col++) {
				if (condicionSupuesta[posFila][posCol] != perception.getLevel()[fila][col]) {
					return false;
				}
				posCol++;
			}
			posFila++;
			posCol = 0;
		}
		return true;
	}


}
