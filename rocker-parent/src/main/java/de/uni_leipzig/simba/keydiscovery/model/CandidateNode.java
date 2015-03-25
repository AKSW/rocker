/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.keydiscovery.model;

import com.hp.hpl.jena.rdf.model.Property;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * @author ngonga
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class CandidateNode implements Comparable<CandidateNode> {

	private Set<Property> properties;
	private double score;
	// not used
	private List<CandidateNode> children;
	// not used
	private int index;
	private boolean isAlmostKey = false;
	
	private Set<String> faultyResourceURIs;

	public CandidateNode(Set<Property> properties) {
		this.properties = properties;
//		this.index = index;
		children = new ArrayList<CandidateNode>();
		faultyResourceURIs = new TreeSet<String>();
	}

	public void setScore(double score) {
		this.score = score;
	}

	public List<CandidateNode> getChildren() {
		return children;
	}
	
	public Set<Property> getProperties() {
		return properties;
	}

	public double getScore() {
		return score;
	}

	public boolean isAlmostKey() {
		return isAlmostKey;
	}

	public void setAlmostKey(boolean isAlmostKey) {
		this.isAlmostKey = isAlmostKey;
	}

	public int getIndex() {
		return index;
	}

	public int compareTo(CandidateNode o) {
		// returning 0 means same object!
		if(sameSets(o.properties, properties))
			return 0;
		// this is for non-ambiguous ordering in a list
		if (o.score >= score) {
			return +1;
		} else {
			return -1;
		}
	}
		
	private boolean sameSets(Set<Property> set1, Set<Property> set2) {
		if(set1.size() != set2.size())
			return false;
		return set1.containsAll(set2) && set2.containsAll(set1);
	}

	public String toString() {
		Set<String> props = new TreeSet<String>();
		for(Property p : properties)
			props.add(p.toString());
		return props.toString();
	}
	
	public Set<String> getPropertySet() {
		Set<String> props = new TreeSet<String>();
		for(Property p : properties)
			props.add("\""+p.toString()+"\"");
		return props;
	}
	
	public boolean equals(Object o) {
		if(o instanceof CandidateNode) {
			CandidateNode ocn = (CandidateNode) o;
			return ocn.toString().equals(this.toString());
		}
		return false;
	}

	public Set<String> getFaultyResourceURIs() {
		return faultyResourceURIs;
	}

	public void addFaultyResourceURI(String faultyResourceURI) {
		this.faultyResourceURIs.add(faultyResourceURI);
	}

}
