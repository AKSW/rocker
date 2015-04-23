package de.uni_leipzig.simba.keydiscovery.rockerone;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.riot.RiotException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import de.uni_leipzig.simba.keydiscovery.model.RKDClassTask;
import de.uni_leipzig.simba.keydiscovery.rockerone.db.SQLiteManager;
import de.uni_leipzig.simba.keydiscovery.util.Timer;

/**
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class DataIndexer {
	
	private final static Logger LOGGER = Logger.getLogger("ROCKER");

	private static final Property RDF_TYPE = ResourceFactory
			.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	private static final Property OWL_SAMEAS = ResourceFactory
			.createProperty("http://www.w3.org/2002/07/owl#sameAs");

	/**
	 * @param filename
	 * @param sql
	 * @param classname might be empty to get all classes FIXME an argument cannot be ""!
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static RKDClassTask index(final String filename, SQLiteManager sql, String classname) throws ClassNotFoundException, SQLException {
		
		Timer t = new Timer();

		Model m = ModelFactory.createDefaultModel();
		
		int errors = 0;
		try {
			m.read(filename, null, "N-TRIPLES");
		} catch (RiotException e) {
			errors++;
		}

		t.lap();
		print("model build runtime = " + t.getLastLapMillis());
		print("parse errors = " + errors);
		
		Resource classRes = m.getResource(classname);
		
		HashSet<Resource> instances = new HashSet<>();
		Iterator<Resource> subjects = m.listResourcesWithProperty(RDF_TYPE,
				classRes);
		while (subjects.hasNext())
			instances.add(subjects.next());

		t.lap();
		print("instance collection runtime = "
				+ t.getLastLapMillis());
		print("# instances = " + instances.size());
		
		// sorted reference for each property
		ArrayList<Property> propRef = new ArrayList<>();
		Iterator<Statement> statements = m.listStatements();
		TreeSet<String> temp = new TreeSet<String>();
		while (statements.hasNext()) {
			Statement st = statements.next();
			if(instances.contains(st.getSubject()))
				temp.add(st.getPredicate().getURI());
		}
		temp.remove(OWL_SAMEAS.getURI());
		temp.remove(RDF_TYPE.getURI());
		Iterator<String> it = temp.iterator();
		while(it.hasNext())
			propRef.add(ResourceFactory.createProperty(it.next()));
		
		int hashSize = propRef.size();
		
		LOGGER.info(propRef);
		
		// DB init
		String className = classRes.getLocalName();
		sql.setPropRef(className, propRef);
		sql.createTable(className, hashSize);

		t.lap();
		print("property collection runtime = "
				+ t.getLastLapMillis());
		print("# properties = " + propRef.size());
		
		RKDClassTask task = new RKDClassTask(classRes);
		task.setProperties(new HashSet<Property>(propRef));
		task.setPropRef(propRef);
		task.setResourceCount(instances.size());

		// for each instance
		for (Resource s : instances) {
			HashMap<Property, TreeSet<String>> localMap = new HashMap<>();
			Iterator<Statement> extension = m.listStatements(s, null,
					(RDFNode) null);
			while (extension.hasNext()) {
				Statement st = extension.next();
				Property p = st.getPredicate();
				if(p.equals(RDF_TYPE) || p.equals(OWL_SAMEAS))
					continue;
				
				String o = toIdentifier(st.getObject());
				if (localMap.containsKey(p)) {
					localMap.get(p).add(o);
				} else {
					TreeSet<String> objects = new TreeSet<String>();
					objects.add(o);
					localMap.put(p, objects);
				}
			}
//			print(localMap);
			
			String[] hashes = new String[hashSize];
			for(Property p : localMap.keySet())
				// save object queues
				hashes[propRef.indexOf(p)] = DigestUtils.shaHex(localMap.get(p).toString());
			
			// DB insert
			sql.insert(className, s.getURI(), hashes);
			
		}
		
		t.lap();
		print("indexing runtime = "
				+ t.getLastLapMillis());
				
		return task;

	}

	/**
	 * This ID may be either a URI or a literal.
	 * 
	 * @param o
	 * @return
	 */
	private static String toIdentifier(RDFNode o) {
		if (o.isResource())
			return o.isAnon() ? "http://rocker.aksw.org/"
					+ "blanknode/BN" + DigestUtils.shaHex(o.toString())
					: ((Resource) o).getURI();
		else
			return o.as(Literal.class).getString();
	}

	private static void print(String s) {
		LOGGER.info(s);
	}


}
