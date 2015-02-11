package de.uni_leipzig.simba.keydiscovery;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import de.uni_leipzig.simba.keydiscovery.model.CandidateNode;
import de.uni_leipzig.simba.keydiscovery.rockerone.Rocker;

/**
 * @author Tommaso Soru <t.soru@informatik.uni-leipzig.de>
 *
 */
public class RockerTest {
	
	@Test
	public void testGetKeys() {

		String name = "person11";
		String workingDir = System.getProperty("user.dir");
		String file = "file://"+workingDir+"/data/OAEI_2011_Person1_1.nt";
		String classname = "http://www.okkam.org/ontology_person1.owl#Person";
		
		boolean find1key = false;
		boolean fastsearch = true;
		double threshold = 1.0;
		
		Rocker r = null;
		try {
			r = new Rocker(name, file, classname,
					find1key, fastsearch, threshold);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		r.run();
		Set<CandidateNode> results = r.getKeys(); 
		
		assertTrue(results.size() == 4);
		for(int i=1; i<=4; i++)
			assertTrue(isIn(getProperties(i), results));
				
	}

	private static boolean isIn(Set<Property> prop1, Set<CandidateNode> results) {
		for(CandidateNode cn : results) {
			Set<Property> prop2 = cn.getProperties();
			// if prop1 equals prop2 => prop1 is in results, else continue
			if(prop1.toString().equals(prop2.toString()))
				return true;
		}
		return false;
	}

	private static Set<Property> getProperties(int i) {
		HashSet<Property> prop = new HashSet<Property>();
		switch(i) {
		case 1:
			prop.add(ResourceFactory.createProperty("http://www.okkam.org/ontology_person1.owl#has_address"));
			break;
		case 2:
			prop.add(ResourceFactory.createProperty("http://www.okkam.org/ontology_person1.owl#soc_sec_id"));
			break;
		case 3:
			prop.add(ResourceFactory.createProperty("http://www.okkam.org/ontology_person1.owl#given_name"));
			prop.add(ResourceFactory.createProperty("http://www.okkam.org/ontology_person1.owl#phone_numer"));
			prop.add(ResourceFactory.createProperty("http://www.okkam.org/ontology_person1.owl#surname"));
			break;
		case 4:
			prop.add(ResourceFactory.createProperty("http://www.okkam.org/ontology_person1.owl#age"));
			prop.add(ResourceFactory.createProperty("http://www.okkam.org/ontology_person1.owl#phone_numer"));
			prop.add(ResourceFactory.createProperty("http://www.okkam.org/ontology_person1.owl#surname"));
			break;
		}
		return prop;
	}
	
}
