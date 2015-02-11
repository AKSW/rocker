package de.uni_leipzig.simba.keydiscovery;

import java.util.Set;

import de.uni_leipzig.simba.keydiscovery.model.CandidateNode;

/**
 * Interface for key discovery algorithms identifying key properties for RDF data sets.
 * @author Klaus Lyko
 *
 */
public interface IKeyDiscovery {

//	interfaces have no constructor
	
	/**
	 * 
	 * @param datasetName Name of the data set
	 * @param inputFile	Path of input RDF file
	 * @param classname Targeted class
	 * @param oneKey If set computes a single CandidateNode
	 * @param coverage value in (0,1] percentage the keys should cover
	 * @param fastSearch In set runs fast version.
	 */
	public void init(String datasetName, String inputFile, String classname, boolean oneKey,
			double coverage, boolean fastSearch);
	
	/**
	 * Run key discovery algorithm
	 */
	public void run();

	/**
	 * Return results of key discovery run.
	 * @return CandidateNodes representing keys of the dataset.
	 */
	public Set<CandidateNode> getResults();
}
