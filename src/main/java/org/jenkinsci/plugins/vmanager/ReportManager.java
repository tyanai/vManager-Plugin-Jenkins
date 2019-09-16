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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author tyanai
 */
public class ReportManager {

    private final static String runsFilter = "{\"filter\":{\"@c\":\".RelationFilter\",\"relationName\":\"session\",\"filter\":{\"@c\":\".ChainedFilter\",\"condition\":\"OR\",\"chain\":[" + "######" + "]}}}";

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

    public String buildPostParamForSummaryReport() throws Exception {

        JSONParser jsonParser = new JSONParser();
        String postData = "";

        if (summaryReportParams.summaryType.equals("wizard")) {

            JSONObject metricsData;
            JSONObject vplanData;
            JSONObject ctxData = null;

            //Go over static params
            String staticParams = SummaryReportParams.staticReportParams;

            if (summaryReportParams.runReport) {
                summaryReportParams.includeTests = true;
                String testsDepth = ",\"testsDepth\":" + summaryReportParams.testsDepth;
                String testsViewName = ",\"testsViewName\":\"" + summaryReportParams.testsViewName.trim() + "\"";

                staticParams = staticParams.replace("$test_depth", testsDepth);
                staticParams = staticParams.replace("$test_view_name", testsViewName);
            } else {
                summaryReportParams.includeTests = false;
                staticParams = staticParams.replace("$test_depth", "");
                staticParams = staticParams.replace("$test_view_name", "");
            }

            if (summaryReportParams.metricsReport) {
                String metricsViewName = ",\"metricsViewName\":\"" + summaryReportParams.metricsViewName.trim() + "\"";
                staticParams = staticParams.replace("$metrics_view_name", metricsViewName);
            } else {
                staticParams = staticParams.replace("$metrics_view_name", "");
            }

            if (summaryReportParams.vPlanReport) {
                String vplanViewName = ",\"vplanViewName\":\"" + summaryReportParams.vplanViewName.trim() + "\"";
                staticParams = staticParams.replace("$vplan_view_name", vplanViewName);
            } else {
                staticParams = staticParams.replace("$vplan_view_name", "");
            }

            postData = postData + "{" + staticParams + ",\"includeTests\":" + summaryReportParams.includeTests;

            if (summaryReportParams.metricsReport) {
                String tmpDataHolder = null;
                try {
                    if (summaryReportParams.metricsInputType.equals("basic")) {
                        tmpDataHolder = SummaryReportParams.metricsData;
                        metricsData = (JSONObject) jsonParser.parse(tmpDataHolder);
                        metricsData.replace("depth", summaryReportParams.metricsDepth);
                    } else {
                        tmpDataHolder = summaryReportParams.metricsAdvanceInput.trim();
                        metricsData = (JSONObject) jsonParser.parse(tmpDataHolder);
                    }
                } catch (Exception e) {
                    listener.getLogger().println("ReportManager - fail to parse metricsData json input: " + tmpDataHolder);
                    throw e;
                }
                postData = postData + ",\"metricsData\":[" + metricsData.toJSONString() + "]";
            }

            if (summaryReportParams.vPlanReport) {
                String tmpDataHolder = null;
                try {
                    if (summaryReportParams.vPlanInputType.equals("basic")) {
                        tmpDataHolder = SummaryReportParams.vPlanData;
                        vplanData = (JSONObject) jsonParser.parse(tmpDataHolder);
                        vplanData.replace("depth", summaryReportParams.vPlanDepth);
                    } else {
                        tmpDataHolder = summaryReportParams.vPlanAdvanceInput.trim();
                        vplanData = (JSONObject) jsonParser.parse(tmpDataHolder);
                    }
                } catch (Exception e) {
                    listener.getLogger().println("ReportManager - fail to parse vplanData json input: " + tmpDataHolder);
                    throw e;
                }

                try {
                    ctxData = (JSONObject) jsonParser.parse(SummaryReportParams.ctxData);
                    ctxData.put("vplanFile", summaryReportParams.vPlanxFileName.trim());
                } catch (Exception e) {
                    listener.getLogger().println("ReportManager - fail to parse ctxData json input for vPlan name: " + summaryReportParams.vPlanxFileName);
                    throw e;
                }

                postData = postData + ",\"vplanData\":[" + vplanData.toJSONString() + "]";
            }

            //See if there's anything additional that comes from ctxData optional input:
            if (summaryReportParams.ctxInput) {
                try {
                    ctxData = (JSONObject) jsonParser.parse(summaryReportParams.ctxAdvanceInput);
                    if (summaryReportParams.vPlanReport) {
                        if (!summaryReportParams.vPlanxFileName.trim().equals("")) {
                            //There is a vPlan, check if there's also in the ctxData
                            if (!ctxData.containsKey("vplanFile")) {
                                ctxData.put("vplanFile", summaryReportParams.vPlanxFileName.trim());
                            }
                        }
                    }

                } catch (Exception e) {
                    listener.getLogger().println("ReportManager - fail to parse ctxData json input for vPlan name: " + summaryReportParams.vPlanxFileName);
                    throw e;
                }

                postData = postData + ",\"ctxData\":" + ctxData.toJSONString();
            } else {
                if (summaryReportParams.vPlanReport) {
                    postData = postData + ",\"ctxData\":" + ctxData.toJSONString();
                }
            }

            postData = postData + ",\"rs\":" + runsFilter.replace("######", buildPostDataSessionFilter()) + "}";

        } else {
            //User choose to place his own full vAPI request.  All we need to do is to add the RS part and send it over.

            //Load json from file
            Utils utils = new Utils();
            String freeVAPISyntax;
            if (this.testMode) {
                freeVAPISyntax = utils.loadUserSyntaxForSummaryReport("20", 20, "" + "c://temp", summaryReportParams.freeVAPISyntax, null, summaryReportParams.deleteReportSyntaxInputFile);
            } else {
                freeVAPISyntax = utils.loadUserSyntaxForSummaryReport(vmgrRun.getRun().getId(), vmgrRun.getRun().getNumber(), "" + vmgrRun.getJobWorkingDir(), summaryReportParams.freeVAPISyntax, listener, summaryReportParams.deleteReportSyntaxInputFile);
            }

            JSONObject userSyntaxData;
            try {
                userSyntaxData = (JSONObject) jsonParser.parse(freeVAPISyntax);
            } catch (Exception e) {
                listener.getLogger().println("ReportManager - fail to parse user free syntax json input for summary report: " + summaryReportParams.vPlanxFileName);
                throw e;
            }

            //Add the RS part
            JSONObject rsData;
            try {
                rsData = (JSONObject) jsonParser.parse(runsFilter.replace("######", buildPostDataSessionFilter()));
            } catch (Exception e) {
                listener.getLogger().println("ReportManager - fail to parse rsData for sessions list: " + buildPostDataSessionFilter());
                throw e;
            }
            userSyntaxData.put("rs", rsData);

            postData = userSyntaxData.toJSONString();

        }

        return postData;

    }

