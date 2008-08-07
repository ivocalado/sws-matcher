package br.ufal.ic.tcc.similaritymeasures;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.mindswap.owls.process.Parameter;
import org.mindswap.pellet.utils.SetUtils;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLNamedClass;
import edu.stanford.smi.protegex.owl.model.query.QueryResults;

/**
 * 
 * @author <a href="mailto:marlos.tacio@gmail.com">marlos</a>
 * 
 */

public abstract class SimilarityMeasure {

	// Attributes ----------------------------------------------------

	protected Vector<Double> service1;

	protected Vector<Double> service2;

	// Static --------------------------------------------------------

	private static Logger logger;

	static {
		logger = Logger.getLogger(SimilarityMeasure.class.getName());
	}

	// Constructor ---------------------------------------------------

	public SimilarityMeasure(final Parameter p1, final Parameter p2)
			throws Exception {
		this.service1 = new Vector<Double>();
		this.service2 = new Vector<Double>();

		Vector<String> ancestor1 = new Vector<String>();
		Vector<String> ancestor2 = new Vector<String>();

		OWLModel model = this.getOntology(p1);
		String individual = p1.getParamType().toString().split("#")[1];
		ancestor1.addAll(this.getAncestor(individual, model));

		model = this.getOntology(p2);
		individual = p2.getParamType().toString().split("#")[1];
		ancestor2.addAll(this.getAncestor(individual, model));

		calculeFrequencies(ancestor1, ancestor2);
	}

	/**
	 * 
	 * @param s1
	 * @param s2
	 * @throws Exception
	 */
	public SimilarityMeasure(final Vector<Parameter> s1,
			final Vector<Parameter> s2) throws Exception {
		this.service1 = new Vector<Double>();
		this.service2 = new Vector<Double>();

		Vector<String> ancestor1 = new Vector<String>();
		Vector<String> ancestor2 = new Vector<String>();
		for (Parameter param : s1) {
			OWLModel model = this.getOntology(param);
			String individual = param.getParamType().toString().split("#")[1];
			ancestor1.addAll(this.getAncestor(individual, model));
		}

		for (Parameter param : s2) {
			OWLModel model = this.getOntology(param);
			String individual = param.getParamType().toString().split("#")[1];
			ancestor2.addAll(this.getAncestor(individual, model));
		}

		calculeFrequencies(ancestor1, ancestor2);
	}

	// Public ---------------------------------------------------------

	/**
	 * 
	 * @return
	 */
	public abstract double calculateSimilarity();

	// Private---------------------------------------------------------

	/**
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	private OWLModel getOntology(Parameter param) throws Exception {
		String uri = param.getParamType().toString().split("#")[0];
		return ProtegeOWL.createJenaOWLModelFromURI(uri);
	}

	/**
	 * 
	 * @param individual
	 * @param model
	 * @return
	 * @throws Exception
	 */
	private Vector<String> getAncestor(final String individual, OWLModel model)
			throws Exception {
		Vector<String> vector = new Vector<String>();
		vector.add(individual);

		String query = "SELECT ?object WHERE {:" + individual
				+ " rdfs:subClassOf ?object}";

		QueryResults results = model.executeSPARQLQuery(query);

		while (results.hasNext()) {
			Map<?, ?> map = results.next();

			try {
				for (Object key : map.keySet()) {
					String name = ((DefaultOWLNamedClass) map.get(key))
							.getLocalName();
					vector.addAll(getAncestor(name, model));
				}
			} catch (Exception e) {
				System.out.println("");
			}
		}
		vector.remove("Thing");
		return vector;
	}

	/**
	 * 
	 * @param s1
	 * @param s2
	 */
	private void calculeFrequencies(final Vector<String> s1,
			final Vector<String> s2) {

		Iterator<?> union = SetUtils.union(s1, s2).iterator();

		while (union.hasNext()) {
			Object individual = union.next();

			double frequency1 = 0.0;
			for (Object s : s1)
				frequency1 += s.equals(individual) ? 1 : 0;
			this.service1.add(frequency1);

			double frequency2 = 0.0;
			for (Object s : s2)
				frequency2 += s.equals(individual) ? 1 : 0;
			this.service2.add(frequency2);
		}
	}
}
