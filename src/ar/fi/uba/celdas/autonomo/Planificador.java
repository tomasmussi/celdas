package ar.fi.uba.celdas.autonomo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import ontology.Types.ACTIONS;

public class Planificador {

	private List<Teoria> teorias;
	private char[][] condicionActual;
	private boolean tieneLlave;

	private PriorityQueue<Teoria> objetivos;
	private boolean hayPlan;
	private List<Teoria> plan;
	
	private boolean hayTeoriaUtil;
	private Teoria teoriaUtil;
	
	public Planificador(List<Teoria> teorias, char[][] condicionActual, boolean tieneLlave, int utilidad) {
		this.teorias = teorias;
		this.condicionActual = condicionActual;
		this.tieneLlave = tieneLlave;
		
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
			buscarTeoriaUtil(utilidad);
		}
	}
	
	private void buscarTeoriaUtil(int utilidad) {
		teoriaUtil = null;
		for (Teoria teoria : teorias) {
			if (teoria.utilidad() > utilidad && teoria.tieneCondicionSupuesta(condicionActual, tieneLlave)) {
				hayTeoriaUtil = true;
				utilidad = teoria.utilidad();
				teoriaUtil = teoria;
			}
		}
	}

	private void buscarPlan() {
		for (Teoria teoria : teorias) {
			if (teoria.utilidad() >= 90) {
				objetivos.add(teoria);				
			}
		}
		while (!objetivos.isEmpty() && !hayPlan) {
			Teoria objetivo = objetivos.poll();
			hayPlan = armarPlan(objetivo);
		}
	}
	
	private boolean armarPlan(Teoria objetivo) {
		List<Teoria> plan = new ArrayList<Teoria>();
		plan.add(objetivo);
		boolean continuar = true;
		Teoria eslabon = objetivo;
		while (continuar) {
			continuar = false;
			for (Teoria teoria : teorias) {
				if (teoria.tieneComoEfecto(eslabon)) {
					plan.add(teoria);
					continuar = true;
					eslabon = teoria;
					break;
				}
			}

			// Encontre una teoria que puede usarse, verifico si es igual a mi situacion actual
			if (eslabon.tieneCondicionSupuesta(condicionActual, tieneLlave)) {
				// Logre obtener un plan
				hayPlan = true;
				this.plan = new ArrayList<Teoria>(plan);
				Collections.reverse(this.plan);
				return true;
			}
		}
		return false;	
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
		return teoriaUtil.getAccionTeoria();
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
