package ar.fi.uba.celdas.autonomo;

import java.util.ArrayList;
import java.util.List;

import ar.fi.uba.celdas.Perception;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class AutonomusAgent extends AbstractPlayer {

	private List<Teoria> teorias;
	private Teoria teoriaPrevia;

	public AutonomusAgent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		teorias = new ArrayList<Teoria>();
		teoriaPrevia = null;
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		Perception perception = new Perception(stateObs);
		Teoria teoria = new Teoria(perception.getLevel(), perception.getAgentPosition());
		if (teoriaPrevia != null) {
			teoriaPrevia.setEfecto(teoria);
			agregarTeoria();
		}

		System.out.println(perception.toString());
		System.out.println(teoria.toString());

		return ACTIONS.ACTION_RIGHT;
	}

	private void agregarTeoria() {
		for (Teoria teoria : teorias) {
			if (teoria.mismasCondiciones(teoriaPrevia)) {
				teoria.reforzarTeoria();
				return;
			}
		}
		teorias.add(teoriaPrevia);
	}

}
