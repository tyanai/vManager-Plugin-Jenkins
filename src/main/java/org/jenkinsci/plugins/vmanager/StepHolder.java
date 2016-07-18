package org.jenkinsci.plugins.vmanager;

public class StepHolder {

	private String inaccessibleResolver;
	private String stoppedResolver;
	private String failedResolver;
	private String doneResolver;
	private String suspendedResolver;
	private boolean waitTillSessionEnds;
	private int stepSessionTimeout = 0;
	private JUnitRequestHolder jUnitRequestHolder = null;
	
	
	
	
	public StepHolder(String inaccessibleResolver, String stoppedResolver, String failedResolver, String doneResolver, String suspendedResolver, boolean waitTillSessionEnds, int stepSessionTimeout, JUnitRequestHolder jUnitRequestHolder) {
		super();
		this.inaccessibleResolver = inaccessibleResolver;
		this.stoppedResolver = stoppedResolver;
		this.failedResolver = failedResolver;
		this.doneResolver = doneResolver;
		this.suspendedResolver = suspendedResolver;
		this.waitTillSessionEnds = waitTillSessionEnds;
		this.stepSessionTimeout = stepSessionTimeout;
		this.jUnitRequestHolder = jUnitRequestHolder;
	}
	
	
	
	public JUnitRequestHolder getjUnitRequestHolder() {
		return jUnitRequestHolder;
	}



	public void setjUnitRequestHolder(JUnitRequestHolder jUnitRequestHolder) {
		this.jUnitRequestHolder = jUnitRequestHolder;
	}



	public int getStepSessionTimeout() {
		return stepSessionTimeout;
	}


	public void setStepSessionTimeout(int stepSessionTimeout) {
		this.stepSessionTimeout = stepSessionTimeout;
	}


	public String getInaccessibleResolver() {
		return inaccessibleResolver;
	}
	public void setInaccessibleResolver(String inaccessibleResolver) {
		this.inaccessibleResolver = inaccessibleResolver;
	}
	public String getStoppedResolver() {
		return stoppedResolver;
	}
	public void setStoppedResolver(String stoppedResolver) {
		this.stoppedResolver = stoppedResolver;
	}
	public String getFailedResolver() {
		return failedResolver;
	}
	public void setFailedResolver(String failedResolver) {
		this.failedResolver = failedResolver;
	}
	public String getDoneResolver() {
		return doneResolver;
	}
	public void setDoneResolver(String doneResolver) {
		this.doneResolver = doneResolver;
	}
	public String getSuspendedResolver() {
		return suspendedResolver;
	}
	public void setSuspendedResolver(String suspendedResolver) {
		this.suspendedResolver = suspendedResolver;
	}
	public boolean isWaitTillSessionEnds() {
		return waitTillSessionEnds;
	}
	public void setWaitTillSessionEnds(boolean waitTillSessionEnds) {
		this.waitTillSessionEnds = waitTillSessionEnds;
	}
	
}
