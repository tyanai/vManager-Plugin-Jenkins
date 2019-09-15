/*
 * The MIT License
 *
 * Copyright 2019 Cadence.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.vmanager.dsl.post;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.util.Set;
import javax.annotation.Nonnull;
//import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
//import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author tyanai
 */
 public class VMGRPostLaunchStep extends Step {
     
     
     
     
     
     
     
    @DataBoundConstructor
    public VMGRPostLaunchStep() {}

 
    
    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new VMGRPostLaunchStepImpl(this, context);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        
        public DescriptorImpl() {
            load();
        }

        @Override
        public String getFunctionName() {
            return "vmanagerPostBuildActions";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "vManager Post Build Actions";
        }

       @Override
        public Set<Class<?>> getRequiredContext() {
            return ImmutableSet.of(FilePath.class, Run.class, Launcher.class, TaskListener.class,EnvVars.class);
        }
    }
}   