    public void fetchFromRemoteURL(String reportUrl) throws Exception {

        JSONParser jsonParser = new JSONParser();
        JSONObject urlObject;
        try {
            urlObject = (JSONObject) jsonParser.parse(reportUrl);
        } catch (Exception e) {
            listener.getLogger().println("ReportManager - fail to parse url from /reports/generate-summary-report: " + reportUrl);
            throw e;
        }

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
        fixUntrustCertificate();
        String username = vAPIConnectionParam.vAPIUser;
        String thePath = vAPIConnectionParam.vAPIUrl + "/rest/reports" + urlObject.get("path");
        URL url = new URL(thePath);
        URLConnection uc = url.openConnection();
        
        if (vAPIConnectionParam.authRequired) {
            // ----------------------------------------------------------------------------------------
            // Authentication
            // ----------------------------------------------------------------------------------------
            if (vAPIConnectionParam.dynamicUserId) {
                BufferedReader reader = null;
                try {
                    Utils utils = new Utils();
                    reader = utils.loadFileFromWorkSpace(buildId, buildNumber, jobWorkingDir, null, listener, false, "user.input");
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        username = line;
                        break;
                    }
                } catch (Exception e) {

                    if (!this.testMode) {
                        listener.getLogger().print("Failed to read input file for the dynamic users. \n");
                    } else {

                        System.out.println("Failed to read input file for the dynamic users. \n");
                    }
                    throw e;
                } finally {
                    reader.close();
                }
            }
            /*
            String authString = user + ":" + password;
            UserUsedForLogin = user;
            PasswordUsedForLogin = password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
             */
            // ----------------------------------------------------------------------------------------
            
            String userpass = username + ":" + vAPIConnectionParam.vAPIPassword;
            String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
            uc.setRequestProperty("Authorization", basicAuth);
        }
        
        InputStream is = uc.getInputStream();
        int ptr = 0;
        StringBuffer buffer = new StringBuffer();
        String fileOutput = jobWorkingDir + File.separator + buildNumber + "." + buildId + ".summary.report";
        FileWriter writer = new FileWriter(fileOutput);
        while ((ptr = is.read()) != -1) {
            //buffer.append((char) ptr);
            writer.append((char) ptr);
        }

        writer.flush();
        writer.close();

    }

    public void retrievReportFromServer() throws Exception {

        //In case user choose to bring the report manualy skip and return
        if (summaryReportParams.summaryType.equals("viewonly")) {
            return;
        }

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
            String postData = buildPostParamForSummaryReport();
            if (!this.testMode) {
                listener.getLogger().println("ReportManager is using the following POST data for getting the summary report:\n" + postData);
            } else {
                System.out.println(postData);
            }

            os.write(postData.getBytes());
            os.flush();

            if (checkResponseCode(conn)) {
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                //String fileOutput = jobWorkingDir + File.separator + buildNumber + "." + buildId + ".summary.report";
                //FileWriter writer = new FileWriter(fileOutput);
                StringBuffer sb = new StringBuffer();
                String output;
                while ((output = br.readLine()) != null) {
                    //writer.append(output);
                    sb.append(output);
                }

                fetchFromRemoteURL(sb.toString());
                //writer.flush();
                //writer.close();

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
                System.out.println("Error - Got wrong response from /reports/stream-summary-report - " + conn.getResponseCode());
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

    public void fixUntrustCertificate() throws KeyManagementException, NoSuchAlgorithmException {

        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }

           

            }
        };

        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // set the  allTrusting verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

}
