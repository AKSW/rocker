package de.uni_leipzig.simba.keydiscovery.model;

/**
 * @author Tommaso Soru <t.soru@informatik.uni-leipzig.de>
 *
 */
public class FaultyPair implements Comparable<FaultyPair> {

	private String sourceURI, targetURI;

	public FaultyPair(String sourceURI, String targetURI) {
		super();
		this.setSourceURI(sourceURI);
		this.setTargetURI(targetURI);
	}

	public String getSourceURI() {
		return sourceURI;
	}

	public void setSourceURI(String sourceURI) {
		this.sourceURI = sourceURI;
	}

	public String getTargetURI() {
		return targetURI;
	}

	public void setTargetURI(String targetURI) {
		this.targetURI = targetURI;
	}

	@Override
	public int compareTo(FaultyPair o) {
		return this.toString()
				.compareTo(o.toString());
	}
	
	public String toString() {
		return "{"+sourceURI+", "+targetURI+"}";
	}
	
}
