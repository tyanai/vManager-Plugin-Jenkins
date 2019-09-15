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
package org.jenkinsci.plugins.vmanager;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.vmanager.BuildStatusMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author tyanai
 */
public class ReportManager {

    private final static String runsFilter = "{\"filter\":{\"@c\":\".RelationFilter\",\"relationName\":\"session\",\"filter\":{\"@c\":\".ChainedFilter\",\"condition\":\"AND\",\"chain\":[" + "######" + "]}}}";

    private Run<?, ?> build;
    private SummaryReportParams summaryReportParams;
    private VAPIConnectionParam vAPIConnectionParam;
    private VMGRRun vmgrRun;
    private TaskListener listener;
    private boolean testMode = false;

    public ReportManager(Run<?, ?> build, SummaryReportParams summaryReportParams, VAPIConnectionParam vAPIConnectionParam, TaskListener listener) {
        this.build = build;
        this.summaryReportParams = summaryReportParams;
        this.vAPIConnectionParam = vAPIConnectionParam;
        this.listener = listener;

        Job job = build.getParent();
        String workingDir = job.getBuildDir() + File.separator + build.getNumber();
        vmgrRun = new VMGRRun(build, workingDir, job.getBuildDir().getAbsolutePath());

    }

    public ReportManager(SummaryReportParams summaryReportParams, VAPIConnectionParam vAPIConnectionParam, boolean testMode) {
        this.testMode = testMode;
        this.summaryReportParams = summaryReportParams;
        this.vAPIConnectionParam = vAPIConnectionParam;
    }

    private String buildPostDataSessionFilter() {

        String sessionIdFromBuild;
        String[] listOfSessions;
        if (this.testMode) {
            listOfSessions = new String[1];
            listOfSessions[0] = "1";
        } else {
            sessionIdFromBuild = BuildStatusMap.getValue(vmgrRun.getRun().getId(), vmgrRun.getRun().getNumber(), vmgrRun.getJobWorkingDir() + "", "id", true);
            listOfSessions = sessionIdFromBuild.split("\\s*,\\s*");
        }

        String result = "";
        int commaCounter = listOfSessions.length - 1;
        for (String listOfSession : listOfSessions) {
            result = result + "{\"attName\":\"id\",\"operand\":\"EQUALS\",\"@c\":\".AttValueFilter\",\"attValue\":\"" + listOfSession.trim() + "\"}";
            if (commaCounter > 0) {
                result = result + ",";
            }
            commaCounter--;
        }
        return result;
    }

    public void retrievReportFromServer() throws Exception {

        HttpURLConnection conn = null;
        Utils utils = new Utils();
        String apiURL = vAPIConnectionParam.vAPIUrl + "/rest/reports/generate-summary-report";

        int buildNumber = 20;
        String buildId = "20";
        String jobWorkingDir = "c://temp";
        String jobRootDir = "c://temp";

        if (!this.testMode) {
            buildNumber = vmgrRun.getRun().getNumber();
            buildId = vmgrRun.getRun().getId();
            jobWorkingDir = vmgrRun.getJobWorkingDir();
            jobRootDir = build.getRootDir().getAbsolutePath();
        }

        try {
            conn = utils.getVAPIConnection(apiURL, vAPIConnectionParam.authRequired, vAPIConnectionParam.vAPIUser, vAPIConnectionParam.vAPIPassword, "POST", vAPIConnectionParam.dynamicUserId, buildId, buildNumber, jobRootDir, listener, vAPIConnectionParam.connTimeout, vAPIConnectionParam.readTimeout, vAPIConnectionParam.advConfig);

            OutputStream os = conn.getOutputStream();
            String postData = "{" + SummaryReportParams.staticReportParams
                    + ",\"includeTests\":" + summaryReportParams.includeTests
                    + ",\"metricsData\":" + summaryReportParams.metricsData
                    + ",\"vplanData\":" + summaryReportParams.vPlanData
                    + ",\"ctxData\":" + summaryReportParams.ctxData
                    + ",\"rs\":" + runsFilter.replace("######", buildPostDataSessionFilter()) + "}";
            System.out.println(postData);
            os.write(postData.getBytes());
            os.flush();

            if (checkResponseCode(conn)) {
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                String fileOutput = jobWorkingDir + File.separator + buildNumber + "." + buildId + ".summary.report";
                FileWriter writer = new FileWriter(fileOutput);
                String output;
                while ((output = br.readLine()) != null) {
                    writer.append(output);
                }
                writer.flush();
                writer.close();

                //To Do
            }
        } catch (Exception e) {
            if (this.testMode) {
                e.printStackTrace();
            } else {
                listener.error("Failed to retrieve report from the vManager server.", e);
            }
            throw e;

        } finally {
            conn.disconnect();

        }

    }

    public String getReportFromWorkspace() {

        String fileInput = vmgrRun.getJobWorkingDir() + File.separator + vmgrRun.getRun().getNumber() + "." + vmgrRun.getRun().getId() + ".summary.report";
        String output = "<div>NO REPORT WAS FOUND.</div>";
        try {
            output = new String(Files.readAllBytes(Paths.get(fileInput)));
        } catch (IOException ex) {
            listener.error("vManager Action - Can't find file for loading report: " + fileInput);
        }

        return output;
    }

    private boolean checkResponseCode(HttpURLConnection conn) {
        try {
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED
                    && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET
                    && conn.getResponseCode() != 406) {
                System.out.println("Error - Got wrong response from /reports/generate-summary-report - " + conn.getResponseCode());
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            // MARK_BUILD_FAIL
            e.printStackTrace();
            return false;
        }
    }

}
