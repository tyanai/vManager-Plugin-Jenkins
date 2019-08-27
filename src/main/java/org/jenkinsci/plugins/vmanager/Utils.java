package org.jenkinsci.plugins.vmanager;

import hudson.model.TaskListener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;

import javax.net.ssl.*;

import org.apache.commons.codec.binary.Base64;

import net.sf.json.JSONObject;

public class Utils {
	
	private String UserUsedForLogin = null;
        private String PasswordUsedForLogin = null;
	

	public BufferedReader loadFileFromWorkSpace(String buildID, int buildNumber, String workPlacePath, String inputFile, TaskListener listener, boolean deleteInputFile, String fileTypeEndingName)
			throws Exception { 

		BufferedReader reader = null;
		String fileName = null;
		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		try {

			if ("".equals(inputFile) || inputFile == null) {
				fileName = workPlacePath + File.separator + buildNumber + "." + buildID + "." + fileTypeEndingName;
				if (notInTestMode) {
					listener.getLogger().print("Loading input file '" + fileName + "\n");
				}
				reader = new BufferedReader(new FileReader(fileName));
			} else {
				//fileName = workPlacePath + File.separator + inputFile;
				fileName = inputFile;
				if (notInTestMode) {
					listener.getLogger().print("Loading input file '" + fileName + "\n");
				}
				reader = new BufferedReader(new FileReader(fileName));
			}
		} catch (Exception e) {

			if (notInTestMode) {
				listener.getLogger().print("Failed to open input file.  Failed to load file '" + fileName + "'\n");

			} else {

				System.out.println("Failed to open the input file .  Failed to load file '" + fileName + "'");
			}

			throw e;
		}
		return reader;

	}

	public String[] loadVSIFFileNames(String buildID, int buildNumber, String workPlacePath, String vSIFInputFile, TaskListener listener, boolean deleteInputFile) throws Exception {
		String[] output = null;
		List<String> listOfNames = new LinkedList<String>();
		BufferedReader reader = null;
		String fileName = null;
		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		// Set the right File name.
		if ("".equals(vSIFInputFile) || vSIFInputFile == null) {
			fileName = workPlacePath + File.separator + buildNumber + "." + buildID + "." + "vsif.input";
		} else {
			fileName = vSIFInputFile;
		}

		try {

			reader = this.loadFileFromWorkSpace(buildID, buildNumber, workPlacePath, vSIFInputFile, listener, deleteInputFile, "vsif.input");
			String line = null;
			while ((line = reader.readLine()) != null) {
				listOfNames.add(line);
			}

		} catch (Exception e) {

			if (notInTestMode) {
				listener.getLogger().print("Failed to read input file for the vsif targets.  Failed to load file '" + fileName + "'\n");
			} else {

				System.out.println("Failed to open the read file for the vsif targets.  Failed to load file '" + fileName + "'");
			}

			throw e;
		} finally {
			reader.close();
		}

		Iterator<String> iter = listOfNames.iterator();
		output = new String[listOfNames.size()];
		int i = 0;
		if (notInTestMode) {
			listener.getLogger().print("Found the following VSIF files for vManager launch:\n");
		}
		String vsiffileName = null;
		while (iter.hasNext()) {
			vsiffileName = new String(iter.next());
			output[i++] = vsiffileName;
			if (notInTestMode) {
				listener.getLogger().print(i + " '" + vsiffileName + "'\n");
			} else {

				System.out.println(i + " '" + vsiffileName + "'");
			}
		}

		if (deleteInputFile) {
			if (notInTestMode) {
				listener.getLogger().print("Job set to delete the input file.  Deleting " + fileName + "\n");
			}
			try {
				File fileToDelete = new File(fileName);
				fileToDelete.renameTo(new File(fileToDelete + ".delete"));
			} catch (Exception e) {
				if (notInTestMode) {
					listener.getLogger().print("Failed to delete input file from workspace.  Failed to delete file '" + fileName + "'\n");

				} else {

					System.out.println("Failed to delete the input file from the workspace.  Failed to delete file '" + fileName + "'");
				}
				throw e;
			}
		}

		return output;
	}
	
