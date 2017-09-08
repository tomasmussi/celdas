package ar.fi.uba.celdas;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class RulesAgent extends AbstractPlayer{


	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 */
	public RulesAgent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
	}

	/**
	 * return ACTION_NIL on every call to simulate doNothing player
	 * @param stateObs Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return 	ACTION_NIL all the time
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

		Perception perception = new Perception(stateObs);
		System.out.println(perception);
		if (perception.isSpiderNear()) {
			System.out.println("Araña cerca!!!!");
			if (perception.canKillSpider()) {
				return ACTIONS.ACTION_USE;
			} else {
				return perception.faceSpider();
			}
		}
		/*ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
		System.out.println(actions);
		int index = (int)(Math.random() * actions.size());
		return  actions.get(index);
		 */
		return ACTIONS.ACTION_RIGHT;
	}
}

