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

package com.atlassian.theplugin.commons.crucible.api.rest;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.PathAndRevision;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.BasicReview;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ExtendedCrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.NewReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.PatchAnchorData;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.RevisionData;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.changes.Changes;
import com.atlassian.theplugin.commons.exception.IncorrectVersionException;
import com.atlassian.theplugin.commons.remoteapi.CaptchaRequiredException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.ProductVersionUtil;
import com.atlassian.theplugin.commons.util.StringUtil;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.theplugin.commons.crucible.api.rest.CrucibleRestXmlHelper.parseActions;

/**
 * Communication stub for Crucible REST API.
 */
public class CrucibleSessionImpl extends AbstractHttpSession implements CrucibleSession {
    private static final CrucibleVersionInfo MIN_VERSION_SUPPORTING_POST_LOGIN = new CrucibleVersionInfo("2.4", null);

    private static final String AUTH_SERVICE = "/rest-service/auth-v1";

    private static final String REVIEW_SERVICE = "/rest-service/reviews-v1";

    private static final String PROJECTS_SERVICE = "/rest-service/projects-v1";

    private static final String REPOSITORIES_SERVICE = "/rest-service/repositories-v1";

    private static final String SEARCH_SERVICE = "/rest-service/search-v1";

    private static final String USER_SERVICE = "/rest-service/users-v1";

    private static final String LOGIN = "/login";

    private static final String REVIEWS_IN_STATES = "?state=";

    private static final String FILTERED_REVIEWS = "/filter";

    private static final String SEARCH_REVIEWS = "/search";

    private static final String SEARCH_REVIEWS_QUERY = "?path=";

    private static final String DETAIL_REVIEW_INFO = "/details";

    private static final String ACTIONS = "/actions";

    private static final String TRANSITIONS = "/transitions";

    private static final String REVIEWERS = "/reviewers";

    private static final String REVIEW_ITEMS = "/reviewitems";

    private static final String REVISIONS = "/revisions";

    private static final String METRICS = "/metrics";

    private static final String VERSION = "/versionInfo";

    private static final String COMMENTS = "/comments";

    private static final String GENERAL_COMMENTS = "/comments/general";

    private static final String VERSIONED_COMMENTS = "/comments/versioned";

    private static final String REPLIES = "/replies";

    private static final String APPROVE_ACTION = "action:approveReview";

    private static final String SUBMIT_ACTION = "action:submitReview";

    private static final String SUMMARIZE_ACTION = "action:summarizeReview";

    private static final String ABANDON_ACTION = "action:abandonReview";

    private static final String CLOSE_ACTION = "action:closeReview";

    private static final String RECOVER_ACTION = "action:recoverReview";

    private static final String REOPEN_ACTION = "action:reopenReview";

    private static final String REJECT_ACTION = "action:rejectReview";

    private static final String TRANSITION_ACTION = "/transition?action=";

    private static final String PUBLISH_COMMENTS = "/publish";

    private static final String COMPLETE_ACTION = "/complete";

    private static final String UNCOMPLETE_ACTION = "/uncomplete";

    private static final String ADD_CHANGESET = "/addChangeset";

    private static final String ADD_REVISIONS = "/reviewitems/revisions";

    private static final String ADD_PATCH = "/addPatch";

    private static final String ADD_FILE = "/addFile";

    private static final String MARK_READ = "/markAsRead";

    private static final String MARK_LEAVE_UNREAD = "/markAsLeaveUnread";

    private static final String MARK_ALL_READ = "/markAllAsRead";

    private static final String PROJECT_EXPAND_ALLOWED_REVIEWERS = "?expand=allowedReviewers";

    private static final String CHANGES = "/changes/";

    private static final String REVIEWS_FOR_ISSUE = "/reviewsForIssue";

    private String authToken;

    private final Map<String, Repository> repositories = new HashMap<String, Repository>();

    private final Map<String, List<CustomFieldDef>> metricsDefinitions = new HashMap<String, List<CustomFieldDef>>();

    @Nullable
    private volatile CrucibleVersionInfo crucibleVersionInfo;

    private boolean loginCalled = false;

    private final Logger logger;

    /**
     * Public constructor for CrucibleSessionImpl.
     *
     * @param serverData The server fisheye configuration for this session
     * @param callback   The callback needed for preparing HttpClient calls
     * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException
     *          when serverCfg configuration is invalid
     */
    public CrucibleSessionImpl(ConnectionCfg serverData, HttpSessionCallback callback, Logger logger)
            throws RemoteApiMalformedUrlException {
        super(serverData, callback);
        this.logger = logger;
    }

    public void login() throws RemoteApiLoginException {
        loginCalled = true;
    }

    public void login(String name, char[] aPassword) throws RemoteApiLoginException {
        login();
    }


    private boolean isPostLoginSupported() throws RemoteApiException {
        final CrucibleVersionInfo cvi = getCrucibleVersionInfo();
        return cvi.compareTo(MIN_VERSION_SUPPORTING_POST_LOGIN) >= 0;
    }

