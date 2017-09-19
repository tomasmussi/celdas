package ar.fi.uba.celdas;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**

Lo que sí es obligatorio, es que la regla esté modelizada en un archivo JSON y que el
Agente Inteligente lea dicho archivo y actue según las reglas allí descritas.
Las reglas tendrán una condición y una acción.
La condición estará de alguna manera determinada por los objetos en el nivel.
Es importante hacer este ejercicio de como modelizar una regla,
de forma que su "condición" sea más o meno genérica
, porque nos servirá más adelante.


Por supuesto las reglas pueden tener otros atributos, por ejemplo: prioridad, utilidad, eficiencia, etc.
 *
 * */
public class RulesParser {

	private List<Rule> rules;

	public RulesParser() {
		rules = new ArrayList<Rule>();
		String rulesFile = "rules.json";
		InputStream fstream = this.getClass().getResourceAsStream(rulesFile);

		int content;
		StringBuffer sb = new StringBuffer();
		try {
			while ((content = fstream.read()) != -1) {
				// convert to char and display it
				sb.append((char) content);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(sb.toString());
		JSONObject js = new JSONObject(sb.toString());
		JSONArray array = js.getJSONArray("rules");
		Iterator it = array.iterator();
		while (it.hasNext()) {
			JSONObject jsonRule = (JSONObject) it.next();
			rules.add(new Rule((String)jsonRule.get("condition"), (String)jsonRule.get("rule")));
		}

	}

	public List<Rule> getRules() {
		return new ArrayList<Rule>(rules);
	}
}