	public String[] loadFileCredentials(String buildID, int buildNumber, String workPlacePath, String credentialInputFile, TaskListener listener, boolean deleteInputFile) throws Exception {
		String[] output = null;
		List<String> listOfNames = new LinkedList<String>();
		BufferedReader reader = null;
		String fileName = null;
		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		// Set the right File name.
		if ("".equals(credentialInputFile) || credentialInputFile == null) {
			fileName = workPlacePath + File.separator + buildNumber + "." + buildID + "." + "credential.input";
		} else {
			fileName = credentialInputFile;
		}

		try {

			reader = this.loadFileFromWorkSpace(buildID, buildNumber, workPlacePath, credentialInputFile, listener, deleteInputFile, "credential.input");
			String line = null;
			while ((line = reader.readLine()) != null) {
				listOfNames.add(line);
			}

		} catch (Exception e) {

			if (notInTestMode) {
				listener.getLogger().print("Failed to read input file for the credentials.  Failed to load file '" + fileName + "'\n");
			} else {

				System.out.println("Failed to open the read file for the credentials.  Failed to load file '" + fileName + "'");
			}

			throw e;
		} finally {
			reader.close();
		}

		Iterator<String> iter = listOfNames.iterator();
		output = new String[listOfNames.size()];
		int i = 0;
		
		String stringValue = null;
		while (iter.hasNext()) {
			stringValue = new String(iter.next());
			output[i++] = stringValue;
		}

		if (deleteInputFile) {
			if (notInTestMode) {
				listener.getLogger().print("Job set to delete the credential file.  Deleting " + fileName + "\n");
			}
			try {
				File fileToDelete = new File(fileName);
				fileToDelete.delete();
			} catch (Exception e) {
				if (notInTestMode) {
					listener.getLogger().print("Failed to delete input file from workspace.  Failed to delete file '" + fileName + "'\n");

				} else {

					System.out.println("Failed to delete the input file from the workspace.  Failed to delete file '" + fileName + "'");
				}
				throw e;
			}
		}

		return output;
	}
	
	
	
	public String loadJSONEnvInput(String buildID, int buildNumber, String workPlacePath, String envInputFile, TaskListener listener) throws Exception {
		String output = null;
		StringBuffer listOfEnvs = new StringBuffer();
		BufferedReader reader = null;
		String fileName = null;
		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		// Set the right File name.
		if ("".equals(envInputFile) || envInputFile == null) {
			fileName = workPlacePath + File.separator + buildNumber + "." + buildID + "." + "environment.input";
		} else {
			fileName = envInputFile;
		}

		try {

			reader = this.loadFileFromWorkSpace(buildID, buildNumber, workPlacePath, fileName, listener, false, "environment.input");
			String line = null;
			while ((line = reader.readLine()) != null) {
				listOfEnvs.append(line);
			}
			
			output = listOfEnvs.toString();
			
			output = "\"environment\":{  "+ output + "}";

		} catch (Exception e) {

			if (notInTestMode) {
				listener.getLogger().print("Failed to read input file for the environment varibles.  Failed to load file '" + fileName + "'\n");
			} else {

				System.out.println("Failed to open the read file for the environment varibles.  Failed to load file '" + fileName + "'");
			}

			throw e;
		} finally {
			reader.close();
		}

		
		return output;
	}
        
        
       public String loadJSONAttrValuesInput(String buildID, int buildNumber, String workPlacePath, String attrValuesFile, TaskListener listener) throws Exception {
		String output = null;
		StringBuffer listOfAttrValues = new StringBuffer();
		BufferedReader reader = null;
		String fileName = null;
		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		// Set the right File name.
		if ("".equals(attrValuesFile) || attrValuesFile == null) {
			fileName = workPlacePath + File.separator + buildNumber + "." + buildID + "." + "attr.values.input";
		} else {
			fileName = attrValuesFile;
		}

		try {

			reader = this.loadFileFromWorkSpace(buildID, buildNumber, workPlacePath, fileName, listener, false, "attr.values.input");
			String line = null;
                        boolean foundOneAttr = false;
			while ((line = reader.readLine()) != null) {
                                String tmpLineResult = "";
                                StringTokenizer tokenizer = new StringTokenizer(line, ",");    
                                tmpLineResult = tmpLineResult + "{\"name\":\"" + tokenizer.nextToken().trim() + "\",";
                                tmpLineResult = tmpLineResult + "\"value\":\"" + tokenizer.nextToken().trim() + "\",";
                                tmpLineResult = tmpLineResult + "\"type\":\"" + tokenizer.nextToken().trim() + "\"},"; 
				listOfAttrValues.append(tmpLineResult);
                                foundOneAttr = true;
			}
			
			output = listOfAttrValues.toString();
                        if (foundOneAttr){
                           //Remove the last comma
                           output = output.substring(0,output.length()-1);
                        }
			
			output = "\"attributes\":["+ output + "]";

		} catch (Exception e) {

			if (notInTestMode) {
				listener.getLogger().print("Failed to read input file for the attribute values.  Failed to load file '" + fileName + "'\n " + e.getMessage());
			} else {

				System.out.println("Failed to open the read file for the attribute values.  Failed to load file '" + fileName + "'");
			}

			throw e;
		} finally {
			reader.close();
		}

		
		return output;
	}

