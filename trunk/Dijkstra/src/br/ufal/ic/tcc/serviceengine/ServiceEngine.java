package br.ufal.ic.tcc.serviceengine;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owls.grounding.Grounding;
import org.mindswap.owls.process.CompositeProcess;
import org.mindswap.owls.process.Input;
import org.mindswap.owls.process.InputList;
import org.mindswap.owls.process.Output;
import org.mindswap.owls.process.OutputList;
import org.mindswap.owls.process.Parameter;
import org.mindswap.owls.process.ParameterList;
import org.mindswap.owls.process.Perform;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.Result;
import org.mindswap.owls.process.Sequence;
import org.mindswap.owls.profile.Profile;
import org.mindswap.owls.service.Service;
import org.mindswap.utils.URIUtils;

import br.ufal.ic.tcc.graph.Dijkstra;
import br.ufal.ic.tcc.graph.Edge;
import br.ufal.ic.tcc.graph.Graph;
import br.ufal.ic.tcc.graph.Path;
import br.ufal.ic.tcc.graph.Vertex;
import br.ufal.ic.tcc.similaritymeasures.CossineSimilarity;
import br.ufal.ic.tcc.similaritymeasures.SimilarityMeasure;

/**
 * 
 * @author Ivo Augusto
 * 
 */

public class ServiceEngine {

	// Constants ------------------------------------------------------

	public static final int EXACT = 0;

	public static final int FAIL = 1;

	public static final double DESVIO = 0.05;

	// Attributes ------------------------------------------------------

	// private Graph<Service> graph;

	private OWLKnowledgeBase kb;

	// Static ----------------------------------------------------------

	private static URI baseURI;

	private static Logger logger;
	
	private Class<? extends SimilarityMeasure> strategyClz;

	static {
		logger = Logger.getLogger(ServiceEngine.class.getName());
	}

	// Constructor -----------------------------------------------------

	public ServiceEngine(String bURI, Class<? extends SimilarityMeasure> strategy) throws URISyntaxException {
		ServiceEngine.baseURI = new URI(bURI);
		this.kb = OWLFactory.createKB();
		this.kb.setReasoner("RDFS");
		if(strategy==null) strategy=CossineSimilarity.class;
	}

	// Public ----------------------------------------------------------

	/**
	 * 
	 */
	public ServiceEngine(Class<? extends SimilarityMeasure> strategy) throws URISyntaxException {
		this("http://localhost:8080/sws/composite.owl", strategy);
	}
	
	public ServiceEngine() throws URISyntaxException {
		this(CossineSimilarity.class);
	}

	/**
	 * Adiciona um servico ao grafo de servicos disponiveis.
	 * 
	 * @param graph
	 * @param service
	 * @throws Exception
	 */
	public void addToAdvertisementGraph(Graph<Service> graph, Service service)
			throws Exception {
		Vector<Vertex<Service>> v = graph.getVertices();
		Vertex<Service> vser = new Vertex<Service>(service);

		for (Vertex<Service> vertex : v) {

			OutputList out1 = vser.getContent().getProfile().getOutputs();
			InputList in2 = vertex.getContent().getProfile().getInputs();
			double value = canFollow(out1, in2);

			logger.info("in: " + in2 + "\nout: " + out1 + "\nDiferença: "
					+ value + "\n");

			if (value != FAIL)
				graph.addEdge(new Edge<Service>(vser, vertex, value));

			OutputList out2 = vertex.getContent().getProfile().getOutputs();
			InputList in1 = vser.getContent().getProfile().getInputs();
			double value2 = canFollow(out2, in1);

			logger.info("in: " + in1 + "\nout: " + out2 + "\nDiferença: "
					+ value2 + "\n");

			if (value2 != FAIL)
				graph.addEdge(new Edge<Service>(vertex, vser, value2));

		}

		graph.addVertex(vser);
	}

	/**
	 * 
	 * @param uris
	 * @return
	 * @throws Exception
	 */
	public Graph<Service> constructAdvertisementGraph(List<URI> uris)
			throws Exception {
		Graph<Service> graph = new Graph<Service>();

		logger.info("Construindo Grafo\n");

		for (URI uri : uris) {
			try {
				Service service = this.kb.readService(uri);
				logger.info("Adicionando Servico: " + service + "\n");
				addToAdvertisementGraph(graph, service);

				// graph.addVertex(new Vertex<Service>(service));
			} catch (FileNotFoundException e) {
				System.out.println("Nao foi possivel ler o servico: " + uri);
				e.printStackTrace();
			}
		}

		return graph;
	}

