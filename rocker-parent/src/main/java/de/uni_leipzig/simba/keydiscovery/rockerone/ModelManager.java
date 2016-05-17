package de.uni_leipzig.simba.keydiscovery.rockerone;

import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import de.uni_leipzig.simba.keydiscovery.model.CandidateNode;
import de.uni_leipzig.simba.keydiscovery.model.RKDClassTask;

/**
 * @author Tommaso Soru {@literal (tsoru@informatik.uni-leipzig.de)}
 * 
 */
public class ModelManager {
	
	private final static Logger LOGGER = Logger.getLogger("ROCKER");

	/**
	 * @param path
	 */
	public static void save(RKDClassTask cl, Model m, String path) {
		
		Resource c = cl.getResource();
		Set<CandidateNode> keys = cl.getKeys();
		
		String ns = "http://aksw.org/ns/";

		Resource graphClass = m.createResource(ns + "Graph",
				m.getResource("http://www.w3.org/2000/01/rdf-schema#Class"));
		Resource keyClass = m.createResource(ns + "Key",
				m.getResource("http://www.w3.org/2000/01/rdf-schema#Class"));

		// FIXME this
		Resource theGraph = m.createResource("http//rocker.aksw.org/", graphClass);
		Property hasClass = m.createProperty(ns + "hasClass");
		Property hasPKey = m.createProperty(ns + "hasPrimaryKey");
		Property formedBy = m
				.createProperty("http://dbpedia.org/property/formedBy");

		Resource theClass = m.createResource(c.getURI());
		m.add(theGraph, hasClass, theClass);
		for (CandidateNode cn : keys) {
			Resource theKey = m.createResource(
					ns + "Key" + sha1(cn.toString()), keyClass);
			m.add(theClass, hasPKey, theKey);
			for (Property p : cn.getProperties()) {
				Resource theProp = m.createResource(p.getURI());
				m.add(theKey, formedBy, theProp);
			}
		}
		
		// save to TURTLE/N3
		try {
			FileOutputStream fout = new FileOutputStream(path);
			m.write(fout, "TURTLE");
			fout.close();
		} catch (Exception e) {
			LOGGER.info("Exception caught" + e.getMessage());
			e.printStackTrace();
		}

	}
	
	/**
	 * @param path
	 * @return
	 */
	public static HashMap<String, List<Resource>> loadClassList(String path) {
		HashMap<String, List<Resource>> res = new HashMap<>();
		// load specification file
		Model model = RDFDataMgr.loadModel(path);
		// get all graphs
		Iterator<Statement> statIt = model.listStatements((Resource) null, 
				ResourceFactory.createProperty("http://aksw.org/deduplication/relatedGraph"), (RDFNode) null);
		while(statIt.hasNext()) {
			Statement s = statIt.next();
			Resource dataset = s.getSubject();
			String graph = s.getObject().as(Resource.class).getURI();
			// get all classes for each graph
			ArrayList<Resource> classes = new ArrayList<>();
			Iterator<RDFNode> nodeIt = model.listObjectsOfProperty(dataset, ResourceFactory.createProperty("http://aksw.org/deduplication/requiredClasses"));
			while(nodeIt.hasNext()) {
				Resource c = nodeIt.next().as(Resource.class);
				classes.add(c);
			}
			res.put(graph, classes);
		}
		return res;
	}

	/**
	 * @param input
	 * @return
	 */
	private static String sha1(String input) {
		MessageDigest mDigest = null;
		try {
			mDigest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
		}
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return sb.toString();
	}

}