	public String loadJSONFromFile(String buildID, int buildNumber, String workPlacePath, String vInputFile, TaskListener listener, boolean deleteInputFile) throws Exception {
		String output = null;
		StringBuffer listOfNames = new StringBuffer();
		BufferedReader reader = null;
		String fileName = null;
		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		// Set the right File name.
		if ("".equals(vInputFile) || vInputFile == null) {
			fileName = workPlacePath + File.separator + buildNumber + "." + buildID + "." + "vapi.input";
		} else {
			fileName = vInputFile;
		}

		try {

			reader = this.loadFileFromWorkSpace(buildID, buildNumber, workPlacePath, vInputFile, listener, deleteInputFile, "vapi.input");
			String line = null;
			while ((line = reader.readLine()) != null) {
				listOfNames.append(line);
			}

		} catch (Exception e) {

			if (notInTestMode) {
				listener.getLogger().print("Failed to read json input file for the vAPI input.  Failed to load file '" + fileName + "'\n");
			} else {

				System.out.println("Failed to open the read file for the vAPI input.  Failed to load file '" + fileName + "'");
			}

			throw e;
		} finally {
			reader.close();
		}

		output = listOfNames.toString();

		if (notInTestMode) {
			listener.getLogger().print("Input jSON for vAPI is:\n");
			listener.getLogger().print(output + "\n");
		}

		if (deleteInputFile) {
			if (notInTestMode) {
				listener.getLogger().print("Job set to delete the input file.  Deleting " + fileName + "\n");
			}
			try {
				File fileToDelete = new File(fileName);
				fileToDelete.renameTo(new File(fileToDelete + ".delete"));
			} catch (Exception e) {
				if (notInTestMode) {
					listener.getLogger().print("Failed to delete input file from workspace.  Failed to delete file '" + fileName + "'\n");

				} else {

					System.out.println("Failed to delete the input file from the workspace.  Failed to delete file '" + fileName + "'");
				}
				throw e;
			}
		}

		return output;
	}

