package br.ufal.ic.tcc.graph;

import org.apache.log4j.Logger;

/**
 * 
 * @author Ivo Augusto
 * 
 * @param <T>
 */
public class Edge<T> {

	// Atributes ------------------------------------------------------

	private Vertex<T> destino;

	private Vertex<T> origem;

	private double peso;
	
	// Static ---------------------------------------------------------
	
	private static Logger logger;

	static {
		logger = Logger.getLogger(Edge.class.getName());
	}

	// Constructor ----------------------------------------------------

	/**
	 * @param origem
	 * @param destino
	 * @param peso
	 */
	public Edge(final Vertex<T> origem, final Vertex<T> destino, double peso) {
		this.setNos(origem, destino);
		this.setPeso(peso);
	}

	// Public --------------------------------------------------------

	/**
	 * 
	 */
	public Vertex<T> getOrigem() {
		return this.origem;
	}

	/**
	 * 
	 * @param origem
	 * @param destino
	 */
	public void setNos(final Vertex<T> origem, final Vertex<T> destino) {
		if (origem == null || destino == null)
			return;

		// S� altera se a origem for diferente do destino
		if (origem.compareTo(destino) == 0)
			return;

		this.origem = origem;
		this.destino = destino;

		this.origem.adicionarAresta(this);
		this.destino.adicionarAresta(this);
	}

	/**
	 * 
	 * @return
	 */
	public int removerAresta() {
		if (this.getOrigem() == null || this.getDestino() == null)
			return 0;

		int res = 0;

		if (this.getOrigem().removerAresta(this))
			++res;

		if (this.getDestino().removerAresta(this))
			++res;

		return res;
	}

	/**
	 * 
	 * @return
	 */
	public Vertex<T> getDestino() {
		return this.destino;
	}

	/**
	 * 
	 * @return
	 */
	public double getPeso() {
		return this.peso;
	}

	/**
	 * 
	 * @param peso
	 */
	public void setPeso(double peso) {
		this.peso = peso;
	}

	/**
	 * 
	 */
	public String toString() {
		return this.getOrigem() + " ---- (" + this.getPeso() + ") ---- "
				+ this.getDestino();
	}

	/**
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(Object obj) {
		if (obj == null)
			return -1;

		if (obj instanceof Edge) {
			Edge<T> other = (Edge<T>) obj;

			/*
			 * if ((this.origem.compareTo(other.getOrigem()) == 0 &&
			 * this.destino.compareTo(other.getDestino()) == 0) ||
			 * (this.origem.compareTo(other.getDestino()) == 0 &&
			 * this.destino.compareTo(other.getOrigem()) == 0))
			 */
			if (this.origem.compareTo(other.getOrigem()) == 0
					&& this.destino.compareTo(other.getDestino()) == 0)
				return 0;

			return -1;
		} else
			return -1;
	}

	/**
	 * 
	 */
	public boolean equals(Object obj) {
		return this.compareTo(obj) == 0;
	}
}
