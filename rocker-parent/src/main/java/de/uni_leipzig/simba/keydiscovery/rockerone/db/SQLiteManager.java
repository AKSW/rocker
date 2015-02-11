package de.uni_leipzig.simba.keydiscovery.rockerone.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import de.uni_leipzig.simba.keydiscovery.util.Randomly;

/**
 * @author Tommaso Soru <t.soru@informatik.uni-leipzig.de>
 *
 */
public class SQLiteManager {
	
	private Statement statement;
	private Connection connection;
	private int dataSize;
	private HashMap<String, ArrayList<Property>> propRefMap;
	private String dbPrefix;
	
	private ResultSet resSet;
	
	// last used aliases for method next();
	private String[] aliasesCache;
	private String filename;

	public SQLiteManager(String dbPrefix)
			throws ClassNotFoundException, SQLException {

		filename = dbPrefix + "_" + Randomly.getRandom() + ".db";
		
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:"
				+ filename);
		connection.setAutoCommit(false);
		statement = connection.createStatement();
		statement.setQueryTimeout(30); // set timeout to 30 sec.
		
		this.dbPrefix = dbPrefix;
		this.propRefMap = new HashMap<String, ArrayList<Property>>();
	}
	
	public void setPropRef(String className, ArrayList<Property> propRef) {
		this.propRefMap.put(className, propRef);
	}
	
	public void createTable(String tableName, int dataSize) throws SQLException {

		if (dataSize <= 0)
			throw new IllegalArgumentException("Table size cannot be < 0.");

		this.dataSize = dataSize;

		statement.executeUpdate("drop table if exists " + tableName);

		String s = "";
		for (int i = 1; i <= dataSize; i++)
			s += "p" + i + ", ";
		s = s.substring(0, s.length() - 2);

		statement.executeUpdate("create table " + tableName + "(id, " + s + ")");
		connection.commit();
		
	}

	/**
	 * @param id
	 * @param hashes
	 * @throws SQLException
	 */
	public void insert(String tableName, String id, String[] hashes) throws SQLException {

		if (hashes.length != dataSize)
			throw new IllegalArgumentException(
					"Hashes shall be the same size of the table (except the first column).");
		
		String s = "";
		for (int i = 0; i < dataSize; i++)
			s += "'" + hashes[i] + "', ";
		s = s.substring(0, s.length() - 2);
		
		id = id.replace("'", "");
//		LOGGER.info(id);
		statement
				.executeUpdate("insert into " + tableName + " values('" + id + "', " + s + ")");
		
	}
	
	public String[] getAllHashes(String tableName, String id) throws SQLException {
		
		String[] res = new String[dataSize];
		
		ResultSet rs = statement
				.executeQuery("select * from " + tableName + " where id = '"+id.replaceAll("'", "\\'")+"'");
		while (rs.next()) {
			// read the result set
			for(int i=1; i<=dataSize; i++)
				res[i-1] = rs.getString("p" + i);
		}

		return res;
	}

	/**
	 * Initialize query for electing some columns only.
	 * 
	 * @param propertynames
	 * @throws SQLException
	 */
	public void getHashes(String tableName, String[] propertynames) throws SQLException {
		
		this.aliasesCache = new String[propertynames.length + 1];
		
		aliasesCache[0] = "id";
		for(int i=1; i<aliasesCache.length; i++)
			aliasesCache[i] = "p" + (propRefMap.get(tableName).indexOf(ResourceFactory.createProperty(propertynames[i-1])) + 1);
		
		String s = "";
		for(int i=1; i<aliasesCache.length; i++) {
			String p = aliasesCache[i];
			s += p + ", ";
		}
		s = s.substring(0, s.length() - 2);
		
		String query = "select id, " + s + " from " + tableName;
		this.resSet = statement
				.executeQuery(query);
		
	}

	public String[] next() throws SQLException {
		
		String[] res = new String[aliasesCache.length];
		
		if (resSet.next()) {
			
			res[0] = resSet.getString("id");
			// read the result set
			for(int i=0; i<res.length; i++)
				res[i] = resSet.getString(aliasesCache[i]);
			return res;
		} else {
			return null;
		}

	}

	public void commit() throws SQLException {
		connection.commit();
	}
	
	public void statementClose() throws SQLException {
		statement.close();
	}

	public void close() throws SQLException {
		connection.close();
		try {
		    new File(filename).delete();
		} catch (Exception x) {
		    System.err.format("Cannot delete db: "+filename);
		}
	}

	public String getDbPrefix() {
		return dbPrefix;
	}

}
