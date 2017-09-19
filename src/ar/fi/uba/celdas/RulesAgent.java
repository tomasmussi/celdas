package ar.fi.uba.celdas;
import java.util.Iterator;
import java.util.List;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class RulesAgent extends AbstractPlayer{

	private List<Rule> rules;

	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 */
	public RulesAgent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		RulesParser parser = new RulesParser();
		rules = parser.getRules();
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
		// System.out.println(perception);
		Iterator<Rule> it = rules.iterator();
		while (it.hasNext()) {
			Rule rule = it.next();
			// System.out.println(rule.toString());
			if (rule.isTrue(perception)) {
				return rule.action();
			}
		}
		return ACTIONS.ACTION_RIGHT;
		/*
		if (perception.isSpiderNear()) {
			// System.out.println("Araña cerca!!!!");
			if (perception.canKillSpider()) {
				return ACTIONS.ACTION_USE;
			} else {
				return perception.faceSpider();
			}
		}
		return perception.getNextMove();
		 */
	}
}

