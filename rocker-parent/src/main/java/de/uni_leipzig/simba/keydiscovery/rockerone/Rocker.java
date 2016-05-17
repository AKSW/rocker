package de.uni_leipzig.simba.keydiscovery.rockerone;

import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.jena.riot.RiotException;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

import de.uni_leipzig.simba.keydiscovery.model.CandidateNode;
import de.uni_leipzig.simba.keydiscovery.model.FaultyPair;
import de.uni_leipzig.simba.keydiscovery.model.Issue;
import de.uni_leipzig.simba.keydiscovery.model.RKDClassTask;
import de.uni_leipzig.simba.keydiscovery.rockerone.db.SQLiteManager;
import de.uni_leipzig.simba.keydiscovery.util.Randomly;


/**
 * @author Tommaso Soru {@literal (tsoru@informatik.uni-leipzig.de)}
 *
 */
public class Rocker implements Runnable {
	
	private final static Logger LOGGER = Logger.getLogger("ROCKER");

	private String dataset, inputFile, classname;
	private boolean find_one_key, fast_search, prop_reduction = 
			Boolean.parseBoolean(bundle.getString("propreduction"));
	private SQLiteManager sql;
	private Score scr;
	public static final ResourceBundle bundle =
            ResourceBundle.getBundle("rocker");
	public static String WORKSPACE_DIR;
	public static final String OUTPUT_PREFIX = bundle.getString("outputprefix");
	public static final boolean ENABLE_VISUALIZATION = Boolean.parseBoolean(bundle.getString("visualization"));
	private Algorithm algo;
	private Set<CandidateNode> keysFound;
	private String jsonString;
	
	public Rocker(String dataset, String inputFile, String classname, boolean find_one_key,
			boolean fast_search, double quasi_keys_rate) throws ClassNotFoundException, SQLException {
		
		if(bundle.containsKey("workspace"))
			WORKSPACE_DIR = bundle.getString("workspace") + "/";
		else
			WORKSPACE_DIR = System.getProperty("user.dir") + "/";
		
		this.setDataset(dataset);
		this.setInputFile(inputFile);
		this.setClassname(classname);
		this.find_one_key = find_one_key;
		this.fast_search = fast_search;
		this.sql = new SQLiteManager(WORKSPACE_DIR + dataset);
		LOGGER.info("name: " + dataset);
		LOGGER.info("workspace: " + WORKSPACE_DIR);
		LOGGER.info("file URI: " + inputFile);
		this.scr = new Score(dataset, sql, quasi_keys_rate);
	}

	public void run() {
		
		try {
			
			RKDClassTask cLass = DataIndexer.index(inputFile, sql, classname);
			sql.commit();
			
			String out = ENABLE_VISUALIZATION
					? null
					: WORKSPACE_DIR + OUTPUT_PREFIX + "_" + Randomly.getRandom() + ".n3";
			
			algo = new Algorithm(out, scr, cLass, find_one_key, fast_search, prop_reduction);
			keysFound = algo.start();
			sql.statementClose();
			
			if(ENABLE_VISUALIZATION)
				computeJSONString();
			
			sql.close();
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public Set<CandidateNode> getKeys() {
		return keysFound;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public Algorithm getAlgo() {
		return algo;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getJSONString() {
		return jsonString;
	}
	
	@SuppressWarnings("unchecked")
	private void computeJSONString() {
		
		Model m = ModelFactory.createDefaultModel();
		
		try {
			m.read(inputFile, null, "N-TRIPLES");
		} catch (RiotException e) {}

		
		JSONObject obj = new JSONObject();
		obj.put("class_uri", classname);
		JSONArray keys = new JSONArray();
		obj.put("keys", keys);
		for(CandidateNode cn : keysFound) {
			JSONObject key = new JSONObject();
			keys.add(key);
			JSONArray props = new JSONArray();
			key.put("properties", props);
			for(Property p : cn.getProperties())
				props.add(p.getURI());
			JSONArray issues = new JSONArray();
			key.put("issues", issues);
			
			for(FaultyPair fpair : cn.getFaultyPairs()) {
				JSONObject issue = new JSONObject();
				/* 
				 * put objectgroups
				 * 
				 * click on an objectgroup => get triples with 'faulty' flag (get them complete, as nothing is implied) 
				 * 
				 */
				Issue is = new Issue(cn.getProperties());
				is.compute(fpair.getSourceURI(), m);
				
				// TODO also for target!
				
				issues.add(issue);
			}
		}			
		
		jsonString = obj.toJSONString();
		
	}

}
