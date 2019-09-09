package org.jenkinsci.plugins.vmanager.dsl.post;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.tasks.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import java.io.IOException;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import java.io.Serializable;


@Extension
public class DSLPublisher extends Recorder implements SimpleBuildStep, Serializable {

   
    private transient Run<?, ?> build;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public DSLPublisher() {
       
    }

   
    
    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath fp, @Nonnull Launcher lnchr, @Nonnull TaskListener tl) throws InterruptedException, IOException {
        
        this.build = run;

        DSLBuildAction buildAction = new DSLBuildAction("NA", run);
        run.addAction(buildAction);

                
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
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }
        
            
         
       
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
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

      
        
    }
}

