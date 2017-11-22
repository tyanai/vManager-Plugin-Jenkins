package org.jenkinsci.plugins.vmanager;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class VMGRLaunch extends Builder {

	private final String vAPIUrl;
	private final boolean authRequired;
	private final boolean advConfig;
	private final String vAPIUser;
	private final String vAPIPassword;
	private final String vSIFName;
	private final String vSIFInputFile;
	private final String credentialInputFile;
	private final boolean deleteInputFile;
	private final boolean deleteCredentialInputFile; 
	private final boolean useUserOnFarm;
	private final boolean dynamicUserId;
	private final String vsifType;
	private final String userFarmType;
        private final String envSourceInputFile;
	
	private int connTimeout = 1;
	private int readTimeout = 30;
	private final boolean envVarible;
	private final String envVaribleFile;
	
	
	private final String inaccessibleResolver;
	private final String stoppedResolver;
	private final String failedResolver;
	private final String doneResolver;
	private final String suspendedResolver;
	private final boolean waitTillSessionEnds;
	private int stepSessionTimeout = 0;
	
	
	private final boolean generateJUnitXML;
	private final boolean extraAttributesForFailures; 
	private final String staticAttributeList;
	private final boolean markBuildAsFailedIfAllRunFailed;
        private final boolean failJobIfAllRunFailed;
	//private final String extraAttributesForFailuresInputFile;
	//private final boolean deleteExtraAttributesFile;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public VMGRLaunch(String vAPIUrl, String vAPIUser, String vAPIPassword, String vSIFName, String vSIFInputFile, String credentialInputFile, boolean deleteInputFile, boolean deleteCredentialInputFile, boolean useUserOnFarm, boolean authRequired, String vsifType, String userFarmType,
			boolean dynamicUserId, boolean advConfig, int connTimeout, int readTimeout,boolean envVarible,String envVaribleFile,String inaccessibleResolver,String stoppedResolver,String failedResolver,String doneResolver,String suspendedResolver,boolean waitTillSessionEnds, int stepSessionTimeout,boolean generateJUnitXML,boolean extraAttributesForFailures,String staticAttributeList,boolean markBuildAsFailedIfAllRunFailed ,boolean failJobIfAllRunFailed,String envSourceInputFile) {
		this.vAPIUrl = vAPIUrl;
		this.vAPIUser = vAPIUser;
		this.vAPIPassword = vAPIPassword;
		this.vSIFName = vSIFName;
		this.vSIFInputFile = vSIFInputFile;
		this.credentialInputFile = credentialInputFile;
		this.authRequired = authRequired;
		this.advConfig = advConfig;
		this.envVarible = envVarible;
		this.deleteInputFile = deleteInputFile;
		this.deleteCredentialInputFile = deleteCredentialInputFile;
		this.useUserOnFarm = useUserOnFarm;
		this.vsifType = vsifType;
		this.userFarmType = userFarmType;
		this.dynamicUserId = dynamicUserId;
		this.envVaribleFile = envVaribleFile;
                this.envSourceInputFile = envSourceInputFile;
		
		this.connTimeout = connTimeout;
		this.readTimeout = readTimeout;
		
		this.inaccessibleResolver = inaccessibleResolver;
		this.stoppedResolver = stoppedResolver;
		this.failedResolver = failedResolver;
		this.doneResolver = doneResolver;
		this.suspendedResolver = suspendedResolver;
		this.waitTillSessionEnds = waitTillSessionEnds;
		this.stepSessionTimeout = stepSessionTimeout;
		
		this.generateJUnitXML = generateJUnitXML;
		this.extraAttributesForFailures = extraAttributesForFailures;
		this.markBuildAsFailedIfAllRunFailed = markBuildAsFailedIfAllRunFailed;
                this.failJobIfAllRunFailed = failJobIfAllRunFailed;
		this.staticAttributeList = staticAttributeList;
		

		
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	
	public boolean isExtraAttributesForFailures() {
		return extraAttributesForFailures;
	}
	
	public boolean isMarkBuildAsFailedIfAllRunFailed() {
		return markBuildAsFailedIfAllRunFailed;
	}
        
        public boolean isFailJobIfAllRunFailed() {
		return failJobIfAllRunFailed;
	}
	
	public String getStaticAttributeList() {
		return staticAttributeList;
	}
	
	
	public boolean isGenerateJUnitXML() {
		return generateJUnitXML;
	}
	
	public String getVAPIUrl() {
		return vAPIUrl;
	}


	public String getVAPIUser() {
		return vAPIUser;
	}

	public String getVAPIPassword() {
		return vAPIPassword;
	}

	public String getVSIFName() {
		return vSIFName;
	}

	public String getEnvVaribleFile() {
		return envVaribleFile;
	}
        
        public String getEnvSourceInputFile() {
		return envSourceInputFile;
	}
	
	public String getVSIFInputFile() {
		return vSIFInputFile;
	}
	
	public String getCredentialInputFile() {
		return credentialInputFile;
	}
	
	public boolean isAuthRequired() {
		return authRequired;
	}

	public boolean isDeleteInputFile() {
		return deleteInputFile;
	}
	
	public boolean isDeleteCredentialInputFile() {
		return deleteCredentialInputFile;
	}
	
	public boolean isUseUserOnFarm() {
		return useUserOnFarm;
	}

	public boolean isDynamicUserId() {
		return dynamicUserId;
	}

	public String getVsifType() {
		return vsifType;
	}
	
	public String getUserFarmType() {
		return userFarmType;
	}
	
	public boolean isAdvConfig() {
		return advConfig;
	}
	
	public boolean isEnvVarible() {
		return envVarible;
	}
	
	public int getConnTimeout() {
		return connTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}
	
	public int getStepSessionTimeout() {
		return stepSessionTimeout;
	}
	
	
	
	
	public String getInaccessibleResolver(){
		return inaccessibleResolver;
	}
	public String getStoppedResolver(){
		return stoppedResolver;
	}
	public String getFailedResolver(){
		return failedResolver;
	}
	public String getDoneResolver(){
		return doneResolver;
	}
	public String getSuspendedResolver(){
		return suspendedResolver;
	}
	public boolean getWaitTillSessionEnds(){
		return waitTillSessionEnds;
	}
	
	
	

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

		String workingJobDir = build.getRootDir().getAbsolutePath();
                listener.getLogger().println("Root dir is: " +  workingJobDir);
            
            
                listener.getLogger().println("The HOST for vAPI is: " + vAPIUrl);
		listener.getLogger().println("The vAPIUser for vAPI is: " + vAPIUser);
		listener.getLogger().println("The vAPIPassword for vAPI is: *******");
		listener.getLogger().println("The vSIFName for vAPI is: " + vSIFName);
		listener.getLogger().println("The vSIFPathForExternalVSIF Input for vAPI is: " + vSIFInputFile);
		listener.getLogger().println("The authRequired for vAPI is: " + authRequired);
		listener.getLogger().println("The deleteInputFile for vAPI is: " + deleteInputFile);
		listener.getLogger().println("The vsif to be executed is for vAPI is " + vsifType);
		listener.getLogger().println("The id is: " + build.getId());
		listener.getLogger().println("The number is: " + build.getNumber());
		listener.getLogger().println("The workspace dir is: " + build.getWorkspace());
		if (advConfig){
			listener.getLogger().println("The connection timeout is: " + connTimeout + " minutes");
			listener.getLogger().println("The read api timeout is: " + readTimeout + " minutes");
		} else {
			listener.getLogger().println("The connection timeout is: 1 minutes");
			listener.getLogger().println("The read api timeout is: 30 minutes");
		}
		if (envVarible){
			listener.getLogger().println("An environment varible file was selected.");
			//listener.getLogger().println("The environment varible file is: " + envVaribleFile);
		} 
		
		if (useUserOnFarm){
			listener.getLogger().println("An User's Credential use was selected.");
			listener.getLogger().println("The User's Credential type is: " + userFarmType);
			if ("dynamic".equals(userFarmType)){
				listener.getLogger().println("The credential file is: " + credentialInputFile);
				listener.getLogger().println("The credential file was set to be deleted after use: " + deleteCredentialInputFile);
			}
                        if (!"".equals(envSourceInputFile.trim())){
                            listener.getLogger().println("The User's source file is: " + envSourceInputFile);
                        } else {
                            listener.getLogger().println("The User's source file wasn't set");
                        }
			
		} 
		
		StepHolder stepHolder = null;
		JUnitRequestHolder jUnitRequestHolder = null;
		
		if (waitTillSessionEnds){
			listener.getLogger().println("Build set to finish only when session finish to run");
			
			listener.getLogger().println("In case session is at state \'inaccessible\' the build will " + inaccessibleResolver);
			listener.getLogger().println("In case session is at state \'failed\' the build will " + failedResolver);
			listener.getLogger().println("In case session is at state \'stopped\' the build will " + stoppedResolver);
			listener.getLogger().println("In case session is at state \'suspended\' the build will " + suspendedResolver);
			listener.getLogger().println("In case session is at state \'done\' the build will " + doneResolver);
			listener.getLogger().println("Timeout for entire step is " + stepSessionTimeout + " minutes");
			listener.getLogger().println("User choosed to mark regression as Failed in case all runs are failing: " + markBuildAsFailedIfAllRunFailed);
                        listener.getLogger().println("User choosed to fail the job in case all runs are failing: " + failJobIfAllRunFailed);
			
			
			
			listener.getLogger().println("Generate XML Report XML output: " + generateJUnitXML );
			if (generateJUnitXML){
				jUnitRequestHolder = new JUnitRequestHolder(generateJUnitXML, extraAttributesForFailures, staticAttributeList);
				listener.getLogger().println("Extra Attributes in JUnit Report: " + extraAttributesForFailures);
				if (extraAttributesForFailures){
					listener.getLogger().println("Extra Attributes list in JUnit Report is: " + staticAttributeList);
				} 
				
			}
			
                        
			
			stepHolder = new StepHolder(inaccessibleResolver, stoppedResolver, failedResolver, doneResolver, suspendedResolver, waitTillSessionEnds,stepSessionTimeout,jUnitRequestHolder, markBuildAsFailedIfAllRunFailed, failJobIfAllRunFailed);
		}

		try {
			Utils utils = new Utils();
			// Get the list of VSIF file to launch
			String[] vsifFileNames = null;
			String jsonEnvInput = null;

			if ("static".equals(vsifType)) {
				listener.getLogger().println("The VSIF file chosen is static. VSIF file static location is: '" + vSIFName + "'");
				vsifFileNames = new String[1];
				vsifFileNames[0] = vSIFName;
			} else {
				if (vSIFInputFile == null || vSIFInputFile.trim().equals("")) {
					listener.getLogger().println("The VSIF file chosen is dynamic. VSIF directory dynamic workspace directory: '" + build.getWorkspace() + "'");
				} else {
					listener.getLogger().println("The VSIF file chosen is static. VSIF file name is: '" +  vSIFInputFile.trim() + "'");
				}
				
				vsifFileNames = utils.loadVSIFFileNames(build.getId(), build.getNumber(), "" + build.getWorkspace(), vSIFInputFile, listener, deleteInputFile);

			}
			
			//check if user set an environment variables in addition:
			if (envVarible){
				if (envVaribleFile == null || envVaribleFile.trim().equals("")) {
					listener.getLogger().println("The environment varible file chosen is dynamic. Env File directory dynamic workspace directory: '" + build.getWorkspace() + "'");
				} else {
					listener.getLogger().println("The environment varible file chosen is static. Environment file name is: '" +  envVaribleFile.trim() + "'");
				}
				jsonEnvInput = utils.loadJSONEnvInput(build.getId(), build.getNumber(), "" + build.getWorkspace(), envVaribleFile, listener);
				listener.getLogger().println("Found the following environment for the vsif: " + jsonEnvInput);
			}
			
			
			String[] farmUserPassword = null;
			if ("dynamic".equals(userFarmType)){
				if (credentialInputFile == null || credentialInputFile.trim().equals("")) {
					listener.getLogger().println("The credential file chosen is dynamic. Credential directory dynamic workspace directory: '" + build.getWorkspace() + "'");
				} else {
					listener.getLogger().println("The credential file chosen is static. Credential file name is: '" +  credentialInputFile.trim() + "'");
				}
				farmUserPassword = utils.loadFileCredentials(build.getId(), build.getNumber(), "" + build.getWorkspace(), credentialInputFile, listener, deleteCredentialInputFile);
			}
			
			

			// Now call the actual launch
			// ----------------------------------------------------------------------------------------------------------------
			
                        
                        
			String output = utils.executeVSIFLaunch(vsifFileNames, vAPIUrl, authRequired, vAPIUser, vAPIPassword, listener, dynamicUserId, build.getId(), build.getNumber(),
					"" + build.getWorkspace(),connTimeout,readTimeout,advConfig,jsonEnvInput,useUserOnFarm,userFarmType,farmUserPassword,stepHolder,envSourceInputFile, workingJobDir);
			if (!"success".equals(output)) {
				listener.getLogger().println("Failed to launch vsifs for build " + build.getId() + " " + build.getNumber() + "\n");
				listener.getLogger().println(output + "\n");
				return false;
			}
			// ----------------------------------------------------------------------------------------------------------------

		} catch (Exception e) {                      
			listener.getLogger().println("Failed to build " + build.getId() + " " + build.getNumber());
                        listener.getLogger().println(e.getMessage());
                        for (StackTraceElement ste :e.getStackTrace()) {
                            listener.getLogger().println(" " + ste);
                        }
                        
			return false;
		}

		return true;
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link VMGRLaunch}. Used as a singleton. The class is
	 * marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See
	 * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	// This indicates to Jenkins that this is an implementation of an extension
	// point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		/**
		 * To persist global configuration information, simply store it in a
		 * field and call save().
		 * 
		

		/**
		 * In order to load the persisted global configuration, you have to call
		 * load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}

		/**
		 * Performs on-the-fly validation of the form field 'name'.
		 * 
		 * @param value
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 *         <p>
		 *         Note that returning {@link FormValidation#error(String)} does
		 *         not prevent the form from being saved. It just means that a
		 *         message will be displayed to the user.
		 */
		public FormValidation doCheckVAPIUrl(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set the vManager vAPI HOST ");
			if (value.length() < 4)
				return FormValidation.warning("Isn't the name too short?");
			return FormValidation.ok();
		}
		
		public FormValidation doCheckStaticAttributeList(@QueryParameter String value) throws IOException, ServletException {
			if (value != null){
				if (value.indexOf(";") > 0) {
					return FormValidation.error("(;) is not allowed for seperation.  Please use only comma as a seperator.");
				} else if (value.indexOf("|") > 0) {
					return FormValidation.error("(|) is not allowed for seperation.  Please use only comma as a seperator.");
				} else if (value.indexOf(".") > 0) {
					return FormValidation.error("(.) is not allowed for seperation.  Please use only comma as a seperator.");
				} 
			} 
			
			List<String> items = Arrays.asList(value.split("\\s*,\\s*"));
			
			Iterator<String> iter = items.iterator();
			
			String tmpAttr = null;
			while (iter.hasNext()){
				tmpAttr = iter.next();
				if (tmpAttr.indexOf(" ") > 0){
					return FormValidation.error("'" + tmpAttr + "' is not a valid option for vManager attribute code name. Attribute code names can't have space.  Try using underscore instaed.");
				} else if (tmpAttr.equals("first_failure_name")){
					return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
				} else if (tmpAttr.equals("first_failure_description")){
					return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
				} else if (tmpAttr.equals("computed_seed")){
					return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
				} else if (tmpAttr.equals("test_group")){
					return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
				} else if (tmpAttr.equals("test_name")){
					return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
				}
			}
			
				
			return FormValidation.ok();
		}


		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project
			// types
			return true;
		}
		
		
		public ListBoxModel doFillInaccessibleResolverItems() {
	        ListBoxModel items = new ListBoxModel();
	        items.add("Mark the build as failed","fail");
	        items.add("Continue, and move to the next build step","continue");
	        items.add("Ignore, and continue to wait","ignore");
	        return items;
	    }
		
		public ListBoxModel doFillStoppedResolverItems() {
			 ListBoxModel items = new ListBoxModel();
			 items.add("Mark the build as failed","fail");
		     items.add("Continue, and move to the next build step","continue");
		     items.add("Ignore, and continue to wait","ignore");
	        return items;
	    }
		
		public ListBoxModel doFillFailedResolverItems() {
	        ListBoxModel items = new ListBoxModel();
	        items.add("Mark the build as failed","fail");
	        items.add("Continue, and move to the next build step","continue");
	        items.add("Ignore, and continue to wait","ignore");
	        return items;
	    }
		
		public ListBoxModel doFillDoneResolverItems() {
			ListBoxModel items = new ListBoxModel();
			items.add("Ignore, and continue to wait","ignore");
			items.add("Continue, and move to the next build step","continue");
	        items.add("Mark the build as failed","fail");
	        return items;
	    }
		
		public ListBoxModel doFillSuspendedResolverItems() {
			ListBoxModel items = new ListBoxModel();
			items.add("Ignore, and continue to wait","ignore");
			items.add("Continue, and move to the next build step","continue");
	        items.add("Mark the build as failed","fail");
	        return items;
	    }
		
		

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Cadence vManager Session Launcher";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			
			save();
			return super.configure(req, formData);
		}

		

		public FormValidation doTestConnection(@QueryParameter("vAPIUser") final String vAPIUser, @QueryParameter("vAPIPassword") final String vAPIPassword,
				@QueryParameter("vAPIUrl") final String vAPIUrl, @QueryParameter("authRequired") final boolean authRequired) throws IOException,
				ServletException {
			try {

				Utils utils = new Utils();
				String output = utils.checkVAPIConnection(vAPIUrl, authRequired, vAPIUser, vAPIPassword);
				if (!output.startsWith("Failed")) {
					return FormValidation.ok("Success. " + output);
				} else {
					return FormValidation.error(output);
				}
			} catch (Exception e) {
				return FormValidation.error("Client error : " + e.getMessage());
			}
		}
		
		public FormValidation doTestExtraStaticAttr(@QueryParameter("vAPIUser") final String vAPIUser, @QueryParameter("vAPIPassword") final String vAPIPassword,
				@QueryParameter("vAPIUrl") final String vAPIUrl, @QueryParameter("authRequired") final boolean authRequired, @QueryParameter("staticAttributeList") final String staticAttributeList) throws IOException,
				ServletException {
			try {

		 		Utils utils = new Utils();
				String output = utils.checkExtraStaticAttr(vAPIUrl, authRequired, vAPIUser, vAPIPassword,staticAttributeList);
				if (!output.startsWith("Failed")) {
					return FormValidation.ok("Success. " + output);
				} else {
					return FormValidation.error(output);
				}
			} catch (Exception e) {
				return FormValidation.error("Client error : " + e.getMessage());
			}
		}
		
		

	}
}