	/**
	 * Efetua o match de servicos
	 * 
	 * @param graph
	 * @param request
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	public Service match(Graph<Service> graph, Service request)
			throws Exception {
		return this.match(graph, request, EXACT);

	}

	/**
	 * Efetua o match de servicos
	 * 
	 * @param graph
	 * @param request
	 * @param filter
	 * @return
	 * @throws Exception
	 */
	public Service match(final Graph<Service> graph, final Service request,
			double filter) throws Exception {
		logger.info("Executando matching.\nRequest: " + request + "\nInputs: "
				+ request.getProcess().getInputs() + "\nOutputs: "
				+ request.getProcess().getOutputs() + "\nFilter: " + filter
				+ "\n");

		Map<Vertex<Service>, Double> inputs = new HashMap<Vertex<Service>, Double>();
		Map<Vertex<Service>, Double> outputs = new HashMap<Vertex<Service>, Double>();

		// Set<Vertex<Service>> inputs = new HashSet<Vertex<Service>>();
		// Set<Vertex<Service>> outputs = new HashSet<Vertex<Service>>();

		for (Vertex<Service> vt : graph.getVertices()) {
			Service adv = vt.getContent();

			logger.info("Comparando...\nRequest: " + request + "\nService: "
					+ adv + "\n");

			double value = 0.0;
			if ((value = outputMatch(adv, request)) <= filter + DESVIO)
				outputs.put(vt, value);

			if ((value = inputMatch(adv, request)) <= filter + DESVIO)
				inputs.put(vt, value);
		}

		double v_direto = filter + DESVIO;
		Service result_direto = null;
		for (Vertex<Service> output : outputs.keySet()) {
			for (Vertex<Service> input : inputs.keySet()) {
				if (output.getContent().equals(input.getContent())) {
					double aux = (outputs.get(output) + inputs.get(input)) / 2;
					if (v_direto > aux) {
						v_direto = aux;
						result_direto = output.getContent();
					}
				}
			}
		}

		List<Path<Service>> paths = new LinkedList<Path<Service>>();

		for (Vertex<Service> destino : outputs.keySet()) {
			for (Vertex<Service> origem : inputs.keySet()) {
				if (destino.getContent() != origem.getContent()) {
					Path<Service> path = graph.getPath(origem, destino);
					if (path != null)
						paths.add(path);
				}
			}
		}

		if (paths.size() == 0) {
			if (result_direto != null) {
				logger.info("Matching Direto:\nServiceRequest: " + request
						+ "\nServiceResult:" + result_direto
						+ "\nSimilaridade: " + v_direto);
				return result_direto;

			}
			logger.info("Servico não encontrado!!!");
			return null;
		}

		int flag = 0;
		double v_indireto = Dijkstra.INFINITY;
		for (int i = 0; i < paths.size(); i++) {
			if (paths.get(i).getWeight() < v_indireto
					&& paths.get(i).getWeight() < filter + DESVIO) {
				flag = i;
				v_indireto = paths.get(i).getWeight();
			}
		}

		if (v_indireto == Dijkstra.INFINITY) {
			if (result_direto != null) {
				logger.info("Matching Direto:\nServiceRequest: " + request
						+ "\nServiceResult: " + result_direto
						+ "\nSimilaridade: " + v_direto);
				return result_direto;
			}
			logger.info("Servico não encontrado!!!");
			return null;
		}

		if (v_direto < v_indireto) {
			logger.info("Matching Direto:\nServiceRequest: " + request
					+ "\nServiceResult:" + result_direto + "\nSimilaridade: "
					+ v_direto);
			return result_direto;
		}

		List<Vertex<Service>> finalPath = paths.get(flag).getVt();
		List<Service> chainOfServices = new LinkedList<Service>();
		String services = "";
		for (Vertex<Service> vertex : finalPath) {
			chainOfServices.add(vertex.getContent());
			services += vertex.getContent() + "\n";
		}

		Service result_indireto = createSequenceService(chainOfServices);

		logger.info("Matching Indireto:\nServiceRequest: " + request
				+ "\nServiceResult:\n" + services + "Similaridade: "
				+ v_indireto);

		return result_indireto;

	}

