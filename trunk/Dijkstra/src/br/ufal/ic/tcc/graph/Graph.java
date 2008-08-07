package br.ufal.ic.tcc.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

public class Graph<T> {

	// Atributes ------------------------------------------------------

	private Vector<Edge<T>> arestas;

	private Vector<Vertex<T>> vertices;

	// Static ---------------------------------------------------------

	private static Logger logger;

	static {
		logger = Logger.getLogger(Graph.class.getName());
	}

	// Constructor ----------------------------------------------------

	/**
	 * 
	 */
	public Graph() {
		this.arestas = new Vector<Edge<T>>();
		this.vertices = new Vector<Vertex<T>>();
	}

	// Public ---------------------------------------------------------

	/**
	 * 
	 */
	public Vector<Edge<T>> getArestas() {
		return this.arestas;
	}

	/**
	 * 
	 * @param arestas
	 */
	public void setArestas(final Vector<Edge<T>> arestas) {
		this.arestas = arestas;
	}

	/**
	 * 
	 * @return
	 */
	public Vector<Vertex<T>> getVertices() {
		return this.vertices;
	}

	/**
	 * 
	 * @param vertices
	 */
	public void setVertices(final Vector<Vertex<T>> vertices) {
		this.vertices = vertices;
	}

	/**
	 * 
	 * @param collection
	 * @param anItem
	 * @return
	 */
	public boolean exists(final Vector<?> collection, final Object anItem) {
		return collection.indexOf(anItem) != -1;
	}

	/**
	 */
	public boolean addVertex(final Vertex<T> vertex) {
		if (!exists(this.vertices, vertex))
			return this.vertices.add(vertex);
		return false;
	}

	/**
	 * 
	 * @param edge
	 * @return
	 */
	public boolean addEdge(final Edge<T> edge) {
		if (!exists(this.arestas, edge))
			return this.arestas.add(edge);
		return false;
	}

	/**
	 * Retorna um Map contendo a lista dos vertices do caminho e um float
	 * contendo o tamanho total. O motivo de se utilizar um map e a necessidade
	 * de ser juntar o caminho com o valor total. No entanto este map contem
	 * apenas um unico par. A lista com os caminho e o pesototal
	 * 
	 * @param origem
	 * @param destino
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Path<T> getPath(final Vertex<T> origem, final Vertex<T> destino) {
		Vertex[] array = new Vertex[this.vertices.size()];
		this.vertices.toArray(array);
		Dijkstra caminho = new Dijkstra(array);

		Vertex[] menor = caminho.doDijkstra(origem, destino);
		List<Vertex<T>> retorno = new LinkedList<Vertex<T>>();
		float pesoTotal = 0f;
		if (menor != null) {
			int count = 0;
			for (int i = 0; i < menor.length; i++) {
				if (menor[i] != null) {
					count++;
					retorno.add(menor[i]);
					if (i > 0)
						pesoTotal += weight(menor[i - 1], menor[i]);

				}
			}
			if (count == 1 && menor[0].getD() == Dijkstra.INFINITY)
				return null;
			else {
				/*
				 * Map<List<Vertex<T>>, Float> m = new HashMap<List<Vertex<T>>,
				 * Float>(); m.put(retorno, pesoTotal);
				 */
				return new Path(retorno, pesoTotal);
			}
		} else
			return null;
	}

	/*
	 * public boolean removeVertex(Vector<Vertex<T>> collection, Vertex<T>
	 * obj) { boolean result = collection.remove(obj); }
	 */

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void updateEdges() {
		this.arestas.clear();

		for (int i = 0; i < this.vertices.size(); i++) {
			Vertex no = (Vertex) this.vertices.elementAt(i);
			this.arestas.addAll(no.getEdges());
		}
	}

	// Private -------------------------------------------------------

	@SuppressWarnings("unchecked")
	private double weight(final Vertex a, final Vertex b) {
		Edge c = new Edge(a, b, 0f);
		for (int i = 0; i < this.arestas.size(); i++)
			if (this.arestas.get(i).equals(c))
				return ((Edge) this.arestas.get(i)).getPeso();

		return Dijkstra.INFINITY;
	}
}
