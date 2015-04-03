package de.uni_leipzig.simba.keydiscovery.rockerone;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Property;

import de.uni_leipzig.simba.keydiscovery.model.CandidateNode;
import de.uni_leipzig.simba.keydiscovery.model.RKDClassTask;
import de.uni_leipzig.simba.keydiscovery.rockerone.db.SQLiteManager;

/**
 * @author Tommaso Soru <t.soru@informatik.uni-leipzig.de>
 *
 */
public class Score {
	
	private final static Logger LOGGER = Logger.getLogger("ROCKER");

	private static final boolean ROCKER_KEY_DEFINITION = true;
	
	private String dataset;
	private SQLiteManager sql;
	private double quasi_keys_rate;
	
	public Score(String dataset, SQLiteManager sql, double quasi_keys_rate) {
		this.dataset = dataset;
		this.sql = sql;
		this.quasi_keys_rate = quasi_keys_rate;
	}

	public double getScore(RKDClassTask c, CandidateNode cn) {
		
		//LOGGER.info("Using DB "+sql.getDbPrefix());
		
		// properties to strings
		Set<Property> inputProperties = cn.getProperties();
		String[] properties = new String[inputProperties.size()];
		Iterator<Property> it = inputProperties.iterator();
		for(int i=0; it.hasNext(); i++)
			properties[i] = it.next().getURI();
		
		// process score
		String[] hashes = null;
		TreeSet<String> distincts = new TreeSet<String>();
		try {
			// prepare
			String name = c.getResource().getLocalName();
			sql.getHashes(name, properties);
			HashMap<String, String> hashToURI = new HashMap<String, String>();
			outer: while(true) {
				hashes = sql.next();
				if(hashes == null)
					break;
				String composed = "";
				// first element is the resource ID
				String resourceID = hashes[0];
				for(int i=1; i<hashes.length; i++) {
					String hash = hashes[i];
					// code for Atencia's supposedly wrong definition of keys
					if(!ROCKER_KEY_DEFINITION)
						if(hash.equals("null"))
							continue outer;
					composed += hash + ",";
				}
				if(distincts.add(composed)) {
					hashToURI.put(composed, resourceID);
				} else {
					LOGGER.debug(resourceID + " is faulty for " + cn);
					cn.addFaultyResourceURI(hashToURI.get(composed), resourceID);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(distincts.size() >= quasi_keys_rate * c.getResourceCount()) {
			double d = (double) distincts.size() / c.getResourceCount();
			LOGGER.info("almost-key found: "+d+" "+cn.getProperties());
			cn.setAlmostKey(true);
			return d;
		}
		return (double) distincts.size() / c.getResourceCount();
	}
	
	public String getDataset() {
		return dataset;
	}

}
