package de.uni_leipzig.simba.keydiscovery;

import java.sql.SQLException;
import java.util.Set;

import de.uni_leipzig.simba.keydiscovery.model.CandidateNode;
import de.uni_leipzig.simba.keydiscovery.rockerone.Rocker;

/**
 * @author Tommaso Soru {@literal (tsoru@informatik.uni-leipzig.de)}
 *
 */
public class RockerKeyDiscovery implements IKeyDiscovery {
	
	private Rocker rocker;

	@Override
	public void init(String datasetName, String inputFile, String classname,
			boolean oneKey, double coverage, boolean fastSearch) {
		try {
			rocker = new Rocker(datasetName, inputFile, classname, oneKey, fastSearch, coverage);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		rocker.run();
	}

	@Override
	public Set<CandidateNode> getResults() {
		return rocker.getKeys();
	}
	
	public static void main(String[] args) {
		
		Rocker rocker = null;
		
		try {
			rocker = new Rocker(args[0], args[1], args[2], Boolean.parseBoolean(args[3]), 
					Boolean.parseBoolean(args[4]), Double.parseDouble(args[5]));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		rocker.run();
	}
	
	/**
	 * @return The algorithm object which provides insights from the key discovery task.
	 */
	public Rocker getRockerObject() {
		return rocker;
	}

}
