// ############################################################################
// File Name: demoappjavaweb.java
// Description:
//  Simple Java Web application using the base jre:8 image and Spark
// Author: Peter Leung (pleungms@hotmail.com)
// Modification history
//  Author         Date       Description
//  -------------- ---------- -------------------------------------------------
//  Peter Leung    27/09/2018 Initial version
//
// ############################################################################

// define the name of the package
package org.pleungms.dockerize;

// import the java libraries
// *** note: should clean up to only import then necessary classes
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.*;

// import the java libraries
// *** note: should clean up to only import then necessary classes
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;

// import the MSSQL Server JDBC libraries
// *** note: should clean up to only import then necessary classes
import com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionJavaKeyStoreProvider;
import com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionKeyStoreProvider;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.microsoft.sqlserver.jdbc.*;

import static spark.Spark.get;

/**
 * Created by pleungms
 */
public class demoappjavaweb {

    // Class variables, quick and dirty way...
    static final String DEFAULT_USER = "World";
    static int    numRows;
    static ResultSet rs;
    static String rsString;

    public static void main(String[] args) {

        // Temporary variables, quick and dirty way...
		String resStatus = "init";
		String resRow = "";
		numRows = 0;
		rsString = "";

        // get the SQL user id from the commandline
		String sqluser = getSQLUser();
		System.out.println("sqluser: " + sqluser);

		//String sqlStmt = "SELECT * FROM TBL_PRO_PROFILES";
		String sqlStmt = "SELECT ID, TXT, SECRET_TXT FROM TEST_ENCRYPT_TBL";

		// Connection String without Encryption
		//String connectionUrl = "jdbc:sqlserver://pleungms.database.windows.net:1433;database=PLEUNGDB;user=pleungadmin@pleungms;password=!P@ssw0rd;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";

		// Connection string with Encryption
		String connectionUrl = "jdbc:sqlserver://pleungms.database.windows.net:1433;database=PLEUNGDB;user=pleungadmin@pleungms;password=!P@ssw0rd;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;columnEncryptionSetting=Enabled;";

        //SQLServerConnection connection = (SQLServerConnection) DriverManager.getConnection(connectionUrl);

        // Appid for School 1, each school will have a Web App registered through the Azure Active Directory > App Registeration
        // This clientid (Application ID)
        String clientID = "668deea9-af08-47b2-8c81-eb0fec6c904f";
        // Key1 for Test App, from the Web App created in the above step to get the clientid, a key is created
        // This key is only shown at creation (upon save) and is hidden afterwards; this key needs to be copied and saved
        // into the Java web app source code and compiled
        String clientKey = "8vQXYxF+0JFTu8aPlulVEmiZQk1Yr9fdYcm/B34tm6Y=";

        try {

            Connection connection = DriverManager.getConnection(connectionUrl);

            //////////////////////////////////////////////////////////////////
            // SQL Server encryption; which requires getting access to the Azure Key Vault
            System.out.println("Starting to get SQLServerColumnEncryptionAzureKeyVaultProvider...");
 			SQLServerColumnEncryptionAzureKeyVaultProvider akvProvider = new SQLServerColumnEncryptionAzureKeyVaultProvider(clientID, clientKey);

 			System.out.println("Starting to get keyStoreMap...");
            Map<String, SQLServerColumnEncryptionKeyStoreProvider> keyStoreMap = new HashMap<String, SQLServerColumnEncryptionKeyStoreProvider>();

            System.out.println("Starting to put keyStoreMap...");
            keyStoreMap.put(akvProvider.getName(), akvProvider);

            System.out.println("Starting to update SQLServerConnection...");
            SQLServerConnection.registerColumnEncryptionKeyStoreProviders(keyStoreMap);

            System.out.println("Completed SQL Server encryption!");
            //////////////////////////////////////////////////////////////////

            System.out.println("Preparing SQL Statement...");
            PreparedStatement selectStatement = connection.prepareStatement(sqlStmt);

            System.out.println("Executing SQL Statement...");
            rs = selectStatement.executeQuery();
            System.out.println("Executed statement successful!");

            // Going through each row of the result set
            while (rs.next()) {

                //resRow = "[ " + rs.getString("PRO_ID") + " " + rs.getString("PRO_NAME") + " " + rs.getString("PRO_EMAIL") + " ] ";
                resRow = "[" + rs.getString("ID") + "," + rs.getString("TXT") + "," + rs.getString("SECRET_TXT") + "]";

                System.out.println(resRow);
                rsString += resRow;
                numRows++;
            } // while rs.next()
		}
        // Handle any errors that may have occurred.
	    catch (SQLException e) {
	        e.printStackTrace();
        } // try connection

        // Sending the resultset back to the http get request
        get("/", (req, res) -> String.format("[demoappjavaweb using Always Encrypted] %s [%s]", getRsString(), sqluser));
    } // demoappjavaweb

    // Getting the environment variable from the command line
    public static String getUsername() {
        // 1. check for presence of environment variable
        String user = System.getenv("USER");
        if(user == null) {
            // 2. load from properties file, if available
            Properties props = new Properties();
            try(InputStream instream = new FileInputStream("/data/application.properties")) {
                props.load(instream);
                return props.getProperty("user.name", DEFAULT_USER);
            }
            catch(IOException e) {
                return DEFAULT_USER;
            }
        }
        return user;
    }

    // Getting the environment variable from the command line
    public static String getSQLUser() {
        // 1. check for presence of environment variable
        String sqluser = System.getenv("SQLUSER");
        if(sqluser == null) {
            // 2. load from properties file, if available
            Properties props = new Properties();
            try(InputStream instream = new FileInputStream("/data/application.properties")) {
                props.load(instream);
                return props.getProperty("sqluser.name", DEFAULT_USER);
            }
            catch(IOException e) {
                return DEFAULT_USER;
            }
        }
        return sqluser;
    }

    // Quick and dirty way to return the number of rows
    public static int getNumRows() {
		return numRows;
    }

    // Quick and dirty way to return the result set
    public static String getRsString() {
		return rsString;
    }
}
