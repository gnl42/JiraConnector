package me.glindholm.connector.eclipse.internal.bamboo.core.service.rest;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.net.Proxy;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.bamboo.invoker.ApiClient;
import me.glindholm.bamboo.invoker.ApiException;
import me.glindholm.bamboo.invoker.ApiResponse;
import me.glindholm.bamboo.model.BuildChange;
import me.glindholm.bamboo.model.BuildChangeFile;
import me.glindholm.bamboo.model.BuildChanges;
import me.glindholm.bamboo.model.BuildPlan;
import me.glindholm.bamboo.model.BuildPlanConfig;
import me.glindholm.bamboo.model.BuildPlans;
import me.glindholm.bamboo.model.BuildResult;
import me.glindholm.bamboo.model.BuildStage;
import me.glindholm.bamboo.model.RestCommentList;
import me.glindholm.bamboo.model.RestInfo;
import me.glindholm.bamboo.model.RestStage;
import me.glindholm.bamboo.model.TestResultErrorMsg;
import me.glindholm.bamboo.model.UserBean;
import me.glindholm.connector.eclipse.internal.bamboo.core.rest.client.api.BambooRestClient;
import me.glindholm.connector.eclipse.internal.bamboo.core.service.BambooClientCache;
import me.glindholm.connector.eclipse.internal.core.JiraConnectorCorePlugin;
import me.glindholm.theplugin.commons.BambooFileInfo;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BambooBuildInfo;
import me.glindholm.theplugin.commons.bamboo.BambooChangeSet;
import me.glindholm.theplugin.commons.bamboo.BambooChangeSetImpl;
import me.glindholm.theplugin.commons.bamboo.BambooJobImpl;
import me.glindholm.theplugin.commons.bamboo.BambooPlan;
import me.glindholm.theplugin.commons.bamboo.BuildDetails;
import me.glindholm.theplugin.commons.bamboo.BuildDetailsInfo;
import me.glindholm.theplugin.commons.bamboo.BuildStatus;
import me.glindholm.theplugin.commons.bamboo.TestDetailsInfo;
import me.glindholm.theplugin.commons.bamboo.TestResult;
import me.glindholm.theplugin.commons.bamboo.api.BambooSessionImpl;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

public class BambooRestClientAdapter {
    public static final String PRODUCT_NAME = "Eclipse Mylyn JiraConnector for Atlassian's Bamboo/" + JiraConnectorCorePlugin.getDefault().getVersion();

    protected String url;
    protected BambooClientCache cache;
    protected boolean followRedirects;

    private BambooRestClient restClient;

    public BambooRestClientAdapter(final String url, final BambooClientCache cache, final boolean followRedirects) {
        this.url = url;
        this.cache = cache;
        this.followRedirects = followRedirects;
    }

    public BambooRestClientAdapter(final String url, final String userName, final String password, final Proxy proxy, final BambooClientCache cache,
            final boolean followRedirects) {
        this(url, cache, followRedirects);

        final ApiClient apiClient = new ApiClient();
        final HttpClient.Builder httpClient = HttpClient.newBuilder();
        httpClient.connectTimeout(Duration.ofMinutes(1)) //
                .followRedirects(followRedirects ? HttpClient.Redirect.ALWAYS : HttpClient.Redirect.NEVER);
        apiClient.setHttpClientBuilder(httpClient);

        apiClient.updateBaseUri(url + "/rest");
        apiClient.setRequestInterceptor(authorize -> addHeaders(userName, password, authorize));

        restClient = BambooRestClientFactory.newClient(apiClient, url, userName);

    }

    private static HttpRequest.Builder addHeaders(final String username, final String password, final HttpRequest.Builder authorize) {
        authorize.header("Authorization", isNotEmpty(password) ? basicAuth(username, password) : bearerAuth(password));
        authorize.header("User-Agent", PRODUCT_NAME);
        return authorize;
    }

