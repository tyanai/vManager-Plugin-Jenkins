package org.jenkinsci.plugins.vmanager.dsl.post;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.tasks.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import java.io.IOException;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import java.io.Serializable;
import javax.servlet.ServletException;
import org.jenkinsci.plugins.vmanager.SummaryReportParams;
import org.jenkinsci.plugins.vmanager.Utils;
import org.jenkinsci.plugins.vmanager.VAPIConnectionParam;
import org.kohsuke.stapler.QueryParameter;


public class DSLPublisher extends Recorder implements SimpleBuildStep, Serializable {

    private transient Run<?, ?> build;

    private String vAPIUrl;
    private boolean authRequired;
    private boolean advConfig;
    private String vAPIUser;
    private String vAPIPassword;
    private boolean dynamicUserId;
    private int connTimeout = 1;
    private int readTimeout = 30;

    private boolean advancedFunctions = false;
    private boolean retrieveSummaryReport;

    private boolean runReport;
    private boolean metricsReport;
    private boolean vPlanReport;

    private String testsViewName;
    private String metricsViewName;
    private String vplanViewName;
    private int testsDepth = 6;
    private int metricsDepth = 6;
    private int vPlanDepth = 6;

    private String metricsInputType;
    private String metricsAdvanceInput;
    private String vPlanInputType;
    private String vPlanAdvanceInput;
    private String vPlanxFileName;

    private String summaryType;
    private boolean ctxInput;
    private String ctxAdvanceInput;
    private String freeVAPISyntax;
    private boolean deleteReportSyntaxInputFile;
    private String vManagerVersion;
    private boolean sendEmail;
    private String emailList;
    private String emailType;
    private String emailInputFile;
    private boolean deleteEmailInputFile;
    private String summaryMode;
    private boolean ignoreSSLError;

    
    
    
    VAPIConnectionParam vAPIConnectionParam;
    SummaryReportParams summaryReportParams;

    

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public DSLPublisher(String vAPIUrl, String vAPIUser, String vAPIPassword, boolean authRequired, boolean advConfig, boolean dynamicUserId, int connTimeout, int readTimeout, boolean advancedFunctions,
            boolean retrieveSummaryReport, boolean runReport, boolean metricsReport, boolean vPlanReport, String testsViewName, String metricsViewName, String vplanViewName, int testsDepth, int metricsDepth,
            int vPlanDepth, String metricsInputType, String metricsAdvanceInput, String vPlanInputType, String vPlanAdvanceInput, String vPlanxFileName, String summaryType, boolean ctxInput,
            String ctxAdvanceInput, String freeVAPISyntax, boolean deleteReportSyntaxInputFile,String vManagerVersion,boolean sendEmail,String emailList,String emailType, String emailInputFile,boolean deleteEmailInputFile, String summaryMode, boolean ignoreSSLError) {

        this.vAPIUrl = vAPIUrl;
        this.authRequired = authRequired;
        this.advConfig = advConfig;
        this.vAPIUser = vAPIUser;
        this.vAPIPassword = vAPIPassword;
        this.dynamicUserId = dynamicUserId;
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
        this.advancedFunctions = advancedFunctions;
        this.retrieveSummaryReport = retrieveSummaryReport;
        this.runReport = runReport;
        this.metricsReport = metricsReport;
        this.vPlanReport = vPlanReport;
        this.testsViewName = testsViewName;
        this.metricsViewName = metricsViewName;
        this.vplanViewName = vplanViewName;
        this.testsDepth = testsDepth;
        this.metricsDepth = metricsDepth;
        this.vPlanDepth = vPlanDepth;

        this.metricsInputType = metricsInputType;
        this.metricsAdvanceInput = metricsAdvanceInput;
        this.vPlanInputType = vPlanInputType;
        this.vPlanAdvanceInput = vPlanAdvanceInput;
        this.vPlanxFileName = vPlanxFileName;
        this.summaryType = summaryType;
        this.ctxInput = ctxInput;
        this.ctxAdvanceInput = ctxAdvanceInput;
        this.freeVAPISyntax = freeVAPISyntax;
        this.deleteReportSyntaxInputFile = deleteReportSyntaxInputFile;
        this.vManagerVersion = vManagerVersion;
        this.sendEmail = sendEmail;
        this.emailList = emailList;
        this.emailType = emailType;
        this.emailInputFile = emailInputFile; 
        this.deleteEmailInputFile = deleteEmailInputFile; 
        this.summaryMode = summaryMode;
        this.ignoreSSLError = ignoreSSLError;
        
        

        vAPIConnectionParam = new VAPIConnectionParam();
        vAPIConnectionParam.vAPIUrl = vAPIUrl;
        vAPIConnectionParam.authRequired = authRequired;
        vAPIConnectionParam.advConfig = advConfig;
        vAPIConnectionParam.vAPIUser = vAPIUser;
        vAPIConnectionParam.vAPIPassword = vAPIPassword;
        vAPIConnectionParam.connTimeout = connTimeout;
        vAPIConnectionParam.readTimeout = readTimeout;

        summaryReportParams = new SummaryReportParams();
        summaryReportParams.runReport = runReport;
        summaryReportParams.metricsReport = metricsReport;
        summaryReportParams.vPlanReport = vPlanReport;
        summaryReportParams.testsViewName = testsViewName;
        summaryReportParams.metricsViewName = metricsViewName;
        summaryReportParams.vplanViewName = vplanViewName;
        summaryReportParams.testsDepth = testsDepth;
        summaryReportParams.metricsDepth = metricsDepth;
        summaryReportParams.vPlanDepth = vPlanDepth;

        summaryReportParams.metricsInputType = metricsInputType;
        summaryReportParams.metricsAdvanceInput = metricsAdvanceInput;
        summaryReportParams.vPlanInputType = vPlanInputType;
        summaryReportParams.vPlanAdvanceInput = vPlanAdvanceInput;
        summaryReportParams.vPlanxFileName = vPlanxFileName;
        summaryReportParams.summaryType = summaryType;
        summaryReportParams.ctxInput = ctxInput;
        summaryReportParams.ctxAdvanceInput = ctxAdvanceInput;
        summaryReportParams.freeVAPISyntax = freeVAPISyntax;
        summaryReportParams.deleteReportSyntaxInputFile = deleteReportSyntaxInputFile;
        summaryReportParams.vManagerVersion = vManagerVersion;
        summaryReportParams.sendEmail = sendEmail;
        summaryReportParams.emailList = emailList;
        
        summaryReportParams.emailType = emailType;
        summaryReportParams.emailInputFile = emailInputFile; 
        summaryReportParams.deleteEmailInputFile = deleteEmailInputFile;
        summaryReportParams.summaryMode = summaryMode;
        summaryReportParams.ignoreSSLError = ignoreSSLError;
    }
    
    
    public DSLPublisher() {

    }

