# Cadence vManager Jenkins Plugin

This plugin adds an ability to perform REST over HTTP calls to Cadence vManager as a step in your build.

Plugin development and support is made by Cadence Design Systems. 

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/vmanager-plugin.svg)](https://plugins.jenkins.io/vmanager-plugin)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/vmanager-plugin.svg?color=blue)](https://plugins.jenkins.io/vmanager-plugin)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/vmanager-plugin.svg)](https://github.com/jenkinsci/vmanager-plugin/graphs/contributors)

### Plugin Dependency

Please make sure you have the **dashboard-view** &
**workflow-step-api **plugin installed on your Jenkins before trying to
install this plugin.

Also, in case you want to chart over the runs results, install the
**Junit Plugin** as well.

![](./docs/images/JunitA.png "Jenkins > Cadence vManager Plugin > JunitA.png")
![](./docs/images/Dashboard.png "Jenkins > Cadence vManager Plugin > Dashboard.png")

### About

Cadence vManager is exposing a REST API (vAPI) for performing automation
queries and updates for its regression/test and coverage data.  This
plugin enables you to add a remote execution for extracting runs
information, reports data or even collecting and launching sessions as part of your
build process.

## Features

-   Free-style job plugin (can perform all vManager API call).
-   Support static/dynamic API calls
-   Support dynamic authentication per user id.
-   Special build step for performing launch of vsif files dynamically.
-   Support Dashboard portal for showing the session status. (plugin ver
    1.9 and above).
-   Support the JUnit Report format for showing pass/fail runs charts
    (plugin ver 1.9 and above).
-   Support the Jenkins Pipeline Step API (plugin ver 2.5.1 and above).
-   Support the ability to delete sessions with every manual/automatic
    build removal (plugin ver 2.5.2 and above).
-   Adds a link within the project page for direct access to the
    vManager web analysis (post-build action).
-   Adds a table of sessions within the project\'s page to track the
    session\'s history and give high level overview about the progress
    (post-build action).
-   Adds an ability to embed the vManager Summary Report within Jenkins
    as part of the build page.
-   Adds an ability to send vManager Summary Report to dynamically
    selective users at the end of the regression.
-   Support Collect Mode.

## Configuration

After installing the plugin you\'ll get two new steps in the build step
selection.

![](./docs/images/addstep.png "Jenkins > Cadence vManager Plugin > addstep.png")\
[*A new step types*]

-   Choose \"**vManager API**\" if you need a free-style vAPI call to
    sends dynamic json input and receive a json output.
-   Choose \"**vManager Session Launcher**\" if you need to add a step
    for remote launching vsif (one or more).


![A new post-build
type](./docs/images/postbuild.png "Jenkins > Cadence vManager Plugin > postbuild.png (A new post-build type)")

-   Choose \"**vManager Post Build Actions**\" if you need analysis
    links and session triage table at the Project (job) and Build (run)
    level. (see pre-requisite below)
-   The Post Build Action **must have** the \'vManager Launcher\' part
    running as part of the build steps as a pre-requisites.

## Usage

##### **vManager API**

The step takes care for the following:

-   Authentication.
-   Defining the API call.
-   Defining the json input string for the API (static/dynamic).
-   Saving the API result (json format) into the workspace. 

The below is an example of defining a **static vAPI call** for getting a
list of runs with session id equals to 1 or 2:

![](./docs/images/apistatic.png "Jenkins > Cadence vManager Plugin > apistatic.png")\
[*Adding static vAPI input*]

The below is an example of defining a **dynamic vAPI call**:

![](./docs/images/dynamicapi.png "Jenkins > Cadence vManager Plugin > dynamicapi.png")\
[*Adding dynamic vAPI input*]

In case of a need in dynamically change the jSON input for the API per
job, the pre-job should place into the workspace directory a file with
the relevant jSON string to be sent to the vAPI.\
The input file should be place into the workspace directory. In case
this field is empty, The file name need to be:
\$BUILD\_NUMBER..\$BUILD\_ID.vapi.input

Please fill this field only in case, you want to hard code the input
file name, to be consist across all builds.

##### **vManager Session Launcher**

There are two modes for launching a session using the vManager Session
Launcher action:

-   **Launcher mode (default)** - choose this if you want the plugin to
    take over the actual launch operation with all the relevant setting,
    environment and parameters.
-   **Batch mode** - choose this mode if you want to launch the sessions
    yourself using shell/batch as part of a pre-step for this action.

   
  ![](./docs/images/executiontype.png "Jenkins > Cadence vManager Plugin > executiontype.png")



**Launcher mode**

The step takes care for the following:

-   Authentication.
-   Launching a vsif that is located on the NFS and is available to
    vManager Server.

The below is an example of defining a **static vsif call.**

![](./docs/images/staticvsif.png "Jenkins > Cadence vManager Plugin > staticvsif.png")\
[*Launching a static vsif file*]

The below is an example of defining a **dynamic launch call with
 multiple vsif files.**

![](./docs/images/dynamicvsif.png "Jenkins > Cadence vManager Plugin > dynamicvsif.png")\
[*Launching more than one vsif file
dynamically*]

In case of a need in dynamically selecting the vsif files to get
launched per job, the pre-job should place into the workspace directory
a file with the full paths of the relevant vsif files to be launched,
new line for each additional vsif file.\
The input file should be place into the workspace directory. In case
this field is empty, The file name need to be:
\$BUILD\_NUMBER.\$BUILD\_ID.vsif.input\
Please fill this field only in case, you want to hard code the input
file name, to be consist across all builds.


**Batch mode  (Continue from a pre-launched
session)**

The below is an example of defining a continues action from a previous
launched session:

![](./docs/images/batchmode.png "Jenkins > Cadence vManager Plugin > batchmode.png")

This option is useful for those who wish to take benefit of their own
environment and scripts for launching sessions using batch, but still
want the benefit of having the Job waiting for the execution to end,
generates JUnit report, produce triage link, real time view of the
run\'s progress, etc\']\
[In this option, there is an assumption that a pre-step (usually from
shell type) is taking over the session launch operation, and as such, in
order for this plugin to continue monitor those sessions - the session
names need to be exchange between the shell step and this
step.\
\
In case of a need to launch the sessions using batch as a pre-step to
this one, the pre-step should place into the workspace directory a file
with the session or session names that were launched - new line for each
session name.\
The input file should be place into the working directory. In case this
field is empty, The file name need to be:
\${BUILD\_NUMBER}.\${BUILD\_ID}.sessions.input]\
[Please fill this field only in case, you want to hard code the input
file name, to be consist across all
builds.\
\
The vManager plugin will look for the any of the input files mentioned
above, and query the server for their respective id automatically. From
that point and on, the flow continues as if the sessions were launched
by this plugin.


**Collect mode  (Continue post
\'collect\')**

The below is an example of defining a continues action from a previous
launched session:

![](./docs/images/batchmode.png "Jenkins > Cadence vManager Plugin > batchmode.png")

This option is useful for those who wish to take advantage of this
plugin, but are not using the vManager Runner, and uses \"**collect**\"
mode instead. For such flow they can still benefit from generated JUnit
report, triage link, real time view of the run\'s progress, summary
report, etc\'.\
In this option, there is an assumption that a pre-step (usually from
shell type) is taking over the session launch operation (or collect),
and as such, in order for this plugin to continue monitoring those
sessions - the session names need to be exchange between the shell step
and this step.\
\
In case of a need to collect the sessions as a pre-step to this one, the
pre-step should place into the workspace directory a file with the
session or session names that were collected - new line for each session
name.\
The input file should be place into the working directory. In case this
field is empty, The file name need to be:
\${BUILD\_NUMBER}.\${BUILD\_ID}.sessions.input\
Please fill this field only in case, you want to hard code the input
file name, to be consist across all builds.\
\
the vManager plugin will look for any of the input files mentioned
above, and query the server for their respective id automatically. From
that point and on, the flow continues as if the sessions were collected
by this plugin.

**Important:**

please make sure \"**Wait for launched session to end**\" is checked
\'on\' within the plugin configuration.  Basically, your collected
sessions must be in a \"complete\" state within vManager.  The step will
only move FW once ALL your collected sessions are in a \"complete\"
state, and as such, this flag must be turned on.  You can still define
different states for the plugin to continue, if you like.\
\
The output of all IDs of the launched/collected sessions can be found
at:
\${BUILD\_NUMBER}.\${BUILD\_ID}.session\_launch.output



**Setting the build to wait till all session end execution:**

In case you want to hold the build till the session end its execution on
the vManager side, please check the \'wait for launched session to end\"
check box.

 
![](./docs/images/wait.png "Jenkins > Cadence vManager Plugin > wait.png")

The above setup allow you to select how the build will behave in each
of the state where the session stop from
running:

**Continue**\
In case you select to continue, the build will assume (on the chosen
state) for a given session that it can continue and finish the wait on
this specific session.\
Please note that in case there are multiple sessions that are being
executed by this step, the build will wait till all sessions got into a
state that allow it to continue. 

**Ignore**\
In case you select to continue, the build will assume (on the chosen
state) for a given session that it can ignore the chosen state and keep
waiting for other state (until get the \'completed\'
state). 

**Fail**\
In case you select to fail, the build will assume (on the chosen state)
for a given session that it should mark this build as a failure
build. **Note:** If
you have multiple sessions on this build step, it is enough for one
single session to be marked as \'failed\' in order to mark the entire
build as a failed build. 

**Other Waiting Considerations**
1. When all sessions on this build step are having the state
\'completed\' the build will be marked as
success.
2. When the vManager server goes down, the build step will keep waiting
till the server will go back up. The build step will only change its
state based on sessions state changes.
3. If the session was manually deleted on the vManager server, before
reaching into final state, the build will be marked as a failure
build.
4. In any case, if the number of minutes waiting is bigger than the
timeout set here, the build will marked as a failed
build.

**Add support in Junit Plugin and Test
Charting**

The launch Session Plugin is also capable of placing an XML file in the
format of Junit Test Report.  The XML file name will
be **session_runs.xml** This format let \'Junit Plugin\"
chart over each build.  In order to activate
this:

1. After activating the \"wait for
session to end\", check the  \"Generate JUnit Result\" checkbox.  You
can also add custom Runs\' attributes to the report using this
configuration.

2. Install the plugin \"Junit Plugin\".
 Add a final build step to pick session_runs.xml
from the workspace.

This will give you a detail summary of the build, and an aggregated
charting over your entire builds.  See below.

![](./docs/images/detail_junit.png "Jenkins > Cadence vManager Plugin > detail_junit.png")
![](./docs/images/JunitA.png "Jenkins > Cadence vManager Plugin > JunitA.png")



**Launching session using pipeline syntax**

The vManager Plugin (Launching New Session only) also support the
Pipeline syntax:

![](./docs/images/pipeline.png "Jenkins > Cadence vManager Plugin > pipeline.png")

![](./docs/images/stage.png "Jenkins > Cadence vManager Plugin > stage.png")


**Setting Jenkins to delete the build\'s sessions from vManager DB when
a build is being removed**

![](./docs/images/delete.png "Jenkins > Cadence vManager Plugin > delete.png")


<div>

Choosing to delete vManager session during build removal, will trigger
an operation during manual/automatic deletion of a build, to deal with
the remote session/sessions that were created during that build on the
vManager DB.\
When this option is enabled the build will place an instruction file
(sdi.properties) within the job directory that specify the sessions to
get deleted, as well as other parameters - that will be used during the
delete operation.\
\
Builds that runs while this option is turned off, will not get effected
during removal, and will keep their sessions.\
\
You can choose between two methodologies: \
\
**Sync Delete Methodology (built-in)**\
In case you select the sync methodology, the plugin will call vManager
vAPI during the build removal process for deleting the sessions that
were created during that same build.\
With this option you can also supply a generic user/password to be used
for the delete operation, otherwise, the same user that was used during
the build will be picked automatically. \
Please note that the sync methodology is lacking two main aspects:
1. Since Jenkins ignores any exception thrown within the callback
functions of RunListener, the build will get deleted even if the session
failed to get deleted from vManager DB.
2. When the vAPI is down, it can take up to 20 seconds to finish the
operation (as it needs to wait till vAPI will be available) - the UX at
that time, might appears as sluggish to the end user.\
\
**Async Delete Methodology (externally)**\
In case you want to introduce a more robust approach (promising a
session deletion even if vManager Server is down, as well as faster UX),
you should use the async methodology.\
When Async Methodology is used, the callback function will not try to
delete the session, but instead will copy the sdi.properties file into
an external location of your choice. \
You should create an additional job, one that is triggered every 1
minute for scanning that directory (and trying to delete the relevant
sessions within these sdi files). To exclude the copy of the sdi files
during build removal, this flow is not managed by the plugin.\
Please note - defining an external directory location (in windows)
requires the use of forward slash instead of backslash.

</div>

## Dashboard

The vManager Plugin also support a new Dashboard portal using the
Dashboard-view plugin.  The new portal reflects the session\'s states
launched by the various builds:

![](./docs/images/Dashboard.png "Jenkins > Cadence vManager Plugin > Dashboard.png")\
To enable the new portal, select \"**vManager Latest Sessions**\" from
the Dashboard drop-down.

## Authentication

##### ![](./docs/images/newlogin.png "Jenkins > Cadence vManager Plugin > newlogin.png")

[*defining the connection settings*]

In case of a need in dynamically selecting the user name per job, the
pre-job should place into the workspace directory a file with single
line that contains the userid to be used.\
The file name should be: \$BUILD\_NUMBER.\$BUILD\_ID.user.input\
The job will pick the userid which is in the file, and connect to vAPI
using this userid and the vAPI secret key.


## vManager Post Build Actions

The vManager Plugin also supports a post-build action that brings:

-   High level view at the  session\'s states launched by the various
    builds:
-   Project level hyperlink that links to vManager Web Regression for
    the last build available for further analysis
-   Fine grained view of the session launched at the build (run) level
    (in case more than one session was launched during the regression)
-   Build level hyperlink that links to vManager Web Regression for the
    launched session for further analysis
-   Adds an ability to embed the vManager Summary Report within Jenkins
    as part of the build page.
-   Adds an ability to send vManager Summary Report to dynamically
    selective users at the end of the regression.

The action is also available for pipeline usage using the
function **vmanagerPostBuildActions()**

*Pre-requisite and limitation when using the post build
action:*
1.  The post-build action uses information that is gathered by the
    Launcher Action.  In order for the launcher action to generate the
    relevant information, please make sure \"Wait for launched session
    to end\" is checked on the **vManager Session Launcher** described
    above.
2.  Please make sure **vManager Session Launcher** step is used in your
    workflow prior of calling the post-build action.
3.  The new added links will **not** appear at the Project level until
    at least one build will get completed on the relevant project (since
    the date the post-action was added)
4.  In case of a Jenkins restart, the links at the Project level will
    not work until at least one build will get completed on the relevant
    project (since the time of restart).  This is because of a bug in
    the Jenkins Platform.

![](./docs/images/projectlevel.png "Jenkins > Cadence vManager Plugin > projectlevel.png")

[*Session View at the Project level*]


![](./docs/images/buildlevel.png "Jenkins > Cadence vManager Plugin > buildlevel.png")

[*Session Triage View at the Build
level*]


![](./docs/images/buildlevellink.png "Jenkins > Cadence vManager Plugin > buildlevellink.png")

[*Links at the build level for doing analysis on the launched
session. (vManager Analysis)*]


![](./docs/images/projectlevellink.png "Jenkins > Cadence vManager Plugin > projectlevellink.png")
[*Links at the project level for doing analysis on the last completed
build. (vManager Analysis)*]


**Summary Report and Email**

*Security issue* was introduced in version 2.7.0. Please use version 2.7.1 and above if possible. If you are already using the below capability on 2.7.0, upgrading to 2.7.1 will add a new checkbox to get your consent regard SSL validation.  Please check for more information within the plugin\'s configurations.

1.  The vManager 2.7.0 post-build now have the ability to embed into
    Jenkins the vManager Summary Report.  This gives you the ability to
    have a quick view of your verification closure, vPlan progress and
    test based hierarchy aggregation right from the Jenkins build page
    using Javascript collapse/expand capability to view the hierarchies
    for any level needed.
2.  The post plugin also allows you to specify static/dynamic list of
    emails to get the summary report at the end of each build.

The Summary Report is part of the Plugin Advanced Functions, and is
supported as part of pipeline dsl or traditional post plugin.

![](./docs/images/report_tests.png "Jenkins > Cadence vManager Plugin > report_tests.png")

![](./docs/images/report_metrics.png "Jenkins > Cadence vManager Plugin > report_metrics.png")


The summary report support 3 modes for bringing the report into the
Jenkins environment:

**Retrieve and Present Job Summary Report**

This mode covers most of the summary report settings, but not all using
some kind of pre-built menu.  It gives you the ability to define which
sections (tests, metrics, vplan) to include in the report, as well as
other advanced parameters within each section.  Using this mode is the
fastest way to try out the capability. 

**Retrieve and Present Job Summary Report (free vAPI syntax)**

In case a finer granularity is needed for the summary report such that
is not available within the menu approach, one can choose to work with
the full definition of the vAPI syntax for the
/reports/generate-summary-report vAPI.  Please make sure not to include
the \"rs\" part as it\'s built by the plugin dynamically based on the
session list that were launched during the specific build. 

**Important (**before and after 19.09 version**):**

In order to embed the summary report into Jenkins, a special format of
report is needed.  This special format can be achieved with the
additional vAPI flag \"Jenkins\":true that is only supported in vManager
19.09 onward. 

In order to make this feature available also for users who are currently
not using vManager \>19.09, please set the \"vManager Version\" drop
down accordingly (default to 19.09), so that the plugin will know
parse/modify the report and make it Jenkins complaint.  The outcome in
both options is the same although there can be differences in the
performance of the overall operation.

**Email**

The email feature comes in this plugin is making use of the vManager
Server Email capability.  Please make sure that Sendmail package is
installed on your Linux machine where the vManager server is installed
if you would like to use this capability.


**Bring the summary report yourself (view mode only)**

As the vAPI runs under the vManager server account, it can introduce a
limitation when trying to get access to coverage or vPlan data.  In such
cases, the vAPI can\'t be used for getting the report, and the user have
the option to generate the report himself using batch command.  This
option is only available for users with vManager version **19.09** and
above as the report generated must have a very specific format that can
only be achieved by adding the flag \"-jenkins true\" within the batch
command -- which only supported starting 19.09.  In order for the plugin
to find the report and show it to the user - please place the report in
the following format within the job working directory: 
**\${BUILD\_NUMBER}.\${BUILD\_ID}.summary.report**

\"**Email**\" is not offered as part of this mode.


![](./docs/images/report_conf.png "Jenkins > Cadence vManager Plugin > report_conf.png")

## Change Log

##### Version 3.0.3 (Aug 3, 2020)

-   Bug fix.  When choosing to use User SSH key, the pre-execution file was mandatory. 

##### Version 3.0.2 (Jan 26, 2020)

-   Added support in vManager session suspension/pause when user abort/cancel/stop Jenkins jobs. 

##### Version 3.0.0 (Nov 1, 2019)

-	Added support in a none shared NFS between master and slave.
	Plugin can now consume/dump input/output files from build running
	on remote slaves.  (Not supported with Pipeline)
-	Added support in using Jenkins macros as part of the input params.
-	Added two new text area for supplying session env params and session
	attribute values without the need for an input file.
-   Library updates. Plugin now requires Jenkins version 2 and above
	with JDK 1.8 and above. 
-	New dependency - Token-Macro (version 2.3 and above)
-	Updated dependency - Dashboard-view (version 2.10 and above).
-	Remove apache-httpcomponents direct dependency (plugin size was also
	reduced).

##### Version 2.7.1 (Oct 11, 2019)

-   Security fix.  See above for information. 

##### Version 2.7.0 (Sep 26, 2019)

-   Added a support in a Summary Report post build.
-   Added support for sending Email with Summary report post build.

##### Version 2.6.0 (Sep 11, 2019)

-   Added a support in a session launching that is made by a pre-step
    using shell/batch.
-   Fixed missing UI in the pipeline syntax reference page.

##### Version 2.5.9 (Sep 10, 2019)

-   Added a link within the project page for direct access to the
    vManager web analysis (post-build action)
-   Added a table of sessions within the project\'s page to track the
    session\'s history and give high level overview about the progress
    (post-build action)
-   Added support for vManager HA architecture
-   Fix JUnit report to aggregate all runs from all session\'s that were
    launched as part of the build (before only the last session\'s runs
    were collected).  This is only relevant for build that launches
    more than one session.

##### Version 2.5.8 (Aug 28, 2019)

-   Added an option to overwrite vsif attribute values before launch.

##### Version 2.5.7 (Aug 21, 2019)

-   A fix for Dashboard view with jobs that lack the build area (there
    was an issue that caused the list of jobs to become empty within the
    vManager Latest Job plugin).

##### Version 2.5.6 (June 28, 2019)

-   Added an option to launch regression using the user\'s stored
    private SSH key.

##### Version 2.5.5 (April 15, 2019)

-   Added an option to mark Jenkins Job as \"Failed\" unless all the
    runs in the session passed.

##### Version 2.5.4 (Sep 27, 2018)

-   Added a support in master/node setup.  An ability to specify the
    master workspace.

##### Version 2.5.3 (Feb 7, 2018)

-   Added a support to avoid appending the seed number to the test name
    in the JUnit XML report.

##### Version 2.5.2 (Dec 10, 2017)

-   Added a support for a session delete when a manual/automatic build
    removal is made.

##### Version 2.5.1 (Nov 22, 2017)

-   Added support in Pipeline.  You can now call vManager Launch using
    the Pipeline syntax.  (Requires Jenkins 2.19.1 and above.)
-   vManager Dashboard will now fetch historical data from the build
    directory instead of the workspace directory.  Once this plugin is
    updated, the Dashboard will only show historical jobs running from
    when the update occurred and on.

##### Version 2.4.9 (Sep 26, 2017)

-   Add an option to support the new 17.10 capability of sourcing an
    alias file for env variable per user before launching a session.

##### Version 2.4.7 (Feb 22, 2017)

-   Add an option to mark Jenkins Job as \"Failed\" when all runs in
    session are also failed.

##### Version 2.4.5 (Dec 9, 2016)

-   Add JUnit support in up to 100K run\'s for the XML report.
-   Fix special characters for JUnit XML.

##### Version 2.4.3 (Oct 26, 2016)

-   Add an option to mark build as \"Failed\" when all runs in session
    are also failed.

##### Version 2.4.2 (Sep 20, 2016)

-   Fixed a crash when saving the Jenkins Global Configuration screen.

##### Version 2.4 (Aug 23, 2016)

-   Changed Junit XML output to mark \'skip\' (yellow) on any run state
    other than \'passed\' or \'failed\'.

##### Version 2.3 (Aug 19, 2016)

-   Add session generated unique id as part of the session name in the
    dashboard.
-   Change the JUnit XML file name to be consisted across all builds.
-   Upgrade the build POM to Jenkins 2.14

##### Version 1.10 (July 24, 2016)

-   Add support for detailed error messages coming back from vAPI such
    as missing env variables.
-   Add support for JUnit Results XML even when build marked as failed.

##### Version 1.9 (July 18, 2016)

-   Add support in dashboard-view plugin.
-   Add support for JUnit Results XML

##### Version 1.8 (July 12, 2016)

-   Add support for the build step to wait till Session ends.
-   Create session\_list.output per job that contain the session ID
    launched.

##### Version 1.7 (July 5, 2016)

-   Add support for Session Launch with the user\'s Linux account
    credentials.

##### Version 1.6 (May 20, 2015)

-   Add support for VSIF variables.

##### Version 1.5 (May 14, 2015)

-   Add support in timeout configuration.

##### Version 1.4 (Sep 11, 2014)

-   Add support in PUT & GET API.

##### Version 1.3 (Aug 24, 2014)

-   Add support in vAPI over SSL.

##### Version 1.2 (Aug 14, 2014)

-   Update code for supporting the automatic launching of vAPI using
    vManager Server.

##### Version 1.1 (July 17, 2014)

-   Update code for more recent API

##### Version 1.0 (July 15, 2014)

-   Initial release

 
