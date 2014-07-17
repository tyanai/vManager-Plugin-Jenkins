package test;

import java.util.logging.Logger;

import org.jenkinsci.plugins.vmanager.Utils;
import org.jenkinsci.plugins.vmanager.VMGRLaunch;

public class TestPlugin {

	
	final String vAPIUrl = "vlnx277";
    final String vAPIPort = "55000";
    final boolean authRequired = true;
    final String vAPIUser = "vAPI";
    final String vAPIPassword = "password";
    final String vSIFName = "/home/tyanai/vsif/vm_basic.vsif";
    final String vSIFInputFile  = "vsifs.input";
    final boolean deleteInputFile = false;
    final String vsifType = "dynamic";
    
    final int buildNumber = 83;
    final String buildID = "2014-45-45-34-56-78";
    final String buildArtifactPath = "d:/temp/artifacts";
    
    	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		TestPlugin testPlugin = new TestPlugin();
		testPlugin.test();
		

	}
	
	public void test() throws Exception{
		
		Utils utils = new Utils();
		
		//Test Reading VSIF input file
		String[] vsifs = utils.loadVSIFFileNames(buildID, buildNumber, buildArtifactPath, vSIFInputFile, null,deleteInputFile);
		
		//Test connection testing
		utils.checkVAPIConnection(vAPIUrl, vAPIPort, authRequired, vAPIUser, vAPIPassword);
		
		
		utils.executeVSIFLaunch(vsifs, vAPIUrl, vAPIPort, authRequired, vAPIUser, vAPIPassword, null,true,buildID,buildNumber,buildArtifactPath);
		
		
		utils.executeAPI("{}", "/runs/count", vAPIUrl, vAPIPort, authRequired, vAPIUser, vAPIPassword,null, false, buildID, buildNumber, buildArtifactPath);
	}
	
	
	

}