	public String checkVAPIConnection(String url, boolean requireAuth, String user, String password) throws Exception {

		String textOut = null;
		try {

			System.out.println("Trying to connect with vManager vAPI " + url);
			String input = "{}";

			String apiURL = url + "/rest/sessions/count";

			HttpURLConnection conn = getVAPIConnection(apiURL, requireAuth, user, password, "POST", false, "", 0, null, null,0,0,false);
			OutputStream os = null;
			try {
				os = conn.getOutputStream();
			} catch (java.net.UnknownHostException e) {

				throw new Exception("Failed to connect to host " + e.getMessage() + ".  Host is unknown.");

			}
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				String reason = "";
				if (conn.getResponseCode() == 503)
					reason = "vAPI process failed to connect to remote vManager server.";
				if (conn.getResponseCode() == 401)
					reason = "Authentication Error";
				if (conn.getResponseCode() == 412)
					reason = "vAPI requires vManager 'Integration Server' license.";
				//String errorMessage = "Failed : HTTP error code : " + conn.getResponseCode() + " (" + reason + ")";
				String errorMessage = processErrorFromRespone(conn, null, false);
				return errorMessage;
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			StringBuilder result = new StringBuilder();
			String output;

			while ((output = br.readLine()) != null) {
				result.append(output);
			}

			conn.disconnect();

			JSONObject tmp = JSONObject.fromObject(result.toString());

			textOut = " The current number of sessions held on this vManager server are: " + tmp.getString("count");

		} catch (Exception e) {
			
			String errorMessage = "Failed : HTTP error: " + e.getMessage() ;
			
			if (e.getMessage().indexOf("Unexpected end of file from server") > -1) {
				errorMessage = errorMessage + " (from Incisive 14.2 onward the connection is secured.  Verify your url is https://)";	
			}
			

			System.out.println(errorMessage);
			textOut = errorMessage;
		}

		return textOut;
	}
	 
	public String checkExtraStaticAttr(String url, boolean requireAuth, String user, String password, String listOfAttr) throws Exception {

		String textOut = null;
		try {
			List<String> items = Arrays.asList(listOfAttr.split("\\s*,\\s*"));
			
			//System.out.println("Trying to connect with vManager vAPI " + url);
			//String input = "{}";

			String apiURL = url + "/rest/$schema/response?action=list&component=runs&extended=true";

			HttpURLConnection conn = getVAPIConnection(apiURL, requireAuth, user, password, "GET", false, "", 0, null, null,0,0,false);
			
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				String reason = "";
				if (conn.getResponseCode() == 503)
					reason = "vAPI process failed to connect to remote vManager server.";
				if (conn.getResponseCode() == 401)
					reason = "Authentication Error";
				//String errorMessage = "Failed : HTTP error code : " + conn.getResponseCode() + " (" + reason + ")";
				String errorMessage = processErrorFromRespone(conn, null, false);
				return errorMessage;
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			StringBuilder result = new StringBuilder();
			
			String output;

			while ((output = br.readLine()) != null) {
				result.append(output);
			}

			conn.disconnect();

			JSONObject tmp = JSONObject.fromObject(result.toString());
			
			JSONObject responseItems = JSONObject.fromObject(tmp.getString("items"));
			JSONObject properties = JSONObject.fromObject(responseItems.getString("properties"));
			
			Iterator<String> iter = items.iterator();
			
			String attr = null;
			JSONObject attrObject = null;
			String attrOutputString = "";
			int i=1;
			boolean isSuccess = true;
			while (iter.hasNext()){
				attr = iter.next();
				if (properties.has(attr)){
					attrObject = JSONObject.fromObject(properties.getString(attr));
					String attrTitle = attrObject.getString("title");
					attrOutputString = attrOutputString + "(" + i +") " + attr + " (" + attrTitle + ")\n";
					i++;
				} else {
					isSuccess = false;
					textOut = "Failed : '" + attr + "' doesn't exist for runs entities in the vManager server you are pointing at.\n";
					break;
				}
			}
			
			if (isSuccess){
				textOut = " All attributes were found on server:\n" + attrOutputString;
			} 

		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = "Failed : HTTP error: " + e.getMessage() ;
			
			if (e.getMessage().indexOf("Unexpected end of file from server") > -1) {
				errorMessage = errorMessage + " (from Incisive 14.2 onward the connection is secured.  Verify your url is https://)";	
			}
			

			System.out.println(errorMessage);
			textOut = errorMessage;
		}

		return textOut;
	}

