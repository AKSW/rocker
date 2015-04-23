package de.uni_leipzig.simba.keydiscovery.rockerone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

import de.uni_leipzig.simba.keydiscovery.model.CandidateNode;
import de.uni_leipzig.simba.keydiscovery.model.RKDClassTask;
import de.uni_leipzig.simba.keydiscovery.util.Timer;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Algorithm {
	
	private final static Logger LOGGER = Logger.getLogger("ROCKER");

	private Set<CandidateNode> classKeys;
	private List<CandidateNode> atomicCandidates;
	private CandidateNode topElement;
	private String outputFile;
	private RKDClassTask c;
	private boolean FIND_ONE_KEY, FAST_SEARCH, PROP_REDUCTION;
	private Score scr;
	
	private double overallVisitedNodes = 0.0;

	
	protected Algorithm(String outputFile, Score scr, RKDClassTask c, boolean FIND_ONE_KEY, boolean FAST_SEARCH, boolean PROP_REDUCTION) {
		this.outputFile = outputFile;
		this.c = c;
		this.FIND_ONE_KEY = FIND_ONE_KEY;
		this.FAST_SEARCH = FAST_SEARCH;
		this.PROP_REDUCTION = PROP_REDUCTION;
		this.scr = scr;
	}

	private static void print(String s) {
		LOGGER.info(s);
	}
	
	protected Set<CandidateNode> start() {
		
		atomicCandidates = new ArrayList<CandidateNode>();
		
		print("Class: "+c.getClassName());
		
		Timer t = new Timer();
		findKeysOfClass(c);
		t.lap();
		
		print("RUNTIME (ms): "+t.getLastLapMillis());
		c.setRuntime(t.getLastLapMillis());
		classKeys = c.getKeys();
		print(classKeys.size()+" KEYS FOUND:");
		for(CandidateNode k : classKeys) {
			print(k.getScore()+"\t"+k.toString());
		}
		
		if(outputFile != null) {
			print("Saving model...");
			ModelManager.save(c, ModelFactory.createDefaultModel(), 
					 outputFile);
		}

		print("------------");
		
		return classKeys;
	}
	
	/**
	 * Runs the refinement key discovery algorithm on a class.
	 * 
	 * @param c
	 */
	private void findKeysOfClass(RKDClassTask c) {
		
		print("FIND_ONE_KEY = "+FIND_ONE_KEY);
				
		Set<Property> properties = c.getProperties();
		if(properties.isEmpty())
			return;
		print("Visited nodes (partial): "+overallVisitedNodes);
		// frontier (or leaves) for tree climbing
		TreeSet<CandidateNode> frontier = new TreeSet<CandidateNode>();
		Set<CandidateNode> keys = c.getKeys();
		
		// create atomic candidates
		for(Property p : properties) {
			Set<Property> pSet = new HashSet<Property>();
			pSet.add(p);
			CandidateNode cn = new CandidateNode(pSet);
			atomicCandidates.add(cn);
			double score = scr.getScore(c, cn);
			cn.setScore(score);
			print("Candidate: "+score+"\t|P|="+pSet.size()+"\t"+cn);
			overallVisitedNodes++;
			if(cn.isAlmostKey()) {
				keys.add(cn);
				print("Key: "+cn);
				if(FIND_ONE_KEY)
					return;
			} else
				frontier.add(cn);
		}
		print("Atomic non-keys: "+frontier.size()+"\t"+frontier);
		// discard atomic candidates with score < lower bound
		Iterator<CandidateNode> itr = frontier.iterator();
		while(itr.hasNext()) {
			CandidateNode cand = itr.next();
			print("score("+cand+") = "+cand.getScore());
			if(cand.getScore() < 1E-3 && PROP_REDUCTION)
				itr.remove();
		}
		TreeSet<CandidateNode> frontierBackup = new TreeSet<>(frontier);
		Set<CandidateNode> atomic = null;
		HashMap<String, Double> atomicMap = null;
		Set<CandidateNode> maxNonKeys = null;
		frontier = new TreeSet<>(frontierBackup);
		atomic = new TreeSet<CandidateNode>(frontier);
		print("Atomic non-keys after cut: "+atomic.size()+"\t"+atomic);
		// mapping between atomic non-keys and their score
		atomicMap = new HashMap<String, Double>();
		for(CandidateNode a : atomic)
			atomicMap.put(a.toString(), a.getScore());
		// set of maximal non-keys
		maxNonKeys = new TreeSet<CandidateNode>(frontier);
		
		if(hasNoKey(atomic, c)) {
			print("No key found.");
			return;
		}
		// main loop
		while(true) {
			print("FRONTIER SIZE: "+frontier.size());
			if(frontier.isEmpty()) {
				print("All leaves have score = 1.0");
				return;
			}
			// select highest non-visited element
			CandidateNode pivot = frontier.first();
			// remove pivot from frontier
			removeFrom(pivot, frontier);
			print("Pivot found! "+pivot.getScore()+"\t"+pivot);
			// add children discarding duplicates
			Set<CandidateNode> children = refine(pivot, atomic, c, keys, maxNonKeys, atomicMap);
			updateNonKeys(maxNonKeys, children);
			for(CandidateNode child : children) {
				if(child.isAlmostKey()) {
					keys.add(child);
					print("Key: "+child);
					if(FIND_ONE_KEY)
						return;
				}
			}
			for(CandidateNode child : children) {
				if(child.isAlmostKey()) {
					if(FAST_SEARCH) {
						Iterator<CandidateNode> it = atomic.iterator();
						while(it.hasNext()) {
							CandidateNode a = it.next();
							if(child.getProperties().containsAll(a.getProperties()))
								it.remove();
						}
					}
					// check top element of remaining properties
					if(hasNoKey(atomic, c))
						return;
				} else {
					frontier.add(child);
				}
			}
		}
	}

	public List<CandidateNode> getAtomicCandidates() {
		return atomicCandidates;
	}

	private Set<CandidateNode> refine(CandidateNode node, Set<CandidateNode> atomic, RKDClassTask c, 
			Set<CandidateNode> keys, Set<CandidateNode> maxNonKeys, HashMap<String, Double> atomicMap) {
		
		print("Refine: add children");
		Set<CandidateNode> children = new TreeSet<CandidateNode>();
		// get properties currently in the candidate
		Set<Property> currProp = node.getProperties();
		// get minimum property by score
		double minScore = Double.POSITIVE_INFINITY;
		for(Property p : node.getProperties()) {
			// all atomic properties have score=0
			if(atomicMap == null) {
				minScore = 0;
				break;
			}
			Double d = atomicMap.get("["+p.getURI()+"]");
			if(d < minScore)
				minScore = d;
		}
		for(CandidateNode a : atomic) {
			// constraint: score({p_i}) <= min(score(node))
			if(a.getScore() > minScore)
				continue;
			// get the only property
			Property p = a.getProperties().iterator().next();
			if(!currProp.contains(p)) {
				// create child
				Set<Property> childProp = new HashSet<Property>(currProp);
				childProp.add(p);
				CandidateNode cn = new CandidateNode(childProp);
//				if(keys.containsKey(cn.toString()))
				if(descendantOfAKey(cn, keys) || ancestorOfANonKey(cn, maxNonKeys))
					continue;
				double score = scr.getScore(c, cn);
				cn.setScore(score);
				overallVisitedNodes++;
				print("Candidate: "+cn.getScore()+"\t"+cn);
				children.add(cn);
			}
		}
		return children;
	}

	private boolean ancestorOfANonKey(CandidateNode cn,
			Set<CandidateNode> maxNonKeys) {
		for(CandidateNode nk : maxNonKeys) {
			if(nk.getScore() == -1.0) // cannot say anything on an error
				continue;
			if(descendantOf(nk, cn)) {
				print("Ancestor found: "+cn+" is ancestor of non-key "+nk);
				return true;
			}
		}
		return false;
	}

	private boolean descendantOfAKey(CandidateNode cn,
			Set<CandidateNode> keys) {
		for(CandidateNode k : keys)
			if(descendantOf(cn, k)) {
				print("Descendant found: "+cn+" is descendant of key "+k);
				return true;
			}
		return false;
	}

	private boolean descendantOf(CandidateNode cn, CandidateNode k) {
		return cn.getProperties().containsAll(k.getProperties());
	}
	
	
	private void updateNonKeys(Set<CandidateNode> maxNonKeys,
			Set<CandidateNode> children) {
		for(CandidateNode child : children) {
			boolean removed = false;
			Iterator<CandidateNode> it = maxNonKeys.iterator();
			while(it.hasNext()) {
				CandidateNode nonKey = it.next();
				if(descendantOf(child, nonKey)) {
					it.remove();
					removed = true;
				}
			}
			if(removed)
				maxNonKeys.add(child);
		}
		
	}

	private boolean removeFrom(CandidateNode cn, TreeSet<CandidateNode> nodes) {
		Iterator<CandidateNode> it = nodes.iterator();
		CandidateNode next;
		while(it.hasNext()) {
			next = it.next();
			if(cn.equals(next)) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the "top" element, i.e. the candidate containing all of the properties, has a score lower than 1.
	 * 
	 * @param atomic
	 * @param rdfClass
	 * @param count 
	 * @return
	 */
	private boolean hasNoKey(Set<CandidateNode> atomic, RKDClassTask c) {
		
		if(atomic.isEmpty()) {
			print("Non-key set is empty!");
			return false;
		}
		// in this case, top = the only atomic node
		if(atomic.size() == 1) {
			print("Top element is atomic");
			return true;
		}
		overallVisitedNodes++;
		Set<Property> properties = new HashSet<Property>();
		for(CandidateNode node : atomic)
			properties.addAll(node.getProperties());
		CandidateNode cn = new CandidateNode(properties);
		double score = scr.getScore(c, cn);
		cn.setScore(score);
		print("Top element "+cn+" has score = "+score);
		if(topElement == null)
			topElement = cn;
		return !cn.isAlmostKey();
	}

	public CandidateNode getAtomicCandidate(Property p) {
		for(CandidateNode cn : atomicCandidates) {
			Property p1 = cn.getProperties().iterator().next();
			if(p.getURI().equals(p1.getURI()))
				return cn;
		}
		return null;
	}

	public CandidateNode getTopElement() {
		return topElement;
	}
	
}
