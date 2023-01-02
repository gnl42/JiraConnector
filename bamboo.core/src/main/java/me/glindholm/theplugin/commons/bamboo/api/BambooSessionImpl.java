/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.theplugin.commons.bamboo.api;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringEscapeUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.xpath.XPath;

import me.glindholm.bamboo.api.BuildApi;
import me.glindholm.bamboo.api.DefaultApi;
import me.glindholm.bamboo.invoker.ApiException;
import me.glindholm.bamboo.model.PlanResults;
import me.glindholm.bamboo.model.RestChange;
import me.glindholm.bamboo.model.RestChangeFile;
import me.glindholm.bamboo.model.RestChangeList;
import me.glindholm.bamboo.model.RestCommentList;
import me.glindholm.bamboo.model.RestInfo;
import me.glindholm.bamboo.model.RestPlan;
import me.glindholm.bamboo.model.RestPlanBranch;
import me.glindholm.bamboo.model.RestPlanConfig;
import me.glindholm.bamboo.model.RestPlans;
import me.glindholm.bamboo.model.RestStage;
import me.glindholm.bamboo.model.Result;
import me.glindholm.bamboo.model.TestResultErrorMsg;
import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.BambooFileInfo;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BambooBuildInfo;
import me.glindholm.theplugin.commons.bamboo.BambooBuildInfo.Builder;
import me.glindholm.theplugin.commons.bamboo.BambooChangeSet;
import me.glindholm.theplugin.commons.bamboo.BambooChangeSetImpl;
import me.glindholm.theplugin.commons.bamboo.BambooJobImpl;
import me.glindholm.theplugin.commons.bamboo.BambooPlan;
import me.glindholm.theplugin.commons.bamboo.BambooProject;
import me.glindholm.theplugin.commons.bamboo.BambooProjectInfo;
import me.glindholm.theplugin.commons.bamboo.BuildDetails;
import me.glindholm.theplugin.commons.bamboo.BuildDetailsInfo;
import me.glindholm.theplugin.commons.bamboo.BuildIssue;
import me.glindholm.theplugin.commons.bamboo.BuildIssueInfo;
import me.glindholm.theplugin.commons.bamboo.BuildStatus;
import me.glindholm.theplugin.commons.bamboo.TestDetailsInfo;
import me.glindholm.theplugin.commons.bamboo.TestResult;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiBadServerVersionException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import me.glindholm.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import me.glindholm.theplugin.commons.util.Logger;
import me.glindholm.theplugin.commons.util.LoggerImpl;
import me.glindholm.theplugin.commons.util.MiscUtil;
import me.glindholm.theplugin.commons.util.UrlUtil;
import me.glindholm.theplugin.commons.util.XmlUtil;

/**
 * Communication stub for Bamboo REST API.
 */
public class BambooSessionImpl extends LoginBambooSession implements BambooSession {

    private final Logger loger;

    private static final String LIST_PROJECT_ACTION = "/api/rest/listProjectNames.action";

    private static final String LIST_PLAN_ACTION = "/api/rest/listBuildNames.action";

    private static final String LATEST_BUILD_FOR_PLAN_ACTION = "/api/rest/getLatestBuildResults.action";

    private static final String LATEST_BUILD_FOR_PLAN = "/rest/api/latest/result/";

    private static final String PLAN_STATE = "/rest/api/latest/plan/";

    private static final String GET_BUILD_ACTION = "/rest/api/latest/build/";

    private static final String GET_BUILD_DETAILS = "/rest/api/latest/result/";

    private static final String GET_ISSUES_SUFFIX = "?expand=jiraIssues";

    private static final String BUILD_QUEUE_SERVICE = "/rest/api/latest/queue/";

    private static final String RECENT_BUILDS_FOR_PLAN_ACTION = "/api/rest/getRecentlyCompletedBuildResultsForBuild.action";

    private static final String RECENT_BUILDS_FOR_USER_ACTION = "/api/rest/getLatestBuildsByUser.action";

    private static final String LATEST_USER_BUILDS_ACTION = "/api/rest/getLatestUserBuilds.action";

    private static final String GET_BUILD_DETAILS_ACTION = "/api/rest/getBuildResultsDetails.action";

    private static final String ADD_LABEL_ACTION = "/api/rest/addLabelToBuildResults.action";

    private static final String ADD_COMMENT_ACTION = "/api/rest/addCommentToBuildResults.action";

    private static final String EXECUTE_BUILD_ACTION = "/api/rest/executeBuild.action";

    private static final String GET_BAMBOO_BUILD_NUMBER_ACTION = "/api/rest/getBambooBuildNumber.action";

    /**
     * Bamboo 2.3 REST API
     */
    private static final String GET_BUILD_BY_NUMBER_ACTION = "/rest/api/latest/build";

    private static final String BUILD_NUMBER_INFO = "/rest/api/latest/info";

    private static final String LIST_PLANS = "/rest/api/latest/plan?expand=plans&max-results=5000";

    private static final String BUILD_COMPLETED_DATE_ELEM = "buildCompletedDate";

    private static final String BUILD_SUCCESSFUL = "Successful";

    private static final String BUILD_FAILED = "Failed";

    private final ConnectionCfg serverData;

    private static final String CANNOT_PARSE_BUILD_TIME = "Cannot parse buildTime.";
    private static final String INVALID_SERVER_RESPONSE = "Invalid server response";

    /**
     * Public constructor for BambooSessionImpl.
     *
     * @param serverData The server configuration for this session
     * @param callback   The callback needed for preparing HttpClient calls
     * @throws RemoteApiMalformedUrlException malformed url
     */
    public BambooSessionImpl(final ConnectionCfg serverData, final HttpSessionCallback callback, final Logger logger) throws RemoteApiMalformedUrlException {
        super(serverData, callback);
        this.serverData = serverData;
        loger = logger;
    }

    private int getBamboBuildNumberImplNew() throws RemoteApiException {
        try {
            final RestInfo info = new DefaultApi(serverData.getApiClient()).getInfo().get();
            return Integer.parseInt(info.getBuildNumber());
        } catch (final InterruptedException | ExecutionException | ApiException e) {
            throw new RemoteApiException("", e);
        }
    }

    @Override
    @Nonnull
    public List<BambooProject> listProjectNames() throws RemoteApiException {
        final String buildResultUrl = getBaseUrl() + LIST_PROJECT_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

        final List<BambooProject> projects = new ArrayList<>();
        try {
            final Document doc = retrieveGetResponse(buildResultUrl);
            final XPath xpath = XPath.newInstance("/response/project");
            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) xpath.selectNodes(doc);
            if (elements != null) {
                for (final Element element : elements) {
                    final String name = element.getChild("name").getText();
                    final String key = element.getChild("key").getText();
                    projects.add(new BambooProjectInfo(name, key));
                }
            }
        } catch (final JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (final IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }

        return projects;
    }

    @Nonnull
    private List<BambooPlan> listPlanNames() throws RemoteApiException {
        final String buildResultUrl = getBaseUrl() + LIST_PLAN_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

        final List<BambooPlan> plans = new ArrayList<>();
        try {
            final Document doc = retrieveGetResponse(buildResultUrl);
            final XPath xpath = XPath.newInstance("/response/build");
            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) xpath.selectNodes(doc);
            if (elements != null) {
                for (final Element element : elements) {
                    final String enabledValue = element.getAttributeValue("enabled");
                    boolean enabled = true;
                    if (enabledValue != null) {
                        enabled = Boolean.parseBoolean(enabledValue);
                    }
                    final String name = element.getChild("name").getText();
                    final String key = element.getChild("key").getText();
                    final BambooPlan plan = new BambooPlan(name, key, null, enabled);
                    plans.add(plan);
                }
            }
        } catch (final JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (final IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }

        return plans;
    }