	public HttpURLConnection getVAPIConnection(String apiUrl, boolean requireAuth, String user, String password, String requestMethod, boolean dynamicUserId, String buildID, int buildNumber, String workPlacePath,
			TaskListener listener,int connConnTimeOut, int connReadTimeout,boolean advConfig) throws Exception {

		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		//In case this is an SSL connections
		
				

		URL url = new URL(apiUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		if (apiUrl.indexOf("https://") > -1){
			configureAllowAll((HttpsURLConnection) conn);
		}
		
		conn.setDoOutput(true);
		conn.setRequestMethod(requestMethod);
		if ("PUT".equals(requestMethod) || "POST".equals(requestMethod)){
			conn.setRequestProperty("Content-Type", "application/json");
		} 
		
		 // set the connection timeouts to one minute and the read timeout to 30 minutes by default
		if (advConfig){
			conn.setConnectTimeout(connConnTimeOut * 60 * 1000);
		} else {
			conn.setConnectTimeout(60000);
		}
		
		if (advConfig){
			conn.setReadTimeout(connReadTimeout * 60 * 1000);
		} else {
			conn.setReadTimeout(1800000);
		}
		
		
		

		if (requireAuth) {
			// ----------------------------------------------------------------------------------------
			// Authentication
			// ----------------------------------------------------------------------------------------
			if (dynamicUserId) {
				BufferedReader reader = null;
				try {

					reader = this.loadFileFromWorkSpace(buildID, buildNumber, workPlacePath, null, listener, false, "user.input");
					String line = null;
					while ((line = reader.readLine()) != null) {
						user = line;
						break;
					}

				} catch (Exception e) {

					if (notInTestMode) {
						listener.getLogger().print("Failed to read input file for the dynamic users. \n");
					} else {

						System.out.println("Failed to read input file for the dynamic users. \n");
					}

					throw e;
				} finally {
					reader.close();
				}
			}

			String authString = user + ":" + password;
                        UserUsedForLogin = user;
                        PasswordUsedForLogin = password;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
			// ----------------------------------------------------------------------------------------
		}
		
		return conn;
	}

	public String executeVSIFLaunch(String[] vsifs, String url, boolean requireAuth, String user, String password, TaskListener listener, boolean dynamicUserId, String buildID, int buildNumber,
			String workPlacePath,int connConnTimeOut, int connReadTimeout, boolean advConfig, String jsonEnvInput, boolean useUserOnFarm, String userFarmType,String[] farmUserPassword, StepHolder stepHolder, String envSourceInputFile, String workingJobDir, VMGRBuildArchiver vMGRBuildArchiver,boolean userPrivateSSHKey, String jsonAttrValuesInput) throws Exception {

		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		String apiURL = url + "/rest/sessions/launch";
		
		List<String> listOfSessions = new ArrayList<String>();
                
               

		for (int i = 0; i < vsifs.length; i++) {

			if (notInTestMode) {
				listener.getLogger().print("vManager vAPI - Trying to launch vsif file: '" + vsifs[i] + "'\n");
			}
			String input = "{\"vsif\":\"" + vsifs[i] + "\"";
			if (jsonEnvInput != null){
				input = input + "," + jsonEnvInput;	
			}
                        if (jsonAttrValuesInput != null){
				input = input + "," + jsonAttrValuesInput;	
			}
			if (useUserOnFarm){
				String userFarm = null;
				String passwordFarm = null;
				if ("static".equals(userFarmType)){
					userFarm = user;
					passwordFarm = password;
					if (dynamicUserId) {
						BufferedReader reader = null;
						try {

							reader = this.loadFileFromWorkSpace(buildID, buildNumber, workPlacePath, null, listener, false, "user.input");
							String line = null;
							while ((line = reader.readLine()) != null) {
								userFarm = line;
								break;
							}

						} catch (Exception e) {

							if (notInTestMode) {
								listener.getLogger().print("Failed to read input file for the dynamic users. \n");
							} else {

								System.out.println("Failed to read input file for the dynamic users. \n");
							}

							throw e;
						} finally {
							reader.close();
						}
					}
				} else {
					userFarm = farmUserPassword[0];;
					passwordFarm = farmUserPassword[1];;	
				} 
                               
				if (!userPrivateSSHKey){
                                   input = input + ",\"credentials\":{\"username\":\"" + userFarm + "\",\"password\":\"" + passwordFarm + "\"}"; 
                                } else {
                                   input = input + ",\"credentials\":{\"connectType\":\"PUBLIC_KEY\"}";
                                }
                                
                                
                                if (!"".equals(envSourceInputFile.trim())){                                 
                                    String scriptShell = "BSH";
                                    input = input + ",\"preliminaryStage\":{\"sourceFilePath\":\"" + envSourceInputFile + "\",\"shell\":\"" + scriptShell + "\"}";
                                } 
				
			}
			input = input + "}";
			
                        
                        //listener.getLogger().print("vManager vAPI input: '" + input + "' with user/password: "+ user + "/" + password +"\n");
                        
                        
			HttpURLConnection conn = getVAPIConnection(apiURL, requireAuth, user, password, "POST", dynamicUserId, buildID, buildNumber, workPlacePath, listener, connConnTimeOut,  connReadTimeout,advConfig);
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET ) {
				String reason = "";
				if (conn.getResponseCode() == 503)
					reason = "vAPI process failed to connect to remote vManager server.";
				if (conn.getResponseCode() == 401)
					reason = "Authentication Error";
				if (conn.getResponseCode() == 412)
					reason = "vAPI requires vManager 'Integration Server' license.";
				if (conn.getResponseCode() == 406)
					reason = "VSIF file '" + vsifs[i] + "' was not found on file system, or is not accessed by the vAPI process.\n";
				
				
				String errorMessage = processErrorFromRespone(conn, listener, notInTestMode);
				
				
				return errorMessage;
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			StringBuilder result = new StringBuilder();
			String output;
			while ((output = br.readLine()) != null) {
				result.append(output);
			}

			conn.disconnect();

			JSONObject tmp = JSONObject.fromObject(result.toString());

			String textOut = "Session Launch Success: Session ID: " + tmp.getString("value") + "\n";
			listOfSessions.add(tmp.getString("value"));

			if (notInTestMode) {
				listener.getLogger().print(textOut);
			} else {

				System.out.println(textOut);
			}

		}
                
                if (vMGRBuildArchiver != null){
                    if (vMGRBuildArchiver.isVMGRBuildArchive()){
                        vMGRBuildArchiver.markBuildForArchive(listOfSessions,apiURL,requireAuth,UserUsedForLogin,PasswordUsedForLogin,workingJobDir,listener);
                    }
                }
                
                
		
		//Write the sessions id into the workspace for further use
		// Flush the output into workspace
		String fileOutput = workPlacePath + File.separator + buildNumber + "." + buildID + ".session_launch.output";

		FileWriter writer = new FileWriter(fileOutput);
		Iterator<String> iter = listOfSessions.iterator();
		while (iter.hasNext()){
			writer.append(iter.next() + "\n");
		}
		
		writer.flush();
		writer.close();
                
                
		
		if (stepHolder != null){
			waitTillSessionEnds(url, requireAuth, user, password, listener, dynamicUserId, buildID, buildNumber,
				workPlacePath,connConnTimeOut, connReadTimeout, advConfig, stepHolder,listOfSessions,notInTestMode,workingJobDir);
		}

		return "success";
	}

	public String executeAPI(String jSON, String apiUrl, String url, boolean requireAuth, String user, String password, String requestMethod, TaskListener listener, boolean dynamicUserId, String buildID, int buildNumber,
			String workPlacePath,int connConnTimeOut, int connReadTimeout, boolean advConfig) throws Exception {
		
		try{
		
		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		String apiURL = url + "/rest" + apiUrl;

		if (notInTestMode) {
			listener.getLogger().print("vManager vAPI - Trying to call vAPI '" + "/rest" + apiUrl + "'\n");
		}
		String input = jSON;
		HttpURLConnection conn = getVAPIConnection(apiURL, requireAuth, user, password, requestMethod, dynamicUserId, buildID, buildNumber, workPlacePath, listener, connConnTimeOut,  connReadTimeout, advConfig);
		
		if ("PUT".equals(requestMethod) || "POST".equals(requestMethod)){
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
		}

		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET ) {
			String reason = "";
			if (conn.getResponseCode() == 503)
				reason = "vAPI process failed to connect to remote vManager server.";
			if (conn.getResponseCode() == 401)
				reason = "Authentication Error";
			if (conn.getResponseCode() == 415)
				reason = "The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method.  Check if you selected the right request method (GET/POST/DELETE/PUT).";
			if (conn.getResponseCode() == 405)
				reason = "The method specified in the Request-Line is not allowed for the resource identified by the Request-URI. The response MUST include an Allow header containing a list of valid methods for the requested resource.  Check if you selected the right request method (GET/POST/DELETE/PUT).";
			if (conn.getResponseCode() == 412)
				reason = "vAPI requires vManager 'Integration Server' license.";
			//String errorMessage = "Failed : HTTP error code : " + conn.getResponseCode() + " (" + reason + ")\n";
			String errorMessage = processErrorFromRespone(conn, listener, notInTestMode);
			if (notInTestMode) {
				listener.getLogger().print(errorMessage);
			} else {
				System.out.println(errorMessage);
			}

			
			return errorMessage;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

		StringBuilder result = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
			result.append(output);
		}

		conn.disconnect();

		// Flush the output into workspace
		String fileOutput = workPlacePath + File.separator + buildNumber + "." + buildID + ".vapi.output";

		FileWriter writer = new FileWriter(fileOutput);

		writer.append(result.toString());
		writer.flush();
		writer.close();

		String textOut = "API Call Success: Output was saved into: " + fileOutput + "\n";

		if (notInTestMode) {
			listener.getLogger().print(textOut);
		} else {

			System.out.println(textOut);
		}

		return "success";
		
		}catch (Exception e){
			listener.getLogger().print("Filed: Error: " + e.getMessage() );
			return e.getMessage();
		}
	}

	
	
