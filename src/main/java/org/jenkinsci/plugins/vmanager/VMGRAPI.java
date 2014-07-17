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

public class VMGRAPI extends Builder {

	private final String vAPIUrl;
	private final String vAPIPort;
	private final boolean authRequired;
	private final String vAPIUser;
	private final String vAPIPassword;
	private final String vAPIInput;
	private final String vJsonInputFile;
	private final boolean deleteInputFile;
	private final boolean dynamicUserId;
	private final String apiType;
	private final String apiUrl;


	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public VMGRAPI(String vAPIUrl, String vAPIPort, String vAPIUser, String vAPIPassword, String vAPIInput, String vJsonInputFile, boolean deleteInputFile, boolean authRequired, String apiType,
			boolean dynamicUserId, String apiUrl) {
		this.vAPIUrl = vAPIUrl;
		this.vAPIPort = vAPIPort;
		this.vAPIUser = vAPIUser;
		this.vAPIPassword = vAPIPassword;
		this.vAPIInput = vAPIInput;
		this.vJsonInputFile = vJsonInputFile;
		this.authRequired = authRequired;
		this.deleteInputFile = deleteInputFile;
		this.apiType = apiType;
		this.dynamicUserId = dynamicUserId;
		this.apiUrl = apiUrl;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getVAPIUrl() {
		return vAPIUrl;
	}

	public String getVAPIPort() {
		return vAPIPort;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public String getVAPIUser() {
		return vAPIUser;
	}

	public String getVAPIPassword() {
		return vAPIPassword;
	}

	public String getVAPIInput() {
		return vAPIInput;
	}

	public String getVJsonInputFile() {
		return vJsonInputFile;
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

	public String getApiType() {
		return apiType;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

		listener.getLogger().println("The HOST for vAPI is: " + vAPIUrl);
		listener.getLogger().println("The PORT for vAPI is: " + vAPIPort);
		listener.getLogger().println("The vAPIUser for vAPI is: " + vAPIUser);
		listener.getLogger().println("The vAPIPassword for vAPI is: *******");
		listener.getLogger().println("The Static jSON query for vAPI is: " + vAPIInput);
		listener.getLogger().println("The Input file name for vAPI is: " + vJsonInputFile);
		listener.getLogger().println("The authRequired for vAPI is: " + authRequired);
		listener.getLogger().println("The deleteInputFile for vAPI is: " + deleteInputFile);
		listener.getLogger().println("The type of input file for vAPI is " + apiType);
		listener.getLogger().println("The api call for vAPI is: " + apiUrl);
		listener.getLogger().println("The id is: " + build.getId());
		listener.getLogger().println("The number is: " + build.getNumber());
		listener.getLogger().println("The workspace dir is: " + build.getWorkspace());

		try {
			Utils utils = new Utils();
			// Get the jSON query string
			String jSonInput = null;

			if ("static".equals(apiType)) {
				listener.getLogger().println("The vAPI query string input chosen is static. jSON input is: '" + vAPIInput + "'");
				jSonInput = vAPIInput;
			} else {
				if (vJsonInputFile == null || vJsonInputFile.trim().equals("")) {
					listener.getLogger().println("The vAPI query string chosen is dynamic. jSON input file dynamic workspace directory: '" + build.getWorkspace() + "'");
				} else {
					listener.getLogger().println(
							"The vAPI query string chosen is dynamic. jSON input file dynamic workspace directory: '" + build.getWorkspace() + File.separator + vJsonInputFile.trim() + "'");
				}
				jSonInput = utils.loadJSONFromFile(build.getId(), build.getNumber(), "" + build.getWorkspace(), vJsonInputFile, listener, deleteInputFile);

			}

			// Now call the actual launch
			// ----------------------------------------------------------------------------------------------------------------
			String output = utils.executeAPI(jSonInput, apiUrl, vAPIUrl, vAPIPort, authRequired, vAPIUser, vAPIPassword, listener, dynamicUserId, build.getId(), build.getNumber(),
					"" + build.getWorkspace());
			if (!"success".equals(output)) {
				listener.getLogger().println("Failed to call vAPI for build " + build.getId() + " " + build.getNumber() + "\n");
				listener.getLogger().println(output + "\n");
				return false;
			}
			// ----------------------------------------------------------------------------------------------------------------

		} catch (Exception e) {
			listener.getLogger().println("Failed to call vAPI for build " + build.getId() + " " + build.getNumber());
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
	 * Descriptor for {@link VMGRAPI}. Used as a singleton. The class is marked
	 * as public so that it can be accessed from views.
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

		public FormValidation doCheckApiUrl(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set the vManager vAPI URL ");
			if (value.length() < 8)
				return FormValidation.warning("Isn't the name too short?");
			return FormValidation.ok();
		}

		public FormValidation doCheckVAPIPort(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set the vManager vAPI PORT ");
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
			return "Cadence vManager API";
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
				@QueryParameter("vAPIUrl") final String vAPIUrl, @QueryParameter("vAPIPort") final String vAPIPort, @QueryParameter("authRequired") final boolean authRequired) throws IOException,
				ServletException {
			try {

				Utils utils = new Utils();
				String output = utils.checkVAPIConnection(vAPIUrl, vAPIPort, authRequired, vAPIUser, vAPIPassword);
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
