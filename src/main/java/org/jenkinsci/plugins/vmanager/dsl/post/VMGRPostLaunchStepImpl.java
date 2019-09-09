package org.jenkinsci.plugins.vmanager.dsl.post;


import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
//import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
//import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

public class VMGRPostLaunchStepImpl extends SynchronousNonBlockingStepExecution<Void> {
    
   private transient final VMGRPostLaunchStep step;
   
   VMGRPostLaunchStepImpl(VMGRPostLaunchStep step, StepContext context) {
        super(context);
        this.step = step;
        
    }
   
   /*
    @StepContextParameter
    private transient TaskListener listener;
    

    @StepContextParameter
    private transient FilePath ws;

    @StepContextParameter
    private transient Run build;

    @StepContextParameter
    private transient Launcher launcher;
   */
  
    
   @Override
   protected Void run() throws Exception {
       
       TaskListener listener = getContext().get(TaskListener.class);
       FilePath ws = getContext().get(FilePath.class);
       Run build = getContext().get(Run.class);
       Launcher launcher = getContext().get(Launcher.class);
       
       listener.getLogger().println("Running vManager Post Job step.");

       DSLPublisher publisher = new DSLPublisher();
       publisher.perform(build, ws, launcher, listener);

       return null;
   }   
}