    private void realLogin() throws RemoteApiLoginException {
        // Login every time access Crucible server - https://studio.atlassian.com/browse/ACC-31
        try {
            Document doc = retrieveLoginResponse();
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiLoginFailedException(exception);
            }
            XPath xpath = XPath.newInstance("/loginResult/token");
            List<?> elements = xpath.selectNodes(doc);
            if (elements == null) {
                throw new RemoteApiLoginException("Server did not return any authentication token");
            }
            if (elements.size() != 1) {
                throw new RemoteApiLoginException("Server returned unexpected number of authentication tokens ("
                        + elements.size() + ")");
            }
            this.authToken = ((Element) elements.get(0)).getText();
        } catch (MalformedURLException e) {
            throw new RemoteApiLoginException("Malformed server URL: " + getBaseUrl(), e);
        } catch (UnknownHostException e) {
            throw new RemoteApiLoginException("Unknown host: " + e.getMessage(), e);
        } catch (IOException e) {
            if (e.getCause() != null && e.getCause().getMessage().contains("maximum")) {
                throw new CaptchaRequiredException(e);
            }
            throw new RemoteApiLoginException(getBaseUrl() + ":" + e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiLoginException("Server:" + getBaseUrl() + " returned malformed response", e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new RemoteApiLoginException("Remote session expired on server:" + getBaseUrl(), e);
        } catch (IllegalArgumentException e) {
            throw new RemoteApiLoginException("Malformed server URL: " + getBaseUrl(), e);
        } catch (RemoteApiLoginException e) {
            throw e;
        } catch (RemoteApiException e) {
            throw new RemoteApiLoginException(e.getMessage(), e);
        }
    }

    private Document retrieveLoginResponse() throws IOException, JDOMException, RemoteApiException {
        final String username = getUsername();
        final String password = getPassword();
        if (username == null || password == null) {
            throw new RemoteApiLoginException("Corrupted configuration. Username or Password null");
        }
        final String loginUrlPrefix = getBaseUrl() + AUTH_SERVICE + LOGIN;

        if (isPostLoginSupported()) {
            final Map<String, String> form = MiscUtil.buildHashMap();
            form.put("userName", username);
            form.put("password", password);
            return retrievePostResponseWithForm(loginUrlPrefix, form, true);
        } else {
            final String loginUrl;
            try {
                loginUrl = loginUrlPrefix + "?userName=" + URLEncoder.encode(username, "UTF-8") + "&password="
                        + URLEncoder.encode(password, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // /CLOVER:OFF
                throw new RuntimeException("URLEncoding problem: " + e.getMessage());
                // /CLOVER:ON
            }
            return retrieveGetResponse(loginUrl);
        }
    }

    public void logout() {
        loginCalled = false;
        if (authToken != null) {
            authToken = null;
        }
    }

    public CrucibleVersionInfo getServerVersion() throws RemoteApiException {

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + VERSION;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("versionInfo");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    this.crucibleVersionInfo = CrucibleRestXmlHelper.parseVersionNode(element);
                    return this.crucibleVersionInfo;
                }
            }

            throw new RemoteApiException("No version info found in server response");
        } catch (UnknownHostException e) {
            throw new RemoteApiException("Unknown host: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    private void updateMetricsMetadata(BasicReview review) {
        try {
            getMetrics(review.getMetricsVersion());
        } catch (RemoteApiException e) {
            // can be swallowed - metrics metadata are useful, but not necessery
        }
    }

    public List<BasicReview> getReviewsInStates(List<State> states) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getBaseUrl());
        sb.append(REVIEW_SERVICE);
        sb.append(DETAIL_REVIEW_INFO);
        if (states != null && states.size() != 0) {
            sb.append(REVIEWS_IN_STATES);
            for (Iterator<State> stateIterator = states.iterator(); stateIterator.hasNext();) {
                State state = stateIterator.next();
                sb.append(state.value());
                if (stateIterator.hasNext()) {
                    sb.append(",");
                }
            }
        }

