import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owls.service.Service;

import br.ufal.ic.tcc.graph.Graph;
import br.ufal.ic.tcc.serviceengine.ServiceEngine;
import br.ufal.ic.tcc.similaritymeasures.CossineSimilarity;
import br.ufal.ic.tcc.utils.Configuration;

public class Teste {

	public static void main(String[] args) throws Exception {

		Vector<URI> services = loadServices();

		ServiceEngine engine = new ServiceEngine(CossineSimilarity.class);

		Graph<Service> graph = engine.constructAdvertisementGraph(services);

		OWLKnowledgeBase kb = OWLFactory.createKB();

		kb.setReasoner("RDFS");

		String s = "http://localhost:8080/TesteServiceProject/onto/service/getAO.owl";

		Service request = kb.readService(s);

		Service result = engine.match(graph, request, 0.50);

	}

	public static Vector<URI> loadServices() throws URISyntaxException {
		Vector<URI> services = new Vector<URI>();
		for (String service : Configuration.getInstance().getProperties())
			services.add(new URI(service));

		return services;
	}
}
