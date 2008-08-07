package br.ufal.ic.tcc.graph;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.mindswap.owls.service.Service;

/**
 * 
 * @author Ivo Augusto
 * 
 * @param <T>
 */

public class Vertex<T> implements Comparable<Vertex<T>> {

	// Attributes ------------------------------------------------------

	private T content;

	private String name = "";

	private Vertex<T> anterior;

	private double D = Dijkstra.INFINITY;

	private Vector<Edge<T>> edges = new Vector<Edge<T>>();

	// Static ----------------------------------------------------------

	private static Logger logger;

	static {
		logger = Logger.getLogger(Vertex.class.getName());
	}

	// Constructor -----------------------------------------------------

	/**
	 * 
	 * @param content
	 */
	public Vertex(T content) {
		this.content = content;
	}

	/**
	 * 
	 */
	public Vertex() {
		this.content = null;
	}

	/**
	 * 
	 * @param name
	 */
	public Vertex(String name) {
		this.setName(name);
	}

	// Public ----------------------------------------------------------

	/**
	 * 
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	public Vector<Edge<T>> getEdges() {
		return this.edges;
	}

	/**
	 * 
	 * @param edges
	 */
	public void setEdges(Vector<Edge<T>> edges) {
		this.edges = edges;
	}

	/**
	 * 
	 * @param destino
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public double getWeight(Vertex<T> destino) {
		Iterator<?> i = this.getEdges().iterator();

		while (i.hasNext()) {
			Edge aresta = (Edge) i.next();

			if ((aresta.getOrigem().compareTo(this) == 0 && aresta.getDestino()
					.compareTo(destino) == 0)
					|| (aresta.getOrigem().compareTo(destino) == 0 && aresta
							.getDestino().compareTo(this) == 0))
				return aresta.getPeso();
		}

		return Dijkstra.INFINITY;
	}

	/**
	 * 
	 * @param edge
	 */
	public void adicionarAresta(Edge<T> edge) {
		if (edge == null)
			throw new IllegalArgumentException(
					"Parametro invalido - aresta nula");

		if (this.getEdges().indexOf(edge) == -1)
			this.getEdges().add(edge);
	}

	/**
	 * 
	 * @param edge
	 * @return
	 */
	public boolean removerAresta(Edge<T> edge) {
		return this.getEdges().remove(edge);
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void removerNo() {
		for (int i = 0; i < this.getEdges().size(); i++) {
			Edge<T> a = (Edge<T>) this.getEdges().elementAt(i);

			if (a.removerAresta() > 0)
				--i;
		}
	}

	/**
	 * 
	 * @return
	 */
	public Vertex<T> getAnterior() {
		return this.anterior;
	}

	/**
	 * 
	 * @param anterior
	 */
	public void setAnterior(Vertex<T> anterior) {
		this.anterior = anterior;
	}

	/**
	 * 
	 * @return
	 */
	public double getD() {
		return this.D;
	}

	/**
	 * 
	 * @param D
	 */
	public void setD(double D) {
		this.D = D;
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Vertex<T>[] getVizinhos() {
		Vertex<T>[] nos = new Vertex[this.getEdges().size()];
		Iterator<?> i = this.getEdges().iterator();

		if (this.getEdges().size() == 0)
			return null; // Sem n�s vizinhos

		int count = 0;
		while (i.hasNext()) {
			Edge aresta = (Edge) i.next();

			if (aresta.getOrigem().compareTo(this) == 0)
				nos[count] = aresta.getDestino();
			else
				nos[count] = aresta.getOrigem();

			count++;
		}

		return nos;
	}

	/**
	 * 
	 */
	public int compareTo(Vertex<T> obj) {
		if (obj == null)
			return -1;

		if (obj instanceof Vertex) {
			Vertex<?> other = (Vertex<?>) obj;
			if (content instanceof Service) {
				if (content.equals(other.content)) {
					return 0;
				}
				return -1;

			}
			// // PONTO DE ALTERACAO PARA ADICIONAR CODIGO DE COMPARACAO ENTRE
			// TERMOS
			return this.getName().compareToIgnoreCase(other.getName());
		} else
			return -1;
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (obj instanceof Vertex) {
			Vertex<T> new_name = (Vertex<T>) obj;

			// return content.equals(new_name.content); //ALTERADO
			if (content instanceof Service) {
				return content.equals(new_name.content);
			}
			return compareTo(new_name) == 0;
		}
		return false;

	}

	/**
	 * 
	 */
	public String toString() {
		return this.getName();
	}

	/**
	 * @return the content
	 */
	public T getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(T content) {
		this.content = content;
	}
}