    @Nonnull
    private List<BambooPlan> listPlanNames_40() throws RemoteApiException {
        try {
            final RestPlans buildPlans = new BuildApi(serverData.getApiClient()).getAllPlanList("plans", null, 5000).get();
            final List<BambooPlan> plans = new ArrayList<>();

            for (final RestPlan plan : buildPlans.getPlans().getPlan()) {
                plans.add(new BambooPlan(plan.getName(), plan.getPlanKey().getKey(), null, plan.getEnabled()));
            }
            return plans;
        } catch (final InterruptedException | ExecutionException | ApiException e) {
            throw new RemoteApiException("", e);
        }

    }

    /**
     * Returns a {@link me.glindholm.theplugin.commons.bamboo.BambooBuild}
     * information about the latest build in a plan.
     * <p/>
     * Returned structure contains either the information about the build or an
     * error message if the connection fails.
     *
     * @param planKey ID of the plan to get info about
     * @return Information about the last build or error message
     */
    @Override
    @Nonnull
    public BambooBuild getLatestBuildForPlan(@Nonnull final String planKey, final int timezoneOffset) throws RemoteApiException {
        final List<BambooPlan> planList = listPlanNames();
        final Boolean isEnabled = isPlanEnabled(planList, planKey);
        return getLatestBuildForPlan(planKey, isEnabled != null ? isEnabled : true, timezoneOffset);
    }

    @Nullable
    public static Boolean isPlanEnabled(@Nonnull final Collection<BambooPlan> allPlans, @Nonnull final String planKey) {
        for (final BambooPlan bambooPlan : allPlans) {
            if (planKey.equals(bambooPlan.getKey())) {
                return bambooPlan.isEnabled();
            }
        }
        return null;
    }

    @Nonnull
    public BambooBuild getLatestBuildForPlan(@Nonnull final String planKey, final boolean isPlanEnabled, final int timezoneOffset) throws RemoteApiException {
        return getLatestBuildBuilderForPlan(planKey, timezoneOffset).enabled(isPlanEnabled).build();
    }

    @Nonnull
    public BambooBuildInfo.Builder getLatestBuildBuilderForPlan(@Nonnull final String planKey, final int timezoneOffset) throws RemoteApiException {
        final String buildResultUrl = getBaseUrl() + LATEST_BUILD_FOR_PLAN_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken) + "&buildKey="
                + UrlUtil.encodeUrl(planKey);