        try {
            Document doc = retrieveGetResponse(sb.toString());

            XPath xpath = XPath.newInstance("/detailedReviews/detailedReviewData");

            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<BasicReview> reviews = new ArrayList<BasicReview>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    reviews.add(parseBasicReview(element));
                }
            }
            for (BasicReview review : reviews) {
                updateMetricsMetadata(review);
            }
            return reviews;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public List<BasicReview> getAllReviews() throws RemoteApiException {
        return getReviewsInStates(null);
    }

    public List<BasicReview> getReviewsForFilter(PredefinedFilter filter) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        try {
            String url = getBaseUrl() + REVIEW_SERVICE + FILTERED_REVIEWS + "/" + filter.getFilterUrl()
                    + DETAIL_REVIEW_INFO;
            Document doc = retrieveGetResponse(url);

            XPath xpath = XPath.newInstance("/detailedReviews/detailedReviewData");

            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<BasicReview> reviews = new ArrayList<BasicReview>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    reviews.add(parseBasicReview(element));
                }
            }
            for (BasicReview review : reviews) {
                updateMetricsMetadata(review);
            }
            return reviews;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    private boolean checkCustomFiltersAsGet() {
        if (crucibleVersionInfo == null) {
            try {
                getServerVersion();
            } catch (RemoteApiException e) {
                return false;
            }
        }
        try {
            ProductVersionUtil version = new ProductVersionUtil(crucibleVersionInfo.getReleaseNumber());
            if (version.greater(new ProductVersionUtil("1.6.3"))) {
                return true;
            }
        } catch (IncorrectVersionException e) {
            return false;
        }
        return false;
    }

    public CrucibleVersionInfo getCrucibleVersionInfo() throws RemoteApiException {
        if (crucibleVersionInfo != null) {
            return crucibleVersionInfo;
        } else {
            return getServerVersion();
        }
    }

    public boolean checkContentUrlAvailable() {
        if (crucibleVersionInfo == null) {
            try {
                getServerVersion();
            } catch (RemoteApiException e) {
                return false;
            }
        }
        try {
            ProductVersionUtil version = new ProductVersionUtil(crucibleVersionInfo.getReleaseNumber());
            if (version.greater(new ProductVersionUtil("1.6.6.0"))) {
                return true;
            }
        } catch (IncorrectVersionException e) {
            return false;
        }
        return false;
    }

    public boolean shouldTrimWikiMarkers() {
        if (crucibleVersionInfo == null) {
            try {
                getServerVersion();
            } catch (RemoteApiException e) {
                return false;
            }
        }
        try {
            ProductVersionUtil version = new ProductVersionUtil(crucibleVersionInfo.getReleaseNumber());
            if (version.greater(new ProductVersionUtil("2.1"))) {
                return true;
            }
        } catch (IncorrectVersionException e) {
            return false;
        }
        return false;
    }

    public List<BasicReview> getReviewsForCustomFilter(CustomFilter filter) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        try {
            Document doc;
            if (checkCustomFiltersAsGet()) {
                doc = getReviewsForCustomFilterAsGet(filter);
            } else {
                doc = getReviewsForCustomFilterAsPost(filter);
            }

            XPath xpath = XPath.newInstance("/detailedReviews/detailedReviewData");

            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<BasicReview> reviews = new ArrayList<BasicReview>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    reviews.add(parseBasicReview(element));
                }
            }
            for (BasicReview review : reviews) {
                updateMetricsMetadata(review);
            }
            return reviews;
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    private Document getReviewsForCustomFilterAsPost(CustomFilter filter) throws RemoteApiException {
        Document request = CrucibleRestXmlHelper.prepareCustomFilter(filter);
        try {
            String url = getBaseUrl() + REVIEW_SERVICE + FILTERED_REVIEWS + DETAIL_REVIEW_INFO;
            return retrievePostResponse(url, request);
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    private Document getReviewsForCustomFilterAsGet(CustomFilter filter) throws RemoteApiException {
        try {
            String url = getBaseUrl() + REVIEW_SERVICE + FILTERED_REVIEWS + DETAIL_REVIEW_INFO;
            String urlFilter = filter.getFilterUrl();
            if (!StringUtils.isEmpty(urlFilter)) {
                url += "?" + urlFilter;
            }

            return retrieveGetResponse(url);
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public List<BasicReview> getAllReviewsForFile(String repoName, String path) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        try {
            String url = getBaseUrl() + REVIEW_SERVICE + SEARCH_REVIEWS + "/" + URLEncoder.encode(repoName, "UTF-8")
                    + DETAIL_REVIEW_INFO + SEARCH_REVIEWS_QUERY + URLEncoder.encode(path, "UTF-8");
            Document doc = retrieveGetResponse(url);

            XPath xpath = XPath.newInstance("/detailedReviews/detailedReviewData");

            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<BasicReview> reviews = new ArrayList<BasicReview>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    reviews.add(parseBasicReview(element));
                }
            }
            for (BasicReview review : reviews) {
                updateMetricsMetadata(review);
            }
            return reviews;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public Review getReview(PermId permId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        try {
            String url = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + DETAIL_REVIEW_INFO;
            Document doc = retrieveGetResponse(url);

            XPath xpath = XPath.newInstance("/detailedReviewData");

            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    return prepareFullDetailReview(element);
                }
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public void fillRepositoryData(CrucibleFileInfo fileInfo) throws RemoteApiException {
        String repoName = fileInfo.getRepositoryName();
        if (repoName == null) {
            // oh well, it can be null - fileInfos are mostly empty now
            return;
        }

        String[] repoNameTokens = repoName.split(":");

        if (!repositories.containsKey(repoName)) {
            Repository repository = getRepository(repoNameTokens.length > 1 ? repoNameTokens[1] : repoNameTokens[0]);
            repositories.put(repoName, repository);
        }
        Repository repository = repositories.get(repoName);
        if (repository != null && repository instanceof SvnRepository) {
            String repoPath = ((SvnRepository) repository).getUrl() + "/" + ((SvnRepository) repository).getPath() + "/";
            VersionedVirtualFile oldDescriptor = fileInfo.getOldFileDescriptor();
            if (!oldDescriptor.getUrl().equals("")) {
                oldDescriptor.setRepoUrl(repoPath);
            }
            VersionedVirtualFile newDescriptor = fileInfo.getFileDescriptor();
            if (!newDescriptor.getUrl().equals("")) {
                newDescriptor.setRepoUrl(repoPath);
            }
        }
    }

    private BasicReview parseBasicReview(Element element) throws RemoteApiException {
        try {
            return CrucibleRestXmlHelper.parseBasicReview(getBaseUrl(), element, shouldTrimWikiMarkers());
        } catch (ParseException e) {
            throw new RemoteApiException(e);
        }
    }

    private Review prepareFullDetailReview(Element element) throws RemoteApiException {
        Review review;
        try {
            review = CrucibleRestXmlHelper.parseFullReview(
                    getBaseUrl(), getUsername(), element, shouldTrimWikiMarkers());
        } catch (ParseException e) {
            throw new RemoteApiException(e);
        }

        for (CrucibleFileInfo fileInfo : review.getFiles()) {
            fillRepositoryData(fileInfo);
        }
        return review;
    }

    private Changes prepareChanges(Element element) throws RemoteApiException {
        return CrucibleRestXmlHelper.parseChangesNode(element);
    }

    public List<Reviewer> getReviewers(PermId permId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + REVIEWERS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/reviewers/reviewer");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<Reviewer> reviewers = new ArrayList<Reviewer>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    reviewers.add(CrucibleRestXmlHelper.parseReviewerNode(element));
                }
            }
            return reviewers;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public List<User> getUsers() throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + USER_SERVICE;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/users/userData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<User> users = new ArrayList<User>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    // bug PL-1002: sometimes we get empty user name
                    User u = CrucibleRestXmlHelper.parseUserNode(element);
                    if (u.getDisplayName().equals("")) {
                        u = new User(u.getUsername(), u.getUsername());
                    }

                    // this is naive avatar handling - it will not go for production as it could be to expensive (remote call
                    // per each user
//					final String fullUserProfileUrl = getBaseUrl() + USER_SERVICE + "/" + u.getUsername();
//					final Document fullUserdoc = retrieveGetResponse(fullUserProfileUrl);
                    // final Object avatarUrlNode =
                    // XPath.newInstance("/restUserProfileData/avatarUrl").selectSingleNode(fullUserdoc);
//
//					if (avatarUrlNode != null) {
//						u.setAvatarUrl(((Element) avatarUrlNode).getText().trim());
//					}
                    users.add(u);
                }
            }
            return users;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    /**
     * Retrieves projects directly from server
     *
     * @return list of Crucible projects
     * @throws RemoteApiException thrown in case of connection problems
     */
    public List<BasicProject> getProjects() throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + PROJECTS_SERVICE;

        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/projects/projectData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<BasicProject> projects = new ArrayList<BasicProject>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    projects.add(CrucibleRestXmlHelper.parseBasicProjectNode(element));
                }
            }
            return projects;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    /**
     * Retrieves project details from server
     *
     * @return list of Crucible projects
     * @throws RemoteApiException thrown in case of connection problems
     */
    public ExtendedCrucibleProject getProject(String key) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + PROJECTS_SERVICE + "/" + key + PROJECT_EXPAND_ALLOWED_REVIEWERS;

        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/projectData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    return CrucibleRestXmlHelper.parseProjectNode(element);
                }
            }
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public List<Repository> getRepositories() throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REPOSITORIES_SERVICE;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/repositories/repoData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<Repository> myRepositories = new ArrayList<Repository>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    myRepositories.add(CrucibleRestXmlHelper.parseRepositoryNode(element));
                }
            }
            return myRepositories;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public Repository getRepository(String repoName) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        List<Repository> myRepositories = getRepositories();
        for (Repository repository : myRepositories) {
            if (repository.getName().equals(repoName)) {
                if (repository.getType().equals("svn")) {
                    String requestUrl = getBaseUrl() + REPOSITORIES_SERVICE + "/" + repoName + "/svn";
                    try {
                        Document doc = retrieveGetResponse(requestUrl);
                        XPath xpath = XPath.newInstance("/svnRepositoryData");
                        @SuppressWarnings("unchecked")
                        List<Element> elements = xpath.selectNodes(doc);
                        if (elements != null && !elements.isEmpty()) {
                            for (Element element : elements) {
                                return CrucibleRestXmlHelper.parseSvnRepositoryNode(element);
                            }
                        }
                    } catch (IOException e) {
                        throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
                    } catch (JDOMException e) {
                        throwMalformedResponseReturned(e);
                    }
                } else {
                    return repository;
                }
            }
        }

        return null;
    }

    public Set<CrucibleFileInfo> getFiles(PermId id) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + id.getId() + REVIEW_ITEMS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("reviewItems/reviewItem");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            Set<CrucibleFileInfo> reviewItems = new HashSet<CrucibleFileInfo>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    CrucibleFileInfo fileInfo = CrucibleRestXmlHelper.parseReviewItemNode(element);
                    fillRepositoryData(fileInfo);
                    reviewItems.add(fileInfo);
                }
            }
            return reviewItems;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        } catch (ParseException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        }

        return null;
    }

