package br.ufal.ic.tcc.similaritymeasures;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.mindswap.owls.process.Parameter;

import br.ufal.ic.tcc.utils.MathUtils;

/**
 * 
 * @author <a href="mailto:marlos.tacio@gmail.com">marlos</a>
 * 
 */

public class CossineSimilarity extends SimilarityMeasure {

	// Static -----------------------------------------------------------

	private static Logger logger;

	static {
		logger = Logger.getLogger(CossineSimilarity.class.getName());
	}

	// Constructor ------------------------------------------------------

	public CossineSimilarity(Parameter s1, Parameter s2) throws Exception {
		super(s1, s2);
	}

	/**
	 * 
	 * @param s1
	 * @param s2
	 * @throws Exception
	 */
	public CossineSimilarity(Vector<Parameter> s1, Vector<Parameter> s2)
			throws Exception {
		super(s1, s2);
	}

	// Public ----------------------------------------------------------

	/**
	 * 
	 */
	public double calculateSimilarity() {
		double num = MathUtils.produtoInterno(service1, service2);
		double den = MathUtils.norma(service1) * MathUtils.norma(service2);
		double result = num / den;
		double r = 1 - result;

		return r < 0.0 ? 0.0 : r > 1.0 ? 1.0 : r;
	}
}
