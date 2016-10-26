package org.jenkinsci.plugins.vmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import hudson.model.BuildListener;

public class SessionStatusHolder {
	
	
	
	String url; 
	boolean requireAuth; 
	String user; 
	String password;
	BuildListener listener;
	boolean dynamicUserId;
	private int buildNumber = 0;
	private String workPlacePath = null;
	private String buildId = null;
	
	
	int connConnTimeOut;
	int connReadTimeout;
	boolean advConfig;
	boolean notInTestMode; 
	boolean markBuildAsFailedIfAllRunFailed = false;
	
	
	List<String> listOfSessions = null;
	private String postDataSessions;
	
	private String postSessionData = "{\"filter\":{\"@c\":\".ChainedFilter\",\"condition\":\"OR\",\"chain\":[" + "######"  + "]},\"grouping\":[\"owner\"],\"settings\":{\"write-hidden\":true,\"stream-mode\":false},\"projection\":{\"type\": \"SELECTION_ONLY\",\"selection\":[\"session_status\",\"name\",\"total_runs_in_session\",\"passed_runs\",\"failed_runs\",\"running\",\"waiting\",\"other_runs\",\"owner\",\"number_of_entities\",\"id\"]}}";
/*
{"attName":"id","operand":"EQUALS","@c":".AttValueFilter","attValue":"851988"},
{"attName":"id","operand":"EQUALS","@c":".AttValueFilter","attValue":"851987"},
{"attName":"id","operand":"EQUALS","@c":".AttValueFilter","attValue":"851986"}
*/
	
	public SessionStatusHolder(int buildNumber, String workspace, String buildId) {
		super();
		this.buildNumber = buildNumber;
		this.workPlacePath = workspace;
		this.buildId = buildId;
		
	}
	
	private void buildPostDataSessionPart(List<String> listOfSessions){
		
		Iterator<String> iter = listOfSessions.iterator();
		String result = "";
		int commaCounter = listOfSessions.size() - 1;
		while (iter.hasNext()){
			result = result + "{\"attName\":\"id\",\"operand\":\"EQUALS\",\"@c\":\".AttValueFilter\",\"attValue\":\"" + iter.next() + "\"}";
			if (commaCounter > 0) {
				result = result + ",";
			}
			commaCounter--;
		}
		
		this.postDataSessions = result;
		
	}
	
	public void dumpSessionStatus(){
		HttpURLConnection conn = null;
		Utils utils = new Utils();
		
		String apiURL = url + "/rest/sessions/list";
		
		try {
			conn = utils.getVAPIConnection(apiURL, requireAuth, user, password, "POST", dynamicUserId, buildId, buildNumber, workPlacePath, listener, connConnTimeOut, connReadTimeout, advConfig);

			OutputStream os = conn.getOutputStream();
			os.write(postSessionData.getBytes());
			os.flush();

			if (checkResponseCode(conn)) {
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				StringBuilder result = new StringBuilder();
				String output;
				while ((output = br.readLine()) != null) {
					result.append(output);
				}

				JSONArray tmpArray = JSONArray.fromObject(result.toString());
				Iterator<JSONObject> iterator = tmpArray.iterator();
				JSONObject sessionObject = null;
				while (iterator.hasNext()) {
					sessionObject = iterator.next();
					break;
				}
				
				//Retrive all the session params
				writeSessionIntoFile(sessionObject);
				
				
				

			}
		} catch (Exception e) {
			if (notInTestMode) {
				listener.getLogger().print(e.getMessage());
			}
			//e.printStackTrace();
		} finally {
			conn.disconnect();

		}
	}
	