//	public CrucibleFileInfo addItemToReview(Review review, NewReviewItem item) throws RemoteApiException {
//		if (!isLoggedIn()) {
//			throw new IllegalStateException("Calling method without calling login() first");
//		}
//
//		Document request = CrucibleRestXmlHelper.prepareAddItemNode(item);
//		try {
//			String url = getBaseUrl() + REVIEW_SERVICE + "/" + review.getPermId().getId() + REVIEW_ITEMS;
//			Document doc = retrievePostResponse(url, request);
//			XPath xpath = XPath.newInstance("/reviewItem");
//			@SuppressWarnings("unchecked")
//			List<Element> elements = xpath.selectNodes(doc);
//
//			if (elements != null && !elements.isEmpty()) {
//				CrucibleFileInfo fileInfo = CrucibleRestXmlHelper.parseReviewItemNode(review, elements.iterator().next());
//				fillRepositoryData(fileInfo);
//				CrucibleFileInfoManager.getInstance().getFiles(review).add(fileInfo);
//				return fileInfo;
//			}
//			return null;
//		} catch (IOException e) {
//			throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
//		} catch (JDOMException e) {
//			throw new RemoteApiException(getBaseUrl() + ": Server returned malformed response", e);
//		}
//	}

    public List<VersionedComment> getVersionedComments(Review review, CrucibleFileInfo reviewItem) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        final String requestUrl =
                getBaseUrl() + REVIEW_SERVICE + "/" + review.getPermId().getId() + REVIEW_ITEMS + "/"
                        + reviewItem.getPermId().getId() + COMMENTS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("comments/versionedLineCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<VersionedComment> comments = new ArrayList<VersionedComment>();

            if (elements != null && !elements.isEmpty()) {
                final Map<PermId, CrucibleFileInfo> reviewItemMap = Collections.singletonMap(reviewItem.getPermId(),
                        reviewItem);
                for (Element element : elements) {
                    final VersionedComment c = CrucibleRestXmlHelper.parseVersionedCommentNode(review, reviewItemMap,
                            getUsername(), element, shouldTrimWikiMarkers());
                    if (c != null) {
                        comments.add(c);
                    }
                }
            }
            return comments;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        } catch (ParseException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        }

        return null;
    }


