package ar.fi.uba.celdas.autonomo;

import java.util.ArrayList;
import java.util.List;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import ar.fi.uba.celdas.Perception;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class AutonomusAgent extends AbstractPlayer {

	private List<Teoria> teorias;
	private Teoria teoriaIteracionAnterior;
	private Planificador planTransitorio;

	public AutonomusAgent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		teorias = new ArrayList<Teoria>();
		teoriaIteracionAnterior = null;
		leerTeorias();
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		Perception perception = new Perception(stateObs);
		Teoria teoriaLocal = new Teoria(perception);
		if (teoriaIteracionAnterior != null) {
			teoriaIteracionAnterior.setEfecto(teoriaLocal);
			agregarTeoria();
		}
		teoriaIteracionAnterior = teoriaLocal;

		// Planificar
		if (planTransitorio != null) {
			if (planTransitorio.todaviaSirve(teoriaLocal)) {
				return planTransitorio.dameAccionPlan();				
			} else {
				planTransitorio = null;
			}			
		}
		Planificador plan = new Planificador(teorias, teoriaIteracionAnterior.getCondicionSupuesta(), teoriaIteracionAnterior.getTieneLlave(), teoriaIteracionAnterior.utilidad());
		if (plan.hayPlan()) {
			ACTIONS accion = plan.dameAccionPlan();
			planTransitorio = plan;
			return accion;
		} else if (plan.hayTeoriaUtil()) {
			return plan.dameAccionTeoriaUtil();
		}
		return teoriaLocal.getAccionTeoria();
	}

	private void agregarTeoria() {
		teoriaIteracionAnterior.reforzarExitos(); // No mori al usar la teoria
		teoriaIteracionAnterior.reforzarUsos(); // Use la teoria
		boolean agregarTeoriaNueva = true;
		// Si existe teoria igual a la local, reforzar teoria
		for (Teoria teoria : teorias) {
			if (teoria.mismasCondiciones(teoriaIteracionAnterior)) {
				teoria.reforzarExitos();
				teoria.reforzarUsos();
				agregarTeoriaNueva = false;
				break; // No deberia encontrar mas teorias iguales, deberian ser unicas
			}
		}
		Teoria teoriaMutante = null;
		for (Teoria teoria : teorias) {
			// Busco generalizar
			if (!teoria.mismasCondiciones(teoriaIteracionAnterior) && teoria.esSimilar(teoriaIteracionAnterior)) {
				// Es similar, pero no la misma
				teoriaMutante = teoria.generalizarCon(teoriaIteracionAnterior);
			}
			// Busco mismas condiciones supuestas y accion, pero efectos predichos distintos
			if (teoria.distintosEfectos(teoriaIteracionAnterior)) {
				teoria.reforzarUsos();
				teoriaIteracionAnterior.copiarUsos(teoria);
			}
		}
		if (teoriaMutante != null) {
			teorias.add(teoriaMutante);
		}
		if (agregarTeoriaNueva) {
			teorias.add(teoriaIteracionAnterior);
		}
	}
	
	@Override
	public void result(StateObservation stateObs, ElapsedCpuTimer elapsedCpuTimer) {
		Perception perception = new Perception(stateObs, true);
		if (teoriaIteracionAnterior != null) {
			teoriaIteracionAnterior.setEfecto(perception);
			teoriaIteracionAnterior.reforzarUsos();
		}
		persistirTeorias();
	}
	
	private void leerTeorias() {
		this.teorias = ParserTeorias.leerTeorias();
	}
	
	private void persistirTeorias() {
		ParserTeorias.persistirTeorias(teorias);		
	}


}