        try {
            final Document doc = retrieveGetResponse(buildResultUrl);
            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                return constructBuildErrorInfo(planKey, exception, Instant.now());
            }

            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance("/response").selectNodes(doc);
            if (elements != null && !elements.isEmpty()) {
                final Element e = elements.iterator().next();
                final Set<String> commiters = constructBuildCommiters(e);
                return constructBuilderItem(e, Instant.now(), planKey, commiters, timezoneOffset);
            } else {
                return constructBuildErrorInfo(planKey, "Malformed server reply: no response element", Instant.now());
            }
        } catch (final JDOMException e) {
            return constructBuildErrorInfo(planKey, "Server returned malformed response", e, Instant.now());
        } catch (IOException | RemoteApiException e) {
            return constructBuildErrorInfo(planKey, e.getMessage(), e, Instant.now());
        }
    }

    @Nonnull
    public BambooBuildInfo.Builder getLatestBuildBuilderForPlan_40(@Nonnull final String planKey, final int timezoneOffset) throws RemoteApiException {
        // String buildResultUrl =
        // getBaseUrl() + LATEST_BUILD_FOR_PLAN_ACTION + "?auth=" +
        // UrlUtil.encodeUrl(authToken) + "&buildKey="
        // + UrlUtil.encodeUrl(planKey);

        // http://tardigrade.sydney.atlassian.com:8085/bamboo/rest/api/latest/result/STD-XML/15?expand=changes
        // http://tardigrade.sydney.atlassian.com:8085/bamboo/rest/api/latest/result/STD-XML-JOB1/15?expand=changes

        final String buildResultUrl = getBaseUrl() + LATEST_BUILD_FOR_PLAN + UrlUtil.encodeUrl(planKey) + "?expand=" + UrlUtil.encodeUrl("results[0].result");

        BambooBuildInfo.Builder buildData;
        BambooBuildInfo.Builder buildData2;

        try {
            final PlanResults result = new DefaultApi(serverData.getApiClient())
                    .getLatestBuildResultsForProject(planKey, "results[0].result", null, null, null, null, null, null, null, null, null).get();
            final Document doc = retrieveGetResponse(buildResultUrl);
            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                return constructBuildErrorInfo(planKey, exception, Instant.now());
            }

            // final List<Element> elements =
            // XPath.newInstance("/response").selectNodes(doc);
//            if (result.getResults().getResult() != null && !result.getResults().getResult().isEmpty()) {
//                final Integer buildNumber = result.getResults().getResult().get(0).getNumber();
//                final Set<String> commiters = getCommitersForBuild_40(planKey, buildNumber + "");
//                return constructBuilderItem_40(e, Instant.now(), planKey, commiters, timezoneOffset);
//            } else {
//                // plan may have no builds (never built)
//                final Integer size = result.getResults().getSize();
//                if (size == 0) {
//                    return new BambooBuildInfo.Builder(planKey, serverData, BuildStatus.UNKNOWN).pollingTime(Instant.now()).reason("Never built");
//                }
//                return constructBuildErrorInfo(planKey, "Malformed server reply: no response element", Instant.now());
//            }

            List<Element> elements = (List<Element>) XPath.newInstance("/results/results/result").selectNodes(doc);

            if (result.getResults().getResult() != null && !result.getResults().getResult().isEmpty()) {
                final Result build = result.getResults().getResult().get(0);
                final String buildNumber = String.valueOf(build.getBuildNumber());
                final Set<String> commiters = getCommitersForBuild_40(planKey, buildNumber);
                buildData2 = constructBuilderItem_40(build, Instant.now(), planKey, commiters, timezoneOffset);
            } else {
                if (result.getResults().getSize() == 0) {
                    buildData2 = new BambooBuildInfo.Builder(planKey, serverData, BuildStatus.UNKNOWN).pollingTime(Instant.now()).reason("Never built");
                }
            }

            if (elements != null && !elements.isEmpty()) {
                final Element e = elements.iterator().next();
                // final Set<String> commiters = constructBuildCommiters(e);
                final String buildNumber = e.getAttributeValue("number");
                final Set<String> commiters = getCommitersForBuild_40(planKey, buildNumber);
                buildData = constructBuilderItem_40(e, Instant.now(), planKey, commiters, timezoneOffset);
            } else {
                // plan may have no builds (never built)
                elements = (List<Element>) XPath.newInstance("/results/results").selectNodes(doc);
                if (elements != null && !elements.isEmpty()) {
                    final Element e = elements.iterator().next();
                    // final Set<String> commiters = constructBuildCommiters(e);
                    final String size = e.getAttributeValue("size");
                    if (size != null && size.length() > 0 && "0".equals(size)) {
                        buildData = new BambooBuildInfo.Builder(planKey, serverData, BuildStatus.UNKNOWN).pollingTime(Instant.now()).reason("Never built");
                    }
                }

                buildData = constructBuildErrorInfo(planKey, "Malformed server reply: no response element", Instant.now());

            }
        } catch (final JDOMException | IOException | RemoteApiException | InterruptedException | ExecutionException | ApiException e) {
            buildData = constructBuildErrorInfo(planKey, e.getMessage(), e, Instant.now());
        }
        return buildData;
    }

    public Set<String> getCommitersForBuild_40(@Nonnull final String planKey, @Nonnull final String buildNumber) throws RemoteApiException {
        final Set<String> commiters = new HashSet<>();

        try {
            final String[] key = planKey.split("-");
            final Result plan = new DefaultApi(serverData.getApiClient()).getBuild(key[0], key[1], buildNumber, "changes", null).get();
            if (plan.getChanges() != null && plan.getChanges().getChange() != null) {
                for (final RestChange change : plan.getChanges().getChange()) {
                    commiters.add(change.getAuthor());
                }
            }
            return commiters;
        } catch (final InterruptedException | ExecutionException | ApiException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    @Override
    @Nonnull
    public BambooPlan getPlanDetails(@Nonnull final String planKey) throws RemoteApiException {
        final String planUrl = getBaseUrl() + PLAN_STATE + UrlUtil.encodeUrl(planKey);

        try {
            final Document doc = retrieveGetResponse(planUrl);

            final RestPlan restPlan = new BuildApi(serverData.getApiClient())
                    .getPlan("AP-CON", "", "actions,stages,branches,variableContext,stages.stage.plans").get();

            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance("/plan").selectNodes(doc);
            if (elements != null && !elements.isEmpty()) {
                final Element e = elements.iterator().next();
                return constructPlanItem(restPlan, true);
            } else {
                throw new RemoteApiException("Malformed server reply: no 'plan' element");
            }
        } catch (final IOException | JDOMException | InterruptedException | ExecutionException | ApiException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    /**
     * It is new version of {@link #getLatestBuildForPlan(String, boolean, int)}
     * Introduces new plan state 'building' and 'in queue'
     *
     * @param planKey
     * @param timezoneOffset
     * @return
     * @throws RemoteApiException
     */
    @Override
    @Nonnull
    public BambooBuild getLatestBuildForPlanNew(@Nonnull final String planKey, @Nullable final String masterPlanKey, final boolean isPlanEnabled,
            final int timezoneOffset) throws RemoteApiException {

        final String planUrl = getBaseUrl() + PLAN_STATE + UrlUtil.encodeUrl(planKey);

        try {
            final Document doc = retrieveGetResponse(planUrl);

            final RestPlan restPlan = new BuildApi(serverData.getApiClient()).getPlan(planKey, "", null).get();

            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance("/plan").selectNodes(doc);
            if (elements != null && !elements.isEmpty()) {
                final Element e = elements.iterator().next();
                final BambooPlan plan = constructPlanItem(restPlan, isPlanEnabled);

                BambooBuildInfo.Builder latestBuildBuilderForPlan;

                latestBuildBuilderForPlan = getLatestBuildBuilderForPlan_40(planKey, timezoneOffset);
                latestBuildBuilderForPlan.planState(plan.getState());
                latestBuildBuilderForPlan.enabled(isPlanEnabled);
                latestBuildBuilderForPlan.masterPlanKey(masterPlanKey);
                return latestBuildBuilderForPlan.build();

                // TODO we can retrieve comments and labels together with build details
                // below new API call can be made instead of old getBuild method
                // String buildUrl =
                // getBaseUrl() + "/rest/api/latest/build/" + UrlUtil.encodeUrl(planKey) +
                // "/latest"
                // + "?expand=comments.comment,labels";
                // Document d = retrieveGetResponse(buildUrl);

            } else {
                return constructBuildErrorInfo(planKey, "Malformed server reply: no 'plan' element", Instant.now()).build();
            }
        } catch (final JDOMException | IOException | InterruptedException | ExecutionException | ApiException e) {
            return constructBuildErrorInfo(planKey, e.getMessage(), e, Instant.now()).build();
        }
    }

    @Override
    @Nonnull
    public BambooBuild getBuildForPlanAndNumber(@Nonnull final String planKey, final int buildNumber, final int timezoneOffset) throws RemoteApiException {

        // try recent build first, as this API is availablke in older Bamboos also
        final Collection<BambooBuild> recentBuilds = getRecentBuildsForPlan(planKey, timezoneOffset);
        try {
            for (final BambooBuild recentBuild : recentBuilds) {
                if (recentBuild.getNumber() == buildNumber) {
                    return recentBuild;
                }
            }
        } catch (final UnsupportedOperationException e) {
            // oh well, it can actually happen for disabled builds. Let's just gobble this
        }

        String buildResultUrl;
        String nodePath;
        buildResultUrl = getBaseUrl() + GET_BUILD_DETAILS + UrlUtil.encodeUrl(planKey) + "-" + buildNumber;
        nodePath = "/result";

        try {
            final Document doc = retrieveGetResponse(buildResultUrl);
            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                return constructBuildErrorInfo(buildResultUrl, exception, Instant.now()).build();
            }

            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance(nodePath).selectNodes(doc);
            final Element el = elements.get(0);
            return constructBuildItemFromNewApi(el, Instant.now(), planKey);

        } catch (IOException | JDOMException e) {
            throw new RemoteApiException(e);
        }
    }

    @Override
    public Collection<BambooBuild> getRecentBuildsForPlan(@Nonnull final String planKey, final int timezoneOffset) throws RemoteApiException {
        return getBuildsCollection_40(planKey, timezoneOffset);
    }

    @Override
    public Collection<BambooBuild> getRecentBuildsForUser(final int timezoneOffset) throws RemoteApiException {
        final String buildResultUrl = getBaseUrl() + RECENT_BUILDS_FOR_USER_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken) + "&username="
                + UrlUtil.encodeUrl(getUsername());
        return getBuildsCollection(buildResultUrl, getUsername(), timezoneOffset);
    }

    private Collection<BambooBuild> getBuildsCollection(@Nonnull final String url, @Nonnull final String planKey, final int timezoneOffset)
            throws RemoteApiException {

        final Instant pollingTime = Instant.now();
        final List<BambooBuild> builds = new ArrayList<>();
        try {
            final Document doc = retrieveGetResponse(url);
            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                builds.add(constructBuildErrorInfo(url, exception, Instant.now()).build());
                return builds;
            }

            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance("/response/build").selectNodes(doc);
            if (elements == null || elements.isEmpty()) {
                builds.add(constructBuildErrorInfo(url, "Malformed server reply: no response element", Instant.now()).build());
            } else {
                for (final Element element : elements) {
                    final Set<String> commiters = constructBuildCommiters(element);
                    builds.add(constructBuilderItem(element, pollingTime, planKey, commiters, timezoneOffset).enabled(true).build());
                }
            }
        } catch (final JDOMException e) {
            builds.add(constructBuildErrorInfo(planKey, "Server returned malformed response", e, pollingTime).build());
        } catch (IOException | RemoteApiException e) {
            builds.add(constructBuildErrorInfo(planKey, e.getMessage(), e, pollingTime).build());
        }
        return builds;
    }

    private Collection<BambooBuild> getBuildsCollection_40(@Nonnull final String planKey, final int timezoneOffset) throws RemoteApiException {

        final String url = getBaseUrl() + LATEST_BUILD_FOR_PLAN + UrlUtil.encodeUrl(planKey) + "?expand=" + UrlUtil.encodeUrl("results[0:10].result");

        final Instant pollingTime = Instant.now();
        final List<BambooBuild> builds = new ArrayList<>();

        try {
            final Document doc = retrieveGetResponse(url);

            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                builds.add(constructBuildErrorInfo(url, exception, Instant.now()).build());
                return builds;
            }

            final List<Element> elements = (List<Element>) XPath.newInstance("/results/results/result").selectNodes(doc);
            if (elements == null || elements.isEmpty()) {
                builds.add(constructBuildErrorInfo(url, "Malformed server reply: no response element", Instant.now()).build());
            } else {
                for (final Element element : elements) {
                    final Set<String> commiters = constructBuildCommiters(element);
                    builds.add(constructBuilderItem_40(element, pollingTime, planKey, commiters, timezoneOffset).enabled(true).build());
                }
            }
        } catch (final JDOMException e) {
            builds.add(constructBuildErrorInfo(planKey, "Server returned malformed response", e, pollingTime).build());
        } catch (IOException | RemoteApiException e) {
            builds.add(constructBuildErrorInfo(planKey, e.getMessage(), e, pollingTime).build());
        }
        return builds;
    }

    private Set<String> constructBuildCommiters(final Element element) throws JDOMException {

        final Set<String> commiters = new HashSet<>();
        @SuppressWarnings("unchecked")
        final List<Element> commitElements = (List<Element>) XPath.newInstance("commits/commit").selectNodes(element);
        if (!commitElements.isEmpty()) {
            for (final Element commiter : commitElements) {
                commiters.add(commiter.getAttributeValue("author"));
            }
        }
        return commiters;
    }

    @Override
    @Nonnull
    public List<String> getFavouriteUserPlans() throws RemoteApiException {
        final List<String> builds = new ArrayList<>();
        final String buildResultUrl = getBaseUrl() + LATEST_USER_BUILDS_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

        try {
            final Document doc = retrieveGetResponse(buildResultUrl);
            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                return builds;
            }

            final XPath xpath = XPath.newInstance("/response/build");
            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) xpath.selectNodes(doc);
            if (elements != null) {
                for (final Element element : elements) {
                    builds.add(element.getChildText("key"));
                }
                return builds;
            } else {
                return builds;
            }
        } catch (IOException | JDOMException e) {
            return builds;
        }
    }

    @Nonnull
    public List<String> getFavouriteUserPlansNew() throws RemoteApiException {
        final List<String> builds = new ArrayList<>();
        try {
            final RestPlans plans = new BuildApi(serverData.getApiClient()).getAllPlanList("plans", null, 5000).get();
            for (final RestPlan plan : plans.getPlans().getPlan()) {
                builds.add(plan.getKey());
            }
        } catch (InterruptedException | ExecutionException | ApiException e) {
        }
        return builds;
    }

    @Nonnull
    private BuildDetails getBuildResultDetailsOld(@Nonnull final String planKey, final int buildNumber) throws RemoteApiException {
        final String buildResultUrl = getBaseUrl() + GET_BUILD_DETAILS_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken) + "&buildKey="
                + UrlUtil.encodeUrl(planKey) + "&buildNumber=" + buildNumber;

        try {
            final BuildDetailsInfo build = new BuildDetailsInfo();
            final Document doc = retrieveGetResponse(buildResultUrl);
            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }

            @SuppressWarnings("unchecked")
            final List<Element> responseElements = (List<Element>) XPath.newInstance("/response").selectNodes(doc);
            for (final Element element : responseElements) {
                final String vcsRevisionKey = element.getAttributeValue("vcsRevisionKey");
                if (vcsRevisionKey != null) {
                    build.setVcsRevisionKey(vcsRevisionKey);
                }
            }

            @SuppressWarnings("unchecked")
            final List<Element> commitElements = (List<Element>) XPath.newInstance("/response/commits/commit").selectNodes(doc);
            if (!commitElements.isEmpty()) {
                int i = 1;
                for (final Element element : commitElements) {
                    final BambooChangeSetImpl cInfo = new BambooChangeSetImpl();
                    cInfo.setAuthor(element.getAttributeValue("author"));
                    cInfo.setCommitDate(parseCommitTime(element.getAttributeValue("date")));
                    cInfo.setComment(getChildText(element, "comment"));

                    final String path = "/response/commits/commit[" + i++ + "]/files/file";
                    final XPath filesPath = XPath.newInstance(path);
                    @SuppressWarnings("unchecked")
                    final List<Element> fileElements = (List<Element>) filesPath.selectNodes(doc);
                    for (final Element file : fileElements) {
                        final BambooFileInfo fileInfo = new BambooFileInfo(file.getAttributeValue("name"), file.getAttributeValue("revision"));
                        cInfo.addCommitFile(fileInfo);
                    }
                    build.addCommitInfo(cInfo);
                }
            }

            @SuppressWarnings("unchecked")
            final List<Element> sucTestResElements = (List<Element>) XPath.newInstance("/response/successfulTests/testResult").selectNodes(doc);
            for (final Element element : sucTestResElements) {
                final TestDetailsInfo tInfo = new TestDetailsInfo();
                tInfo.setTestClassName(element.getAttributeValue("testClass"));
                tInfo.setTestMethodName(element.getAttributeValue("testMethod"));
                double duration;
                try {
                    duration = Double.parseDouble(element.getAttributeValue("duration"));
                } catch (final NumberFormatException e) {
                    // leave 0
                    duration = 0;
                }
                tInfo.setTestDuration(duration);
                tInfo.setTestResult(TestResult.TEST_SUCCEED);
                build.addSuccessfulTest(tInfo);
            }

            @SuppressWarnings("unchecked")
            final List<Element> failedTestResElements = (List<Element>) XPath.newInstance("/response/failedTests/testResult").selectNodes(doc);
            if (!failedTestResElements.isEmpty()) {
                int i = 1;
                for (final Element element : failedTestResElements) {
                    final TestDetailsInfo tInfo = new TestDetailsInfo();
                    tInfo.setTestClassName(element.getAttributeValue("testClass"));
                    tInfo.setTestMethodName(element.getAttributeValue("testMethod"));
                    double duration;
                    try {
                        duration = Double.parseDouble(element.getAttributeValue("duration"));
                    } catch (final NumberFormatException e) {
                        // leave 0
                        duration = 0;
                    }
                    tInfo.setTestDuration(duration);
                    tInfo.setTestResult(TestResult.TEST_FAILED);

                    final String path = "/response/failedTests/testResult[" + i++ + "]/errors/error";
                    final XPath errorPath = XPath.newInstance(path);
                    @SuppressWarnings("unchecked")
                    final List<Element> errorElements = (List<Element>) errorPath.selectNodes(doc);
                    for (final Element error : errorElements) {
                        tInfo.setTestErrors(error.getText());
                    }
                    build.addFailedTest(tInfo);
                }
            }

            return build;
        } catch (final JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (final IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    /**
     * includes cached build number (version) of Bamboo given session connects too
     * Currently we do not support clearing it, so the restart will be required to
     * use new API - this limitation is something we can definitely live with
     */
    private Integer serverBuildNumber;

    /**
     * Returns possible cached version information (build/compilation number) of the
     * server
     *
     * @return
     * @throws RemoteApiException
     */
    @Override
    public int getBamboBuildNumber() throws RemoteApiException {
        if (serverBuildNumber != null) {
            return serverBuildNumber;
        }
        serverBuildNumber = getBamboBuildNumberImplNew();
        return serverBuildNumber;
    }

    @Override
    @Nonnull
    public BuildDetails getBuildResultDetails(@Nonnull final String planKey, final int buildNumber) throws RemoteApiException {

        // tests are available for separate jobs since Bamboo v 2.7
        final List<BambooJobImpl> jobs = getJobsForPlan(planKey);

        final BuildDetailsInfo build = new BuildDetailsInfo();

        for (final BambooJobImpl job : jobs) { // job key contains project key

            build.addJob(job);

            if (!job.isEnabled()) {
                // there are no details for disabled jobs
                continue;
            }

            final String url = new StringBuilder().append(getBaseUrl()).append(GET_BUILD_DETAILS).append(job.getKey()).append("-").append(buildNumber)
                    .append("?").append("expand=testResults.all.testResult.errors").toString();
            try {
                final String[] id = job.getKey().split("-");
                final Result testData = new DefaultApi(serverData.getApiClient())
                        .getBuild(id[0], id[1], id[2] + "-" + buildNumber, "testResults.allTests.testResult.errors", null).get();
                for (final me.glindholm.bamboo.model.TestResult data : testData.getTestResults().getAllTests().getTestResult()) {
                    final TestDetailsInfo tInfo = new TestDetailsInfo();

                    tInfo.setTestClassName(data.getClassName());
                    tInfo.setTestMethodName(data.getMethodName());
                    final String status = data.getStatus();
                    try {
                        tInfo.setTestResult(parseTestResult(status));
                    } catch (final ParseException e1) {
                        loger.warn("Cannot parse test result element:" + status, e1);
                        continue;
                    }

                    final Integer duration = data.getDuration();
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
                if (testData.getChanges().getChange() != null) {
                    build.setCommitInfo(parseChangeSets(testData.getChanges()));
                }

//                final Document doc = retrieveGetResponse(url);
//                final String exception = getExceptionMessages(doc);
//                if (null != exception) {
//                    throw new RemoteApiException(exception);
//                }
//
//                @SuppressWarnings("unchecked")
//                final List<Element> testResElements = (List<Element>) XPath.newInstance("/result/testResults/all/testResult").selectNodes(doc);
//                for (final Element element : testResElements) {
//                    final TestDetailsInfo tInfo = new TestDetailsInfo();
//                    tInfo.setTestClassName(element.getAttributeValue("className"));
//                    tInfo.setTestMethodName(element.getAttributeValue("methodName"));
//                    try {
//                        tInfo.setTestResult(parseTestResult(element.getAttributeValue("status")));
//                    } catch (final ParseException e1) {
//                        loger.warn("Cannot parse test result element:\n" + XmlUtil.toPrettyFormatedString(element), e1);
//                        continue;
//                    }
//                    tInfo.setTestDuration(parseDuration(element.getChild("duration")));
//
//                    final StringBuilder errorBuilder = new StringBuilder();
//                    final XPath errorPath = XPath.newInstance("errors/error");
//                    @SuppressWarnings("unchecked")
//                    final List<Element> errorElements = (List<Element>) errorPath.selectNodes(element);
//                    for (final Element errorElement : errorElements) {
//                        final String errorEntry = errorElement.getChildText("message");
//                        if (errorEntry != null) {
//                            errorBuilder.append(errorEntry).append('\n');
//                        }
//                    }
//                    tInfo.setTestErrors(errorBuilder.toString());
//
//                    switch (tInfo.getTestResult()) {
//                    case TEST_FAILED:
//                        build.addFailedTest(tInfo); // TODO: remove when IntelliJ is ready with the jobs
//                        job.addFailedTest(tInfo);
//                        break;
//                    case TEST_SUCCEED:
//                        build.addSuccessfulTest(tInfo); // TODO: remove when IntelliJ is ready with the jobs
//                        job.addSuccessfulTest(tInfo);
//                        break;
//                    default:
//                        break;
//                    }
//                }
//                final Element changesElement = doc.getRootElement().getChild("changes");
//                if (changesElement != null) {
//                    build.setCommitInfo(parseChangeSets(changesElement));
//                }
                // return build;
            } catch (final InterruptedException | ExecutionException | ApiException e) {
                throw new RemoteApiException(e.getMessage(), e);
            }
        }

        return build;
    }

    @Nonnull
    BuildDetails getBuildResultDetailsMoreRestish(@Nonnull final String planKey, final int buildNumber) throws RemoteApiException {

        final String buildResultUrl = getBaseUrl() + GET_BUILD_BY_NUMBER_ACTION + "/" + UrlUtil.encodeUrl(planKey) + "/" + buildNumber + "?auth="
                + UrlUtil.encodeUrl(authToken) + "&expand=testResults.all.testResult.errors&expand=changes.change.files";

        try {
            final BuildDetailsInfo build = new BuildDetailsInfo();
            final Document doc = retrieveGetResponse(buildResultUrl);
            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }

            @SuppressWarnings("unchecked")
            final List<Element> testResElements = (List<Element>) XPath.newInstance("/build/testResults/all/testResult").selectNodes(doc);
            for (final Element element : testResElements) {
                final TestDetailsInfo tInfo = new TestDetailsInfo();
                tInfo.setTestClassName(element.getAttributeValue("className"));
                tInfo.setTestMethodName(element.getAttributeValue("methodName"));
                try {
                    tInfo.setTestResult(parseTestResult(element.getAttributeValue("status")));
                } catch (final ParseException e1) {
                    loger.warn("Cannot parse test result element:\n" + XmlUtil.toPrettyFormatedString(element), e1);
                    continue;
                }
                tInfo.setTestDuration(parseDuration(element.getChild("duration")));

                final StringBuilder errorBuilder = new StringBuilder();
                final XPath errorPath = XPath.newInstance("errors/error");
                @SuppressWarnings("unchecked")
                final List<Element> errorElements = (List<Element>) errorPath.selectNodes(element);
                for (final Element errorElement : errorElements) {
                    final String errorEntry = errorElement.getChildText("message");
                    if (errorEntry != null) {
                        errorBuilder.append(errorEntry).append('\n');
                    }
                }
                tInfo.setTestErrors(errorBuilder.toString());

                switch (tInfo.getTestResult()) {
                case TEST_FAILED:
                    build.addFailedTest(tInfo);
                    break;
                case TEST_SUCCEED:
                    build.addSuccessfulTest(tInfo);
                    break;
                default:
                    break;
                }
            }
            final Element changesElement = doc.getRootElement().getChild("changes");
            if (changesElement != null) {
                build.setCommitInfo(parseChangeSets(changesElement));
            }
            return build;
        } catch (final IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException | ParseException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    private List<BambooChangeSet> parseChangeSets(final RestChangeList changes) {
        final List<BambooChangeSet> changeSets = new ArrayList<>();
        for (final RestChange change : changes.getChange()) {
            final BambooChangeSetImpl cInfo = new BambooChangeSetImpl();
            cInfo.setAuthor(change.getAuthor());
            cInfo.setCommitDate(change.getDate().toInstant());
            cInfo.setComment(change.getComment());
            if (change.getChangeFiles() != null) {
                for (final RestChangeFile file : change.getChangeFiles().getChangeFiles()) {
                    final BambooFileInfo fileInfo = new BambooFileInfo(file.getName(), file.getRevision());
                    cInfo.addCommitFile(fileInfo);
                }
            }
            changeSets.add(cInfo);
        }

        return changeSets;
    }

    List<BambooChangeSet> parseChangeSets(final Element changesElement) throws RemoteApiException {
        final List<BambooChangeSet> changeSets = MiscUtil.buildArrayList();
        final List<Element> changeElements = XmlUtil.getChildElements(changesElement, "change");
        for (final Element changeElement : changeElements) {
            final BambooChangeSetImpl cInfo = new BambooChangeSetImpl();
            cInfo.setAuthor(changeElement.getAttributeValue("author"));
            final String dateStr = changeElement.getChildText("date");
            if (dateStr == null) {
                throw new RemoteApiException("change element does not have mandatory date element");
            }
            cInfo.setCommitDate(parseNewApiBuildTime(dateStr));
            cInfo.setComment(getChildText(changeElement, "comment"));
            final Element filesElement = changeElement.getChild("files");
            if (filesElement != null) {
                final List<Element> fileElements = XmlUtil.getChildElements(filesElement, "file");
                for (final Element fileElement : fileElements) {
                    final BambooFileInfo fileInfo = new BambooFileInfo(getChildText(fileElement, "name").trim(), getChildText(fileElement, "revision").trim());
                    cInfo.addCommitFile(fileInfo);

                }
            }
            changeSets.add(cInfo);
        }
        return changeSets;
    }

    private double parseDuration(final Element durationElement) throws ParseException {
        if (durationElement == null) {
            throw new ParseException("null duration element", 0);
        }

        final String durationStr = durationElement.getText();

        try {
            return Double.valueOf(durationStr) / 1000;
        } catch (final NumberFormatException e) {
            throw new ParseException("Cannot parse duration element as floating point number [" + durationStr + "]", 0);
        }
    }

    private TestResult parseTestResult(final String attributeValue) throws ParseException {
        if ("failed".equals(attributeValue)) {
            return TestResult.TEST_FAILED;
        } else if ("successful".equals(attributeValue)) {
            return TestResult.TEST_SUCCEED;
        }
        throw new ParseException("Invalid test result [" + attributeValue + "]", 0);
    }

    /**
     * Currently length of the comment is limited by poor implementation which uses
     * GET HTTP method (sic!) to post a new comment and the comment becomes part of
     * URL, which is typically truncated by web servers.
     */
    @Override
    public void addLabelToBuild(@Nonnull final String planKey, final int buildNumber, final String buildLabel) throws RemoteApiException {
        final String buildResultUrl = getBaseUrl() + ADD_LABEL_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken) + "&buildKey=" + UrlUtil.encodeUrl(planKey)
                + "&buildNumber=" + buildNumber + "&label=" + UrlUtil.encodeUrl(buildLabel);

        try {
            final Document doc = retrieveGetResponse(buildResultUrl);
            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }
        } catch (final JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (final IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    /**
     * Currently length of the comment is limited by poor implementation which uses
     * GET HTTP method (sic!) to post a new comment and the comment becomes part of
     * URL, which is typically truncated by web servers.
     */
    @Override
    public void addCommentToBuild(@Nonnull final String planKey, final int buildNumber, final String buildComment) throws RemoteApiException {
        final String buildResultUrl = getBaseUrl() + ADD_COMMENT_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken) + "&buildKey=" + UrlUtil.encodeUrl(planKey)
                + "&buildNumber=" + buildNumber + "&content=" + UrlUtil.encodeUrl(buildComment);

        try {
            final Document doc = retrieveGetResponse(buildResultUrl);
            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }
        } catch (final JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (final IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    @Override
    public void executeBuild(@Nonnull final String planKey) throws RemoteApiException {

        executeBuildNewApi(planKey);
    }

    private void executeBuildNewApi(final String planKey) throws RemoteApiException {
        final String url = getBaseUrl() + BUILD_QUEUE_SERVICE + UrlUtil.encodeUrl(planKey);

        try {
            retrievePostResponse(url, "", false);
        } catch (final JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    BambooBuildInfo.Builder constructBuildErrorInfo(final String planKey, final String message, final Instant instant) {
        return new BambooBuildInfo.Builder(planKey, null, serverData, null, null, BuildStatus.UNKNOWN).pollingTime(instant).errorMessage(message);
    }

    BambooBuildInfo.Builder constructBuildErrorInfo(final String planKey, final String message, final Throwable exception, final Instant lastPollingTime) {
        return new BambooBuildInfo.Builder(planKey, null, serverData, null, null, BuildStatus.UNKNOWN).pollingTime(lastPollingTime).errorMessage(message,
                exception);
    }

    private int parseInt(final String number) throws RemoteApiException {
        try {
            return Integer.parseInt(number);
        } catch (final NumberFormatException ex) {
            throw new RemoteApiException("Invalid number", ex);
        }
    }

    private BambooPlan constructPlanItem(final RestPlan restPlan, final boolean isEnabledDefault) throws RemoteApiException {
        final String name = restPlan.getName();
        final String key = restPlan.getKey();

        final String projectName = restPlan.getProjectName();
        final String projectKey = restPlan.getProjectKey();

        // todo do not break parsing if single value is broken
        final boolean isFavourite = restPlan.getIsFavourite();

        final Integer averageBuildTime = restPlan.getAverageBuildTimeInSeconds().intValue();
        final boolean isInQueue = false; // Boolean.parseBoolean(planNode.getChildText("isInBuildQueue"));

        final boolean isBuilding = restPlan.getIsBuilding();

        final boolean isEnabled = restPlan.getEnabled();

        return new BambooPlan(name, key, null, isEnabled, isFavourite, projectName, projectKey, averageBuildTime, isInQueue, isBuilding);
    }

    private BambooBuildInfo.Builder constructBuilderItem(final Element buildItemNode, final Instant instant, final String aPlanKey, final Set<String> commiters,
            final int timezoneOffset) throws RemoteApiException {

        BambooBuildInfo.Builder builder;
        // for never executed build we actually have no data here (no children)
        if (!buildItemNode.getChildren().iterator().hasNext()) {
            builder = new BambooBuildInfo.Builder(aPlanKey, serverData, BuildStatus.UNKNOWN).pollingTime(instant).reason("Never built");
        } else {

            final String planKey = getChildText(buildItemNode, "buildKey");
            final String buildName = getChildText(buildItemNode, "buildName");
            final String projectName = getChildText(buildItemNode, "projectName");
            final int buildNumber = parseInt(getChildText(buildItemNode, "buildNumber"));
            final String relativeBuildDate = getChildText(buildItemNode, "buildRelativeBuildDate");
            final Instant startTime = parseBuildDate(getChildText(buildItemNode, "buildTime"), CANNOT_PARSE_BUILD_TIME, timezoneOffset);
            final String buildCompletedDateStr = getChildText(buildItemNode, BUILD_COMPLETED_DATE_ELEM);
            final Instant completionTime = buildCompletedDateStr != null && buildCompletedDateStr.length() > 0
                    ? parseDateUniversal(buildCompletedDateStr, BUILD_COMPLETED_DATE_ELEM, timezoneOffset)
                    // older Bamboo versions do not generate buildCompletedDate so we set it as
                    // buildTime
                    : startTime;
            final String durationDescription = getChildText(buildItemNode, "buildDurationDescription");

            final String stateStr = getChildText(buildItemNode, "buildState");
            builder = new BambooBuildInfo.Builder(planKey, buildName, serverData, projectName, buildNumber, getStatus(stateStr)).pollingTime(instant)
                    .reason(getChildText(buildItemNode, "buildReason")).startTime(startTime).testSummary(getChildText(buildItemNode, "buildTestSummary"))
                    .commitComment(getChildText(buildItemNode, "buildCommitComment"))
                    .testsPassedCount(parseInt(getChildText(buildItemNode, "successfulTestCount")))
                    .testsFailedCount(parseInt(getChildText(buildItemNode, "failedTestCount"))).completionTime(completionTime)
                    .relativeBuildDate(relativeBuildDate).durationDescription(durationDescription).commiters(commiters);
        }
        return builder;
    }

    private Builder constructBuilderItem_40(final Result build, final Instant instant, final String aPlanKey, final Set<String> commiters,
            final int timezoneOffset) {
        BambooBuildInfo.Builder builder = null;
        // for never executed build we actually have no data here (no children)
        if (build.getBuildStartedTime() == null) {
            builder = new BambooBuildInfo.Builder(aPlanKey, serverData, BuildStatus.UNKNOWN).pollingTime(instant).reason("Never built");
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
            builder = new BambooBuildInfo.Builder(planKey, buildName, serverData, projectName, buildNumber, getStatus(stateStr)).pollingTime(instant)
                    .reason(getBuildReason_40(build.getBuildReason())).startTime(startTime).testSummary(build.getBuildTestSummary()).commitComment(buildComment)
                    .testsPassedCount(build.getSuccessfulTestCount()).testsFailedCount(build.getFailedTestCount()).completionTime(completionTime)
                    .relativeBuildDate(relativeBuildDate).durationDescription(durationDescription).commiters(commiters);
        }
        return builder;
    }

    private BambooBuildInfo.Builder constructBuilderItem_40(final Element buildItemNode, final Instant instant, final String aPlanKey,
            final Set<String> commiters, final int timezoneOffset) throws RemoteApiException {

        BambooBuildInfo.Builder builder;
        // for never executed build we actually have no data here (no children)
        if (!buildItemNode.getChildren().iterator().hasNext()) {
            builder = new BambooBuildInfo.Builder(aPlanKey, serverData, BuildStatus.UNKNOWN).pollingTime(instant).reason("Never built");
        } else {

            final String planKey = aPlanKey;
            final String buildName = getChildText(buildItemNode, "planName");
            final String projectName = getChildText(buildItemNode, "projectName");
            final int buildNumber = parseInt(buildItemNode.getAttributeValue("number"));
            final String relativeBuildDate = getChildText(buildItemNode, "buildRelativeTime");
            final Instant startTime = parseNewApiBuildTime(getChildText(buildItemNode, "buildStartedTime"));
            final Instant completionTime = parseNewApiBuildTime(getChildText(buildItemNode, "buildCompletedTime"));

            final String durationDescription = getChildText(buildItemNode, "buildDurationDescription");

            final String stateStr = buildItemNode.getAttributeValue("state");

            final String commitComment = getChildText(buildItemNode, "buildCommitComment");
            builder = new BambooBuildInfo.Builder(planKey, buildName, serverData, projectName, buildNumber, getStatus(stateStr)).pollingTime(instant)
                    .reason(getBuildReason_40(getChildText(buildItemNode, "buildReason"))).startTime(startTime)
                    .testSummary(getChildText(buildItemNode, "buildTestSummary")).commitComment(commitComment)
                    .testsPassedCount(parseInt(getChildText(buildItemNode, "successfulTestCount")))
                    .testsFailedCount(parseInt(getChildText(buildItemNode, "failedTestCount"))).completionTime(completionTime)
                    .relativeBuildDate(relativeBuildDate).durationDescription(durationDescription).commiters(commiters);
        }
        return builder;
    }

    private String getBuildReason_40(final String reasonOriginal) {
        // Pattern pattern = Pattern.compile("<a([^>]+)>(.+?)</a>");
        final Pattern pattern = Pattern.compile("(.*)<a([^>]+)>(.+)</a>");

        final Matcher m = pattern.matcher(reasonOriginal);
        if (m.find()) {
            return StringEscapeUtils.unescapeHtml(m.group(1) + m.group(3));
            // return m.group(1) + m.group(3);
        }

        return reasonOriginal;
    }

    private BambooBuild constructBuildItemFromNewApi(final Element el, final Instant instant, final String planKey) throws RemoteApiException {

        final BambooPlan plan = getPlanDetails(planKey);

        final BambooBuildInfo.Builder builder = new BambooBuildInfo.Builder(planKey, plan.getName(), serverData, plan.getProjectName(),
                parseInt(el.getAttributeValue("number")), getStatus(el.getAttributeValue("state")));

        builder.testsFailedCount(parseInt(getChildText(el, "failedTestCount")));
        builder.testsPassedCount(parseInt(getChildText(el, "successfulTestCount")));
        builder.startTime(parseNewApiBuildTime(getChildText(el, "buildStartedTime")));
        builder.completionTime(parseNewApiBuildTime(getChildText(el, "buildCompletedTime")));
        builder.durationDescription(getChildText(el, "buildDurationDescription"));
        builder.reason(getChildText(el, "buildReason"));
        builder.pollingTime(instant);
        builder.planState(plan.getState());

        return builder.build();
    }

    @Nonnull
    private BuildStatus getStatus(@Nullable final String stateStr) {
        if (BUILD_SUCCESSFUL.equalsIgnoreCase(stateStr)) {
            return BuildStatus.SUCCESS;
        } else if (BUILD_FAILED.equalsIgnoreCase(stateStr)) {
            return BuildStatus.FAILURE;
        } else {
            return BuildStatus.UNKNOWN;
        }
    }

    private static Instant parseDateUniversal(@Nullable final String dateStr, @Nonnull final String element, final int timezoneOffset)
            throws RemoteApiException {
        if (dateStr != null) {
            if (dateStr.indexOf('T') != -1) {
                // new format
                return parseCommitTime(dateStr);
            } else {
                // old format
                return parseBuildDate(dateStr, "Cannot parse " + element, timezoneOffset);
            }
        }
        throw new RemoteApiException(element + " cannot be found");
    }

    private static final DateTimeFormatter buildDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter commitDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private static final DateTimeFormatter newApiDateFormat = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Parses date without timezone info
     * <p/>
     * wseliga: I have no idea why this method silently returns null in case of
     * parsing problem. For now, I am going to leave it as it is to avoid hell of
     * the problems, should it be really necessary (and I am now a few days before
     * 2.0.0 final release)
     *
     * @param date         string to parse
     * @param errorMessage message used during logging
     * @return parsed date
     */
    @Nullable
    private static Instant parseBuildDate(final String date, final String errorMessage, final int timezoneOffset) {
        try {
            // now adjust the time for local caller time, as Bamboo servers always serves
            // its local time
            // without the timezone info
            return LocalDateTime.parse(date, buildDateFormat).atZone(ZoneId.systemDefault()).plusHours(timezoneOffset).toInstant();
        } catch (final IllegalArgumentException e) {
            LoggerImpl.getInstance().debug("Cannot parse build date: " + errorMessage);
            return null;
        }
    }

    private static Instant parseCommitTime(final String date) throws RemoteApiException {
        try {
            return OffsetDateTime.parse(date, commitDateFormat).toInstant();
        } catch (final IllegalArgumentException e) {
            throw new RemoteApiException("Cannot parse date/time string [" + date + "]", e);
        }
    }

    private static Instant parseNewApiBuildTime(final String dateTime) throws RemoteApiException {
        try {
            return OffsetDateTime.parse(dateTime, newApiDateFormat).toInstant();
        } catch (final IllegalArgumentException e) {
            throw new RemoteApiException("Cannot parse date/time string [" + dateTime + "]", e);
        }
    }

    private static String getChildText(final Element node, final String childName) {
        final Element child = node.getChild(childName);
        return child == null ? "" : child.getText();
    }

    @Override
    public String getBuildLogs(@Nonnull final String planKey, final int buildNumber) throws RemoteApiException {

        String buildResultUrl = null;

        // log is available for separate jobs since Bamboo v 2.7 (build number not known
        // yet)

        final List<BambooJobImpl> jobs = getJobsForPlan(planKey);

        if (jobs.size() > 1) {
            throw new RemoteApiException("Logs are only available for Plans with a single Job.");
        }

        if (jobs.size() == 1 && jobs.get(0).isEnabled()) {

            final String jobKey = jobs.get(0).getKey(); // job key contains project key

            buildResultUrl = new StringBuilder().append(getBaseUrl()).append("/download/").append(jobKey).append("/build_logs/").append(jobKey).append("-")
                    .append(buildNumber).append(".log").toString();

        }

        if (buildResultUrl != null && buildResultUrl.length() > 0) {
            try {
                return doUnconditionalGetForTextNonXmlResource(buildResultUrl);
            } catch (final IOException e) {
                throw new RemoteApiException(e.getMessage(), e);
            }
        }

        return null;
    }

    @Override
    @Nonnull
    public Collection<String> getBranchKeys(final String planKey, final boolean useFavourites, final boolean myBranchesOnly) throws RemoteApiException {
        final List<String> branches = new ArrayList();
        final String my = myBranchesOnly ? "&my" : "";
        try {
            final RestPlan plans = new BuildApi(serverData.getApiClient()).getPlan(planKey, "", "branches.branch" + my).get();
            for (final RestPlanBranch branch : plans.getBranches().getBranch()) {
                if (!useFavourites && !branch.getIsFavourite()) {
                    branches.add(branch.getKey());
                }
            }
        } catch (InterruptedException | ExecutionException | ApiException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
        return branches;
    }

    @Override
    public List<BambooJobImpl> getJobsForPlan(final String planKey) throws RemoteApiException {

        final List<BambooJobImpl> jobs = new ArrayList<>();
        try {
            final String[] id = planKey.split("-");
            final RestPlan plan = new BuildApi(serverData.getApiClient()).getPlan(id[0], id[1], "stages.stage.plans").get();

            for (final RestStage stage : plan.getStages().getStage()) {
                for (final RestPlanConfig plan2 : stage.getPlans().getPlan()) {
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

    @Override
    public Collection<BuildIssue> getIssuesForBuild(@Nonnull final String planKey, final int buildNumber) throws RemoteApiException {
        final int bambooBuild = getBamboBuildNumber();
        if (bambooBuild < BambooServerVersionNumberConstants.BAMBOO_1401_BUILD_NUMBER) {
            throw new RemoteApiBadServerVersionException("Bamboo build 1401 or newer required");
        }

        final String planUrl = getBaseUrl() + GET_BUILD_DETAILS + UrlUtil.encodeUrl(planKey + "-" + buildNumber) + GET_ISSUES_SUFFIX + "&auth="
                + UrlUtil.encodeUrl(authToken);

        try {
            final Document doc = retrieveGetResponse(planUrl);

            final List<BuildIssue> issues = new ArrayList<>();
            @SuppressWarnings("unchecked")
            final List<Element> jiraIssuesNode = (List<Element>) XPath.newInstance("result/jiraIssues").selectNodes(doc);

            if (jiraIssuesNode == null) {
                throw new RemoteApiException(INVALID_SERVER_RESPONSE);
            }
            if (jiraIssuesNode.size() != 1) {
                throw new RemoteApiException(INVALID_SERVER_RESPONSE);
            }
            @SuppressWarnings("unchecked")
            final List<Element> issuesNodes = (List<Element>) XPath.newInstance("issue").selectNodes(jiraIssuesNode.get(0));
            if (issuesNodes == null) {
                throw new RemoteApiException(INVALID_SERVER_RESPONSE);
            }
            for (final Element element : issuesNodes) {
                final Element url = element.getChild("url");
                if (url == null) {
                    LoggerImpl.getInstance().error("getIssuesForBuild: \"url\" node of the \"issue\" element is null");
                    continue;
                }
                final BuildIssue issue = new BuildIssueInfo(element.getAttributeValue("key"), url.getAttributeValue("href"));
                issues.add(issue);
            }
            return issues;
        } catch (JDOMException | IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    @Override
    @Nonnull
    public Collection<BambooPlan> getPlanList() throws RemoteApiException {

        List<BambooPlan> plans;

        plans = listPlanNames_40();

        try {
            List<String> favPlans;
            favPlans = getFavouriteUserPlansNew();
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

    @Override
    @Nonnull
    public Collection<BambooBuild> getSubscribedPlansResults(final Collection<SubscribedPlan> plans, final boolean isUseFavourities, final int timezoneOffset)
            throws RemoteApiLoginException {
        final Collection<BambooBuild> builds = new ArrayList<>();

        Collection<BambooPlan> plansForServer = null;
        RemoteApiException exception = null;
        try {
            plansForServer = getPlanList();
        } catch (final RemoteApiException e) {
            // can go further, no disabled info will be available
            loger.warn("Cannot fetch plan list from Bamboo server [" + getUrl() + "]");
            exception = e;
        }

        if (isUseFavourities) {
            if (plansForServer != null) {
                for (final BambooPlan bambooPlan : plansForServer) {
                    if (bambooPlan.isFavourite()) {
                        if (isLoggedIn()) {
                            try {
                                final BambooBuild buildInfo = getLatestBuildBuilderForPlan(bambooPlan.getKey(), timezoneOffset).enabled(bambooPlan.isEnabled())
                                        .build();
                                builds.add(buildInfo);
                            } catch (final RemoteApiException e) {
                                // go ahead, there are other builds
                                loger.warn("Cannot fetch latest build for plan [" + bambooPlan.getKey() + "] from Bamboo server [" + getUrl() + "]");
                            }
                        } else {
                            builds.add(constructBuildErrorInfo(bambooPlan.getKey(), exception == null ? "" : exception.getMessage(), exception, Instant.now())
                                    .build());
                        }
                    }
                }
            }
        } else {
            for (final SubscribedPlan plan : plans) {
                if (isLoggedIn()) {
                    try {
                        final Boolean isEnabled = plansForServer != null ? BambooSessionImpl.isPlanEnabled(plansForServer, plan.getKey()) : null;
                        final BambooBuild buildInfo = getLatestBuildBuilderForPlan(plan.getKey(), timezoneOffset).enabled(isEnabled != null ? isEnabled : true)
                                .build();
                        builds.add(buildInfo);
                    } catch (final RemoteApiException e) {
                        // go ahead, there are other builds
                        // todo what about any error info
                    }
                } else {
                    builds.add(constructBuildErrorInfo(plan.getKey(), exception == null ? "" : exception.getMessage(), exception, Instant.now()).build());
                }
            }
        }

        return builds;
    }

}