//	public List<Comment> getComments(PermId id) throws RemoteApiException {
//		if (!isLoggedIn()) {
//			throw new IllegalStateException("Calling method without calling login() first");
//		}
//
//		String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + id.getId() + COMMENTS;
//		try {
//			Document doc = retrieveGetResponse(requestUrl);
//
//			XPath xpath = XPath.newInstance("comments/generalCommentData");
//			@SuppressWarnings("unchecked")
//			List<Element> elements = xpath.selectNodes(doc);
//			List<Comment> comments = new ArrayList<Comment>();
//
//			if (elements != null && !elements.isEmpty()) {
//				int i = 1;
//				for (Element element : elements) {
//					GeneralCommentBean comment = CrucibleRestXmlHelper.parseGeneralCommentNode(element);
//					XPath repliesPath = XPath.newInstance("comments/generalCommentData[" + (i++)
//							+ "]/replies/generalCommentData");
//					@SuppressWarnings("unchecked")
//					final List<Element> replies = repliesPath.selectNodes(doc);
//					if (replies != null && !replies.isEmpty()) {
//						for (Element reply : replies) {
//							comment.addReply(CrucibleRestXmlHelper.parseGeneralCommentNode(reply));
//						}
//					}
//					comments.add(comment);
//				}
//			}
//
//			xpath = XPath.newInstance("comments/versionedLineCommentData");
//			@SuppressWarnings("unchecked")
//			List<Element> vElements = xpath.selectNodes(doc);
//
//			if (vElements != null && !vElements.isEmpty()) {
//				int i = 1;
//				for (Element element : vElements) {
//					VersionedCommentBean comment = CrucibleRestXmlHelper.parseVersionedCommentNode(element);
//					XPath repliesPath = XPath.newInstance("comments/versionedLineCommentData[" + (i++)
//							+ "]/replies/generalCommentData");
//					@SuppressWarnings("unchecked")
//					final List<Element> replies = repliesPath.selectNodes(doc);
//					if (replies != null && !replies.isEmpty()) {
//						for (Element reply : replies) {
//							comment.addReply(CrucibleRestXmlHelper.parseVersionedCommentNode(reply));
//						}
//					}
//					comments.add(comment);
//				}
//			}
//
//			return comments;
//		} catch (IOException e) {
//			throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
//		} catch (JDOMException e) {
//			throw new RemoteApiException(getBaseUrl() + ": Server returned malformed response", e);
//		}
//	}

    public Comment addGeneralComment(Review review, Comment comment) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareGeneralComment(comment);

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + review.getPermId().getId() + COMMENTS;
        try {
            Document doc = retrievePostResponse(requestUrl, request);

            XPath xpath = XPath.newInstance("generalCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    return CrucibleRestXmlHelper.parseGeneralCommentNode(review, null, getUsername(), element,
                            shouldTrimWikiMarkers());
                }
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public void removeComment(PermId id, Comment comment) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }
        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + id.getId() + COMMENTS + "/"
                + comment.getPermId().getId();
        try {
            retrieveDeleteResponse(requestUrl, false);
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }
    }

    public void updateComment(PermId id, Comment comment) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareGeneralComment(comment);
        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + id.getId() + COMMENTS + "/"
                + comment.getPermId().getId();

        try {
            retrievePostResponse(requestUrl, request, false);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }
    }

    public void publishComment(PermId reviewId, PermId commentId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + reviewId.getId() + PUBLISH_COMMENTS;
        if (commentId != null) {
            requestUrl += "/" + commentId.getId();
        }

        try {
            retrievePostResponse(requestUrl, "", false);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        }
    }

    public VersionedComment addVersionedComment(Review review, PermId riId, VersionedComment comment)
            throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareVersionedComment(riId, comment);
        String requestUrl =
                getBaseUrl() + REVIEW_SERVICE + "/" + review.getPermId().getId() + REVIEW_ITEMS + "/" + riId.getId()
                        + COMMENTS;
        try {
            Document doc = retrievePostResponse(requestUrl, request);
            XPath xpath = XPath.newInstance("versionedLineCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    final Map<PermId, CrucibleFileInfo> fileInfoMap = Collections.singletonMap(comment.getCrucibleFileInfo()
                            .getPermId(), comment.getCrucibleFileInfo());
                    return CrucibleRestXmlHelper.parseVersionedCommentNode(review, fileInfoMap,
                            getUsername(), element, shouldTrimWikiMarkers());
                }
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        } catch (ParseException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        }

        return null;
    }

    @Nullable
    public Comment addReply(Review review, Comment reply)
            throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        final Comment parentComment = reply.getParentComment();
        if (parentComment == null) {
            throw new RemoteApiException("Reply must have a parent comment defined");
        }

        Document request = CrucibleRestXmlHelper.prepareGeneralComment(reply);

        final PermId permId = review.getPermId();
        if (permId == null) {
            throw new RemoteApiException("Review must have permId defined");
        }
        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + COMMENTS + "/"
                + parentComment.getPermId().getId() + REPLIES;

        try {
            Document doc = retrievePostResponse(requestUrl, request);

            XPath xpath = XPath.newInstance("generalCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    GeneralComment receivedReply =
                            CrucibleRestXmlHelper.parseGeneralCommentNode(review, parentComment,
                                    getUsername(), element, shouldTrimWikiMarkers());
                    if (receivedReply != null) {
                        receivedReply.setReply(true);
                    }
                    return receivedReply;
                }
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }


    public void updateReply(PermId id, PermId cId, PermId rId, Comment comment) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareGeneralComment(comment);

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + id.getId() + COMMENTS + "/" + cId.getId() + REPLIES
                + "/" + rId.getId();

        try {
            retrievePostResponse(requestUrl, request, false);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }
    }

    @Nullable
    public BasicReview createReview(Review review) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }
        return createReviewFromPatch(review, null);
    }

    @Nullable
    public BasicReview createSnippetReview(Review review, String snippet, String filename) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareCreateSnippetReviewNode(review, snippet, filename);
        try {
            Document doc = retrievePostResponse(getBaseUrl() + REVIEW_SERVICE, request);

            XPath xpath = XPath.newInstance("/reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return parseBasicReview(elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }


    @Nullable
    public BasicReview createReviewFromPatch(Review review, String patch)
			throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareCreateReviewNode(review, patch);

        StringBuilder txtHolder = new StringBuilder();
        try {
            Document doc = retrievePostResponse(getBaseUrl() + REVIEW_SERVICE, request, txtHolder);

            XPath xpath = XPath.newInstance("/reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return parseBasicReview(elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e, txtHolder.toString());
        }

        return null;
    }
    @Nullable
    public BasicReview createReviewFromPatch(Review review, String patch, PatchAnchorData anchorData)
			throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareCreateReviewNode(review, patch);
		if (getCrucibleVersionInfo().isVersion24OrGrater() && anchorData != null) {

			CrucibleRestXmlHelper.addAnchorData(request, anchorData);
		}

        StringBuilder txtHolder = new StringBuilder();
        try {
            Document doc = retrievePostResponse(getBaseUrl() + REVIEW_SERVICE, request, txtHolder);

            XPath xpath = XPath.newInstance("/reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return parseBasicReview(elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e, txtHolder.toString());
        }

        return null;
    }

    private List<Element> getReviewData(Document doc) throws JDOMException {
        XPath xpath = XPath.newInstance("/reviewData");
        @SuppressWarnings("unchecked")
        List<Element> elements = xpath.selectNodes(doc);
        return elements;

    }

    @Nullable
    public BasicReview createReviewFromUpload(Review review, Collection<UploadItem> uploadItems) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }
        BasicReview newReview = createReviewFromPatch(review, null);

        try {
            String urlString = getBaseUrl() + REVIEW_SERVICE + "/" + newReview.getPermId().getId() + ADD_FILE;
            for (UploadItem uploadItem : uploadItems) {
                String bogusOld = "[--item is empty--]";
                String bogusNew = "[--item deleted--]";

                byte[] oldContent = uploadItem.getOldContent();
                byte[] newContent = uploadItem.getNewContent();

                if (oldContent == null) {
                    oldContent = bogusOld.getBytes();
                }
                if (newContent == null) {
                    newContent = bogusNew.getBytes();
                }

                ByteArrayPartSource targetOldFile = new ByteArrayPartSource(uploadItem.getFileName(), oldContent);
                ByteArrayPartSource targetNewFile = new ByteArrayPartSource(uploadItem.getFileName(), newContent);

                Part[] parts = {new FilePart("file", targetNewFile), new FilePart("diffFile", targetOldFile)};

                retrievePostResponse(urlString, parts, true);
            }
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return newReview;
    }

    @Nullable
    public BasicReview createReviewFromRevision(Review review, List<String> revisions) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareCreateReviewNode(review, revisions);

//		XmlUtil.printXml(request);

        try {
            Document doc = retrievePostResponse(getBaseUrl() + REVIEW_SERVICE, request);
            XPath xpath = XPath.newInstance("/reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return parseBasicReview(elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public byte[] getFileContent(String contentUrl) throws RemoteApiException {
        return getFileContent(contentUrl, false);
    }

    public byte[] getFileContent(String contentUrl, boolean ignoreBase) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        if (StringUtils.isBlank(contentUrl)) {
            throw new RemoteApiException("Content URL is blank");
        }

        final String requestUrl = ignoreBase ? contentUrl : getBaseUrl() + contentUrl;
        try {
            return doConditionalGet(requestUrl);
        } catch (IOException e) {
            throw new RemoteApiException(requestUrl + ": " + e.getMessage(), e);
        }
    }

    public List<CrucibleAction> getAvailableActions(PermId permId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + ACTIONS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/actions/actionData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            return parseActions(elements);
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public List<CrucibleAction> getAvailableTransitions(PermId permId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + TRANSITIONS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/transitions/actionData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            return parseActions(elements);
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    @Nullable
    public BasicReview addRevisionsToReview(PermId permId, String repository, Collection<String> revisions)
            throws RemoteApiException {

        return addChangesetRevisionsToReview(permId, repository, revisions);
    }

    @Nullable
    public BasicReview addFileRevisionsToReview(PermId permId, String repository, List<PathAndRevision> revisions)
            throws RemoteApiException {

        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        if (!getServerVersion().isVersion21OrGreater()) {
            throw new RemoteApiException("Crucible 2.1 or newer is required");
        }

        Document request = CrucibleRestXmlHelper.prepareRevisionDataNode(repository, revisions);

        try {
            String url = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + ADD_REVISIONS;
            Document doc = retrievePostResponse(url, request);

            XPath xpath = XPath.newInstance("/detailedReviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return parseBasicReview(elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    @Nullable
    private BasicReview addChangesetRevisionsToReview(PermId permId, String repository, Collection<String> revisions)
            throws RemoteApiException {

        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareAddChangesetNode(repository, revisions);

        try {
            String url = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + ADD_CHANGESET;
            Document doc = retrievePostResponse(url, request);

            XPath xpath = XPath.newInstance("/reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return parseBasicReview(elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public void addFileToReview(PermId permId, NewReviewItem newReviewItem) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        if (!getServerVersion().isVersion21OrGreater()) {
            throw new RemoteApiException("Crucible 2.1 or newer is required");
        }

        Document request = CrucibleRestXmlHelper.prepareAddItemNode(newReviewItem);

        try {
            String url = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + REVIEW_ITEMS;
            Document doc = retrievePostResponse(url, request);

            // TODO parse response if necessary (review item is returned)
            return;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return;
    }

    @Nullable
    public BasicReview addPatchToReview(PermId permId, String repository, String patch) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareAddPatchNode(repository, patch);

        try {
            String url = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + ADD_PATCH;
            Document doc = retrievePostResponse(url, request);

            XPath xpath = XPath.newInstance("/reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return parseBasicReview(elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public void addItemsToReview(PermId permId, Collection<UploadItem> uploadItems) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        try {
            String urlString = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + ADD_FILE;
            for (UploadItem uploadItem : uploadItems) {
                ByteArrayPartSource targetOldFile =
                        new ByteArrayPartSource(uploadItem.getFileName(), uploadItem.getOldContent());
                ByteArrayPartSource targetNewFile =
                        new ByteArrayPartSource(uploadItem.getFileName(), uploadItem.getNewContent());

                Part[] parts = {
                        new FilePart("file", targetNewFile,
                                uploadItem.getNewContentType(), uploadItem.getNewCharset()),
                        new FilePart("diffFile", targetOldFile,
                                uploadItem.getOldContentType(), uploadItem.getOldCharset())};

                retrievePostResponse(urlString, parts, true);
            }
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }
    }

    public void addReviewers(PermId permId, Set<String> users) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + REVIEWERS;
        String reviewers = "";
        for (String user : users) {
            if (reviewers.length() > 0) {
                reviewers += ",";
            }
            reviewers += user;
        }

        try {
            retrievePostResponse(requestUrl, reviewers, false);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }
    }

    public void removeReviewer(PermId permId, String username) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + REVIEWERS + "/" + username;
        try {
            retrieveDeleteResponse(requestUrl, false);
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }
    }

    private void throwMalformedResponseReturned(JDOMException e) throws RemoteApiException {
        throw new RemoteApiException(getBaseUrl() + ": Server returned malformed response", e);
    }

    private void throwMalformedResponseReturned(JDOMException e, String responseText) throws RemoteApiException {
        throw new RemoteApiException(getBaseUrl() + ": Server returned malformed response: \n\n" + responseText + "\n\n", e);
    }

    private static void throwNotLoggedIn() {
        throw new IllegalStateException("Calling method without calling login() first");
    }

    public void markCommentRead(PermId reviewId, PermId commentId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + reviewId.getId()
                + COMMENTS + "/" + commentId.getId() + MARK_READ;

        try {
            retrievePostResponse(requestUrl, "", false);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }
    }

    public void markCommentLeaveRead(PermId reviewId, PermId commentId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + reviewId.getId()
                + COMMENTS + "/" + commentId.getId() + MARK_LEAVE_UNREAD;

        try {
            retrievePostResponse(requestUrl, "", false);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }
    }

    public void markAllCommentsRead(PermId reviewId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + reviewId.getId() + COMMENTS + MARK_ALL_READ;

        try {
            retrievePostResponse(requestUrl, "", false);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }
    }

    // @todo wseliga it even returns less than BasicReview according to Cru API spec

    @Nullable
    public BasicReview changeReviewState(PermId permId, CrucibleAction action) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + TRANSITION_ACTION + action.getId();
        try {
            Document doc = retrievePostResponse(requestUrl, "", true);

            XPath xpath = XPath.newInstance("reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            Review review = null;

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    return parseBasicReview(element);
                }
            }
            return review;
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public void completeReview(PermId permId, boolean complete) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId();
        if (complete) {
            requestUrl += COMPLETE_ACTION;
        } else {
            requestUrl += UNCOMPLETE_ACTION;
        }

        try {
            retrievePostResponse(requestUrl, "", false);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }
    }

    @Nullable
    public BasicReview closeReview(PermId permId, String summarizeMessage) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        try {
            Document doc;
            if (summarizeMessage != null && !"".equals(summarizeMessage)) {
                Document request = CrucibleRestXmlHelper.prepareCloseReviewSummaryNode(summarizeMessage);
                String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + "/close";
                doc = retrievePostResponse(requestUrl, request);
            } else {
                String requestUrl = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + TRANSITION_ACTION
                        + CLOSE_ACTION;
                doc = retrievePostResponse(requestUrl, "", true);
            }

            XPath xpath = XPath.newInstance("reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return parseBasicReview(elements.get(0));
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    public List<CustomFieldDef> getMetrics(int version) throws RemoteApiException {
        String key = Integer.toString(version);
        if (!metricsDefinitions.containsKey(key)) {
            // workaround for ACC-31
            if (!isLoggedIn()) {
                throwNotLoggedIn();
            }

            String requestUrl = getBaseUrl() + REVIEW_SERVICE + METRICS + "/" + Integer.toString(version);
            try {
                Document doc = retrieveGetResponse(requestUrl);

                XPath xpath = XPath.newInstance("metrics/metricsData");
                @SuppressWarnings("unchecked")
                List<Element> elements = xpath.selectNodes(doc);
                List<CustomFieldDef> metrics = new ArrayList<CustomFieldDef>();

                if (elements != null && !elements.isEmpty()) {
                    for (Element element : elements) {
                        metrics.add(CrucibleRestXmlHelper.parseMetricsNode(element));
                    }
                }
                metricsDefinitions.put(key, metrics);
            } catch (IOException e) {
                throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
            } catch (JDOMException e) {
                throwMalformedResponseReturned(e);
            }
        }
        return metricsDefinitions.get(key);
    }

    @Override
    protected void adjustHttpHeader(HttpMethod method) {
        method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));
    }

    @Override
    protected void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException {

    }

    @Override
    protected void preprocessMethodResult(HttpMethod method) {
    }

    private String getAuthHeaderValue() {
        return "Basic " + StringUtil.encode(getUsername() + ":" + getPassword());
    }

    private static String getExceptionMessages(Document doc) throws JDOMException {
        XPath xpath = XPath.newInstance("/loginResult/error");
        @SuppressWarnings("unchecked")
        List<Element> elements = xpath.selectNodes(doc);

        if (elements != null && elements.size() > 0) {
            StringBuffer exceptionMsg = new StringBuffer();
            for (Element e : elements) {
                exceptionMsg.append(e.getText());
                exceptionMsg.append("\n");
            }
            return exceptionMsg.toString();
        } else {
            /* no exception */
            return null;
        }
    }

    public boolean isLoggedIn() throws RemoteApiLoginException {
        if (!loginCalled) {
            return false;
        }
        // TODO: check if http://jira.atlassian.com/browse/CRUC-1452 was fixed then fix this code.
        // Refresh login to fix problem with https://studio.atlassian.com/browse/ACC-31
        realLogin();
        return authToken != null;
    }

    @Nullable
    public BasicReview addRevisionsToReviewItems(PermId permId, Collection<RevisionData> revisions)
            throws RemoteApiException {

        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        Document request = CrucibleRestXmlHelper.prepareRevisions(revisions);

        try {
            String url = getBaseUrl() + REVIEW_SERVICE + "/" + permId.getId() + REVIEW_ITEMS + REVISIONS;
            Document doc = retrievePostResponse(url, request);

            XPath xpath = XPath.newInstance("/detailedReviewData");

            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    return parseBasicReview(element);
                }
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return null;
    }

    @NotNull
    public Changes getChanges(@NotNull String repository, @Nullable String oldestCsid, boolean includeOldest,
                              @Nullable String newestCsid,
                              boolean includeNewest, @Nullable Integer max) throws RemoteApiException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }

        try {
            String url = getBaseUrl() + REPOSITORIES_SERVICE + CHANGES + repository + "/?";
            if (oldestCsid != null) {
                url += "oldestCsid=" + oldestCsid + "&includeOldest=" + Boolean.toString(includeOldest) + "&";
            }
            if (newestCsid != null) {
                url += "newestCsid=" + newestCsid + "&includeNewest=" + Boolean.toString(includeNewest) + "&";
            }
            if (max != null) {
                url += "max=" + max.toString() + "&";
            }

            Document doc = retrieveGetResponse(url);

            XPath xpath = XPath.newInstance("/changes");

            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    return prepareChanges(element);
                }
            }

            xpath = XPath.newInstance("/error");
            Element errorNode = (Element) xpath.selectSingleNode(doc);
            if (errorNode != null) {
                CrucibleRestXmlHelper.parseErrorAndThrowIt(errorNode);
            }
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        throw new RemoteApiException("No changes returned by server.");
    }

    @NotNull
    public List<BasicReview> getReviewsForIssue(@NotNull String jiraIssueKey, @NotNull int maxReturn)
            throws RemoteApiException, RemoteApiSessionExpiredException {
        if (!isLoggedIn()) {
            throwNotLoggedIn();
        }
        List<BasicReview> reviews = new ArrayList<BasicReview>();

        try {
            String url = getBaseUrl() + SEARCH_SERVICE + REVIEWS_FOR_ISSUE + "/?jiraKey="
                    + URLEncoder.encode(jiraIssueKey, "UTF-8") + "&maxReturn=" + maxReturn;
            Document doc = retrieveGetResponse(url);

            XPath xpath = XPath.newInstance("/detailedReviews/detailedReviewData");

            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    reviews.add(parseBasicReview(element));
                }
            }
            for (BasicReview review : reviews) {
                updateMetricsMetadata(review);
            }
            return reviews;
        } catch (IOException e) {
            throw new RemoteApiException(getBaseUrl() + ": " + e.getMessage(), e);
        } catch (JDOMException e) {
            throwMalformedResponseReturned(e);
        }

        return reviews;
    }
}