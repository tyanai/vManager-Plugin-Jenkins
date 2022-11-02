package org.jenkinsci.plugins.vmanager.dsl;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.vmanager.Utils;
import org.jenkinsci.plugins.vmanager.VMGRBuildArchiver;
import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class VMGRLaunchStep extends Step {

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
    private final boolean attrValues;
    private final String attrValuesFile;
    
    private final boolean pipelineNodes;
    private final String masterWorkspaceLocation;

    private final String envSourceInputFileType;
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
    private final boolean markBuildAsPassedIfAllRunPassed;
    private final boolean failJobUnlessAllRunPassed;
    private final boolean userPrivateSSHKey;
    
    private final boolean vMGRBuildArchive;
    private final boolean deleteAlsoSessionDirectory;
    private final boolean genericCredentialForSessionDelete;
    private final String archiveUser;
    private final String archivePassword;
    
    private final String famMode;
    private final String famModeLocation;
    private final boolean noAppendSeed;

    private final String executionType ;
    private final String sessionsInputFile;
    private final boolean deleteSessionInputFile;
    private final boolean pauseSessionOnBuildInterruption;
    
    private final String executionScript;
    private final String executionShellLocation;
    private final String executionVsifFile;
    
    private final String defineVaribleFile;
    private final boolean defineVarible;
    private final String defineVariableType;
    private final String defineVariableText;
    private String defineVaribleFileFix;
    

    @DataBoundConstructor
    public VMGRLaunchStep(String vAPIUrl, String vAPIUser, String vAPIPassword, String vSIFName, String vSIFInputFile, String credentialInputFile, boolean deleteInputFile, boolean deleteCredentialInputFile, boolean useUserOnFarm, boolean authRequired, String vsifType, String userFarmType,
            boolean dynamicUserId, boolean advConfig, int connTimeout, int readTimeout, boolean envVarible, String envVaribleFile, String inaccessibleResolver, String stoppedResolver, String failedResolver, String doneResolver, String suspendedResolver, boolean waitTillSessionEnds, int stepSessionTimeout, 
            boolean generateJUnitXML, boolean extraAttributesForFailures, String staticAttributeList, boolean markBuildAsFailedIfAllRunFailed, boolean failJobIfAllRunFailed, String envSourceInputFile, boolean vMGRBuildArchive, boolean deleteAlsoSessionDirectory, boolean genericCredentialForSessionDelete, 
            String archiveUser, String archivePassword, String famMode, String famModeLocation, boolean noAppendSeed, boolean pipelineNodes, String masterWorkspaceLocation, boolean markBuildAsPassedIfAllRunPassed, boolean failJobUnlessAllRunPassed, boolean userPrivateSSHKey, boolean attrValues, 
            String attrValuesFile, String executionType, String sessionsInputFile, boolean deleteSessionInputFile, boolean pauseSessionOnBuildInterruption, String envSourceInputFileType, String executionScript, String executionShellLocation, String executionVsifFile, String defineVaribleFile, boolean defineVarible, String defineVariableType, String defineVariableText) {
        this.vAPIUrl = vAPIUrl;
        this.vAPIUser = vAPIUser;
        this.vAPIPassword = vAPIPassword;
        this.vSIFName = vSIFName;
        this.vSIFInputFile = vSIFInputFile;
        this.credentialInputFile = credentialInputFile;
        this.authRequired = authRequired;
        this.advConfig = advConfig;
        this.envVarible = envVarible;
        this.attrValues = attrValues;
        this.deleteInputFile = deleteInputFile;
        this.deleteCredentialInputFile = deleteCredentialInputFile;
        this.useUserOnFarm = useUserOnFarm;
        this.vsifType = vsifType;
        this.userFarmType = userFarmType;
        this.dynamicUserId = dynamicUserId;
        this.envVaribleFile = envVaribleFile;
        this.attrValuesFile = attrValuesFile;
        this.pipelineNodes = pipelineNodes;
        this.masterWorkspaceLocation = masterWorkspaceLocation;
        this.envSourceInputFile = envSourceInputFile;

        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;

        this.envSourceInputFileType = envSourceInputFileType;
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
        
        this.markBuildAsPassedIfAllRunPassed = markBuildAsPassedIfAllRunPassed;
        this.failJobUnlessAllRunPassed = failJobUnlessAllRunPassed;
        this.userPrivateSSHKey = userPrivateSSHKey;
        
        this.staticAttributeList = staticAttributeList;
        
        this.vMGRBuildArchive = vMGRBuildArchive;
        this.deleteAlsoSessionDirectory = deleteAlsoSessionDirectory;
        this.genericCredentialForSessionDelete = genericCredentialForSessionDelete;
        this.archiveUser = archiveUser;
        this.archivePassword = archivePassword;

        this.famMode = famMode;
        this.famModeLocation = famModeLocation;
        this.noAppendSeed = noAppendSeed;
        
        this.executionType = executionType;
        this.sessionsInputFile = sessionsInputFile;
        this.deleteSessionInputFile = deleteSessionInputFile; 
        this.pauseSessionOnBuildInterruption = pauseSessionOnBuildInterruption;
        
        this.executionScript = executionScript;
        this.executionShellLocation = executionShellLocation;
        this.executionVsifFile = executionVsifFile;
        
        this.defineVaribleFile = defineVaribleFile;
        this.defineVarible = defineVarible;
        this.defineVariableType = defineVariableType;
        this.defineVariableText = defineVariableText;
    }

    
    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    
    public String getExecutionScript() {
        return executionScript;
    }

    public String getExecutionShellLocation() {
        return executionShellLocation;
    }

    
    public String getExecutionVsifFile() {
        return executionVsifFile;
    }

    public String getSessionsInputFile() {
        return sessionsInputFile;
    }

    public boolean isDeleteSessionInputFile() {
        return deleteSessionInputFile;
    }
    
    public String getExecutionType() {
        return executionType;
    }    
    
    
    public boolean isExtraAttributesForFailures() {
        return extraAttributesForFailures;
    }

    public boolean isNoAppendSeed() {
        return noAppendSeed;
    }
    
    public boolean isPauseSessionOnBuildInterruption(){
        return pauseSessionOnBuildInterruption;
    }
    

    public boolean isMarkBuildAsFailedIfAllRunFailed() {
        return markBuildAsFailedIfAllRunFailed;
    }

    public boolean isFailJobIfAllRunFailed() {
        return failJobIfAllRunFailed;
    }
    
    public boolean isMarkBuildAsPassedIfAllRunPassed() {
        return markBuildAsPassedIfAllRunPassed;
    }

    public boolean isFailJobUnlessAllRunPassed() {
        return failJobUnlessAllRunPassed;
    }
    
    public boolean isUserPrivateSSHKey() {
        return userPrivateSSHKey;
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
    
     public String getAttrValuesFile() {
        return attrValuesFile;
    }
    
    public String getMasterWorkspaceLocation() {
        return masterWorkspaceLocation;
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
    
    public boolean isAttrValues() {
        return attrValues;
    }
    
    public boolean isPipelineNodes() {
        return pipelineNodes;
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

    public String getInaccessibleResolver() {
        return inaccessibleResolver;
    }
    
    public String getEnvSourceInputFileType(){
        return envSourceInputFileType;
    }

    public String getStoppedResolver() {
        return stoppedResolver;
    }

    public String getFailedResolver() {
        return failedResolver;
    }

    public String getDoneResolver() {
        return doneResolver;
    }

    public String getSuspendedResolver() {
        return suspendedResolver;
    }

    public boolean isWaitTillSessionEnds() {
        return waitTillSessionEnds;
    }
    
    public boolean isVMGRBuildArchive() {
        return vMGRBuildArchive;
    }

    public boolean isDeleteAlsoSessionDirectory() {
        return deleteAlsoSessionDirectory;
    }

    public boolean isGenericCredentialForSessionDelete() {
        return genericCredentialForSessionDelete;
    }

    public String getArchiveUser() {
        return archiveUser;
    }

    public String getArchivePassword() {
        return archivePassword;
    }

    public String getFamMode() {
        return famMode;
    }

    public String getFamModeLocation() {
        return famModeLocation;
    }
    
    public String getDefineVaribleFile() {
        return defineVaribleFile;
    }

    public boolean isDefineVarible() {
        return defineVarible;
    }

    public String getDefineVariableType() {
        return defineVariableType;
    }

    public String getDefineVariableText() {
        return defineVariableText;
    }

    
    
     @Override
    public StepExecution start(StepContext context) throws Exception {
        return new VMGRLaunchStepImpl(this, context);
    }
    
    

    @Extension
    public static class DescriptorImpl extends StepDescriptor  {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getFunctionName() {
            return "vmanagerLaunch";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Cadence vManager Session Launcher";
        }

        public FormValidation doCheckVAPIUrl(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set the Verisium Manager vAPI HOST ");
            }
            if (value.length() < 4) {
                return FormValidation.warning("Isn't the name too short?");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckStaticAttributeList(@QueryParameter String value) throws IOException, ServletException {
            if (value != null) {
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
            while (iter.hasNext()) {
                tmpAttr = iter.next();
                if (tmpAttr.indexOf(" ") > 0) {
                    return FormValidation.error("'" + tmpAttr + "' is not a valid option for Verisium Manager attribute code name. Attribute code names can't have space.  Try using underscore instaed.");
                } else if (tmpAttr.equals("first_failure_name")) {
                    return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
                } else if (tmpAttr.equals("first_failure_description")) {
                    return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
                } else if (tmpAttr.equals("computed_seed")) {
                    return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
                } else if (tmpAttr.equals("test_group")) {
                    return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
                } else if (tmpAttr.equals("test_name")) {
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
            items.add("Mark the build as failed", "fail");
            items.add("Continue, and move to the next build step", "continue");
            items.add("Ignore, and continue to wait", "ignore");
            return items;
        }
        
        public ListBoxModel doFillEnvSourceInputFileTypeItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("bash", "BSH");
            items.add("csh", "CSH");
            return items;
        }
        
        

        public ListBoxModel doFillStoppedResolverItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Mark the build as failed", "fail");
            items.add("Continue, and move to the next build step", "continue");
            items.add("Ignore, and continue to wait", "ignore");
            return items;
        }

        public ListBoxModel doFillFailedResolverItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Mark the build as failed", "fail");
            items.add("Continue, and move to the next build step", "continue");
            items.add("Ignore, and continue to wait", "ignore");
            return items;
        }

        public ListBoxModel doFillDoneResolverItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Ignore, and continue to wait", "ignore");
            items.add("Continue, and move to the next build step", "continue");
            items.add("Mark the build as failed", "fail");
            return items;
        }

        public ListBoxModel doFillSuspendedResolverItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Ignore, and continue to wait", "ignore");
            items.add("Continue, and move to the next build step", "continue");
            items.add("Mark the build as failed", "fail");
            return items;
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
        
        public FormValidation doTestArchiveUser(@QueryParameter("archiveUser") final String archiveUser, @QueryParameter("archivePassword") final String archivePassword,
                @QueryParameter("vAPIUrl") final String vAPIUrl) throws IOException,
                ServletException {
            try {

                Utils utils = new Utils();
                String output = utils.checkVAPIConnection(vAPIUrl, true, archiveUser, archivePassword);
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
                String output = utils.checkExtraStaticAttr(vAPIUrl, authRequired, vAPIUser, vAPIPassword, staticAttributeList);
                if (!output.startsWith("Failed")) {
                    return FormValidation.ok("Success. " + output);
                } else {
                    return FormValidation.error(output);
                }
            } catch (Exception e) {
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }

        @Override
        public Set<Class<?>> getRequiredContext() {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            return ImmutableSet.of(FilePath.class, Run.class, Launcher.class, TaskListener.class,EnvVars.class);
        }
        
       

    }
}
