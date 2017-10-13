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

	public AutonomusAgent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		teorias = new ArrayList<Teoria>();
		teoriaIteracionAnterior = null;
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		Perception perception = new Perception(stateObs);
		Teoria teoriaLocal = new Teoria(perception.getLevel(), perception.getAgentPosition());
		if (teoriaIteracionAnterior != null) {
			teoriaIteracionAnterior.setEfecto(teoriaLocal);
			agregarTeoria();
		}
		if (teoriaIteracionAnterior != null) {
			System.out.println(teoriaIteracionAnterior.toString());
		}
		teoriaIteracionAnterior = teoriaLocal;


		return teoriaLocal.getAccionTeoria();
	}

	private void agregarTeoria() {
		// Si existe teoria igual a la local, reforzar teoria
		for (Teoria teoria : teorias) {
			if (teoria.mismasCondiciones(teoriaIteracionAnterior)) {
				teoria.reforzarTeoria();
				System.out.println("YA HABIA TEORIA COMO LA ACTUAL: " + teoria.toString());
				return;
			}
		}
		// Si no existe teoria como la local, verificar si hay teoria similar
		Teoria teoriaMutante = null;
		for (Teoria teoria : teorias) {
			if (teoria.esSimilar(teoriaIteracionAnterior)) {
				teoriaMutante = teoria.generalizarCon(teoriaIteracionAnterior);
				System.out.println("NUEVA TEORIA MAS GENERALIZADA" + teoria.toString());
				// TODO: No estoy seguro si deberia cortar aca o deberia seguir generalizando
				break;
			}
		}
		if (teoriaMutante != null) {
			System.out.println("Agregando a la lista...");
			teorias.add(teoriaMutante);
		}
		teorias.add(teoriaIteracionAnterior);

	}


}
