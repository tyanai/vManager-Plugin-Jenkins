package hudson.plugins.vmanager;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.plugins.view.dashboard.DashboardPortlet;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.plugins.view.dashboard.Messages;

import java.util.PriorityQueue;

public class VmgrBuilds extends DashboardPortlet {

	/**
	 * Number of latest builds which will be displayed on the screen
	 */
	private int numBuilds = 10;
	

	@DataBoundConstructor
	public VmgrBuilds(String name, int numBuilds) {
		super(name);
		this.numBuilds = numBuilds;
	}

	public int getNumBuilds() {
		return numBuilds <= 0 ? 10 : numBuilds;
	}

	public String getTimestampSortData(Run run) {

		return String.valueOf(run.getTimeInMillis());
	}

	public String getBuildOwner(AbstractBuild run) {
		
		//This is the only time we set true.  It means to load inot the hashmap for each line, the rest of the get methods will use the hashmap
		return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "owner", true);
	}

	public String getSessionTriage(AbstractBuild run) {
		String url = BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "url", false);
		if ("NA".equals(url)){
			return "NA";
		}
		url = url.replaceAll("/vapi", "");
		String sessionId = BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "id", false);
		if (sessionId != null){
			if (sessionId.indexOf(",") > -1){
				sessionId = sessionId.substring(0,sessionId.indexOf(","));
			} 
		}
		
		return  url + "/regression/index.html?sessionid=" + sessionId;
	}

	public String getSessionStatus(AbstractBuild run) {
		return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "status", false);
	}

	public String getSessionName(AbstractBuild run) {
		String sessionName = BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "name", false);
		if (!"NA".equals(sessionName)){
			String sessionCode = BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "session_code", false);
			sessionName = sessionName + " (" + sessionCode + ")";
		}
		return sessionName;
	}

	public String getTotalRuns(AbstractBuild run) {
		return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "total_runs_in_session", false);
	}

	public String getPassedRuns(AbstractBuild run) {
		return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "passed_runs", false);
	}

	public String getFailedRuns(AbstractBuild run) {
		return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "failed_runs", false);
	}

	public String getOtherRuns(AbstractBuild run) {
		return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "other_runs", false);
	}
	
	public String getRunningRuns(AbstractBuild run) {
		return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "running", false);
	}
	
	public String getWaitingRuns(AbstractBuild run) {
		return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "waiting", false);
	}
	
	public String getTotalSessions(AbstractBuild run) {
		return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "number_of_entities", false);
	}
	

	public String getTimestampString(Run run) {
		return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(run.getTimeInMillis()));
	}

	/**
	 * Last <code>N_LATEST_BUILDS</code> builds
	 * 
	 */
	public List<Run> getFinishedBuilds() {
		List<Job> jobs = getDashboardJobs();

		PriorityQueue<Run> queue = new PriorityQueue<Run>(numBuilds, Run.ORDER_BY_DATE);
		for (Job job : jobs) {
			Run lb = job.getLastBuild();
			if (lb != null) {
				queue.add(lb);
			}
		}

		List<Run> recentBuilds = new ArrayList<Run>(numBuilds);
		Run build;
		while ((build = queue.poll()) != null) {
			recentBuilds.add(build);
			if (recentBuilds.size() == numBuilds) {
				break;
			}
			Run pb = build.getPreviousBuild();
			if (pb != null) {
				queue.add(pb);
			}
		}

		return recentBuilds;
	}

	/**
	 * for unit test
	 */
	protected List<Job> getDashboardJobs() {
		return getDashboard().getJobs();
	}

	public String getBuildColumnSortData(Run<?, ?> build) {
		
		
		return String.valueOf(build.getNumber());
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<DashboardPortlet> {

		@Override
		public String getDisplayName() {
			return "vManager Latest Sessions"; 
		}
	}

}
