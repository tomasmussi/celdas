package ar.fi.uba.celdas;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ontology.Types.ACTIONS;

public class Rule {

	private String condition;
	private List<String> multipleConditions;
	private String rule;
	private Perception perception;


	public Rule(String condicion, String regla) {
		multipleConditions = new ArrayList<String>();
		this.condition = condicion;
		this.rule = regla;
		if (this.condition.contains(" and ")) {
			String[] conditions = this.condition.split(" and ");
			for (int i = 0 ; i < conditions.length; i++) {
				multipleConditions.add(conditions[i]);
			}
		}
	}

	public boolean isTrue(Perception perception) {
		this.perception = perception;
		if (multipleConditions.isEmpty()) {
			try {
				Method method = perception.getClass().getMethod(condition);
				return ((Boolean) method.invoke(perception)).booleanValue();
			} catch (Exception e) {
				System.out.println("condition: " + condition + " cualquiera");
				return false;
			}
		}
		// Multiple conditions concatenated by "AND"
		Iterator<String> it = multipleConditions.iterator();
		while (it.hasNext()) {
			String nextCondition = it.next();
			try {
				Method method = perception.getClass().getMethod(nextCondition);
				boolean result = ((Boolean) method.invoke(perception)).booleanValue();
				if (!result) {
					return false;
				}
			} catch (Exception e) {
				// System.out.println("condition: " + condition + " cualquiera");
				return false;
			}
		}
		// System.out.println("AAAAcondition: " + this.condition + " true");
		return true;
	}

	public ACTIONS action() {
		if ("USE".equals(rule)) {
			return ACTIONS.ACTION_USE;
		} else if ("RIGHT".equals(rule)) {
			return ACTIONS.ACTION_RIGHT;
		} else if ("LEFT".equals(rule)) {
			return ACTIONS.ACTION_LEFT;
		} else if ("UP".equals(rule)) {
			return ACTIONS.ACTION_UP;
		} else if ("DOWN".equals(rule)) {
			return ACTIONS.ACTION_DOWN;
		}
		return ACTIONS.ACTION_RIGHT;
	}

	public String toString() {
		if (this.perception != null) {
			boolean isTrue = this.isTrue(this.perception);
			return this.condition + ": " + ((isTrue) ? "TRUE" : "FALSE" );
		}
		return this.condition;
	}
}
