package br.ufal.ic.tcc.graph;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author iaarc
 * 
 * Classe que armazena uma lista de vertices indicando um caminho e o peso total
 */

public class Path<T> implements Comparable<Path<T>> {

	// Attributes ------------------------------------------------------

	private List<Vertex<T>> vt;

	private double weight;

	// Static ----------------------------------------------------------

	private static Logger logger;

	static {
		logger = Logger.getLogger(Path.class.getName());
	}

	// Constructor -----------------------------------------------------

	public Path(final List<Vertex<T>> vt, final double weight) {
		this.vt = vt;
		this.weight = weight;
	}

	// Public ----------------------------------------------------------

	/**
	 * @return the vt
	 */
	public List<Vertex<T>> getVt() {
		return vt;
	}

	/**
	 * @param vt
	 *            the vt to set
	 */
	public void setVt(List<Vertex<T>> vt) {
		this.vt = vt;
	}

	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(final double weight) {
		this.weight = weight;
	}

	/**
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Path) {
			Path<T> new_name = (Path<T>) obj;
			return new_name.vt.equals(vt) && new_name.weight == weight;
		}
		return false;
	}

	@Override
	public int compareTo(final Path<T> o) {
		return 0;
	}
}