    private static String basicAuth(final String username, final String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    private static String bearerAuth(final String token) {
        return "Bearer " + token;
    }

    public RestInfo getServerInfo() throws RemoteApiException {
        return call(new Callable<RestInfo>() {
            @Override
            public RestInfo call() throws RemoteApiException {
                try {
                    return restClient.getDefaultApi().getInfo().get();
                } catch (InterruptedException | ExecutionException | ApiException e) {
                    throw new RemoteApiException(e);
                }
            }
        });
    }

    private <V> V call(final Callable<V> callable) throws RemoteApiException {
        try {
            return callable.call();
        } catch (final Exception e) {
            throw new RemoteApiException(e);
        }
    }

    public UserBean getCurrentUser() throws RemoteApiException {
        return call(new Callable<UserBean>() {
            @Override
            public UserBean call() throws RemoteApiException {
                try {
                    return restClient.getDefaultApi().getCurrentUser().get();
                } catch (InterruptedException | ExecutionException | ApiException e) {
                    throw new RemoteApiException(e);
                }
            }
        });
    }

    public List<BambooPlan> getPlanList() throws RemoteApiException {

        final List<BambooPlan> plans = listPlanNames();

        try {
            final List<String> favPlans = getFavouriteUserPlansNew();
            for (final String fav : favPlans) {
                for (final ListIterator<BambooPlan> it = plans.listIterator(); it.hasNext();) {
                    final BambooPlan plan = it.next();
                    if (plan.getKey().equalsIgnoreCase(fav)) {
                        it.set(plan.withFavourite(true));
                        break;
                    }
                }
            }
        } catch (final RemoteApiException e) {
            // lack of favourite info is not a blocker here
        }
        return plans;
    }

    @NonNull
    public List<String> getFavouriteUserPlansNew() throws RemoteApiException {
        final List<String> builds = new ArrayList<>();
        try {
            final BuildPlans plans = restClient.getBuildApi().getAllPlanList("plans", null, 5000).get();
            for (final BuildPlan plan : plans.getPlans().getPlan()) {
                builds.add(plan.getKey());
            }
        } catch (InterruptedException | ExecutionException | ApiException e) {
        }
        return builds;
    }

    @NonNull
    private List<BambooPlan> listPlanNames() throws RemoteApiException {
        try {
            final BuildPlans buildPlans = restClient.getBuildApi().getAllPlanList("plans", null, 5000).get();
            final List<BambooPlan> plans = new ArrayList<>();

            for (final BuildPlan plan : buildPlans.getPlans().getPlan()) {
                plans.add(new BambooPlan(plan.getName(), plan.getPlanKey().getKey(), null, plan.getEnabled()));
            }
            return plans;
        } catch (final InterruptedException | ExecutionException | ApiException e) {
            throw new RemoteApiException("", e);
        }

    }

    public String getUrl() {
        return "bamboo";
    }

    public Collection<BambooBuild> getSubscribedPlansResults(final Collection<SubscribedPlan> plans, final boolean isUseFavourities, final boolean showBranches,
            final boolean myBranchesOnly, final int timezoneOffset) throws RemoteApiException {
        final List<BambooBuild> builds = new ArrayList<>();

        Collection<BambooPlan> plansForServer = null;
        RemoteApiException exception = null;
        try {
            plansForServer = getPlanList();
        } catch (final RemoteApiException e) {
            // can go further, no disabled info will be available
//            loger.warn("Cannot fetch plan list from Bamboo server [" + getUrl() + "]");
            exception = e;
        }

        if (isUseFavourities) {
            if (plansForServer != null) {
                for (final BambooPlan bambooPlan : plansForServer) {
                    if (bambooPlan.isFavourite()) {
                        try {
                            final BambooBuild buildInfo = getLatestBuildBuilderForPlan(bambooPlan.getKey(), timezoneOffset).enabled(bambooPlan.isEnabled())
                                    .build();
                            builds.add(buildInfo);
                        } catch (final RemoteApiException e) {
                            // go ahead, there are other builds
//                                loger.warn("Cannot fetch latest build for plan [" + bambooPlan.getKey() + "] from Bamboo server [" + getUrl() + "]");
                        }
                    }
                }
            }
        } else {
            for (final SubscribedPlan plan : plans) {
                try {
                    final Boolean isEnabled = plansForServer != null ? BambooSessionImpl.isPlanEnabled(plansForServer, plan.getKey()) : null;
                    final BambooBuild buildInfo = getLatestBuildBuilderForPlan(plan.getKey(), timezoneOffset).enabled(isEnabled != null ? isEnabled : true)
                            .build();
                    builds.add(buildInfo);
                } catch (final RemoteApiException e) {
                    // go ahead, there are other builds
                    // todo what about any error info
                }
            }
        }

        return builds;
    }

    public BuildDetails getBuildDetails(@NonNull final String planKey, final int buildNumber) throws RemoteApiException {

        // tests are available for separate jobs since Bamboo v 2.7
        final List<BambooJobImpl> jobs = getJobsForPlan(planKey);

        final BuildDetailsInfo build = new BuildDetailsInfo();

        for (final BambooJobImpl job : jobs) { // job key contains project key

            build.addJob(job);

            if (!job.isEnabled()) {
                // there are no details for disabled jobs
                continue;
            }
            try {
                final String[] id = job.getKey().split("-");
                final BuildResult testData = restClient.getDefaultApi()
                        .getBuild(id[0], id[1], id[2] + "-" + buildNumber, "testResults.allTests.testResult.errors", null).get();
                if (testData.getTestResults() != null) {
                    for (final me.glindholm.bamboo.model.TestResult data : testData.getTestResults().getAllTests().getTestResult()) {
                        final TestDetailsInfo tInfo = new TestDetailsInfo();

                        tInfo.setTestClassName(data.getClassName());
                        tInfo.setTestMethodName(data.getMethodName());
                        final String status = data.getStatus();
                        try {
                            tInfo.setTestResult(parseTestResult(status));
                        } catch (final ParseException e1) {
//                            loger.warn("Cannot parse test result element:" + status, e1);
                            continue;
                        }

                        tInfo.setTestDuration(data.getDuration().doubleValue() / 1000);

                        final StringBuilder errMsgs = new StringBuilder();
                        for (final TestResultErrorMsg error : data.getErrors().getError()) {
                            final String[] msgs = error.getMessage().split("\n");
                            errMsgs.append(msgs[0]).append("\n");
                            for (int i = msgs.length - 1; i > 0; i--) {
                                errMsgs.append(msgs[i]).append("\n");
                            }
                        }
                        tInfo.setTestErrors(errMsgs.toString());

                        switch (tInfo.getTestResult()) {
                        case TEST_FAILED:
                            build.addFailedTest(tInfo);
                            job.addFailedTest(tInfo);
                            break;
                        case TEST_SUCCEED:
                            build.addSuccessfulTest(tInfo);
                            job.addSuccessfulTest(tInfo);
                            break;
                        default:
                            break;
                        }

                    }
                }
                if (testData.getChanges().getChange() != null) {
                    build.setCommitInfo(parseChangeSets(testData.getChanges()));
                }

                // return build;
            } catch (final InterruptedException | ExecutionException | ApiException e) {
                throw new RemoteApiException(e.getMessage(), e);
            }
        }

        return build;

    }

    private static List<BambooChangeSet> parseChangeSets(final BuildChanges changes) {
        final List<BambooChangeSet> changeSets = new ArrayList<>();
        for (final BuildChange change : changes.getChange()) {
            final BambooChangeSetImpl cInfo = new BambooChangeSetImpl();
            cInfo.setAuthor(change.getAuthor());
            cInfo.setCommitDate(change.getDate().toInstant());
            cInfo.setComment(change.getComment());
            if (change.getFiles() != null) {
                for (final BuildChangeFile file : change.getFiles().getFile()) {
                    final BambooFileInfo fileInfo = new BambooFileInfo(file.getName(), file.getRevision());
                    cInfo.addCommitFile(fileInfo);
                }
            }
            changeSets.add(cInfo);
        }

        return changeSets;
    }

    private static TestResult parseTestResult(final String attributeValue) throws ParseException {
        if ("failed".equals(attributeValue)) {
            return TestResult.TEST_FAILED;
        } else if ("successful".equals(attributeValue)) {
            return TestResult.TEST_SUCCEED;
        }
        throw new ParseException("Invalid test result [" + attributeValue + "]", 0);
    }

    public List<BambooJobImpl> getJobsForPlan(final String planKey) throws RemoteApiException {

        final List<BambooJobImpl> jobs = new ArrayList<>();
        try {
            final String[] id = planKey.split("-");
            final BuildPlan plan = restClient.getBuildApi().getPlan(id[0], id[1], "stages.stage.plans").get();

            for (final RestStage stage : plan.getStages().getStage()) {
                for (final BuildPlanConfig plan2 : stage.getPlans().getPlan()) {
                    final String key = plan2.getKey();
                    final String shortKey = plan2.getShortKey();
                    final String name = plan2.getName();
                    final String shortName = plan2.getShortName();

                    final BambooJobImpl job = new BambooJobImpl(key, shortKey, name, shortName);
                    job.setEnabled(plan2.getEnabled());
                    jobs.add(job);
                }
            }
            return jobs;
        } catch (InterruptedException | ExecutionException | ApiException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    public String getBuildLogs(@NonNull final String planKey, final int buildNumber) throws RemoteApiException {
        throw new RemoteApiException("Not ported yet");
    }

    public void addLabelToBuild(@NonNull final String planKey, final int buildNumber, final String buildLabel) throws RemoteApiException {
        throw new RemoteApiException("Not ported yet");
    }

    public void addCommentToBuild(@NonNull final String planKey, final int buildNumber, final String buildComment) throws RemoteApiException {
        throw new RemoteApiException("Not ported yet");
    }

    public void executeBuild(@NonNull final String planKey) throws RemoteApiException {
        try {
            final String[] id = planKey.split("-");
            restClient.getDefaultApi().startBuild(id[0], id[1], true, null, null, null).get();
        } catch (final InterruptedException | ExecutionException | ApiException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public BambooBuild getBuildForPlanAndNumber(@NonNull final String planKey, final int buildNumber, final int timezoneOffset) throws RemoteApiException {
        // TODO Auto-generated method stub
        throw new RemoteApiException("Not ported yet");
    }

    public BambooBuildInfo.Builder getLatestBuildBuilderForPlan(@NonNull final String planKey, final int timezoneOffset) throws RemoteApiException {
        try {
            final ApiResponse<BuildResult> buildResultResponse = restClient.getDefaultApi().getLatestBuildResultForProjectWithHttpInfo(planKey,
                    "changes,changes.change,changes.change.files,comments,comments.comment,labels,stages.stage[0],stages.stage[0].results.result.testResults.allTests.testResult.errors",
                    null, null, null, null, null, null, null, null, null).get();

            if (buildResultResponse.getStatusCode() == 200) {
                final BuildResult buildResult = buildResultResponse.getData();
                final List<BuildStage> stage = buildResult.getStages().getStage();
                final String buildNumber = String.valueOf(buildResult.getBuildNumber());
                final Set<String> commiters = new HashSet<>();
                for (final BuildChange changer : buildResult.getChanges().getChange()) {
                    commiters.add(changer.getAuthor());
                }
                return constructBuilderItem_40(buildResult, Instant.now(), planKey, commiters, timezoneOffset);
            } else {
                return new BambooBuildInfo.Builder(planKey, restClient, BuildStatus.UNKNOWN).pollingTime(Instant.now()).reason("Never built");

            }

        } catch (final InterruptedException | ExecutionException | ApiException e) {
            if (e.getCause() != null && e.getCause() instanceof ApiException) {
                final ApiException realE = (ApiException) e.getCause();
                if (realE.getCode() == 404) {
                    return new BambooBuildInfo.Builder(planKey, restClient, BuildStatus.UNKNOWN).pollingTime(Instant.now()).reason("Never built");
                }
            }

            return constructBuildErrorInfo(planKey, e.getMessage(), e, Instant.now());
        }
    }

    private BambooBuildInfo.Builder constructBuilderItem_40(final BuildResult build, final Instant instant, final String aPlanKey, final Set<String> commiters,
            final int timezoneOffset) {
        BambooBuildInfo.Builder builder = null;
        // for never executed build we actually have no data here (no children)
        if (build.getBuildStartedTime() == null) {
            builder = new BambooBuildInfo.Builder(aPlanKey, restClient, BuildStatus.UNKNOWN).pollingTime(instant).reason("Never built");
        } else {

            final String planKey = aPlanKey;
            final String buildName = build.getPlanName();
            final String projectName = build.getProjectName();
            final int buildNumber = build.getBuildNumber();
            final String relativeBuildDate = build.getBuildRelativeTime();
            final Instant startTime = build.getBuildStartedTime().toInstant();
            final Instant completionTime = build.getBuildCompletedTime().toInstant();

            final String durationDescription = build.getBuildDurationDescription();

            final String stateStr = build.getBuildState();
            final RestCommentList comments = build.getComments();
            final String buildComment = ""; // build.getComments().getComment().get(0),getComment.;
            builder = new BambooBuildInfo.Builder(planKey, buildName, restClient, projectName, buildNumber, getStatus(stateStr)).pollingTime(instant)
                    .reason(getBuildReason(build.getBuildReason())).startTime(startTime).testSummary(build.getBuildTestSummary()).commitComment(buildComment)
                    .testsPassedCount(build.getSuccessfulTestCount()).testsFailedCount(build.getFailedTestCount()).completionTime(completionTime)
                    .relativeBuildDate(relativeBuildDate).durationDescription(durationDescription).commiters(commiters);
        }
        return builder;
    }

    private static String getBuildReason(final String reasonOriginal) {
        // Pattern pattern = Pattern.compile("<a([^>]+)>(.+?)</a>");
        final Pattern pattern = Pattern.compile("(.*)<a([^>]+)>(.+)</a>");

        final Matcher m = pattern.matcher(reasonOriginal);
        if (m.find()) {
            return StringEscapeUtils.unescapeHtml(m.group(1) + m.group(3));
            // return m.group(1) + m.group(3);
        }

        return reasonOriginal;
    }

    private static final String BUILD_COMPLETED_DATE_ELEM = "buildCompletedDate";

    private static final String BUILD_SUCCESSFUL = "Successful";

    private static final String BUILD_FAILED = "Failed";

    private static final String CANNOT_PARSE_BUILD_TIME = "Cannot parse buildTime.";
    private static final String INVALID_SERVER_RESPONSE = "Invalid server response";

    @NonNull
    private static BuildStatus getStatus(@Nullable final String stateStr) {
        if (BUILD_SUCCESSFUL.equalsIgnoreCase(stateStr)) {
            return BuildStatus.SUCCESS;
        } else if (BUILD_FAILED.equalsIgnoreCase(stateStr)) {
            return BuildStatus.FAILURE;
        } else {
            return BuildStatus.UNKNOWN;
        }
    }

    BambooBuildInfo.Builder constructBuildErrorInfo(final String planKey, final String message, final Instant instant) {
        return new BambooBuildInfo.Builder(planKey, null, restClient, null, null, BuildStatus.UNKNOWN).pollingTime(instant).errorMessage(message);
    }

    BambooBuildInfo.Builder constructBuildErrorInfo(final String planKey, final String message, final Throwable exception, final Instant lastPollingTime) {
        return new BambooBuildInfo.Builder(planKey, null, restClient, null, null, BuildStatus.UNKNOWN).pollingTime(lastPollingTime).errorMessage(message,
                exception);
    }

}
