package org.jenkinsci.plugins.vmanager.dsl;


import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.File;
import org.jenkinsci.plugins.vmanager.JUnitRequestHolder;
import org.jenkinsci.plugins.vmanager.StepHolder;
import org.jenkinsci.plugins.vmanager.Utils;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;

public class VMGRLaunchStepImpl extends SynchronousNonBlockingStepExecution {
    
   private transient final VMGRLaunchStep step;
   
   VMGRLaunchStepImpl(VMGRLaunchStep step, StepContext context) {
        super(context);
        this.step = step;
        
    }
    
   @Override
   protected Void run() throws Exception {
       
        
        
        
       
        TaskListener listener = getContext().get(TaskListener.class);
        EnvVars envVars = getContext().get(EnvVars.class);
        String buildId = envVars.get("BUILD_ID");
        int buildNumber = new Integer(envVars.get("BUILD_NUMBER"));  
        String workspace = envVars.get("WORKSPACE");
        
        //Chekc if workspace is there, unless create it
        File theWSDir = new File(workspace); 
        // if the directory does not exist, create it
        if (!theWSDir.exists()) {
            listener.getLogger().println("creating workspace directory: " + theWSDir.getName());
            boolean result = false;

            try{
                theWSDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                 listener.getLogger().println("Failed to create workspace dir.  Permisisons issues");  
                 throw se;
            }        
            if(result) {    
                listener.getLogger().println("Workspace dir created");  
            }
        }
        
        
        Run run = getContext().get(Run.class);
       
        String workingJobDir = run.getRootDir().getAbsolutePath();
        listener.getLogger().println("Root dir is: " +  workingJobDir);
        
        
        listener.getLogger().println("Running Cadence vManager Session Launcher step");
        listener.getLogger().println("The HOST for vAPI is: " + step.getVAPIUrl());
        listener.getLogger().println("The vAPIUser for vAPI is: " + step.getVAPIUser());
        listener.getLogger().println("The vAPIPassword for vAPI is: *******");
        listener.getLogger().println("The vSIFName for vAPI is: " + step.getVSIFName());
        listener.getLogger().println("The vSIFPathForExternalVSIF Input for vAPI is: " + step.getVSIFInputFile());
        listener.getLogger().println("The authRequired for vAPI is: " + step.isAuthRequired());
        listener.getLogger().println("The deleteInputFile for vAPI is: " + step.isDeleteInputFile());
        listener.getLogger().println("The vsif to be executed is for vAPI is " + step.getVsifType());
        listener.getLogger().println("The id is: " + buildId);
        listener.getLogger().println("The number is: " + buildNumber);
        listener.getLogger().println("The workspace dir is: " + workspace);
        
       
        
        if (step.isAdvConfig()) {
            listener.getLogger().println("The connection timeout is: " + step.getConnTimeout() + " minutes");
            listener.getLogger().println("The read api timeout is: " + step.getReadTimeout() + " minutes");
        } else {
            listener.getLogger().println("The connection timeout is: 1 minutes");
            listener.getLogger().println("The read api timeout is: 30 minutes");
        }
        if (step.isEnvVarible()) {
            listener.getLogger().println("An environment varible file was selected.");
        }

        if (step.isUseUserOnFarm()) {
            listener.getLogger().println("An User's Credential use was selected.");
            listener.getLogger().println("The User's Credential type is: " + step.getUserFarmType());
            if ("dynamic".equals(step.getUserFarmType())) {
                listener.getLogger().println("The credential file is: " + step.getCredentialInputFile());
                listener.getLogger().println("The credential file was set to be deleted after use: " + step.isDeleteCredentialInputFile());
            }
            if (!"".equals(step.getEnvSourceInputFile().trim())) {
                listener.getLogger().println("The User's source file is: " + step.getEnvSourceInputFile());
            } else {
                listener.getLogger().println("The User's source file wasn't set");
            }

        }

        StepHolder stepHolder = null;
        JUnitRequestHolder jUnitRequestHolder = null;

        if (step.getWaitTillSessionEnds()) {
            listener.getLogger().println("Build set to finish only when session finish to run");

            listener.getLogger().println("In case session is at state \'inaccessible\' the build will " + step.getInaccessibleResolver());
            listener.getLogger().println("In case session is at state \'failed\' the build will " + step.getFailedResolver());
            listener.getLogger().println("In case session is at state \'stopped\' the build will " + step.getStoppedResolver());
            listener.getLogger().println("In case session is at state \'suspended\' the build will " + step.getSuspendedResolver());
            listener.getLogger().println("In case session is at state \'done\' the build will " + step.getDoneResolver());
            listener.getLogger().println("Timeout for entire step is " + step.getStepSessionTimeout() + " minutes");
            listener.getLogger().println("User choosed to mark regression as Failed in case all runs are failing: " + step.isMarkBuildAsFailedIfAllRunFailed());
            listener.getLogger().println("User choosed to fail the job in case all runs are failing: " + step.isFailJobIfAllRunFailed());

            listener.getLogger().println("Generate XML Report XML output: " + step.isGenerateJUnitXML());
            if (step.isGenerateJUnitXML()) {
                jUnitRequestHolder = new JUnitRequestHolder(step.isGenerateJUnitXML(), step.isExtraAttributesForFailures(), step.getStaticAttributeList());
                listener.getLogger().println("Extra Attributes in JUnit Report: " + step.isExtraAttributesForFailures());
                if (step.isExtraAttributesForFailures()) {
                    listener.getLogger().println("Extra Attributes list in JUnit Report is: " + step.getStaticAttributeList());
                }

            }

            stepHolder = new StepHolder(step.getInaccessibleResolver(), step.getStoppedResolver(), step.getFailedResolver(), step.getDoneResolver(), step.getSuspendedResolver(), step.getWaitTillSessionEnds(), step.getStepSessionTimeout(), jUnitRequestHolder, step.isMarkBuildAsFailedIfAllRunFailed(), step.isFailJobIfAllRunFailed());
        }

        try {
            Utils utils = new Utils();
            // Get the list of VSIF file to launch
            String[] vsifFileNames = null;
            String jsonEnvInput = null;

            if ("static".equals(step.getVsifType())) {
                listener.getLogger().println("The VSIF file chosen is static. VSIF file static location is: '" + step.getVSIFName() + "'");
                vsifFileNames = new String[1];
                vsifFileNames[0] = step.getVSIFName();
            } else {
                if (step.getVSIFInputFile() == null || step.getVSIFInputFile().trim().equals("")) {
                    //listener.getLogger().println("The VSIF file chosen is dynamic. VSIF directory dynamic workspace directory: '" + build.getWorkspace() + "'");
                } else {
                    listener.getLogger().println("The VSIF file chosen is static. VSIF file name is: '" + step.getVSIFInputFile().trim() + "'");
                }

                vsifFileNames = utils.loadVSIFFileNames(buildId, buildNumber, "" + workspace, step.getVSIFInputFile(), listener, step.isDeleteInputFile());

            }

            //check if user set an environment variables in addition:
            if (step.isEnvVarible()) {
                if (step.getEnvVaribleFile() == null || step.getEnvVaribleFile().trim().equals("")) {
                   listener.getLogger().println("The environment varible file chosen is dynamic. Env File directory dynamic workspace directory: '" + workspace + "'");
                } else {
                    listener.getLogger().println("The environment varible file chosen is static. Environment file name is: '" + step.getEnvVaribleFile().trim() + "'");
                }
                jsonEnvInput = utils.loadJSONEnvInput(buildId, buildNumber, "" + workspace, step.getEnvVaribleFile(), listener);
                listener.getLogger().println("Found the following environment for the vsif: " + jsonEnvInput);
            }

            String[] farmUserPassword = null;
            if ("dynamic".equals(step.getUserFarmType())) {
                if (step.getCredentialInputFile() == null || step.getCredentialInputFile().trim().equals("")) {
                    listener.getLogger().println("The credential file chosen is dynamic. Credential directory dynamic workspace directory: '" + workspace + "'");
                } else {
                    listener.getLogger().println("The credential file chosen is static. Credential file name is: '" + step.getCredentialInputFile().trim() + "'");
                }
                farmUserPassword = utils.loadFileCredentials(buildId, buildNumber, "" + workspace, step.getCredentialInputFile(), listener, step.isDeleteCredentialInputFile());
            }

            // Now call the actual launch
            
            // ----------------------------------------------------------------------------------------------------------------
            String output = utils.executeVSIFLaunch(vsifFileNames, step.getVAPIUrl(), step.isAuthRequired(), step.getVAPIUser(), step.getVAPIPassword(), listener, step.isDynamicUserId(), buildId, buildNumber,
                    "" + workspace, step.getConnTimeout(), step.getReadTimeout(), step.isAdvConfig(), jsonEnvInput, step.isUseUserOnFarm(), step.getUserFarmType(), farmUserPassword, stepHolder, step.getEnvSourceInputFile(), workingJobDir);
            if (!"success".equals(output)) {
                listener.getLogger().println("Failed to launch vsifs for build " + buildId + " " + buildNumber + "\n");
                listener.getLogger().println(output + "\n");
                throw new Exception("Failed to launch vsifs for build " + buildId + " " + buildNumber + "\n"); //false;
            }
            // ----------------------------------------------------------------------------------------------------------------

        } catch (Exception e) {
            //listener.getLogger().println("Failed to build " + build.getId() + " " + build.getNumber());
            listener.getLogger().println(e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                listener.getLogger().println(" " + ste);
            }
            
            throw e; //"false;
            
        }

        return null; //true
    }
}
