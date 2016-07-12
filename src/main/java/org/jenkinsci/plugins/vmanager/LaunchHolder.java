package org.jenkinsci.plugins.vmanager;

import hudson.model.BuildListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class LaunchHolder {

	private StepHolder stepHolder = null;
	private List<String> listOfSessions = null;
	private List<String> listOfSessionsForCountDown = null;
	private static long TIME_TO_SLEEP = 60000;
	private static final String postData1 = "{\"filter\":{\"attName\":\"id\",\"operand\":\"EQUALS\",\"@c\":\".AttValueFilter\",\"attValue\":\"";
	private static final String postData2 = "\"},\"projection\": {\"type\":\"SELECTION_ONLY\",\"selection\":[\"session_status\",\"name\"]}}";

	public LaunchHolder(StepHolder stepHolder, List<String> listOfSessions) {
		super();
		this.stepHolder = stepHolder;
		this.listOfSessions = listOfSessions;
		
		this.listOfSessionsForCountDown = new ArrayList<String>();
		Iterator<String> iter = listOfSessions.iterator();
		while (iter.hasNext()){
			this.listOfSessionsForCountDown.add(iter.next());
		}
	}

	public StepHolder getStepHolder() {
		return stepHolder;
	}

	public void setStepHolder(StepHolder stepHolder) {
		this.stepHolder = stepHolder;
	}

	public List<String> getListOfSessions() {
		return listOfSessions;
	}

	public void setListOfSessions(List<String> listOfSessions) {
		this.listOfSessions = listOfSessions;
	}

	public void performWaiting(String url, boolean requireAuth, String user, String password, BuildListener listener, boolean dynamicUserId, String buildID, int buildNumber, String workPlacePath,
			int connConnTimeOut, int connReadTimeout, boolean advConfig, boolean notInTestMode) throws Exception {

		String requestMethod = "POST";
		String apiURL = url + "/rest/sessions/list";
		boolean keepWaiting = true;

		Utils utils = new Utils();
		HttpURLConnection conn = null;
		long startTime = new Date().getTime();
		long startTimeForDebugInfo = new Date().getTime();
		long timeToWaitOverall = stepHolder.getStepSessionTimeout() * 60 * 1000;
		long timeBetweenPrintStatus = 30 * 60 * 1000;
		boolean debugPrint = true;
		String buildResult = null;

		if (notInTestMode) {
			listener.getLogger().print("Waiting until all sessions will end...\n");
			listener.getLogger().print("Checking for state change every " + (TIME_TO_SLEEP/60000)  +" minutes.\n");
			listener.getLogger().print("Printing out session state every " + (timeBetweenPrintStatus/60000)  +" minutes.\n");
		} else {
			System.out.println("Waiting until all sessions will end...\n");
			System.out.println("Checking for state change every " + (TIME_TO_SLEEP/60000)  +" minutes.");
			System.out.println("Printing out session state every " + (timeBetweenPrintStatus/60000)  +" minutes.");
		}

		while (keepWaiting) {

			buildResult = "";
			if (stepHolder.getStepSessionTimeout() != 0) {
				if (new Date().getTime() - startTime > timeToWaitOverall) {
					// MARK_BUILD_FAIL
					buildResult = "(" + new Date().toString() + ") - Timeout.  Waiting for more than " + stepHolder.getStepSessionTimeout() + " minutes. Marking build as failed.\n";
					if (notInTestMode) {
						listener.getLogger().print(buildResult);
					} else {
						System.out.println(buildResult);
					}
					break;
				}
			}

			try {
				Thread.sleep(TIME_TO_SLEEP);
			} catch (InterruptedException e1) {
				// MARK_BUILD_FAIL
				e1.printStackTrace();
				break;
			}

			try {

				// Check if to print information
				if (new Date().getTime() - startTimeForDebugInfo > timeBetweenPrintStatus) {
					startTimeForDebugInfo = new Date().getTime();
					debugPrint = true;
				}

				// Make the vAPI call to get sessions status
				Iterator<String> sessionIter = this.listOfSessions.iterator();
				String tmpSessionId = null;
				String tmpPostData = null;
				String sessionState = null;
				while (sessionIter.hasNext()) {
					tmpSessionId = sessionIter.next();
					tmpPostData = postData1 + tmpSessionId + postData2;

					try {
						conn = utils.getVAPIConnection(apiURL, requireAuth, user, password, requestMethod, dynamicUserId, buildID, buildNumber, workPlacePath, listener, connConnTimeOut,
								connReadTimeout, advConfig);

						OutputStream os = conn.getOutputStream();
						os.write(tmpPostData.getBytes());
						os.flush();

						if (checkResponseCode(conn)) {
							BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
							StringBuilder result = new StringBuilder();
							String output;
							while ((output = br.readLine()) != null) {
								result.append(output);
							}

							JSONArray tmpArray = JSONArray.fromObject(result.toString());

							// Check if session exist:
							if (tmpArray.size() == 0) {
								// MARK_THE_BUILD_FAIL
								buildResult = "(" + new Date().toString() + ") -  Session id (" + tmpSessionId + ") has been deleted on the vManager system.  Failing the build.\n";
								if (notInTestMode) {
									listener.getLogger().print(buildResult);
								} else {
									System.out.println(buildResult);
								}
								keepWaiting = false;
								break;
							}

							JSONObject tmp = tmpArray.getJSONObject(0);
							sessionState = tmp.getString("session_status");

							if (notInTestMode) {
								if (debugPrint) {
									listener.getLogger().print(
											"(" + new Date().toString() + ") - State of Session '" + tmp.getString("name") + "' (" + tmpSessionId + ") = " + tmp.getString("session_status") + "\n");
								}
							} else {
								if (debugPrint) {
									System.out
											.println("(" + new Date().toString() + ") - State of Session '" + tmp.getString("name") + "' (" + tmpSessionId + ") = " + tmp.getString("session_status"));
								}
							}

							if (toContinue(sessionState, tmpSessionId)) {
								// MARK THAT ALL SESSION ENDED
								buildResult = "(" + new Date().toString() + ") - All sessions got into a state in which the build step can continue.\n";
								if (notInTestMode) {
									listener.getLogger().print(buildResult);
								} else {
									System.out.println(buildResult);
								}
								buildResult = "success";
								keepWaiting = false;
								break;
							}

							if (toFail(sessionState, tmpSessionId)) {
								// MARK_BUILD_FAIL
								buildResult = "(" + new Date().toString() + ") - State of Session '" + tmp.getString("name") + "' (" + tmpSessionId + ") = " + tmp.getString("session_status")
										+ " - Marking build failed.\n";
								if (notInTestMode) {
									listener.getLogger().print(buildResult);
								} else {
									System.out.println(buildResult);
								}
								keepWaiting = false;
								break;
							}

							if (toIgnore(sessionState, tmpSessionId)) {
								// Don't do anything, just continue.
							}
						}

					} catch (java.net.ConnectException e) {
						if (notInTestMode) {
							if (debugPrint){
								listener.getLogger().print("(" + new Date().toString() + ") - vManager Server is not responding or is down. Build will keep try to connect.\n");
							}
						} else {
							System.out.println("(" + new Date().toString() + ") - vManager Server is not responding or is down. Build will keep try to connect.'");
						}
						break;
					} catch (Exception e) {
						if (notInTestMode) {
							listener.getLogger().print(e.getMessage());
						}
						e.printStackTrace();
					} finally {
						conn.disconnect();
						
					}

				}

			} catch (Exception e) {
				if (notInTestMode) {
					listener.getLogger().print(e.getMessage());
				}
				e.printStackTrace();
			} finally {
				if (listOfSessions.size() > 1){
					if (notInTestMode) {
						if (debugPrint){
							listener.getLogger().print("\n");
						}
					} else {
						if (debugPrint){
							System.out.println("\n");
						}
					}
				}
				debugPrint = false;
			}

		}

		if (!"success".equals(buildResult)) {
			throw new Exception(buildResult);
		}

	}

	private boolean toContinue(String state, String sessionId) {
		return checkWhatNext(state, sessionId, "continue");
	}

	private boolean toFail(String state, String sessionId) {
		return checkWhatNext(state, sessionId, "fail");
	}

	private boolean toIgnore(String state, String sessionId) {
		return checkWhatNext(state, sessionId, "ignore");
	}

	private boolean checkWhatNext(String state, String sessionId, String checkFor) {

		if (("inaccessible").equals(state)) {

			if (stepHolder.getInaccessibleResolver().equals(checkFor)) {
				return stepResolver(checkFor, sessionId);
			}

		} else if (("stopped").equals(state)) {

			if (stepHolder.getStoppedResolver().equals(checkFor)) {
				return stepResolver(checkFor, sessionId);
			}

		} else if (("failed").equals(state)) {

			if (stepHolder.getFailedResolver().equals(checkFor)) {
				return stepResolver(checkFor, sessionId);
			}

		} else if (("done").equals(state)) {

			if (stepHolder.getDoneResolver().equals(checkFor)) {
				return stepResolver(checkFor, sessionId);
			}

		} else if (("suspended").equals(state)) {

			if (stepHolder.getSuspendedResolver().equals(checkFor)) {
				return stepResolver(checkFor, sessionId);
			}

		} else if (("completed").equals(state)) {

			if (checkFor.equals("continue")) {
				return checkIfAllSessionsEnded(sessionId);
			}
		}

		return false;

	}

	private boolean checkIfAllSessionsEnded(String sessionId) {

		listOfSessionsForCountDown.remove(sessionId);

		if (listOfSessionsForCountDown.size() == 0) {
			return true;
		} else {
			return false;
		}

	}

	private boolean stepResolver(String checkFor, String sessionId) {
		if (checkFor.equals("continue")) {
			return checkIfAllSessionsEnded(sessionId);
		} else if (checkFor.equals("fail")) {
			return true;
		} else if (checkFor.equals("ignore")) {
			return true;
		}

		return false;
	}

	private boolean checkResponseCode(HttpURLConnection conn) {
		try {
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED
					&& conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET) {
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

}