	private void writeSessionIntoFile(JSONObject session) throws IOException{
		
		
		
		SessionState sessionData = new SessionState();
		if (session.has("session_status")) {
			if ("MIXED_GROUP_VALUE".equals(session.getString("session_status"))){
				sessionData.setStatus("Mixed");
			} else {
				sessionData.setStatus(session.getString("session_status"));
			}
		}
		if (session.has("name")) {
			if ("ZZZZZZZZZZZZZZZZ".equals(session.getString("name"))){
				sessionData.setName("Mixed");
			} else {
				sessionData.setName(cutSessionNameDate(session.getString("name")));
				sessionData.setSessionCode(cutSessionCodeDate(session.getString("name")));
				
			}
			
		}
		if (session.has("total_runs_in_session")) sessionData.setTotalRuns(session.getString("total_runs_in_session"));
		if (session.has("passed_runs")) sessionData.setPassed(session.getString("passed_runs"));
		if (session.has("failed_runs")) sessionData.setFailed(session.getString("failed_runs"));
		if (session.has("running")) sessionData.setRunning(session.getString("running"));
		if (session.has("waiting")) sessionData.setWaiting(session.getString("waiting"));
		if (session.has("other_runs")) sessionData.setOther(session.getString("other_runs"));
		if (session.has("owner")) {
			if ("ZZZZZZZZZZZZZZZZ".equals(session.getString("name"))){
				sessionData.setOwner("Mixed");
			} else {
				sessionData.setOwner(session.getString("owner"));
			}
			
		}
		if (session.has("number_of_entities")) sessionData.setNumOfSession(session.getString("number_of_entities"));
		if (session.has("id")) sessionData.setId(session.getString("id"));
		sessionData.setServerUrl(this.url);
		
		
		String fileOutput = workPlacePath + File.separator + buildNumber + "." + buildId + ".session_status.properties";
		
		//Just before writing the file, check if user choose to overwide session status to "Failed" in case the session is in completed state and all runs failed.
		if (markBuildAsFailedIfAllRunFailed){
			if (sessionData.getTotalRuns().trim().equals(sessionData.getFailed().trim())){
				sessionData.setStatus("failed");
			}
			
		}

		FileWriter writer = new FileWriter(fileOutput);
		
		writer.append("status=" + sessionData.getStatus() + "\n");
		writer.append("name=" + sessionData.getName() + "\n");
		writer.append("session_code=" + sessionData.getSessionCode() + "\n");
		writer.append("total_runs_in_session=" + sessionData.getTotalRuns() + "\n");
		writer.append("passed_runs=" + sessionData.getPassed() + "\n");
		writer.append("failed_runs=" + sessionData.getFailed() + "\n");
		writer.append("running=" + sessionData.getRunning() + "\n");
		writer.append("waiting=" + sessionData.getWaiting() + "\n");
		writer.append("other_runs=" + sessionData.getOther() + "\n");
		writer.append("owner=" + sessionData.getOwner() + "\n");
		writer.append("number_of_entities=" + sessionData.getNumOfSession() + "\n");
		
		//Set the id (might be more than one
		Iterator<String> iter = listOfSessions.iterator();
		String result = "";
		int commaCounter = listOfSessions.size() - 1;
		while (iter.hasNext()){
			result = result + iter.next();
			if (commaCounter > 0) {
				result = result + ",";
			}
			commaCounter--;
		}
		writer.append("id=" + result + "\n");
		
		writer.append("url=" + sessionData.getServerUrl() + "\n");
		
		
		writer.flush();
		writer.close();
		
		
		
	}
	
