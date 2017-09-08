package ar.fi.uba.celdas;
import java.util.ArrayList;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

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
	    //System.out.println(perception);
	    
	    
		ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        int index = (int)(Math.random() * actions.size());
        return  actions.get(index);
	}
}

