package de.uni_leipzig.simba.keydiscovery.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Issue {
	
	private final static Logger LOGGER = Logger.getLogger("ROCKER");
	
	private Map<Property, Set<RDFNode>> map;
	
	public Issue(Set<Property> properties) {
		map = new HashMap<Property, Set<RDFNode>>();
		for(Property p : properties)
			map.put(p, null);
	}

	public void compute(String fres, Model m) {
		
		// collect objects
		for(Property p : map.keySet()) {
			Resource s = m.getResource(fres);
			Set<RDFNode> objset = m.listObjectsOfProperty(s, p).toSet();
			LOGGER.debug("<"+s+"> <"+p+">");
			LOGGER.debug("size(objset) = "+objset.size());
			map.put(p, objset);
			
			// TODO
			if(objset.isEmpty())
				;
		}
		
		LOGGER.debug(map);
		
	}

}
