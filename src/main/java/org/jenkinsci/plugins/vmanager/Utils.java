package org.jenkinsci.plugins.vmanager;

import hudson.model.BuildListener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

import net.sf.json.JSONObject;

public class Utils {

	public BufferedReader loadFileFromWorkSpace(String buildID, int buildNumber, String workPlacePath, String inputFile, BuildListener listener, boolean deleteInputFile, String fileTypeEndingName)
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
				fileName = workPlacePath + File.separator + inputFile;
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

	public String[] loadVSIFFileNames(String buildID, int buildNumber, String workPlacePath, String vSIFInputFile, BuildListener listener, boolean deleteInputFile) throws Exception {
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
			fileName = workPlacePath + File.separator + vSIFInputFile;
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

	public String loadJSONFromFile(String buildID, int buildNumber, String workPlacePath, String vInputFile, BuildListener listener, boolean deleteInputFile) throws Exception {
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
			fileName = workPlacePath + File.separator + vInputFile;
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

	public String checkVAPIConnection(String url, String port, boolean requireAuth, String user, String password) throws Exception {

		String textOut = null;
		try {

			url = url.toLowerCase().trim() + ":" + port.trim();
			if (!("http".indexOf(url) > 0)) {
				url = "http://" + url;
			}

			System.out.println("Trying to connect with vManager vAPI " + url);
			String input = "{}";

			String apiURL = url + "/vmgr/rest/runs/count";

			HttpURLConnection conn = getVAPIConnection(apiURL, requireAuth, user, password, false, "", 0, null, null);
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
				String errorMessage = "Failed : HTTP error code : " + conn.getResponseCode() + " (" + reason + ")";

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

			textOut = " The current number of runs held on this vManager server are: " + tmp.getString("count");

		} catch (Exception e) {
			String errorMessage = "Failed : HTTP error: " + e.getMessage();

			System.out.println(errorMessage);
			textOut = errorMessage;
		}

		return textOut;
	}

	public HttpURLConnection getVAPIConnection(String apiUrl, boolean requireAuth, String user, String password, boolean dynamicUserId, String buildID, int buildNumber, String workPlacePath,
			BuildListener listener) throws Exception {

		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		URL url = new URL(apiUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");

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
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
			// ----------------------------------------------------------------------------------------
		}

		return conn;
	}

	public String executeVSIFLaunch(String[] vsifs, String url, String port, boolean requireAuth, String user, String password, BuildListener listener, boolean dynamicUserId, String buildID,
			int buildNumber, String workPlacePath) throws Exception {

		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		url = url.toLowerCase().trim() + ":" + port.trim();
		if (!("http".indexOf(url) > 0)) {
			url = "http://" + url;
		}

		String apiURL = url + "/vmgr/rest/sessions/launch";

		for (int i = 0; i < vsifs.length; i++) {

			if (notInTestMode) {
				listener.getLogger().print("vManager vAPI - Trying to launch vsif file: '" + vsifs[i] + "'\n");
			}
			String input = "{\"vsif\":\"" + vsifs[i] + "\"}";
			HttpURLConnection conn = getVAPIConnection(apiURL, requireAuth, user, password, dynamicUserId, buildID, buildNumber, workPlacePath, listener);
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				String reason = "";
				if (conn.getResponseCode() == 503)
					reason = "vAPI process failed to connect to remote vManager server.";
				if (conn.getResponseCode() == 401)
					reason = "Authentication Error\n";
				if (conn.getResponseCode() == 406)
					reason = "VSIF file '" + vsifs[i] + "' was not found on file system, or is not accessed by the vAPI process.\n";
				String errorMessage = "Failed : HTTP error code : " + conn.getResponseCode() + " (" + reason + ")\n";
				if (notInTestMode) {
					listener.getLogger().print(errorMessage);
				}

				System.out.println(errorMessage);
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

			if (notInTestMode) {
				listener.getLogger().print(textOut);
			} else {

				System.out.println(textOut);
			}

		}

		return "success";
	}

	public String executeAPI(String jSON, String apiUrl, String url, String port, boolean requireAuth, String user, String password, BuildListener listener, boolean dynamicUserId, String buildID,
			int buildNumber, String workPlacePath) throws Exception {

		boolean notInTestMode = true;
		if (listener == null) {
			notInTestMode = false;
		}

		url = url.toLowerCase().trim() + ":" + port.trim();
		if (!("http".indexOf(url) > 0)) {
			url = "http://" + url;
		}

		String apiURL = url + "/vmgr/rest" + apiUrl;

		if (notInTestMode) {
			listener.getLogger().print("vManager vAPI - Trying to call vAPI '" + "/vmgr/rest" + apiUrl + "'\n");
		}
		String input = jSON;
		HttpURLConnection conn = getVAPIConnection(apiURL, requireAuth, user, password, dynamicUserId, buildID, buildNumber, workPlacePath, listener);
		OutputStream os = conn.getOutputStream();
		os.write(input.getBytes());
		os.flush();

		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			String reason = "";
			if (conn.getResponseCode() == 503)
				reason = "vAPI process failed to connect to remote vManager server.";
			if (conn.getResponseCode() == 401)
				reason = "Authentication Error\n";
			String errorMessage = "Failed : HTTP error code : " + conn.getResponseCode() + " (" + reason + ")\n";
			if (notInTestMode) {
				listener.getLogger().print(errorMessage);
			}

			System.out.println(errorMessage);
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
	}

}
