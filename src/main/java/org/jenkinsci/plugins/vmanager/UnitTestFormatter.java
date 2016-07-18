package org.jenkinsci.plugins.vmanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UnitTestFormatter {
	
	JSONArray runs = null;
	String sessionId = null;
	JUnitRequestHolder jUnitRequestHolder = null;
	Map<String,String> extraAttrLabels = null;
	
	
	public UnitTestFormatter(JSONArray runs, String sessionId,JUnitRequestHolder jUnitRequestHolder, Map<String,String> extraAttrLabels) {
		super();
		this.runs = runs;
		this.sessionId = sessionId;
		this.jUnitRequestHolder = jUnitRequestHolder;
		this.extraAttrLabels = extraAttrLabels;
		
	}

	public JSONArray getRuns() {
		return runs;
	}

	public void setRuns(JSONArray runs) {
		this.runs = runs;
	}
	
	public void dumpXMLFile(String workPlacePath, int buildNumber, String buildID) throws IOException {
		
		if (runs.size() > 0){
			// Flush the output into workspace
			String fileOutput = workPlacePath + File.separator + buildNumber + "." + buildID + "." + sessionId + ".session_runs.xml";

			FileWriter writer = new FileWriter(fileOutput);
			writer.append("<testsuite tests=\"vManager\">" + "\n");		
			Iterator<JSONObject> runsIter = runs.iterator();
			JSONObject tmpRun = null;
			String testStatus = "NA";
			String testGroup = "NA";
			String name = "NA";
			String testSeed = "NA";
			int testDuration = 0;
			String testFirstErrorCode = "NA";
			String testFirstErrorDescription = "NA";
			
			while (runsIter.hasNext()){
				tmpRun = runsIter.next();
				if (tmpRun.has("status"))  testStatus = tmpRun.getString("status");
				if (tmpRun.has("test_group"))  testGroup = tmpRun.getString("test_group");
				if (tmpRun.has("test_name"))  name = tmpRun.getString("test_name");
				if (tmpRun.has("computed_seed"))  	testSeed = tmpRun.getString("computed_seed");
				if (tmpRun.has("duration")) {
					try{
						testDuration = new Integer(tmpRun.getString("duration")).intValue();
					} catch (Exception e){
						//In case it's not a number like "undefined"
					}
				}
				
				
				
				if ("failed".equals(testStatus)){
					if (tmpRun.has("first_failure_name")) testFirstErrorCode = tmpRun.getString("first_failure_name");
					if (tmpRun.has("first_failure_description")) testFirstErrorDescription = tmpRun.getString("first_failure_description");
					writer.append("		<testcase classname=\"" + testGroup  + "\" name=\"" + name  + " : Seed-" + testSeed + "\" time=\"" + testDuration + "\">" + "\n");
					writer.append("			<failure message=\"" + testFirstErrorCode +"\" type=\"" + testFirstErrorCode +"\">First Error Description: \n" + testFirstErrorDescription +  "\n" + "Computed Seed: \n" + testSeed +  "\n" + addExtraAttrValues(tmpRun) + "</failure>" + "\n");
					writer.append("		</testcase>" + "\n");
				} else if ("stopped".equals(testStatus)){
					writer.append("		<testcase classname=\"" + testGroup  + "\" name=\"" + name  + " : Seed-" + testSeed + "\" time=\"" + testDuration + "\">" + "\n");
					writer.append("		 <skipped />" + "\n");
					writer.append("		</testcase>" + "\n");
				}else {
					writer.append("		<testcase classname=\"" + testGroup  + "\" name=\"" + name  + " : Seed-" + testSeed + "\" time=\"" + testDuration + "\"/>" + "\n");
				}
				
			}
			
			writer.append("</testsuite>" + "\n");
			writer.flush();
			writer.close();
		}
		
	}
	
	private String addExtraAttrValues(JSONObject tmpRun){
		List<String> items = Arrays.asList(jUnitRequestHolder.getStaticAttributeList().split("\\s*,\\s*"));
		
		Iterator<String> iter = items.iterator();
		String extraAttributesForRuns = "";
		
		String tmpAttr = null;
		String attrValue = null;
		while (iter.hasNext()){
			tmpAttr = iter.next();
			if (tmpAttr.indexOf(" ") > 0 || tmpAttr.equals("first_failure_name") || tmpAttr.equals("first_failure_description") || tmpAttr.equals("computed_seed") || tmpAttr.equals("test_group") || tmpAttr.equals("test_name")){
				continue;
			} else {
				if (tmpRun.has(tmpAttr)){
					
					attrValue = tmpRun.getString(tmpAttr);
					attrValue = attrValue.replaceAll("<__SEPARATOR__>", "\n    ");
					extraAttributesForRuns = extraAttributesForRuns + extraAttrLabels.get(tmpAttr) + ":\n    " + attrValue + "\n";
				}
			} 
		}
		
		return extraAttributesForRuns;
	}
	
	
	
	
}
