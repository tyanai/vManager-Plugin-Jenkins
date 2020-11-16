/*
 * The MIT License
 *
 * Copyright 2020 Cadence.
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
package org.jenkinsci.plugins.vmanager;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jenkinsci.remoting.RoleChecker;

/**
 *
 * @author tyanai
 */
public class BatchExecManager implements java.io.Serializable {

    TaskListener jobListener = null;
    String executionScript = null;
    String executionShellLocation = null;
    String vsifFile = null;
    String buildId = null;
    int buildNumber = 0;

    public BatchExecManager(TaskListener listener, String executionScript, String executionShellLocation, String executionVsifFile, String buildId, int buildNumber) {
        this.jobListener = listener;
        this.executionScript = executionScript;
        this.executionShellLocation = executionShellLocation;
        this.vsifFile = executionVsifFile;
        this.buildId = buildId;
        this.buildNumber = buildNumber;
    }

    public void execBatchCommand(FilePath filePath) throws IOException, InterruptedException {

        String[] command = {executionShellLocation, executionScript, vsifFile};

        filePath.act(new FileCallable<Void>() {

            private static final long serialVersionUID = 6166111757469534436L;

            @Override
            public void checkRoles(final RoleChecker checker) throws SecurityException {
            }

            @Override
            public Void invoke(final File workspace, final VirtualChannel channel)
                    throws IOException {

                //new Thread(() -> {
                String sessionNameToMonitor = null;
                jobListener.getLogger().print("\nTrying to launch a session using batch on this agent workspace:\n");
                jobListener.getLogger().print("Select script for this execution is " + executionScript + "\n");
                jobListener.getLogger().print("Select shell type for this execution is " + executionShellLocation + "\n");
                jobListener.getLogger().print("Select vsif for this execution is " + vsifFile + "\n");
                boolean foundGoodVSIF = false;
                try {
                    ProcessBuilder builder = new ProcessBuilder(command);
                    /*
                    Map<String, String> env = builder.environment();
                    env.put("VMGR_REGION", "default");
                    env.put("VMGR_REGION_ROUTE_POLICY", "LOCAL");
                    env.put("VMGR_USER", "tyanai");
                    env.put("VMGR_PASSWORD", "xxxxxx");
                     */
                    builder.redirectErrorStream(true);
                    Process proc = builder.start();

                    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    BufferedReader inError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                    String s = null;

                    while ((s = in.readLine()) != null) {

                        jobListener.getLogger().print(s + "\n");
                        if (s.indexOf("*I,runner.sessionStarted: Session") > -1) {
                            foundGoodVSIF = true;
                            sessionNameToMonitor = s.substring(34, s.indexOf(" started."));

                            //Now creates the file of sessions.input
                            String fileOutput = buildNumber + "." + buildId + ".sessions.input";
                            StringBuffer writer = new StringBuffer();
                            writer.append(sessionNameToMonitor);
                            hudson.FilePath newFile = filePath.child(fileOutput);
                            newFile.write(writer.toString(), StandardCharsets.UTF_8.name());
                            
                        }

                    }

                    String sError = null;

                    while ((sError = inError.readLine()) != null) {

                        jobListener.getLogger().print(sError + "\n");

                    }

                } catch (IOException e) {
                    jobListener.getLogger().println(e.getMessage());
                    for (StackTraceElement ste : e.getStackTrace()) {
                        jobListener.getLogger().println(" " + ste);
                    }

                    jobListener.getLogger().println(ExceptionUtils.getFullStackTrace(e));
                }
                catch (InterruptedException ex) {
                        jobListener.getLogger().println(ex.getMessage());
                        for (StackTraceElement ste : ex.getStackTrace()) {
                            jobListener.getLogger().println(" " + ste);
                        }

                        jobListener.getLogger().println(ExceptionUtils.getFullStackTrace(ex));
                    }
                //}).start();

                if (foundGoodVSIF) {
                    jobListener.getLogger().print("Found session name to monitor: " + sessionNameToMonitor + "\n");
                } else {
                    throw new IOException("Failed to launch vsif using batch.  Job stopped.");
                }

                return null;
            }

        });

    }

}
