package ar.fi.uba.celdas.autonomo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import ar.fi.uba.celdas.Perception;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class AutonomusAgent extends AbstractPlayer {

	private List<Teoria> teorias;
	private Teoria teoriaIteracionAnterior;
	private Planificador planTransitorio;
	private Integer nextId;
	private int accionNula;
	private static final int MAX_INTENTOS = 2;
	private static final int CUPOS = 1500;

	public AutonomusAgent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		teorias = new ArrayList<Teoria>();
		teoriaIteracionAnterior = null;
		nextId = Integer.valueOf(1); 
		leerTeorias();
		accionNula = 0;
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		Perception perception = new Perception(stateObs);
		if (teoriaIteracionAnterior != null && teoriaIteracionAnterior.efectoNulo(perception) && accionNula < MAX_INTENTOS) {
			// Si accion nula vale menos de 2 es tolerable... sino es porque estoy encajonado contra una pared
			accionNula++;
			return teoriaIteracionAnterior.getAccionTeoria();
		}
		boolean superoIntentos = accionNula == MAX_INTENTOS;
		accionNula = 0;
		Teoria teoriaLocal = new Teoria(perception, nextId);
		nextId++;
		if (teoriaIteracionAnterior != null) {
			teoriaIteracionAnterior.setEfecto(teoriaLocal);
			agregarTeoria(superoIntentos);
		}
		teoriaIteracionAnterior = teoriaLocal;

		// Planificar
		if (planTransitorio != null) {
			if (planTransitorio.todaviaSirve(teoriaLocal)) {
				teoriaIteracionAnterior = planTransitorio.dameTeoria();
				return teoriaIteracionAnterior.getAccionTeoria();				
			} else {
				planTransitorio = null;
			}		
		}

		Planificador plan = new Planificador(teorias, teoriaLocal);
		if (plan.hayPlan()) {
			teoriaIteracionAnterior = plan.dameTeoria();
			planTransitorio = plan;
			return teoriaIteracionAnterior.getAccionTeoria();
		} else if (plan.hayTeoriaUtil()) {
			return plan.dameAccionTeoriaUtil();
		}
		return teoriaLocal.getAccionTeoria();
	}

	private void agregarTeoria(boolean superoIntentos) {
		teoriaIteracionAnterior.reforzarExitos(); // No mori al usar la teoria
		teoriaIteracionAnterior.reforzarUsos(); // Use la teoria
		boolean agregarTeoriaNueva = true;
		// Si existe teoria igual a la local, reforzar teoria
		for (Teoria teoria : teorias) {
			if (teoria.mismasCondiciones(teoriaIteracionAnterior)) {
				if (!superoIntentos) {
					teoria.reforzarExitos();					
				}				
				teoria.reforzarUsos();
				agregarTeoriaNueva = false;
				break; // No deberia encontrar mas teorias iguales, deberian ser unicas
			}
		}
		Teoria teoriaMutante = null;
		int cont = 0;
		int index = -1;
		for (Teoria teoria : teorias) {
			// Busco generalizar
			if (teoriaMutante == null) {
				if (!teoria.mismasCondiciones(teoriaIteracionAnterior) && teoria.esSimilar(teoriaIteracionAnterior)) {
					// Es similar, pero no la misma
					teoriaMutante = teoria.generalizarCon(teoriaIteracionAnterior, nextId);
					index = cont;
				}
			}			
			// Busco mismas condiciones supuestas y accion, pero efectos predichos distintos
			if (teoria.distintosEfectos(teoriaIteracionAnterior)) {
				teoria.reforzarUsos();
				teoriaIteracionAnterior.copiarUsos(teoria);
			}
			cont++;
		}
		boolean doAdd = false;
		if (teoriaMutante != null) {
			/*
			for (Teoria teoria : teorias) {
				if (teoriaMutante.mismasCondiciones(teoria)) {
					doAdd = true;
					break;
				}
			}*/
			// TODO: DESCOMENTAR CUANDO SE AGREGUEN TEORIAS MUTANTES
			nextId++;
			teorias.add(teoriaMutante);
			teorias.remove(index);
			
		} else if (agregarTeoriaNueva) {
			teorias.add(teoriaIteracionAnterior);
		}
		if (agregarTeoriaNueva && !doAdd) {
			
		}
	}
	
	@Override
	public void result(StateObservation stateObs, ElapsedCpuTimer elapsedCpuTimer) {
		// stateObs.getGameWinner()
		// stateObs.getGameScore()
		Perception perception = new Perception(stateObs, true);
		if (teoriaIteracionAnterior != null) {
			teoriaIteracionAnterior.setEfecto(perception);
			teoriaIteracionAnterior.reforzarUsos();
		}
		persistirTeorias();
	}
	
	private void leerTeorias() {
		this.teorias = ParserTeorias.leerTeorias();
		if (!teorias.isEmpty()) {
			nextId = teorias.get(teorias.size() - 1).getId() + 1;			
		}
		System.out.println("Teorias leidas: " + teorias.size());		
	}
	
	private void persistirTeorias() {
		if (teorias.size() <= CUPOS) {
			ParserTeorias.persistirTeorias(teorias);
			return;
		}
		List<Teoria> teoriasPersistir = new ArrayList<Teoria>();
		PriorityQueue<Teoria> colaTeorias = new PriorityQueue<Teoria>(new Comparator<Teoria>() {
			@Override
			public int compare(Teoria o1, Teoria o2) {
				return o2.cociente() - o1.cociente();
			}
		});
		for (Teoria teoria : teorias) {
			if (teoria.utilidad() >= 40) {
				teoriasPersistir.add(teoria);
			} else {
				colaTeorias.add(teoria);
			}
		}
		while (!colaTeorias.isEmpty() && teoriasPersistir.size() < CUPOS) {
			Teoria t = colaTeorias.poll();
			teoriasPersistir.add(t);
		}
		ParserTeorias.persistirTeorias(teoriasPersistir);		
	}


}
