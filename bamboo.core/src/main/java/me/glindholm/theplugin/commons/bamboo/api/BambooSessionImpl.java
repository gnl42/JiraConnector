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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.xpath.XPath;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.BambooFileInfo;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BambooBuildInfo;
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

    private static final int BAMBOO_23_BUILD_NUMBER = 1308;
    private static final int BAMBOO_2_6_BUILD_NUMBER = 1839;
    private static final int BAMBOO_2_6_3_BUILD_NUMBER = 1904;
    private static final int BAMBOO_2_7_2_BUILD_NUMBER = 2101;
    private static final int BAMBOO_4_0_BUILD_NUMBER = 2906;
    private static final int BAMBOO_5_0_BUILD_NUMBER = 3600;

    private static final String CANNOT_PARSE_BUILD_TIME = "Cannot parse buildTime.";
    private static final String INVALID_SERVER_RESPONSE = "Invalid server response";

    /**
     * Public constructor for BambooSessionImpl.
     *
     * @param serverData
     *            The server configuration for this session
     * @param callback
     *            The callback needed for preparing HttpClient calls
     * @throws RemoteApiMalformedUrlException
     *             malformed url
     */
    public BambooSessionImpl(ConnectionCfg serverData, HttpSessionCallback callback, Logger logger)
            throws RemoteApiMalformedUrlException {
        super(serverData, callback);
        this.serverData = serverData;
        loger = logger;
    }

    private int getBamboBuildNumberImpl() throws RemoteApiException {
        String queryUrl = getBaseUrl() + GET_BAMBOO_BUILD_NUMBER_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

        try {
            Document doc = retrieveGetResponse(queryUrl);

            String exception = getExceptionMessages(doc);
            if (null != exception) {
                // error - method does nt exists (session errors handled in retrieveGetReponse
                return -1;
            }

            XPath xpath = XPath.newInstance("/response/bambooBuildNumber");
            Element element = (Element) xpath.selectSingleNode(doc);
            if (element != null) {
                String bNo = element.getText();
                return Integer.parseInt(bNo);
            }
            return -1;
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    private int getBamboBuildNumberImplNew() throws RemoteApiException {
        String queryUrl = getBaseUrl() + BUILD_NUMBER_INFO + "?auth=" + UrlUtil.encodeUrl(authToken);

        try {
            Document doc = retrieveGetResponse(queryUrl);

            String exception = getExceptionMessages(doc);
            if (null != exception) {
                // error - method does nt exists (session errors handled in retrieveGetReponse
                return -1;
            }

            // XPath xpath = XPath.newInstance("/response/bambooBuildNumber");
            XPath xpath = XPath.newInstance("/info/buildNumber");
            Element element = (Element) xpath.selectSingleNode(doc);
            if (element != null) {
                String bNo = element.getText();
                return Integer.parseInt(bNo);
            }
            return -1;
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    @Override
    @Nonnull
    public List<BambooProject> listProjectNames() throws RemoteApiException {
        String buildResultUrl = getBaseUrl() + LIST_PROJECT_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

        List<BambooProject> projects = new ArrayList<>();
        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            XPath xpath = XPath.newInstance("/response/project");
            @SuppressWarnings("unchecked")
            List<Element> elements = (List<Element>) xpath.selectNodes(doc);
            if (elements != null) {
                for (Element element : elements) {
                    String name = element.getChild("name").getText();
                    String key = element.getChild("key").getText();
                    projects.add(new BambooProjectInfo(name, key));
                }
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }

        return projects;
    }

    @Nonnull
    private List<BambooPlan> listPlanNames() throws RemoteApiException {
        String buildResultUrl = getBaseUrl() + LIST_PLAN_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

        List<BambooPlan> plans = new ArrayList<>();
        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            XPath xpath = XPath.newInstance("/response/build");
            @SuppressWarnings("unchecked")
            List<Element> elements = (List<Element>) xpath.selectNodes(doc);
            if (elements != null) {
                for (Element element : elements) {
                    String enabledValue = element.getAttributeValue("enabled");
                    boolean enabled = true;
                    if (enabledValue != null) {
                        enabled = Boolean.parseBoolean(enabledValue);
                    }
                    String name = element.getChild("name").getText();
                    String key = element.getChild("key").getText();
                    BambooPlan plan = new BambooPlan(name, key, null, enabled);
                    plans.add(plan);
                }
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }

        return plans;
    }

    @Nonnull
    private List<BambooPlan> listPlanNames_40() throws RemoteApiException {
        // String buildResultUrl = getBaseUrl() + LIST_PLANS + "?auth=" + UrlUtil.encodeUrl(authToken);
        String buildResultUrl = getBaseUrl() + LIST_PLANS;

        List<BambooPlan> plans = new ArrayList<>();
        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            // XPath xpath = XPath.newInstance("/response/build");
            XPath xpath = XPath.newInstance("/plans/plans/plan");
            @SuppressWarnings("unchecked")
            List<Element> elements = (List<Element>) xpath.selectNodes(doc);
            if (elements != null) {
                for (Element element : elements) {
                    String enabledValue = element.getAttributeValue("enabled");
                    boolean enabled = true;
                    if (enabledValue != null) {
                        enabled = Boolean.parseBoolean(enabledValue);
                    }
                    String name = element.getAttributeValue("name");
                    // String name = element.getChild("name").getText();
                    // String key = element.getChild("key").getText();
                    String key = element.getAttributeValue("key");
                    BambooPlan plan = new BambooPlan(name, key, null, enabled);
                    plans.add(plan);
                }
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }

        return plans;
    }

    /**
     * Returns a {@link me.glindholm.theplugin.commons.bamboo.BambooBuild} information about the latest build in a plan.
     * <p/>
     * Returned structure contains either the information about the build or an error message if the connection fails.
     *
     * @param planKey
     *            ID of the plan to get info about
     * @return Information about the last build or error message
     */
    @Override
    @Nonnull
    public BambooBuild getLatestBuildForPlan(@Nonnull String planKey, final int timezoneOffset)
            throws RemoteApiException {
        final List<BambooPlan> planList = listPlanNames();
        final Boolean isEnabled = isPlanEnabled(planList, planKey);
        return getLatestBuildForPlan(planKey, isEnabled != null ? isEnabled : true, timezoneOffset);
    }

    @Nullable
    public static Boolean isPlanEnabled(@Nonnull Collection<BambooPlan> allPlans, @Nonnull String planKey) {
        for (BambooPlan bambooPlan : allPlans) {
            if (planKey.equals(bambooPlan.getKey())) {
                return bambooPlan.isEnabled();
            }
        }
        return null;
    }

    @Nonnull
    public BambooBuild getLatestBuildForPlan(@Nonnull final String planKey, final boolean isPlanEnabled,
            final int timezoneOffset) throws RemoteApiException {
        return getLatestBuildBuilderForPlan(planKey, timezoneOffset).enabled(isPlanEnabled).build();
    }

    @Nonnull
    public BambooBuildInfo.Builder getLatestBuildBuilderForPlan(@Nonnull final String planKey,
            final int timezoneOffset) throws RemoteApiException {
        String buildResultUrl =
                getBaseUrl() + LATEST_BUILD_FOR_PLAN_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken) + "&buildKey="
                        + UrlUtil.encodeUrl(planKey);

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                return constructBuildErrorInfo(planKey, exception, Instant.now());
            }

            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance("/response").selectNodes(doc);
            if (elements != null && !elements.isEmpty()) {
                Element e = elements.iterator().next();
                final Set<String> commiters = constructBuildCommiters(e);
                return constructBuilderItem(e, Instant.now(), planKey, commiters, timezoneOffset);
            } else {
                return constructBuildErrorInfo(planKey, "Malformed server reply: no response element", Instant.now());
            }
        } catch (JDOMException e) {
            return constructBuildErrorInfo(planKey, "Server returned malformed response", e, Instant.now());
        } catch (IOException | RemoteApiException e) {
            return constructBuildErrorInfo(planKey, e.getMessage(), e, Instant.now());
        }
    }

    @Nonnull
    public BambooBuildInfo.Builder getLatestBuildBuilderForPlan_40(@Nonnull final String planKey,
            final int timezoneOffset) throws RemoteApiException {
        // String buildResultUrl =
        // getBaseUrl() + LATEST_BUILD_FOR_PLAN_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken) + "&buildKey="
        // + UrlUtil.encodeUrl(planKey);

        // http://tardigrade.sydney.atlassian.com:8085/bamboo/rest/api/latest/result/STD-XML/15?expand=changes
        // http://tardigrade.sydney.atlassian.com:8085/bamboo/rest/api/latest/result/STD-XML-JOB1/15?expand=changes

        String buildResultUrl =
                getBaseUrl() + LATEST_BUILD_FOR_PLAN + UrlUtil.encodeUrl(planKey) + "?expand="
                        + UrlUtil.encodeUrl("results[0].result");

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                return constructBuildErrorInfo(planKey, exception, Instant.now());
            }

            @SuppressWarnings("unchecked")
            // final List<Element> elements = XPath.newInstance("/response").selectNodes(doc);
            List<Element> elements = (List<Element>) XPath.newInstance("/results/results/result").selectNodes(doc);
            if (elements != null && !elements.isEmpty()) {
                Element e = elements.iterator().next();
                // final Set<String> commiters = constructBuildCommiters(e);
                String buildNumber = e.getAttributeValue("number");
                final Set<String> commiters = getCommitersForBuild_40(planKey, buildNumber);
                return constructBuilderItem_40(e, Instant.now(), planKey, commiters, timezoneOffset);
            } else {
                // plan may have no builds (never built)
                elements = (List<Element>) XPath.newInstance("/results/results").selectNodes(doc);
                if (elements != null && !elements.isEmpty()) {
                    Element e = elements.iterator().next();
                    // final Set<String> commiters = constructBuildCommiters(e);
                    String size = e.getAttributeValue("size");
                    if (size != null && size.length() > 0 && "0".equals(size)) {
                        return new BambooBuildInfo.Builder(planKey, serverData, BuildStatus.UNKNOWN).pollingTime(
                                Instant.now()).reason("Never built");
                    }
                }

                return constructBuildErrorInfo(planKey, "Malformed server reply: no response element", Instant.now());

            }
        } catch (JDOMException e) {
            return constructBuildErrorInfo(planKey, "Server returned malformed response", e, Instant.now());
        } catch (IOException | RemoteApiException e) {
            return constructBuildErrorInfo(planKey, e.getMessage(), e, Instant.now());
        }
    }

    public Set<String> getCommitersForBuild_40(@Nonnull final String planKey, @Nonnull final String buildNumber)
            throws RemoteApiException {

        String buildResultUrl =
                getBaseUrl() + GET_BUILD_DETAILS + UrlUtil.encodeUrl(planKey) + "/" + buildNumber + "?expand=changes";

        Set<String> commiters = new HashSet<>();

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance("/result/changes/change").selectNodes(doc);

            if (!elements.isEmpty()) {
                for (Element commiter : elements) {
                    commiters.add(commiter.getAttributeValue("author"));
                }
            }
            return commiters;
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    @Override
    @Nonnull
    public BambooPlan getPlanDetails(@Nonnull final String planKey) throws RemoteApiException {
        String planUrl = getBaseUrl() + PLAN_STATE + UrlUtil.encodeUrl(planKey);

        try {
            Document doc = retrieveGetResponse(planUrl);
            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance("/plan").selectNodes(doc);
            if (elements != null && !elements.isEmpty()) {
                Element e = elements.iterator().next();
                return constructPlanItem(e, true);
            } else {
                throw new RemoteApiException("Malformed server reply: no 'plan' element");
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    /**
     * It is new version of {@link #getLatestBuildForPlan(String, boolean, int)} Introduces new plan state 'building' and 'in
     * queue'
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

        String planUrl = getBaseUrl() + PLAN_STATE + UrlUtil.encodeUrl(planKey);

        try {
            Document doc = retrieveGetResponse(planUrl);

            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance("/plan").selectNodes(doc);
            if (elements != null && !elements.isEmpty()) {
                Element e = elements.iterator().next();
                BambooPlan plan = constructPlanItem(e, isPlanEnabled);

                BambooBuildInfo.Builder latestBuildBuilderForPlan;

                if (getBamboBuildNumber() >= BAMBOO_4_0_BUILD_NUMBER) {
                    latestBuildBuilderForPlan = getLatestBuildBuilderForPlan_40(planKey, timezoneOffset);
                } else {
                    latestBuildBuilderForPlan = getLatestBuildBuilderForPlan(planKey, timezoneOffset);
                }
                latestBuildBuilderForPlan.planState(plan.getState());
                latestBuildBuilderForPlan.enabled(isPlanEnabled);
                latestBuildBuilderForPlan.masterPlanKey(masterPlanKey);
                return latestBuildBuilderForPlan.build();

                // TODO we can retrieve comments and labels together with build details
                // below new API call can be made instead of old getBuild method
                // String buildUrl =
                // getBaseUrl() + "/rest/api/latest/build/" + UrlUtil.encodeUrl(planKey) + "/latest"
                // + "?expand=comments.comment,labels";
                // Document d = retrieveGetResponse(buildUrl);

            } else {
                return constructBuildErrorInfo(planKey, "Malformed server reply: no 'plan' element", Instant.now()).build();
            }
        } catch (JDOMException e) {
            return constructBuildErrorInfo(planKey, "Server returned malformed response", e, Instant.now()).build();
        } catch (IOException | RemoteApiException e) {
            return constructBuildErrorInfo(planKey, e.getMessage(), e, Instant.now()).build();
        }
    }

    @Override
    @Nonnull
    public BambooBuild getBuildForPlanAndNumber(@Nonnull String planKey, final int buildNumber, final int timezoneOffset)
            throws RemoteApiException {

        // try recent build first, as this API is availablke in older Bamboos also
        Collection<BambooBuild> recentBuilds = getRecentBuildsForPlan(planKey, timezoneOffset);
        try {
            for (BambooBuild recentBuild : recentBuilds) {
                if (recentBuild.getNumber() == buildNumber) {
                    return recentBuild;
                }
            }
        } catch (UnsupportedOperationException e) {
            // oh well, it can actually happen for disabled builds. Let's just gobble this
        }

        // well, it is an old build, let's try to use new API
        int bambooBuild = getBamboBuildNumber();
        if (bambooBuild < BAMBOO_23_BUILD_NUMBER) {
            throw new RemoteApiBadServerVersionException("Bamboo version 2.3 or newer required");
        }
        String buildResultUrl;
        String nodePath;
        if (bambooBuild < BAMBOO_2_6_3_BUILD_NUMBER) {
            buildResultUrl = getBaseUrl()
                    + GET_BUILD_BY_NUMBER_ACTION + "/" + UrlUtil.encodeUrl(planKey)
                    + "/" + buildNumber + "?auth=" + UrlUtil.encodeUrl(authToken);
            nodePath = "/build";
        } else {
            buildResultUrl = getBaseUrl()
                    + GET_BUILD_DETAILS + UrlUtil.encodeUrl(planKey) + "-" + buildNumber;
            nodePath = "/result";
        }

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                return constructBuildErrorInfo(buildResultUrl, exception, Instant.now()).build();
            }

            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance(nodePath).selectNodes(doc);
            Element el = elements.get(0);
            return constructBuildItemFromNewApi(el, Instant.now(), planKey);

        } catch (IOException | JDOMException e) {
            throw new RemoteApiException(e);
        }
    }

    @Override
    public Collection<BambooBuild> getRecentBuildsForPlan(@Nonnull final String planKey, final int timezoneOffset)
            throws RemoteApiException {
        if (getBamboBuildNumber() >= BAMBOO_4_0_BUILD_NUMBER) {
            return getBuildsCollection_40(planKey, timezoneOffset);
        } else {
            String buildResultUrl = getBaseUrl() + RECENT_BUILDS_FOR_PLAN_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
            + "&buildKey=" + UrlUtil.encodeUrl(planKey);
            return getBuildsCollection(buildResultUrl, planKey, timezoneOffset);
        }
    }

    @Override
    public Collection<BambooBuild> getRecentBuildsForUser(final int timezoneOffset) throws RemoteApiException {
        String buildResultUrl = getBaseUrl() + RECENT_BUILDS_FOR_USER_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
        + "&username=" + UrlUtil.encodeUrl(getUsername());
        return getBuildsCollection(buildResultUrl, getUsername(), timezoneOffset);
    }

    private Collection<BambooBuild> getBuildsCollection(@Nonnull final String url, @Nonnull final String planKey,
            final int timezoneOffset) throws RemoteApiException {

        final Instant pollingTime = Instant.now();
        final List<BambooBuild> builds = new ArrayList<>();
        try {
            Document doc = retrieveGetResponse(url);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                builds.add(constructBuildErrorInfo(url, exception, Instant.now()).build());
                return builds;
            }

            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance("/response/build").selectNodes(doc);
            if (elements == null || elements.isEmpty()) {
                builds.add(constructBuildErrorInfo(url, "Malformed server reply: no response element", Instant.now()).build());
            } else {
                for (Element element : elements) {
                    final Set<String> commiters = constructBuildCommiters(element);
                    builds.add(constructBuilderItem(element, pollingTime, planKey, commiters, timezoneOffset).enabled(true)
                            .build());
                }
            }
        } catch (JDOMException e) {
            builds.add(constructBuildErrorInfo(planKey, "Server returned malformed response", e, pollingTime).build());
        } catch (IOException | RemoteApiException e) {
            builds.add(constructBuildErrorInfo(planKey, e.getMessage(), e, pollingTime).build());
        }
        return builds;
    }

    private Collection<BambooBuild> getBuildsCollection_40(@Nonnull final String planKey,
            final int timezoneOffset) throws RemoteApiException {

        String url = getBaseUrl() + LATEST_BUILD_FOR_PLAN + UrlUtil.encodeUrl(planKey) + "?expand="
                + UrlUtil.encodeUrl("results[0:10].result");

        final Instant pollingTime = Instant.now();
        final List<BambooBuild> builds = new ArrayList<>();

        try {
            Document doc = retrieveGetResponse(url);

            String exception = getExceptionMessages(doc);
            if (null != exception) {
                builds.add(constructBuildErrorInfo(url, exception, Instant.now()).build());
                return builds;
            }

            List<Element> elements = (List<Element>) XPath.newInstance("/results/results/result").selectNodes(doc);
            if (elements == null || elements.isEmpty()) {
                builds.add(constructBuildErrorInfo(url, "Malformed server reply: no response element", Instant.now()).build());
            } else {
                for (Element element : elements) {
                    final Set<String> commiters = constructBuildCommiters(element);
                    builds.add(constructBuilderItem_40(element, pollingTime, planKey, commiters, timezoneOffset).enabled(true)
                            .build());
                }
            }
        } catch (JDOMException e) {
            builds.add(constructBuildErrorInfo(planKey, "Server returned malformed response", e, pollingTime).build());
        } catch (IOException | RemoteApiException e) {
            builds.add(constructBuildErrorInfo(planKey, e.getMessage(), e, pollingTime).build());
        }
        return builds;
    }

    private Set<String> constructBuildCommiters(final Element element) throws JDOMException {

        Set<String> commiters = new HashSet<>();
        @SuppressWarnings("unchecked")
        final List<Element> commitElements = (List<Element>) XPath.newInstance("commits/commit").selectNodes(element);
        if (!commitElements.isEmpty()) {
            for (Element commiter : commitElements) {
                commiters.add(commiter.getAttributeValue("author"));
            }
        }
        return commiters;
    }

    @Override
    @Nonnull
    public List<String> getFavouriteUserPlans() throws RemoteApiException {
        List<String> builds = new ArrayList<>();
        String buildResultUrl = getBaseUrl() + LATEST_USER_BUILDS_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken);

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                return builds;
            }

            final XPath xpath = XPath.newInstance("/response/build");
            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) xpath.selectNodes(doc);
            if (elements != null) {
                for (Element element : elements) {
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
        List<String> builds = new ArrayList<>();
        String buildResultUrl = getBaseUrl() + PLAN_STATE + "?favourite&expand=plans";

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                return builds;
            }

            final XPath xpath = XPath.newInstance("/plans/plans/plan");
            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) xpath.selectNodes(doc);
            if (elements != null) {
                for (Element element : elements) {
                    builds.add(element.getAttributeValue("key"));
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
    private BuildDetails getBuildResultDetailsOld(@Nonnull String planKey, int buildNumber) throws RemoteApiException {
        final String buildResultUrl = getBaseUrl() + GET_BUILD_DETAILS_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
        + "&buildKey=" + UrlUtil.encodeUrl(planKey) + "&buildNumber=" + buildNumber;

        try {
            BuildDetailsInfo build = new BuildDetailsInfo();
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }

            @SuppressWarnings("unchecked")
            final List<Element> responseElements = (List<Element>) XPath.newInstance("/response").selectNodes(doc);
            for (Element element : responseElements) {
                String vcsRevisionKey = element.getAttributeValue("vcsRevisionKey");
                if (vcsRevisionKey != null) {
                    build.setVcsRevisionKey(vcsRevisionKey);
                }
            }

            @SuppressWarnings("unchecked")
            final List<Element> commitElements = (List<Element>) XPath.newInstance("/response/commits/commit").selectNodes(doc);
            if (!commitElements.isEmpty()) {
                int i = 1;
                for (Element element : commitElements) {
                    BambooChangeSetImpl cInfo = new BambooChangeSetImpl();
                    cInfo.setAuthor(element.getAttributeValue("author"));
                    cInfo.setCommitDate(parseCommitTime(element.getAttributeValue("date")));
                    cInfo.setComment(getChildText(element, "comment"));

                    String path = "/response/commits/commit[" + i++ + "]/files/file";
                    XPath filesPath = XPath.newInstance(path);
                    @SuppressWarnings("unchecked")
                    final List<Element> fileElements = (List<Element>) filesPath.selectNodes(doc);
                    for (Element file : fileElements) {
                        BambooFileInfo fileInfo = new BambooFileInfo(file.getAttributeValue("name"),
                                file.getAttributeValue("revision"));
                        cInfo.addCommitFile(fileInfo);
                    }
                    build.addCommitInfo(cInfo);
                }
            }

            @SuppressWarnings("unchecked")
            final List<Element> sucTestResElements = (List<Element>) XPath.newInstance("/response/successfulTests/testResult")
            .selectNodes(doc);
            for (Element element : sucTestResElements) {
                TestDetailsInfo tInfo = new TestDetailsInfo();
                tInfo.setTestClassName(element.getAttributeValue("testClass"));
                tInfo.setTestMethodName(element.getAttributeValue("testMethod"));
                double duration;
                try {
                    duration = Double.parseDouble(element.getAttributeValue("duration"));
                } catch (NumberFormatException e) {
                    // leave 0
                    duration = 0;
                }
                tInfo.setTestDuration(duration);
                tInfo.setTestResult(TestResult.TEST_SUCCEED);
                build.addSuccessfulTest(tInfo);
            }

            @SuppressWarnings("unchecked")
            final List<Element> failedTestResElements = (List<Element>) XPath.newInstance("/response/failedTests/testResult")
            .selectNodes(doc);
            if (!failedTestResElements.isEmpty()) {
                int i = 1;
                for (Element element : failedTestResElements) {
                    TestDetailsInfo tInfo = new TestDetailsInfo();
                    tInfo.setTestClassName(element.getAttributeValue("testClass"));
                    tInfo.setTestMethodName(element.getAttributeValue("testMethod"));
                    double duration;
                    try {
                        duration = Double.parseDouble(element.getAttributeValue("duration"));
                    } catch (NumberFormatException e) {
                        // leave 0
                        duration = 0;
                    }
                    tInfo.setTestDuration(duration);
                    tInfo.setTestResult(TestResult.TEST_FAILED);

                    String path = "/response/failedTests/testResult[" + i++ + "]/errors/error";
                    XPath errorPath = XPath.newInstance(path);
                    @SuppressWarnings("unchecked")
                    final List<Element> errorElements = (List<Element>) errorPath.selectNodes(doc);
                    for (Element error : errorElements) {
                        tInfo.setTestErrors(error.getText());
                    }
                    build.addFailedTest(tInfo);
                }
            }

            return build;
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    /**
     * includes cached build number (version) of Bamboo given session connects too Currently we do not support clearing it, so
     * the restart will be required to use new API - this limitation is something we can definitely live with
     */
    private Integer serverBuildNumber;

    /**
     * Returns possible cached version information (build/compilation number) of the server
     *
     * @return
     * @throws RemoteApiException
     */
    @Override
    public int getBamboBuildNumber() throws RemoteApiException {
        if (serverBuildNumber != null) {
            return serverBuildNumber;
        }
        // I am not afraid of races here, they are not harmful
        try {
            serverBuildNumber = getBamboBuildNumberImpl();
        } catch (RemoteApiException e) {
            loger.info("Old Bamboo API is not available. Server error or Bamboo 4.0+ detected.");
            serverBuildNumber = getBamboBuildNumberImplNew();
        }
        return serverBuildNumber;
    }

    @Override
    public BuildDetails getBuildResultDetails(@Nonnull String planKey, int buildNumber) throws RemoteApiException {
        // as we are phasing out BambooServerFacade and BambooServerFacade2,
        // but still make life of the clients using this lib easy, we make this decision here which API to use

        final int bamboBuildNumber = getBamboBuildNumber();
        if (bamboBuildNumber >= BAMBOO_2_6_BUILD_NUMBER && bamboBuildNumber <= BAMBOO_2_6_3_BUILD_NUMBER) {
            return getBuildResultDetailsMoreRestish(planKey, buildNumber);
        } else if (bamboBuildNumber > BAMBOO_2_6_3_BUILD_NUMBER && bamboBuildNumber <= BAMBOO_2_7_2_BUILD_NUMBER) {
            return getBuildResultDetailsNew(planKey, buildNumber);
        } else if (bamboBuildNumber > BAMBOO_2_7_2_BUILD_NUMBER) {
            return getBuildResultDetails3x(planKey, buildNumber);
        } else {
            return getBuildResultDetailsOld(planKey, buildNumber);
        }
    }

    private BuildDetails getBuildResultDetails3x(String planKey, int buildNumber) throws RemoteApiException {

        // tests are available for separate jobs since Bamboo v 2.7 (build number not known yet)
        List<BambooJobImpl> jobs = getJobsForPlan(planKey);

        BuildDetailsInfo build = new BuildDetailsInfo();

        for (BambooJobImpl job : jobs) { // job key contains project key

            build.addJob(job);

            if (!job.isEnabled()) {
                // there are no details for disabled jobs
                continue;
            }

            final String url = new StringBuilder().append(getBaseUrl())
                    .append(GET_BUILD_DETAILS)
                    .append(job.getKey())
                    .append("-")
                    .append(buildNumber)
                    .append("?")
                    .append("expand=testResults.allTests.testResult.errors").toString();

            try {
                Document doc = retrieveGetResponse(url);
                String exception = getExceptionMessages(doc);
                if (null != exception) {
                    throw new RemoteApiException(exception);
                }

                @SuppressWarnings("unchecked")
                final List<Element> testResElements = (List<Element>) XPath.newInstance("/result/testResults/allTests/testResult")
                .selectNodes(doc);
                for (Element element : testResElements) {
                    TestDetailsInfo tInfo = new TestDetailsInfo();
                    tInfo.setTestClassName(element.getAttributeValue("className"));
                    tInfo.setTestMethodName(element.getAttributeValue("methodName"));
                    try {
                        tInfo.setTestResult(parseTestResult(element.getAttributeValue("status")));
                    } catch (ParseException e1) {
                        loger.warn("Cannot parse test result element:\n" + XmlUtil.toPrettyFormatedString(element), e1);
                        continue;
                    }
                    tInfo.setTestDuration(parseDuration(element.getChild("duration")));

                    StringBuilder errorBuilder = new StringBuilder();
                    XPath errorPath = XPath.newInstance("errors/error");
                    @SuppressWarnings("unchecked")
                    final List<Element> errorElements = (List<Element>) errorPath.selectNodes(element);
                    for (Element errorElement : errorElements) {
                        final String errorEntry = errorElement.getChildText("message");
                        if (errorEntry != null) {
                            errorBuilder.append(errorEntry).append('\n');
                        }
                    }
                    tInfo.setTestErrors(errorBuilder.toString());

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
                final Element changesElement = doc.getRootElement().getChild("changes");
                if (changesElement != null) {
                    build.setCommitInfo(parseChangeSets(changesElement));
                }
                // return build;
            } catch (IOException e) {
                throw new RemoteApiException(e.getMessage(), e);
            } catch (JDOMException | ParseException e) {
                throw new RemoteApiException("Server returned malformed response", e);
            }
        }

        return build;
    }

    @Nonnull
    private BuildDetails getBuildResultDetailsNew(@Nonnull String planKey, int buildNumber) throws RemoteApiException {

        // tests are available for separate jobs since Bamboo v 2.7
        List<BambooJobImpl> jobs = getJobsForPlan(planKey);

        BuildDetailsInfo build = new BuildDetailsInfo();

        for (BambooJobImpl job : jobs) { // job key contains project key

            build.addJob(job);

            if (!job.isEnabled()) {
                // there are no details for disabled jobs
                continue;
            }

            final String url = new StringBuilder().append(getBaseUrl())
                    .append(GET_BUILD_DETAILS)
                    .append(job.getKey())
                    .append("-")
                    .append(buildNumber)
                    .append("?")
                    .append("expand=testResults.all.testResult.errors").toString();

            try {
                Document doc = retrieveGetResponse(url);
                String exception = getExceptionMessages(doc);
                if (null != exception) {
                    throw new RemoteApiException(exception);
                }

                @SuppressWarnings("unchecked")
                final List<Element> testResElements = (List<Element>) XPath.newInstance("/result/testResults/all/testResult")
                .selectNodes(doc);
                for (Element element : testResElements) {
                    TestDetailsInfo tInfo = new TestDetailsInfo();
                    tInfo.setTestClassName(element.getAttributeValue("className"));
                    tInfo.setTestMethodName(element.getAttributeValue("methodName"));
                    try {
                        tInfo.setTestResult(parseTestResult(element.getAttributeValue("status")));
                    } catch (ParseException e1) {
                        loger.warn("Cannot parse test result element:\n" + XmlUtil.toPrettyFormatedString(element), e1);
                        continue;
                    }
                    tInfo.setTestDuration(parseDuration(element.getChild("duration")));

                    StringBuilder errorBuilder = new StringBuilder();
                    XPath errorPath = XPath.newInstance("errors/error");
                    @SuppressWarnings("unchecked")
                    final List<Element> errorElements = (List<Element>) errorPath.selectNodes(element);
                    for (Element errorElement : errorElements) {
                        final String errorEntry = errorElement.getChildText("message");
                        if (errorEntry != null) {
                            errorBuilder.append(errorEntry).append('\n');
                        }
                    }
                    tInfo.setTestErrors(errorBuilder.toString());

                    switch (tInfo.getTestResult()) {
                    case TEST_FAILED:
                        build.addFailedTest(tInfo); // TODO: remove when IntelliJ is ready with the jobs
                        job.addFailedTest(tInfo);
                        break;
                    case TEST_SUCCEED:
                        build.addSuccessfulTest(tInfo); // TODO: remove when IntelliJ is ready with the jobs
                        job.addSuccessfulTest(tInfo);
                        break;
                    default:
                        break;
                    }
                }
                final Element changesElement = doc.getRootElement().getChild("changes");
                if (changesElement != null) {
                    build.setCommitInfo(parseChangeSets(changesElement));
                }
                // return build;
            } catch (IOException e) {
                throw new RemoteApiException(e.getMessage(), e);
            } catch (JDOMException | ParseException e) {
                throw new RemoteApiException("Server returned malformed response", e);
            }
        }

        return build;
    }

    @Nonnull
    BuildDetails getBuildResultDetailsMoreRestish(@Nonnull String planKey, int buildNumber) throws RemoteApiException {

        final String buildResultUrl = getBaseUrl() + GET_BUILD_BY_NUMBER_ACTION + "/" + UrlUtil.encodeUrl(planKey)
        + "/" + buildNumber + "?auth=" + UrlUtil.encodeUrl(authToken)
        + "&expand=testResults.all.testResult.errors&expand=changes.change.files";

        try {
            BuildDetailsInfo build = new BuildDetailsInfo();
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }

            @SuppressWarnings("unchecked")
            final List<Element> testResElements = (List<Element>) XPath.newInstance("/build/testResults/all/testResult")
            .selectNodes(doc);
            for (Element element : testResElements) {
                TestDetailsInfo tInfo = new TestDetailsInfo();
                tInfo.setTestClassName(element.getAttributeValue("className"));
                tInfo.setTestMethodName(element.getAttributeValue("methodName"));
                try {
                    tInfo.setTestResult(parseTestResult(element.getAttributeValue("status")));
                } catch (ParseException e1) {
                    loger.warn("Cannot parse test result element:\n" + XmlUtil.toPrettyFormatedString(element), e1);
                    continue;
                }
                tInfo.setTestDuration(parseDuration(element.getChild("duration")));

                StringBuilder errorBuilder = new StringBuilder();
                XPath errorPath = XPath.newInstance("errors/error");
                @SuppressWarnings("unchecked")
                final List<Element> errorElements = (List<Element>) errorPath.selectNodes(element);
                for (Element errorElement : errorElements) {
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
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException | ParseException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    List<BambooChangeSet> parseChangeSets(Element changesElement) throws RemoteApiException {
        List<BambooChangeSet> changeSets = MiscUtil.buildArrayList();
        final List<Element> changeElements = XmlUtil.getChildElements(changesElement, "change");
        for (Element changeElement : changeElements) {
            BambooChangeSetImpl cInfo = new BambooChangeSetImpl();
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
                for (Element fileElement : fileElements) {
                    BambooFileInfo fileInfo = new BambooFileInfo(getChildText(fileElement, "name").trim(),
                            getChildText(fileElement, "revision").trim());
                    cInfo.addCommitFile(fileInfo);

                }
            }
            changeSets.add(cInfo);
        }
        return changeSets;
    }

    private double parseDuration(Element durationElement) throws ParseException {
        if (durationElement == null) {
            throw new ParseException("null duration element", 0);
        }

        final String durationStr = durationElement.getText();

        try {
            return Double.valueOf(durationStr) / 1000;
        } catch (NumberFormatException e) {
            throw new ParseException("Cannot parse duration element as floating point number [" + durationStr + "]", 0);
        }
    }

    private TestResult parseTestResult(String attributeValue) throws ParseException {
        if ("failed".equals(attributeValue)) {
            return TestResult.TEST_FAILED;
        } else if ("successful".equals(attributeValue)) {
            return TestResult.TEST_SUCCEED;
        }
        throw new ParseException("Invalid test result [" + attributeValue + "]", 0);
    }

    /**
     * Currently length of the comment is limited by poor implementation which uses GET HTTP method (sic!) to post a new comment
     * and the comment becomes part of URL, which is typically truncated by web servers.
     */
    @Override
    public void addLabelToBuild(@Nonnull String planKey, int buildNumber, String buildLabel) throws RemoteApiException {
        String buildResultUrl = getBaseUrl() + ADD_LABEL_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
        + "&buildKey=" + UrlUtil.encodeUrl(planKey) + "&buildNumber=" + buildNumber + "&label="
        + UrlUtil.encodeUrl(buildLabel);

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    /**
     * Currently length of the comment is limited by poor implementation which uses GET HTTP method (sic!) to post a new comment
     * and the comment becomes part of URL, which is typically truncated by web servers.
     */
    @Override
    public void addCommentToBuild(@Nonnull String planKey, int buildNumber, String buildComment)
            throws RemoteApiException {
        String buildResultUrl = getBaseUrl() + ADD_COMMENT_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken)
        + "&buildKey=" + UrlUtil.encodeUrl(planKey) + "&buildNumber=" + buildNumber + "&content="
        + UrlUtil.encodeUrl(buildComment);

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    @Override
    public void executeBuild(@Nonnull String planKey) throws RemoteApiException {

        if (getBamboBuildNumber() >= BAMBOO_2_7_2_BUILD_NUMBER) {
            executeBuildNewApi(planKey);
        } else {
            executeBuildOldApi(planKey);
        }
    }

    private void executeBuildOldApi(String planKey) throws RemoteApiException {
        String buildResultUrl;

        buildResultUrl = getBaseUrl() + EXECUTE_BUILD_ACTION + "?auth=" + UrlUtil.encodeUrl(authToken) + "&buildKey="
                + UrlUtil.encodeUrl(planKey);

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }

    }

    private void executeBuildNewApi(String planKey) throws RemoteApiException {
        String url = getBaseUrl() + BUILD_QUEUE_SERVICE + UrlUtil.encodeUrl(planKey);

        try {
            retrievePostResponse(url, "", false);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    BambooBuildInfo.Builder constructBuildErrorInfo(String planKey, String message, Instant instant) {
        return new BambooBuildInfo.Builder(planKey, null, serverData, null, null, BuildStatus.UNKNOWN).pollingTime(
                instant).errorMessage(message);
    }

    BambooBuildInfo.Builder constructBuildErrorInfo(String planKey, String message, Throwable exception, Instant lastPollingTime) {
        return new BambooBuildInfo.Builder(planKey, null, serverData, null, null, BuildStatus.UNKNOWN).pollingTime(
                lastPollingTime).errorMessage(message, exception);
    }

    private int parseInt(String number) throws RemoteApiException {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException ex) {
            throw new RemoteApiException("Invalid number", ex);
        }
    }

    private double parseDouble(String number) throws RemoteApiException {
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException ex) {
            throw new RemoteApiException("Invalid double", ex);
        }
    }

    private BambooPlan constructPlanItem(Element planNode, boolean isEnabledDefault) throws RemoteApiException {
        String name = planNode.getAttributeValue("name");
        String key = planNode.getAttributeValue("key");

        String projectName = planNode.getChildText("projectName");
        String projectKey = planNode.getChildText("projectKey");

        // todo do not break parsing if single value is broken
        boolean isFavourite = Boolean.parseBoolean(planNode.getChildText("isFavourite"));

        Integer averageBuildTime = new Double(parseDouble(planNode.getChildText("averageBuildTimeInSeconds"))).intValue();
        boolean isInQueue = Boolean.parseBoolean(planNode.getChildText("isInBuildQueue"));

        String isBuildingString = planNode.getChildText("isBuilding");

        // old Bamboo protection
        if (isBuildingString == null && isInQueue) {
            isBuildingString = "true";
        }
        boolean isBuilding = Boolean.parseBoolean(isBuildingString);

        String isEnabledString = planNode.getAttributeValue("enabled");
        if (isEnabledString == null) {
            isEnabledString = Boolean.toString(isEnabledDefault);
        }
        boolean isEnabled = Boolean.parseBoolean(isEnabledString);

        return new BambooPlan(name, key, null, isEnabled, isFavourite, projectName, projectKey, averageBuildTime, isInQueue,
                isBuilding);
    }

    private BambooBuildInfo.Builder constructBuilderItem(Element buildItemNode, Instant instant, final String aPlanKey,
            Set<String> commiters, final int timezoneOffset)
                    throws RemoteApiException {

        BambooBuildInfo.Builder builder;
        // for never executed build we actually have no data here (no children)
        if (!buildItemNode.getChildren().iterator().hasNext()) {
            builder =
                    new BambooBuildInfo.Builder(aPlanKey, serverData, BuildStatus.UNKNOWN).pollingTime(
                            instant).reason("Never built");
        } else {

            final String planKey = getChildText(buildItemNode, "buildKey");
            final String buildName = getChildText(buildItemNode, "buildName");
            final String projectName = getChildText(buildItemNode, "projectName");
            final int buildNumber = parseInt(getChildText(buildItemNode, "buildNumber"));
            final String relativeBuildDate = getChildText(buildItemNode, "buildRelativeBuildDate");
            final Instant startTime =
                    parseBuildDate(getChildText(buildItemNode, "buildTime"), CANNOT_PARSE_BUILD_TIME, timezoneOffset);
            final String buildCompletedDateStr = getChildText(buildItemNode, BUILD_COMPLETED_DATE_ELEM);
            final Instant completionTime =
                    buildCompletedDateStr != null && buildCompletedDateStr.length() > 0 ? parseDateUniversal(
                            buildCompletedDateStr, BUILD_COMPLETED_DATE_ELEM, timezoneOffset)
                            // older Bamboo versions do not generate buildCompletedDate so we set it as buildTime
                            : startTime;
                    final String durationDescription = getChildText(buildItemNode, "buildDurationDescription");

                    final String stateStr = getChildText(buildItemNode, "buildState");
                    builder =
                            new BambooBuildInfo.Builder(planKey, buildName, serverData, projectName, buildNumber, getStatus(stateStr))
                            .pollingTime(instant).reason(getChildText(buildItemNode, "buildReason"))
                            .startTime(startTime).testSummary(getChildText(buildItemNode, "buildTestSummary")).commitComment(
                                    getChildText(buildItemNode, "buildCommitComment")).testsPassedCount(
                                            parseInt(getChildText(buildItemNode, "successfulTestCount"))).testsFailedCount(
                                                    parseInt(getChildText(buildItemNode, "failedTestCount"))).completionTime(completionTime)
                            .relativeBuildDate(relativeBuildDate).durationDescription(durationDescription)
                            .commiters(commiters);
        }
        return builder;
    }

    private BambooBuildInfo.Builder constructBuilderItem_40(Element buildItemNode, Instant instant,
            final String aPlanKey,
            Set<String> commiters, final int timezoneOffset)
                    throws RemoteApiException {

        BambooBuildInfo.Builder builder;
        // for never executed build we actually have no data here (no children)
        if (!buildItemNode.getChildren().iterator().hasNext()) {
            builder =
                    new BambooBuildInfo.Builder(aPlanKey, serverData, BuildStatus.UNKNOWN).pollingTime(
                            instant).reason("Never built");
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

            builder =
                    new BambooBuildInfo.Builder(planKey, buildName, serverData, projectName, buildNumber, getStatus(stateStr))
                    .pollingTime(instant)
                    .reason(getBuildReason_40(getChildText(buildItemNode, "buildReason")))
                    .startTime(startTime).testSummary(getChildText(buildItemNode, "buildTestSummary")).commitComment(
                            getChildText(buildItemNode, "buildCommitComment")).testsPassedCount(
                                    parseInt(getChildText(buildItemNode, "successfulTestCount"))).testsFailedCount(
                                            parseInt(getChildText(buildItemNode, "failedTestCount"))).completionTime(completionTime)
                    .relativeBuildDate(relativeBuildDate).durationDescription(durationDescription)
                    .commiters(commiters);
        }
        return builder;
    }

    private String getBuildReason_40(String reasonOriginal) {
        // Pattern pattern = Pattern.compile("<a([^>]+)>(.+?)</a>");
        Pattern pattern = Pattern.compile("(.*)<a([^>]+)>(.+)</a>");

        Matcher m = pattern.matcher(reasonOriginal);
        if (m.find()) {
            return StringEscapeUtils.unescapeHtml(m.group(1) + m.group(3));
            // return m.group(1) + m.group(3);
        }

        return reasonOriginal;
    }

    private BambooBuild constructBuildItemFromNewApi(Element el, Instant instant, String planKey)
            throws RemoteApiException {

        BambooPlan plan = getPlanDetails(planKey);

        BambooBuildInfo.Builder builder =
                new BambooBuildInfo.Builder(planKey, plan.getName(), serverData, plan.getProjectName(),
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
    private BuildStatus getStatus(@Nullable String stateStr) {
        if (BUILD_SUCCESSFUL.equalsIgnoreCase(stateStr)) {
            return BuildStatus.SUCCESS;
        } else if (BUILD_FAILED.equalsIgnoreCase(stateStr)) {
            return BuildStatus.FAILURE;
        } else {
            return BuildStatus.UNKNOWN;
        }
    }

    private static Instant parseDateUniversal(@Nullable String dateStr, @Nonnull String element, final int timezoneOffset)
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
     * wseliga: I have no idea why this method silently returns null in case of parsing problem. For now, I am going to leave it
     * as it is to avoid hell of the problems, should it be really necessary (and I am now a few days before 2.0.0 final
     * release)
     *
     * @param date
     *            string to parse
     * @param errorMessage
     *            message used during logging
     * @return parsed date
     */
    @Nullable
    private static Instant parseBuildDate(String date, String errorMessage, final int timezoneOffset) {
        try {
            // now adjust the time for local caller time, as Bamboo servers always serves its local time
            // without the timezone info
            return LocalDateTime.parse(date, buildDateFormat).atZone(ZoneId.systemDefault()).plusHours(timezoneOffset).toInstant();
        } catch (IllegalArgumentException e) {
            LoggerImpl.getInstance().debug("Cannot parse build date: " + errorMessage);
            return null;
        }
    }

    private static Instant parseCommitTime(String date) throws RemoteApiException {
        try {
            return OffsetDateTime.parse(date, commitDateFormat).toInstant();
        } catch (IllegalArgumentException e) {
            throw new RemoteApiException("Cannot parse date/time string [" + date + "]", e);
        }
    }

    private static Instant parseNewApiBuildTime(String dateTime) throws RemoteApiException {
        try {
            return OffsetDateTime.parse(dateTime, newApiDateFormat).toInstant();
        } catch (IllegalArgumentException e) {
            throw new RemoteApiException("Cannot parse date/time string [" + dateTime + "]", e);
        }
    }

    private static String getChildText(Element node, String childName) {
        final Element child = node.getChild(childName);
        return child == null ? "" : child.getText();
    }

    @Override
    public String getBuildLogs(@Nonnull String planKey, int buildNumber) throws RemoteApiException {

        String buildResultUrl = null;

        // log is available for separate jobs since Bamboo v 2.7 (build number not known yet)
        if (getBamboBuildNumber() > BAMBOO_2_6_3_BUILD_NUMBER) {

            List<BambooJobImpl> jobs = getJobsForPlan(planKey);

            if (jobs.size() > 1) {
                throw new RemoteApiException("Logs are only available for Plans with a single Job.");
            }

            if (jobs.size() == 1 && jobs.get(0).isEnabled()) {

                String jobKey = jobs.get(0).getKey(); // job key contains project key

                buildResultUrl = new StringBuilder().append(getBaseUrl())
                        .append("/download/")
                        .append(jobKey)
                        .append("/build_logs/")
                        .append(jobKey)
                        .append("-")
                        .append(buildNumber)
                        .append(".log")
                        .toString();

            }
        } else {
            buildResultUrl = new StringBuilder().append(getBaseUrl())
                    .append("/download/")
                    .append(UrlUtil.encodeUrl(planKey))
                    .append("/build_logs/")
                    .append(UrlUtil.encodeUrl(planKey))
                    .append("-")
                    .append(buildNumber)
                    .append(".log")
                    .toString();
        }

        if (buildResultUrl != null && buildResultUrl.length() > 0) {
            try {
                return doUnconditionalGetForTextNonXmlResource(buildResultUrl);
            } catch (IOException e) {
                throw new RemoteApiException(e.getMessage(), e);
            }
        }

        return null;
    }

    @Override
    @Nonnull
    public Collection<String> getBranchKeys(String planKey, boolean useFavourites, boolean myBranchesOnly) throws RemoteApiException {
        List<String> branches = new ArrayList();
        String my = myBranchesOnly ? "&my" : "";
        String url = getBaseUrl() + PLAN_STATE + UrlUtil.encodeUrl(planKey) + "?expand=branches.branch" + my;
        try {
            Document doc = retrieveGetResponse(url);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }
            final XPath xpath = XPath.newInstance("/plan/branches/branch");
            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) xpath.selectNodes(doc);
            if (elements != null) {
                for (Element element : elements) {
                    if (useFavourites) {
                        String favourite = getChildText(element, "isFavourite");
                        if (StringUtils.equals(favourite, "false")) {
                            continue;
                        }
                    }
                    String branchKey = element.getAttributeValue("key");
                    branches.add(branchKey);
                }
            }
        } catch (IOException | JDOMException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
        return branches;
    }

    @Override
    public List<BambooJobImpl> getJobsForPlan(String planKey) throws RemoteApiException {

        List<BambooJobImpl> jobs = new ArrayList<>();

        String url = getBaseUrl() + PLAN_STATE + UrlUtil.encodeUrl(planKey) + "?expand=stages.stage.plans";

        try {
            Document doc = retrieveGetResponse(url);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }

            final XPath xpath = XPath.newInstance("/plan/stages/stage/plans/plan");
            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) xpath.selectNodes(doc);
            if (elements != null) {
                for (Element element : elements) {
                    String key = element.getAttributeValue("key");
                    String shortKey = element.getAttributeValue("shortKey");
                    String name = element.getAttributeValue("name");
                    String shortName = element.getAttributeValue("shortName");

                    BambooJobImpl job = new BambooJobImpl(key, shortKey, name, shortName);
                    String enabled = element.getAttributeValue("enabled");
                    // we treat job as enabled by default (old Bamboo compatibility)
                    // TODO jj test it!
                    if (enabled != null && enabled.equalsIgnoreCase("false")) {
                        job.setEnabled(false);
                    } else {
                        job.setEnabled(true);
                    }

                    jobs.add(job);
                }
            }

            return jobs;
        } catch (IOException | JDOMException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    @Override
    public Collection<BuildIssue> getIssuesForBuild(@Nonnull String planKey, int buildNumber) throws RemoteApiException {
        int bambooBuild = getBamboBuildNumber();
        if (bambooBuild < BambooServerVersionNumberConstants.BAMBOO_1401_BUILD_NUMBER) {
            throw new RemoteApiBadServerVersionException("Bamboo build 1401 or newer required");
        }

        String planUrl = getBaseUrl()
                + (bambooBuild <= BAMBOO_2_6_3_BUILD_NUMBER ? GET_BUILD_ACTION : GET_BUILD_DETAILS)
                + UrlUtil.encodeUrl(planKey + "-" + buildNumber)
                + GET_ISSUES_SUFFIX + "&auth=" + UrlUtil.encodeUrl(authToken);

        try {
            Document doc = retrieveGetResponse(planUrl);

            List<BuildIssue> issues = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Element> jiraIssuesNode = bambooBuild <= BAMBOO_2_6_3_BUILD_NUMBER
            ? (List<Element>) XPath.newInstance("build/jiraIssues").selectNodes(doc)
                    : (List<Element>) XPath.newInstance("result/jiraIssues").selectNodes(doc);

            if (jiraIssuesNode == null) {
                throw new RemoteApiException(INVALID_SERVER_RESPONSE);
            }
            if (jiraIssuesNode.size() != 1) {
                throw new RemoteApiException(INVALID_SERVER_RESPONSE);
            }
            @SuppressWarnings("unchecked")
            List<Element> issuesNodes = (List<Element>) XPath.newInstance("issue").selectNodes(jiraIssuesNode.get(0));
            if (issuesNodes == null) {
                throw new RemoteApiException(INVALID_SERVER_RESPONSE);
            }
            for (Element element : issuesNodes) {
                Element url = element.getChild("url");
                if (url == null) {
                    LoggerImpl.getInstance().error("getIssuesForBuild: \"url\" node of the \"issue\" element is null");
                    continue;
                }
                BuildIssue issue = new BuildIssueInfo(element.getAttributeValue("key"), url.getAttributeValue("href"));
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

        if (getBamboBuildNumber() >= BAMBOO_4_0_BUILD_NUMBER) {
            plans = listPlanNames_40();
        } else {
            plans = listPlanNames();
        }

        try {
            List<String> favPlans;
            if (getBamboBuildNumber() > BAMBOO_2_6_3_BUILD_NUMBER) {
                favPlans = getFavouriteUserPlansNew();
            } else {
                favPlans = getFavouriteUserPlans();
            }
            for (String fav : favPlans) {
                for (ListIterator<BambooPlan> it = plans.listIterator(); it.hasNext();) {
                    final BambooPlan plan = it.next();
                    if (plan.getKey().equalsIgnoreCase(fav)) {
                        it.set(plan.withFavourite(true));
                        break;
                    }
                }
            }
        } catch (RemoteApiException e) {
            // lack of favourite info is not a blocker here
        }
        return plans;
    }

    @Override
    @Nonnull
    public Collection<BambooBuild> getSubscribedPlansResults(final Collection<SubscribedPlan> plans,
            boolean isUseFavourities,
            int timezoneOffset) throws RemoteApiLoginException {
        Collection<BambooBuild> builds = new ArrayList<>();

        Collection<BambooPlan> plansForServer = null;
        RemoteApiException exception = null;
        try {
            plansForServer = getPlanList();
        } catch (RemoteApiException e) {
            // can go further, no disabled info will be available
            loger.warn("Cannot fetch plan list from Bamboo server [" + getUrl() + "]");
            exception = e;
        }

        if (isUseFavourities) {
            if (plansForServer != null) {
                for (BambooPlan bambooPlan : plansForServer) {
                    if (bambooPlan.isFavourite()) {
                        if (isLoggedIn()) {
                            try {
                                BambooBuild buildInfo =
                                        getLatestBuildBuilderForPlan(bambooPlan.getKey(), timezoneOffset).enabled(
                                                bambooPlan.isEnabled()).build();
                                builds.add(buildInfo);
                            } catch (RemoteApiException e) {
                                // go ahead, there are other builds
                                loger.warn("Cannot fetch latest build for plan [" + bambooPlan.getKey()
                                + "] from Bamboo server [" + getUrl() + "]");
                            }
                        } else {
                            builds.add(constructBuildErrorInfo(bambooPlan.getKey(),
                                    exception == null ? "" : exception.getMessage(), exception, Instant.now()).build());
                        }
                    }
                }
            }
        } else {
            for (SubscribedPlan plan : plans) {
                if (isLoggedIn()) {
                    try {
                        final Boolean isEnabled = plansForServer != null ? BambooSessionImpl.isPlanEnabled(
                                plansForServer, plan.getKey()) : null;
                        BambooBuild buildInfo =
                                getLatestBuildBuilderForPlan(plan.getKey(), timezoneOffset).enabled(
                                        isEnabled != null ? isEnabled : true).build();
                        builds.add(buildInfo);
                    } catch (RemoteApiException e) {
                        // go ahead, there are other builds
                        // todo what about any error info
                    }
                } else {
                    builds.add(constructBuildErrorInfo(plan.getKey(), exception == null ? "" : exception.getMessage(),
                            exception, Instant.now()).build());
                }
            }
        }

        return builds;
    }

}