	public SessionState loadSessionFromFile(){
		
		SessionState sessionData = new SessionState();
		String fileInput = workPlacePath + File.separator + buildNumber + "." + buildId + ".session_status.properties";
		
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(fileInput);

			// load a properties file
			prop.load(input);

			// get the property value 
			sessionData.setStatus((prop.getProperty("status") != null) ? prop.getProperty("status") : "NA");
			sessionData.setName((prop.getProperty("name") != null) ? prop.getProperty("name") : "NA");
			sessionData.setSessionCode((prop.getProperty("session_code") != null) ? prop.getProperty("session_code") : "NA");
			sessionData.setTotalRuns((prop.getProperty("total_runs_in_session") != null) ? prop.getProperty("total_runs_in_session") : "NA");
			sessionData.setPassed((prop.getProperty("passed_runs") != null) ? prop.getProperty("passed_runs") : "NA");
			sessionData.setFailed((prop.getProperty("failed_runs") != null) ? prop.getProperty("failed_runs") : "NA");
			sessionData.setRunning((prop.getProperty("running") != null) ? prop.getProperty("running") : "NA");
			sessionData.setWaiting((prop.getProperty("waiting") != null) ? prop.getProperty("waiting") : "NA");
			sessionData.setOther((prop.getProperty("other_runs") != null) ? prop.getProperty("other_runs") : "NA");
			sessionData.setOwner((prop.getProperty("owner") != null) ? prop.getProperty("owner") : "NA");
			sessionData.setNumOfSession((prop.getProperty("number_of_entities") != null) ? prop.getProperty("number_of_entities") : "NA");
			sessionData.setId((prop.getProperty("id") != null) ? prop.getProperty("id") : "NA");
			sessionData.setServerUrl((prop.getProperty("url") != null) ? prop.getProperty("url") : "NA");
			
			

		} catch (IOException ex) {
			//ex.printStackTrace();
			System.out.println("vManager Dashboard - Can't find file " + fileInput);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		return sessionData;
	}
	
	private boolean checkResponseCode(HttpURLConnection conn) {
		try {
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED
					&& conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET
					&& conn.getResponseCode() != 406) {
				System.out.println("Error - Got wrong response from /session/list request for session status - " + conn.getResponseCode());
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			// MARK_BUILD_FAIL
			e.printStackTrace();
			return false;
		}
	}

	private String cutSessionNameDate(String sessionName){
		
		for (int i=0;i<6;i++){
			sessionName = sessionName.substring(0,sessionName.lastIndexOf("_"));
		}
		sessionName = sessionName.substring(0,sessionName.lastIndexOf("."));
		sessionName = sessionName.substring(0,sessionName.lastIndexOf("."));
		return sessionName;
	}
	
	private String cutSessionCodeDate(String sessionName){
		
		String sessionCode = "NA";
		try{
			sessionCode = sessionName.substring(sessionName.lastIndexOf("_")+1, sessionName.length());
		}catch (Exception e){
			
		}
		return sessionCode;
	}
		
	
	public SessionStatusHolder(String url, boolean requireAuth, String user, String password, BuildListener listener, boolean dynamicUserId, int buildNumber, String workPlacePath, String buildId,
			int connConnTimeOut, int connReadTimeout, boolean advConfig, boolean notInTestMode,List<String> listOfSessions, boolean markBuildAsFailedIfAllRunFailed) {
		
		super();
		this.url = url;
		this.requireAuth = requireAuth;
		this.user = user;
		this.password = password;
		this.listener = listener;
		this.dynamicUserId = dynamicUserId;
		this.buildNumber = buildNumber;
		this.workPlacePath = workPlacePath;
		this.buildId = buildId;
		this.connConnTimeOut = connConnTimeOut;
		this.connReadTimeout = connReadTimeout;
		this.advConfig = advConfig;
		this.notInTestMode = notInTestMode;
		this.listOfSessions = listOfSessions;
		this.markBuildAsFailedIfAllRunFailed = markBuildAsFailedIfAllRunFailed;
		
		buildPostDataSessionPart(listOfSessions);
		
		this.postSessionData = this.postSessionData.replaceAll("######", postDataSessions);
		
		
	}


	public int getBuildNumber() {
		return buildNumber;
	}
	public void setBuildNumber(int buildNumber) {
		this.buildNumber = buildNumber;
	}
	public String getWorkspace() {
		return workPlacePath;
	}
	public void setWorkPlacePath(String workspace) {
		this.workPlacePath = workspace;
	}
	public String getBuildId() {
		return buildId;
	}
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}
	
	

}
