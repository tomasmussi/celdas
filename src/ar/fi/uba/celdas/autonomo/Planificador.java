package ar.fi.uba.celdas.autonomo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ontology.Types.ACTIONS;
import ar.fi.uba.celdas.autonomo.grafo.DijkstraAlgorithm;
import ar.fi.uba.celdas.autonomo.grafo.Edge;
import ar.fi.uba.celdas.autonomo.grafo.Graph;
import ar.fi.uba.celdas.autonomo.grafo.Vertex;

public class Planificador {

	private List<Teoria> teorias;
	private Teoria actual;

	private PriorityQueue<Teoria> objetivos;
	private boolean hayPlan;
	private List<Teoria> plan;
	
	private boolean hayTeoriaUtil;
	private Teoria teoriaUtil;
	
	public Planificador(List<Teoria> teorias, Teoria actual) {
		this.teorias = teorias;
		this.actual = actual;
		
		this.hayPlan = false;
		this.objetivos = new PriorityQueue<Teoria>(new Comparator<Teoria>() {
			@Override
			public int compare(Teoria o1, Teoria o2) {
				return o2.utilidad() - o1.utilidad();
			}			
		});
		this.hayTeoriaUtil = false;
		this.teoriaUtil = null;
		buscarPlan();
		if (!hayPlan) {
			buscarTeoriaUtil(30);
		}
	}
	
	private void buscarTeoriaUtil(int utilidad) {
		teoriaUtil = null;
		for (Teoria teoria : teorias) {
			if (teoria.utilidad() > utilidad && teoria.tieneCondicionSupuesta(actual.getCondicionSupuesta(), actual.getTieneLlave())) {
				hayTeoriaUtil = true;
				utilidad = teoria.utilidad();
				teoriaUtil = teoria;
			}
		}
	}

	private void buscarPlan() {
		List<Edge> conexiones = new ArrayList<Edge>();
		Map<Integer, Vertex> mapa = new HashMap<Integer, Vertex>();
		Map<Integer, Teoria> mapaTeorias = new HashMap<Integer, Teoria>();
		Map<Vertex,Integer> mapaInvertido = new HashMap<Vertex,Integer>();
		for (Teoria teoria : teorias) {
			Vertex vertex = new Vertex(String.valueOf(teoria.getId()), String.valueOf(teoria.getId()));
			mapa.put(teoria.getId(), vertex);
			mapaTeorias.put(teoria.getId(), teoria);
			mapaInvertido.put(vertex, teoria.getId());
		}
		String idAct = "0";
		Vertex actualVertex = new Vertex(idAct, idAct);
		mapa.put(Integer.valueOf(0), actualVertex);
		mapaTeorias.put(Integer.valueOf(0), actual);
		for (Teoria teoria : teorias) {			
			for (Teoria otro : teorias) {
				if (teoria.siguientePaso(otro)) {
					String id = String.valueOf(teoria.getId()) + "+" +String.valueOf(otro.getId());
					int peso = 200 - teoria.cociente();
					conexiones.add(new Edge(id, mapa.get(teoria.getId()), mapa.get(otro.getId()), peso));
				}
			}
			if (teoria.utilidad() == 100 && actual.getTieneLlave()) {
				objetivos.add(teoria);				
			}
			if (teoria.utilidad() == 90 && !actual.getTieneLlave()) {
				objetivos.add(teoria);
			}
		}
		int cociente = -1;
		for (Teoria teoria : teorias) {
			if (teoria.tieneCondicionSupuesta(actual.getCondicionSupuesta(), actual.getTieneLlave())) {				
				String id = String.valueOf(teoria.getId()) + "+" +String.valueOf(actual.getId());
				int peso = 200 - teoria.cociente();
				conexiones.add(new Edge(id, actualVertex, mapa.get(teoria.getId()), peso));
				if (teoria.cociente() > cociente) {
					cociente = teoria.cociente();
					actual = teoria;					
				}
			}
		}
		Graph grafo = new Graph(new ArrayList<Vertex>(mapa.values()), conexiones);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(grafo);
		while (!objetivos.isEmpty() && !hayPlan) {
			Teoria objetivo = objetivos.poll();
			dijkstra.execute(actualVertex);
			LinkedList<Vertex> path = dijkstra.getPath(mapa.get(objetivo.getId()));
			if (path != null) {
				hayPlan = !path.isEmpty();
				if (hayPlan) {
					this.plan = new ArrayList<Teoria>();
					for (Vertex vertex : path) {
						Integer id = mapaInvertido.get(vertex);
						if (id != null) {
							plan.add(mapaTeorias.get(id));						
						} else {
							// plan.add(actual);
						}
					}
				}				
			}			
		}
	}


	public boolean hayPlan() {
		return hayPlan;
	}
	
	public boolean hayTeoriaUtil() {
		return hayTeoriaUtil;
	}

	public ACTIONS dameAccionPlan() {
		ACTIONS accion = plan.get(0).getAccionTeoria();
		plan.remove(0);
		return accion;
	}

	public ACTIONS dameAccionTeoriaUtil() {
		if (teoriaUtil != null) {
			return teoriaUtil.getAccionTeoria();
		}
		return actual.getAccionTeoria();
	}

	/**
	 * Verifico que al haber tomado la accion que estaba en el plan se cumplio el efecto predicho
	 * */
	public boolean todaviaSirve(Teoria teoriaLocal) {
		if (plan.isEmpty()) {
			return false;
		}
		return plan.get(0).tieneCondicionSupuesta(teoriaLocal.getCondicionSupuesta(), teoriaLocal.getTieneLlave());
	}
	
	
}