	public void waitTillSessionEnds(String url, boolean requireAuth, String user, String password, TaskListener listener, boolean dynamicUserId, String buildID, int buildNumber,
			String workPlacePath,int connConnTimeOut, int connReadTimeout, boolean advConfig, StepHolder stepHolder, List listOfSessions, boolean notInTestMode, String workingJobDir) throws Exception{
		
			LaunchHolder launchHolder = new LaunchHolder(stepHolder,listOfSessions);
			launchHolder.performWaiting( url,  requireAuth,  user,  password,  listener,  dynamicUserId,  buildID,  buildNumber,
					 workPlacePath, connConnTimeOut,  connReadTimeout,  advConfig, notInTestMode,workingJobDir);
		
	}
        
       
	
	
	
	public static void configureAllowAll(HttpsURLConnection connection) {
		connection.setHostnameVerifier(new HostnameVerifier() {
	           @Override
	           public boolean verify(String s, SSLSession sslSession) {
	               return true;
	           }
	       });

	       try {
	    	   connection.setSSLSocketFactory(getSocketFactory());
	       } catch (Exception e) {
	           throw new RuntimeException("XTrust Failed to set SSL Socket factory", e);
	       }

	   }


	    private static SSLSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
	        SSLContext sslContext = SSLContext.getInstance("TLS");
	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };
	        sslContext.init(null, new TrustManager[] { tm }, null);
	        return sslContext.getSocketFactory();
	    }
	
	
	    @SuppressWarnings("finally")
		public String processErrorFromRespone(HttpURLConnection conn, TaskListener listener, boolean notInTestMode){
	    	String errorMessage = "";
			StringBuilder resultFromError = null;
			int responseCode = 0;
			try{
				resultFromError = new StringBuilder(conn.getResponseMessage());
				responseCode = conn.getResponseCode();
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));

				
				String output;
				while ((output = br.readLine()) != null) {
					resultFromError.append(output);
				}
			}catch (Exception e){
				
			} finally {
				errorMessage = "Failed : HTTP error code : " + responseCode + " (" + resultFromError + ")\n";
				if (notInTestMode) {
					listener.getLogger().print(errorMessage);
				} else {
					System.out.println(errorMessage);
				}
				return errorMessage;
			}
	    }
	
	
	
}
