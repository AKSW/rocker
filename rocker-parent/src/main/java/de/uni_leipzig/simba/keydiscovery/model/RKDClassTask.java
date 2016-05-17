package de.uni_leipzig.simba.keydiscovery.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A refinement key discovery task on a given class.
 * 
 * @author Tommaso Soru {@literal (tsoru@informatik.uni-leipzig.de)}
 * 
 */
public class RKDClassTask implements Comparable<RKDClassTask> {
	
	private String className;
	private Resource resource;
	private int resourceCount;
	private Set<CandidateNode> keys = null;
	private Set<Property> properties = null;
	private double runtime;
	private ArrayList<Property> propRef;

	public RKDClassTask(Resource resource) {
		super();
		this.setResource(resource);
		this.setClassName(resource.getURI());
		this.setKeys(new TreeSet<CandidateNode>());
		this.setProperties(new HashSet<Property>());
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getResourceCount() {
		return resourceCount;
	}

	public void setResourceCount(int resourceCount) {
		this.resourceCount = resourceCount;
	}

	public Set<CandidateNode> getKeys() {
		return keys;
	}

	public void setKeys(Set<CandidateNode> keys) {
		this.keys = keys;
	}

	@Override
	public int compareTo(RKDClassTask o) {
		if(this.className.equals(o.className))
			return 0;
		if(this.resourceCount >= o.resourceCount)
			return +1;
		else
			return -1;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Gets the keys returned by the `refine` method as sets of properties.
	 * 
	 * @param rdfClass
	 * @return
	 */
	public Set<Set<Property>> getKeysAsSets(Resource rdfClass) {
		if(keys == null)
			return null;
		Set<Set<Property>> output = new HashSet<Set<Property>>();
		for(CandidateNode c : keys) {
			output.add(c.getProperties());
		}
		return output;
	}

	public Set<Property> getProperties() {
		return properties;
	}

	public void setProperties(Set<Property> properties) {
		this.properties = properties;
	}

	public double getRuntime() {
		return runtime;
	}

	public void setRuntime(double runtime) {
		this.runtime = runtime;
	}

	public ArrayList<Property> getPropRef() {
		return propRef;
	}

	public void setPropRef(ArrayList<Property> propRef) {
		this.propRef = propRef;
	}

}