	// Private --------------------------------------------------------

	/**
	 * Determina se dois servicos podem ser conectados. A analise eh feita
	 * verificando as entradas de um servico em relacao as saida do outro
	 * 
	 * @param s1
	 *            servico de entrada
	 * @param s2
	 *            servico de saida
	 * @return Peso da aresta
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private double canFollow(ParameterList s1, ParameterList s2)
			throws Exception {

		Vector<Parameter> inputs = new Vector<Parameter>();
		Vector<Parameter> outputs = new Vector<Parameter>();

		/*
		 * listInputs.addAll(s1.getProfile().getInputs());
		 * listOutputs.addAll(s2.getProfile().getOutputs());
		 */
		inputs.addAll(s1);
		outputs.addAll(s2);

		if (inputs.size() > outputs.size())
			return ServiceEngine.FAIL;

		//return createStrategy(inputs, outputs).calculateSimilarity();
		return new CossineSimilarity(inputs, outputs).calculateSimilarity();

		// int result = FAIL;

		// Vector<Integer> results = new Vector<Integer>();
		//
		// for (int i = 0; i < listInputs.size(); i++) {
		// int tmpResult = FAIL;
		// int flag = -1;
		// for (int j = 0; j < listOutputs.size(); j++) {
		// int rm = matchOntologies(listInputs.get(i).getParamType(),
		// listOutputs.get(j).getParamType());
		// if (rm < tmpResult) {
		// tmpResult = rm;
		// flag = j;
		// }
		// }
		// if (tmpResult == FAIL)
		// return Dijkstra.INFINITY;
		// else {
		// results.add(tmpResult);
		// listInputs.remove(i--);
		// listOutputs.remove(flag);
		// }
		// }
		//
		// float media = 0f;
		// for (Integer integer : results) {
		// media += integer;
		// }

