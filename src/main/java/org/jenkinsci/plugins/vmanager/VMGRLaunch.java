package org.jenkinsci.plugins.vmanager;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
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
	private final boolean deleteInputFile;
	private final boolean dynamicUserId;
	private final String vsifType;
	
	private int connTimeout = 1;
	private int readTimeout = 30;
	private final boolean envVarible;
	private final String envVaribleFile;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public VMGRLaunch(String vAPIUrl, String vAPIUser, String vAPIPassword, String vSIFName, String vSIFInputFile, boolean deleteInputFile, boolean authRequired, String vsifType,
			boolean dynamicUserId, boolean advConfig, int connTimeout, int readTimeout,boolean envVarible,String envVaribleFile) {
		this.vAPIUrl = vAPIUrl;
		this.vAPIUser = vAPIUser;
		this.vAPIPassword = vAPIPassword;
		this.vSIFName = vSIFName;
		this.vSIFInputFile = vSIFInputFile;
		this.authRequired = authRequired;
		this.advConfig = advConfig;
		this.envVarible = envVarible;
		this.deleteInputFile = deleteInputFile;
		this.vsifType = vsifType;
		this.dynamicUserId = dynamicUserId;
		this.envVaribleFile = envVaribleFile;
		
		this.connTimeout = connTimeout;
		this.readTimeout = readTimeout;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
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
	
	public String getVSIFInputFile() {
		return vSIFInputFile;
	}

	public boolean isAuthRequired() {
		return authRequired;
	}

	public boolean isDeleteInputFile() {
		return deleteInputFile;
	}

	public boolean isDynamicUserId() {
		return dynamicUserId;
	}

	public String getVsifType() {
		return vsifType;
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

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

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
			
			
			

			// Now call the actual launch
			// ----------------------------------------------------------------------------------------------------------------
			String output = utils.executeVSIFLaunch(vsifFileNames, vAPIUrl, authRequired, vAPIUser, vAPIPassword, listener, dynamicUserId, build.getId(), build.getNumber(),
					"" + build.getWorkspace(),connTimeout,readTimeout,advConfig,jsonEnvInput);
			if (!"success".equals(output)) {
				listener.getLogger().println("Failed to launch vsifs for build " + build.getId() + " " + build.getNumber() + "\n");
				listener.getLogger().println(output + "\n");
				return false;
			}
			// ----------------------------------------------------------------------------------------------------------------

		} catch (Exception e) {
			listener.getLogger().println("Failed to launch vsifs for build " + build.getId() + " " + build.getNumber());
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
		 * <p>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */
		private boolean useFrench;

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


		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project
			// types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Cadence vManager Session Launcher";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			useFrench = formData.getBoolean("useFrench");
			// ^Can also use req.bindJSON(this, formData);
			// (easier when there are many fields; need set* methods for this,
			// like setUseFrench)
			save();
			return super.configure(req, formData);
		}

		/**
		 * This method returns true if the global configuration says we should
		 * speak French.
		 * 
		 * The method name is bit awkward because global.jelly calls this method
		 * to determine the initial state of the checkbox by the naming
		 * convention.
		 */
		public boolean getUseFrench() {
			return useFrench;
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

	}
}
