package org.jenkinsci.plugins.vmanager.dsl.post;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.vmanager.BuildStatusMap;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Arrays;
import jenkins.model.RunAction2;
import org.jenkinsci.plugins.vmanager.PostActionBase;
import org.jenkinsci.plugins.vmanager.ReportManager;
import org.jenkinsci.plugins.vmanager.SummaryReportParams;
import org.jenkinsci.plugins.vmanager.Utils;
import org.jenkinsci.plugins.vmanager.VAPIConnectionParam;
import org.jenkinsci.plugins.vmanager.VMGRRun;

public class ReportBuildAction extends PostActionBase implements Serializable, RunAction2 {

    private transient Run<?, ?> build;
    private transient TaskListener listener;
    private transient ReportManager reportManager;

    @Override
    public String getIconFileName() {
        return "/plugin/vmanager-plugin/img/report.png";
    }

    @Override
    public String getDisplayName() {
        return "vManager Summary Report";
    }

    @Override
    public String getUrlName() {
        return "vManagerSummaryReport";
    }

    /*
    public List<String> getJobSessions() {

        Job job = build.getParent();
        String workingDir = job.getBuildDir() + File.separator + build.getNumber();
        vmgrRun = new VMGRRun(build, workingDir, job.getBuildDir().getAbsolutePath());

        String ids = BuildStatusMap.getValue(vmgrRun.getRun().getId(), vmgrRun.getRun().getNumber(), vmgrRun.getJobWorkingDir() + "", "id", true);
        List<String> items = Arrays.asList(ids.split("\\s*,\\s*"));

        return items;
    }
*/
    

    public int getBuildNumber() {
        return this.build.number;
    }

    public Run<?, ?> getBuild() {
        return build;
    }

    public ReportBuildAction(final Run<?, ?> build, SummaryReportParams summaryReportParams, VAPIConnectionParam vAPIConnectionParam, TaskListener listener) {

        this.build = build;
        this.listener = listener;
        
        this.reportManager = new ReportManager(build, summaryReportParams, vAPIConnectionParam, listener);
        try{
            this.reportManager.retrievReportFromServer();
        }catch (Exception e){
            listener.getLogger().println("Failed to get summary report from server");
            e.printStackTrace();
        }
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.build = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.build = run;
    }
    
    public String getReportFromWorkspace() {
        ReportManager reportManager = new ReportManager(build, null, null, listener);
        return reportManager.getReportFromWorkspace();
    }
    
    

}