    public String getSummaryMode() {
        return summaryMode;
    }
  
    public String getEmailType() {
        return emailType;
    }

    public String getEmailInputFile() {
        return emailInputFile;
    }

    
    public boolean isIgnoreSSLError() {
        return ignoreSSLError;
    }
    
    public boolean isDeleteEmailInputFile() {
        return deleteEmailInputFile;
    }
    
    public String getVManagerVersion() {
        return vManagerVersion;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public String getEmailList() {
        return emailList;
    }
    
    public boolean isDeleteReportSyntaxInputFile() {
        return deleteReportSyntaxInputFile;
    }

    public String getSummaryType() {
        return summaryType;
    }

    public boolean isCtxInput() {
        return ctxInput;
    }

    public String getCtxAdvanceInput() {
        return ctxAdvanceInput;
    }

    public String getFreeVAPISyntax() {
        return freeVAPISyntax;
    }

    public String getMetricsInputType() {
        return metricsInputType;
    }

    public String getMetricsAdvanceInput() {
        return metricsAdvanceInput;
    }

    public String getVPlanInputType() {
        return vPlanInputType;
    }

    public String getVPlanAdvanceInput() {
        return vPlanAdvanceInput;
    }

    public String getVPlanxFileName() {
        return vPlanxFileName;
    }

    public String getTestsViewName() {
        return testsViewName;
    }

    public String getMetricsViewName() {
        return metricsViewName;
    }

    public String getVplanViewName() {
        return vplanViewName;
    }

    public int getTestsDepth() {
        return testsDepth;
    }

    public int getMetricsDepth() {
        return metricsDepth;
    }

    public int getVPlanDepth() {
        return vPlanDepth;
    }

    public boolean isRunReport() {
        return runReport;
    }

    public boolean isMetricsReport() {
        return metricsReport;
    }

    public boolean isVPlanReport() {
        return vPlanReport;
    }

    public boolean isAdvancedFunctions() {
        return advancedFunctions;
    }

    public boolean isRetrieveSummaryReport() {
        return retrieveSummaryReport;
    }

    public String getVAPIUrl() {
        return vAPIUrl;
    }

    public boolean isAuthRequired() {
        return authRequired;
    }

    public boolean isAdvConfig() {
        return advConfig;
    }

    public String getVAPIUser() {
        return vAPIUser;
    }

    public String getVAPIPassword() {
        return vAPIPassword;
    }

    public boolean isDynamicUserId() {
        return dynamicUserId;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath fp, @Nonnull Launcher lnchr, @Nonnull TaskListener tl) throws InterruptedException, IOException {

        this.build = run;
        DSLBuildAction buildAction = new DSLBuildAction("NA", run);
        run.addAction(buildAction);

        if (advancedFunctions) {
            if (retrieveSummaryReport) {
                ReportBuildAction reportAction = new ReportBuildAction(run, summaryReportParams, vAPIConnectionParam, tl);
                run.addAction(reportAction);
            }
        }

    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * In order to load the persisted global configuration, you have to call
         * load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }
        
        public ListBoxModel doFillVManagerVersionItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("19.09 and above", "stream");
            items.add("Lower than 19.09", "html");
            return items;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "vManager Post Build Actions";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().

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

        public FormValidation doCheckVAPIUrl(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set the vManager vAPI HOST ");
            }
            if (value.length() < 4) {
                return FormValidation.warning("Isn't the name too short?");
            }
            return FormValidation.ok();
        }

    }
}