		// return media / (float) results.size();
	}
	
	private SimilarityMeasure createStrategy(Object param1, Object param2) {
		try {
			return strategyClz.getConstructor(param1.getClass(), param2.getClass()).newInstance(param1, param2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}

	/**
	 * Verifica se duas ontologias estao de alguma forma conectadas
	 * 
	 * @param clz1
	 *            entrada
	 * @param clz2
	 *            saida
	 * @return
	 */
	private double matchOntologies(Parameter clz1, Parameter clz2) {
		try {
			return new CossineSimilarity(clz1, clz2).calculateSimilarity();
			//return createStrategy(clz1, clz2).calculateSimilarity();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return FAIL;
		// if (clz1.isEquivalent(clz2)) // {
		// return EXACT;
		// } else if (clz2.isSubTypeOf(clz1)) {
		// return PLUGIN;
		// } else if (clz1.isSubTypeOf(clz2)) {
		// return SUBSUME;
		// }
		// return FAIL;
	}

	/**
	 * Cria um servico composto a partir de uma lista de servicos
	 * 
	 * @param services
	 * @return
	 */
	private Service createSequenceService(List<Service> services) {
		OWLOntology onto = OWLFactory.createOntology();
		onto.getKB().setReasoner("Pellet");

		Service aService = onto.createService(URIUtils.createURI(baseURI,
				"CompositeService"));

		CompositeProcess aProcess = onto.createCompositeProcess(URIUtils
				.createURI(baseURI, "CompositeProcess"));

		Profile aProfile = onto.createProfile(URIUtils.createURI(baseURI,
				"Profile"));
		Grounding aGrounding = onto.createGrounding(URIUtils.createURI(baseURI,
				"Grounding"));

		aService.setProfile(aProfile);
		aService.setProcess(aProcess);
		aService.setGrounding(aGrounding);

		Sequence aSequence = onto.createSequence();
		aProcess.setComposedOf(aSequence);

		Perform[] performs = new Perform[services.size()];

		for (int i = 0; i < services.size(); i++) {

			Service s = services.get(i);
			// OWLObject q = s.getProcess();
			Process p = s.getProcess();

			/*
			 * if (p instanceof OWLObject) { Process q = (Process)p; }
			 */

			performs[i] = onto.createPerform();
			performs[i].setProcess(p);
			// performs[i].getProcess();

			// Adiciona a composicao de servicos
			aSequence.addComponent(performs[i]);

			// Efetua a ligacao entre as entradas e saidas do servico
			if (i > 0) {
				Perform prevPerform = performs[i - 1];

				// prevPerform.getProcess();

				Map<Input, Output> map = bindingInputAndOutputs(p.getInputs(),
						services.get(i - 1).getProcess().getOutputs());

				for (Input input : map.keySet()) {
					// Efetua a ligacao entre as entradas e saidas dos servicos
					performs[i].addBinding(input, prevPerform, map.get(input));
				}
			}
		}

		Perform firstPerform = performs[0];
		Perform lastPerform = performs[services.size() - 1];
		Service firstService = services.get(0);
		Service lastService = services.get(services.size() - 1);

		boolean createInput = firstService.getProcess().getInputs().size() > 0;
		boolean createOutput = lastService.getProcess().getOutputs().size() > 0;

		if (createInput) {
			InputList fi = firstService.getProcess().getInputs();
			for (int i = 0; i < fi.size(); i++) {
				Input input = fi.inputAt(i);
				Input newInput = onto.createInput(URIUtils.createURI(baseURI,
						"Input" + i));
				input.getLabel();
				newInput.setLabel(input.getLabel() == null ? "" : input
						.getLabel());
				newInput.setParamType(input.getParamType());
				newInput.setProcess(aProcess);
				firstPerform.addBinding(input, Perform.TheParentPerform,
						newInput); // Liga a entrada do servico inicial com a
				// entrada inicial do servico composto
			}
		}

		if (createOutput) {
			OutputList fo = lastService.getProcess().getOutputs();
			for (int i = 0; i < fo.size(); i++) {
				Output output = fo.outputAt(i);
				Output newOutput = onto.createOutput(URIUtils.createURI(
						baseURI, "Output" + i));
				newOutput.setLabel(output.getLabel() == null ? "" : output
						.getLabel());
				newOutput.setParamType(output.getParamType());
				newOutput.setProcess(aProcess);
				Result result = onto.createResult();
				result.addBinding(newOutput, lastPerform, output);
				aProcess.setResult(result); // the output of the composite
				// process is the output pf last
				// process

			}

		}
		return aService;
	}

	/**
	 * Metodo que efetua ligacao entre entradas e saidas de dois servicos. Faz
	 * uma iteracao nas entradas verificando qual saida melhor pode ser
	 * conectada e coloca o par em um map para retorno
	 * 
	 * @param inputs
	 * @param outputs
	 * @return map contendo os parametros ligados um a um
	 */
	@SuppressWarnings("unchecked")
	private Map<Input, Output> bindingInputAndOutputs(InputList inputs,
			OutputList outputs) {
		Map<Input, Output> ret = new HashMap<Input, Output>();

		List<Input> listInputs = new LinkedList<Input>();
		List<Output> listOutputs = new LinkedList<Output>();

		listInputs.addAll(inputs);
		listOutputs.addAll(outputs);

		for (int i = 0; i < listInputs.size(); i++) {
			double tmpResult = FAIL;
			int flag = -1;
			for (int j = 0; j < listOutputs.size(); j++) {
				double rm = matchOntologies(listInputs.get(i), listOutputs
						.get(j));
				if (rm < tmpResult) {
					tmpResult = rm;
					flag = j;
				}
			}

			ret.put(listInputs.remove(i--), listOutputs.remove(flag));
		}
		return ret;
	}

	/**
	 * 
	 * @param adv
	 * @param rqs
	 * @return
	 * @throws Exception
	 */

	private double inputMatch(Service adv, Service rqs) throws Exception {
		InputList in1 = adv.getProfile().getInputs();
		InputList in2 = rqs.getProfile().getInputs();
		double result = canFollow(in1, in2);

		logger.info("Comparação de inputs:\nin1: " + in1 + "\nin2: " + in2
				+ "\nDiferença: " + result + "\n");

		return result;
	}

	/**
	 * 
	 * @param adv
	 * @param rqs
	 * @return
	 * @throws Exception
	 */
	private double outputMatch(Service adv, Service rqs) throws Exception {
		OutputList out1 = adv.getProfile().getOutputs();
		OutputList out2 = rqs.getProfile().getOutputs();
		double result = canFollow(out1, out2);

		logger.info("Comparação de outputs:\nout1: " + out1 + "\nout2: " + out2
				+ "\nDiferença: " + result + "\n");

		return result;
	}
}
