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

public class ExtendedJacquardSimilarity extends SimilarityMeasure {

	// Static -----------------------------------------------------------
	
	private static Logger logger;

	static {
		logger = Logger.getLogger(ExtendedJacquardSimilarity.class.getName());
	}

	// Constructor -----------------------------------------------------

	/**
	 * 
	 * @param s1
	 * @param s2
	 * @throws Exception
	 */
	public ExtendedJacquardSimilarity(Vector<Parameter> s1, Vector<Parameter> s2)
			throws Exception {
		super(s1, s2);
	}

	// Public ----------------------------------------------------------

	/**
	 * 
	 */
	@Override
	public double calculateSimilarity() {
		double prod = MathUtils.produtoInterno(super.service1, super.service2);
		double quo = Math.pow(MathUtils.norma(super.service1), 2)
				+ Math.pow(MathUtils.norma(super.service2), 2) - prod;

		return quo == 0 ? 1 : 1 - (prod / quo);
	}

}
