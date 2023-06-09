pipeline { 
    agent any 
    options {
        skipStagesAfterUnstable()
    }
    environment {
		//A global variable to indicate when the regression is over.
        REGRESSION_IS_DONE = false
    }
    stages {
        
        stage('Build') {
            
            parallel {
                stage('Monitor Regression Progress') {
                    steps { 
                        script {
                            try {
                                vmanagerLaunch userFarmType: null, famMode: null, masterWorkspaceLocation: null, defineVariableType: null, defineVariableText: null, advConfig: false, archivePassword: '', archiveUser: '', attrValues: false, attrValuesFile: '', authRequired: true, connTimeout: 1, credentialInputFile: '', defineVarible: false, defineVaribleFile: '', deleteAlsoSessionDirectory: false, deleteCredentialInputFile: false, deleteInputFile: false, deleteSessionInputFile: false, doneResolver: 'ignore', dynamicUserId: false, envSourceInputFile: '', envSourceInputFileType: 'BSH', envVarible: false, envVaribleFile: '', executionScript: '', executionShellLocation: '', executionType: 'launcher', executionVsifFile: '', extraAttributesForFailures: false, failJobIfAllRunFailed: false, failJobUnlessAllRunPassed: false, failedResolver: 'fail', famModeLocation: '', generateJUnitXML: false, genericCredentialForSessionDelete: false, inaccessibleResolver: 'fail', markBuildAsFailedIfAllRunFailed: false, markBuildAsPassedIfAllRunPassed: false, noAppendSeed: false, pauseSessionOnBuildInterruption: false, pipelineNodes: false, readTimeout: 30, sessionsInputFile: '', staticAttributeList: '', stepSessionTimeout: 30, stoppedResolver: 'fail', suspendedResolver: 'ignore', useUserOnFarm: false, userPrivateSSHKey: false, vAPIPassword: 'letmein', vAPIUrl: 'https://vlnx488:50500/vmgr/vapi', vAPIUser: 'root', vMGRBuildArchive: false, vSIFInputFile: '', vSIFName: '/home/tyanai/vsif/test.vsif', vsifType: 'static', waitTillSessionEnds: true
                            } finally {
								//Must reside within try/catch block as the build itself might fail
                                echo "Marking the regression as completed for the report job (the other parallel branch) to also stop running"
                                REGRESSION_IS_DONE = true
                            }
                        }
                    }
                }
                stage('Fetching Summary Report') {
                    steps {
                        //No point in trying to get the report before 2 minutes passed.  It takes at least 1 minute for the regression
						//to create the session_status.properties for the build which is also an indication that the report can be generated
                        sleep(time:120,unit:"SECONDS")
                        
                        
                        //The below is to make sure we don't try to bring the report before the file session_status.properties was created
						//However, in case 2 minutes passed since the regression started, most likely something is wrong so timeout here is 2 minutes.
                        timeout(time:2,unit:'MINUTES') {
                            script {
                                waitUntil {
                                    echo "Looking for $WORKSPACE/${env.BUILD_NUMBER}.${env.BUILD_ID}.session_status.properties"  
                                    def exists = fileExists "$WORKSPACE/${env.BUILD_NUMBER}.${env.BUILD_ID}.session_status.properties"
                                    if (exists) {
                                        echo "Found the input file for the vManager Report.  Starting to execute the plugin to fetch reports."
                                        return true
                                    } else if (REGRESSION_IS_DONE){
                                        echo "Regression is already done, so no point in keep waiting."
                                        return true
                                    } else {
                                        echo "File for Report is not ready yet.  Keep waiting..."
                                        return false
                                    }                        
                                }
                                echo "Waiting conditions have met to start generating the report or timeout."
                            }
                        }
                        
                        script {
						
                            def timeToWait = 10
                            while (!REGRESSION_IS_DONE) {
                                vmanagerPostBuildActions metricsInputType: null, vPlanInputType: null, emailType: null, advConfig: false, advancedFunctions: true, authRequired: true, connTimeout: 1, ctxAdvanceInput: '', ctxInput: false, deleteEmailInputFile: false, deleteReportSyntaxInputFile: false, dynamicUserId: false, emailInputFile: '', emailList: '', freeVAPISyntax: '', ignoreSSLError: false, metricsAdvanceInput: '', metricsDepth: 6, metricsReport: false, metricsViewName: 'All_Metrics', readTimeout: 30, retrieveSummaryReport: true, runReport: true, sendEmail: false, summaryMode: 'full', summaryType: 'wizard', testsDepth: 6, testsViewName: 'Test_Hierarchy', vAPIPassword: 'letmein', vAPIUrl: 'https://vlnx488:50500/vmgr/vapi', vAPIUser: 'root', vManagerVersion: 'stream', vPlanAdvanceInput: '', vPlanDepth: 6, vPlanReport: false, vPlanxFileName: '', vplanViewName: 'All_Vplan'
								//The below line is optional - it adds a link to the summary report right below the history table on the left.
								currentBuild.description = "Click <a href='./${env.BUILD_NUMBER}/vManagerSummaryReport'>Regression Progress Report</a>"
								
								//Wait X amount of seconds before trying to fetch yet an additonal report
								echo "Waiting ${timeToWait} seconds before updating/fetching the summary report..."
                                sleep(time:timeToWait,unit:"SECONDS")
                            }
                        }
                        
                        
                    }
                }
            }
            
        }
        
        stage('Post'){
			//In any case, bring the report one last time at the end of the execution
            steps {
                vmanagerPostBuildActions metricsInputType: null, vPlanInputType: null, emailType: null, advConfig: false, advancedFunctions: true, authRequired: true, connTimeout: 1, ctxAdvanceInput: '', ctxInput: false, deleteEmailInputFile: false, deleteReportSyntaxInputFile: false, dynamicUserId: false, emailInputFile: '', emailList: '', freeVAPISyntax: '', ignoreSSLError: false, metricsAdvanceInput: '', metricsDepth: 6, metricsReport: false, metricsViewName: 'All_Metrics', readTimeout: 30, retrieveSummaryReport: true, runReport: true, sendEmail: false, summaryMode: 'full', summaryType: 'wizard', testsDepth: 6, testsViewName: 'Test_Hierarchy', vAPIPassword: 'letmein', vAPIUrl: 'https://vlnx488:50500/vmgr/vapi', vAPIUser: 'root', vManagerVersion: 'stream', vPlanAdvanceInput: '', vPlanDepth: 6, vPlanReport: false, vPlanxFileName: '', vplanViewName: 'All_Vplan'
				
				//The below line is optional - it adds a link to the summary report right below the history table on the left.
				script{
					currentBuild.description = "Click <a href='./${env.BUILD_NUMBER}/vManagerSummaryReport'>Regression Progress Report</a>"
				}
            }
        }
    }
    
}


        
    