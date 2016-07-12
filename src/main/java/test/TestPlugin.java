package test;

import java.util.logging.Logger;

import org.jenkinsci.plugins.vmanager.Utils;
import org.jenkinsci.plugins.vmanager.VMGRLaunch;
import org.jenkinsci.plugins.vmanager.StepHolder;

public class TestPlugin {

	
	final String vAPIUrl = "https://vlnx488:50500/camera/vapi";
    final boolean authRequired = true;
    final String vAPIUser = "root";
    final String vAPIPassword = "letmein";
    final String vSIFName = "/home/tyanai/vsif/vm_basic.vsif";
    final String vSIFInputFile  = "d:/temp/artifacts/vsifs.input";
    final boolean deleteInputFile = false;
    final String vsifType = "dynamic";
    
    final int buildNumber = 83;
    final String buildID = "2014-45-45-34-56-78";
    final String buildArtifactPath = "d:/temp/artifacts";
    
    private String inaccessibleResolver = "fail";
	private String stoppedResolver = "continue";
	private String failedResolver = "fail";
	private String doneResolver = "ignore";
	private String suspendedResolver = "ignore";
	private boolean waitTillSessionEnds = true;
	private int stepSessionTimeout = 10;
    
    	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		TestPlugin testPlugin = new TestPlugin();
		testPlugin.test();
		

	}
	
	public void test() throws Exception{
		
		Utils utils = new Utils();
		
		//Test Reading VSIF input file
		String[] vsifFileNames = null;
		if ("static".equals(vsifType)) {
			vsifFileNames = new String[1];
			vsifFileNames[0] = vSIFName;
		} else {		
			vsifFileNames = utils.loadVSIFFileNames(buildID, buildNumber, buildArtifactPath, vSIFInputFile, null,deleteInputFile);
		}
		
		
		//Test connection testing
		utils.checkVAPIConnection(vAPIUrl, authRequired, vAPIUser, vAPIPassword);
		
		
		StepHolder stepHolder = null;
		if (waitTillSessionEnds){
			stepHolder = new StepHolder(inaccessibleResolver, stoppedResolver, failedResolver, doneResolver, suspendedResolver, waitTillSessionEnds,stepSessionTimeout);
		}
		utils.executeVSIFLaunch(vsifFileNames, vAPIUrl, authRequired, vAPIUser, vAPIPassword, null,false,buildID,buildNumber,buildArtifactPath,0,0,false,null,false,null,null, stepHolder);
		
		
		//utils.executeAPI("{}", "/sessions/count", vAPIUrl, authRequired, vAPIUser, vAPIPassword, "POST", null, false, buildID+"-1", buildNumber, buildArtifactPath,0,0,false);
		//utils.executeAPI("{}", "/runs/get?id=5", vAPIUrl, authRequired, vAPIUser, vAPIPassword, "GET", null, false, buildID+"-2", buildNumber, buildArtifactPath,0,0,false);
	}
	
	
	

}
