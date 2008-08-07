package examplesMindswap;

import java.net.URI;
import java.util.Iterator;

import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owls.OWLSFactory;
import org.mindswap.owls.process.AtomicProcess;
import org.mindswap.owls.process.Input;
import org.mindswap.owls.process.InputList;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.execution.ProcessExecutionEngine;
import org.mindswap.owls.process.execution.ProcessExecutionListener;
import org.mindswap.owls.service.Service;
import org.mindswap.query.ValueMap;

/**
 * 
 * Examples to show how services can be executed. Some examples of simple
 * execution monitoring is included.
 * 
 * @author Evren Sirin
 */
public class RunService2 {

	public static void main(String[] args) throws Exception {
		Service service;
		Process process;
		String outValue;
		ValueMap values;
		ProcessExecutionEngine exec;

		exec = OWLSFactory.createExecutionEngine();
		exec.setPreconditionCheck(false);

		// Attach a listener to the execution engine
		exec.addExecutionListener(new ProcessExecutionListener() {

			public void setCurrentExecuteService(Process p) {
				System.out.println("Start executing process " + p);
			}

			public void printMessage(String message) {
			}

			public void finishExecution(int retCode) {
				System.out
						.println("Finished execution "
								+ ((retCode == ProcessExecutionListener.EXEC_ERROR) ? "with errors"
										: "successfully"));
			}
		});

		String langOnt = "http://www.daml.org/2003/09/factbook/languages#";

		OWLKnowledgeBase kb = OWLFactory.createKB();
		// we at least need RDFS reasoning to evaluate preconditions (to
		// understand
		// that process:Parameter is subclass of swrl:Variable)
		kb.setReasoner("RDFS");

		service = kb
				.readService("http://www.mindswap.org/2004/owl-s/1.1/BabelFishTranslator.owl");
		
		
		service.getProfile();
		Service s2 = kb
		.readService("http://www.mindswap.org/2004/owl-s/1.1/ZipCodeFinder.owl");
		
		if (s2.equals(service)) {
			System.out.println("Servicos iguais");
		} else System.out.println("Fudeo");
		process = service.getProcess();

		// get the references for these values
		OWLIndividual English = kb.getIndividual(URI
				.create(langOnt + "English"));
		OWLIndividual French = kb.getIndividual(URI.create(langOnt + "French"));

		// initialize the input values to be empty
		values = new ValueMap();

		values.setDataValue(process.getInput("InputString"), "Hello world!");
		values.setValue(process.getInput("InputLanguage"), English);
		values.setValue(process.getInput("OutputLanguage"), French);
		values = exec.execute(process, values);

		// get the output using local name
		outValue = values.getValue(process.getOutput()).toString();

		// display the results
		System.out.println("Executed service '" + service + "'");
		System.out.println("Grounding WSDL: "
				+ ((AtomicProcess) process).getGrounding().getDescriptionURL());
		System.out.println("Output = " + outValue);
		System.out.println();
		System.out.println();
		
		
		
		InputList list = service.getProfile().getInputs();
		
		for(Iterator iter = list.iterator(); iter.hasNext();){
			
			//Input input = (Input) object;
		//	System.out.println(input);
			//URI uri = (URI)iter.next();
			Input input = (Input) iter.next();
			System.out.println(input.getParamType().getURI());
			
			System.out.println(input.getParamType());
			
			  
			/*System.out.println(input.getOntology());
			System.out.println(input.getSourceOntology());*/
			//processClass(kb, (URI) object);
			/*OWLClass clazz = kb.getClass((URI) uri);
			if (clazz==null) {
				System.out.println("Merdou!!");
			}
			*/
			
			
			System.out.println();
		}

	}
	
	
	/*private void addService(Integer serviceID,
			org.mindswap.owl.OWLOntology onto, Vector inputurilist,
			Vector outputurilist) {
		try {
			Set conceptsToAdd = new HashSet();
			conceptsToAdd.addAll(inputurilist);
			conceptsToAdd.addAll(outputurilist);
			localOntologyContainer.processClasses(base, conceptsToAdd);
			updateReasoner();
			if (useSyntacticFilter())
				SimpleIndex.instanceOf().addDocument(
						"" + serviceID,
						reason.unfoldURIs(inputurilist) + " "
								+ reason.unfoldURIs(outputurilist));
			registry.addConcepts(true, serviceID.intValue(), inputurilist);
			registry.addConcepts(false, serviceID.intValue(), outputurilist);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean processClass(OWLKnowledgeBase kbase, URI clazzURI) {
    	OWLClass clazz =kbase.getClass(clazzURI);
    	if (clazz==null) {
    		owlsmx.io.ErrorLog.instanceOf().report("Couldn't find the clazz " + clazzURI + " in base " + kbase);
    		return false;
    	}
    	//debugDisplay("Processing            " + clazz.getURI().toString());
//    	kbase.setReasoner("RDFS");
    	createClass(kbase, clazz);
//    	kbase.setReasoner(null);
    	
//    	debugDisplay("Process class " + clazzURI);
//    	ontologyHasChanged = false;
//    	OWLClass clazz =kbase.getClass(clazzURI);
//    	debugDisplay("Processing            " + clazz.getURI().toString());
//
//    	createClass(kbase, clazz);
    	//save();
    	return ontologyHasChanged;
    }*/
}
