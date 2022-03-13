package com.atlassian.theplugin.commons.crucible.api.rest;

import com.atlassian.connector.commons.misc.IntRange;
import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.theplugin.commons.crucible.api.model.BasicReview;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ExtendedCrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewType;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.XmlUtil;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.CrucibleMockUtil;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author pmaruszak
 * @date Sep 17, 2009
 */
public class CrucibleRestXmlHelperTest extends TestCase {

    public void testParsePermId() throws JDOMException, IOException {
        final XPath xpath = XPath.newInstance("reviewItem");
        final SAXBuilder builder = new SAXBuilder();

        Document doc = builder.build(new CrucibleMockUtil().getResource("reviewItemNode_220_M3.xml"));
        List<Element> elements = xpath.selectNodes(doc);
        CrucibleFileInfo fileInfo = null;

        try {
            fileInfo = CrucibleRestXmlHelper.parseReviewItemNode((elements.get(0)));
            assertEquals("CFR-32137", fileInfo.getPermId().getId());
        } catch (ParseException e) {
            fail();
        }

        doc = builder.build(new CrucibleMockUtil().getResource("reviewItemNode_below_220.xml"));
        elements = xpath.selectNodes(doc);
        try {
            fileInfo = CrucibleRestXmlHelper.parseReviewItemNode((elements.get(0)));
            assertEquals("CFR-32137", fileInfo.getPermId().getId());
        } catch (ParseException e) {
            fail();
        }

        TestUtil.assertThrows(ParseException.class, new IAction() {
            public void run() throws Throwable {
                Document doc = builder.build(new CrucibleMockUtil().getResource("reviewItemNode_nopermId.xml"));
                List<Element> elements = xpath.selectNodes(doc);
                CrucibleRestXmlHelper.parseReviewItemNode((elements.get(0)));
            }
        });

        TestUtil.assertThrows(ParseException.class, new IAction() {
            public void run() throws Throwable {
                Document doc = builder.build(new CrucibleMockUtil().getResource("reviewItemNode_empty_permId.xml"));
                List<Element> elements = xpath.selectNodes(doc);
                CrucibleRestXmlHelper.parseReviewItemNode((elements.get(0)));
            }
        });

    }

    public void testParseProjectNode() throws JDOMException, IOException {
        XPath xpath = XPath.newInstance("projectData");
        final SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new CrucibleMockUtil().getResource("projectDataCrucible1_6.xml"));

        @SuppressWarnings("unchecked")
        List<Element> elements = xpath.selectNodes(doc);
        ExtendedCrucibleProject cp = CrucibleRestXmlHelper.parseProjectNode(elements.get(0));

        assertEquals(cp.getAllowedReviewers(), null);


        doc = builder.build(new CrucibleMockUtil().getResource("projectDataCrucible2_0.xml"));
        elements = xpath.selectNodes(doc);
        cp = CrucibleRestXmlHelper.parseProjectNode(elements.get(0));

        assertTrue(cp != null);
        assertEquals(5, cp.getAllowedReviewers().size());


        doc = builder.build(new CrucibleMockUtil().getResource("reviewDetailsResponse-testLineRanges.xml"));
        xpath = XPath.newInstance("detailedReviewData");
        elements = xpath.selectNodes(doc);

        Review review = null;
        try {
            review = CrucibleRestXmlHelper.parseFullReview("http://localhost", "pstefaniak", elements.get(0), false);
        } catch (ParseException e) {
            fail(); // check "reviewDetailsResponse-testLineRanges.xml" - should be valid .xml...
        }
        assertTrue(review != null);

        Iterator<CrucibleFileInfo> it = review.getFiles().iterator();
        List<VersionedComment> versionedComments = it.next().getVersionedComments();

        VersionedComment comment = versionedComments.get(0);
        assertEquals(comment.getMessage(), "this is yellow coment");
        assertEquals(comment.getFromStartLine(), 0);
        assertEquals(comment.getFromEndLine(), 0);
        assertEquals(comment.getToStartLine(), 0);
        assertEquals(comment.getToEndLine(), 0);
        assertNull(comment.getToLineRanges());
        assertNull(comment.getFromLineRanges());
        assertNull(comment.getLineRanges());

        comment = versionedComments.get(1);
        assertEquals(comment.getMessage(), "this is green comment");
        assertEquals(comment.getFromStartLine(), 0);
        assertEquals(comment.getFromEndLine(), 0);
        assertEquals(comment.getToStartLine(), 52);
        assertEquals(comment.getToEndLine(), 52);
        assertEquals(comment.getToLineRanges(), new IntRanges(new IntRange(52)));
        assertNull(comment.getFromLineRanges());
        assertEquals(comment.getLineRanges().get("51224"), new IntRanges(new IntRange(52)));

        comment = versionedComments.get(2);
        assertEquals(comment.getMessage(), "this is red coment");
        assertEquals(comment.getFromStartLine(), 0);
        assertEquals(comment.getFromEndLine(), 0);
        assertEquals(comment.getToStartLine(), 51);
        assertEquals(comment.getToEndLine(), 51);
        assertEquals(comment.getToLineRanges(), new IntRanges(new IntRange(51)));
        assertNull(comment.getFromLineRanges());
        assertEquals(comment.getLineRanges().get("38347"), new IntRanges(new IntRange(51)));

        comment = versionedComments.get(3);
        assertEquals(comment.getMessage(), "this is blue comment (lines 49, 52, 53)");
        assertEquals(comment.getFromStartLine(), 49);
        assertEquals(comment.getFromEndLine(), 53);
        assertEquals(comment.getToStartLine(), 49);
        assertEquals(comment.getToEndLine(), 54);
        assertEquals(comment.getToLineRanges(), new IntRanges(new IntRange(49), new IntRange(53, 54)));
        assertEquals(comment.getFromLineRanges(), new IntRanges(new IntRange(49), new IntRange(52, 53)));
        assertEquals(comment.getLineRanges().get("38347"), new IntRanges(new IntRange(49), new IntRange(52, 53)));
        assertEquals(comment.getLineRanges().get("51224"), new IntRanges(new IntRange(49), new IntRange(53, 54)));


    }

    private static final String TEST_URL = "http://localhost";

    public void testParseReviewDataWithDueDate() throws JDOMException, IOException, ParseException {
        final Document doc = loadDocument("reviewDataWithDueDate.xml");
        final BasicReview review = CrucibleRestXmlHelper.parseBasicReview(TEST_URL, doc.getRootElement(), false);
        final DateTime expected = new DateTime(2010, 03, 21, 13, 29, 35, 289, DateTimeZone.forOffsetHours(1));
        assertTrue(expected.isEqual(review.getDueDate()));
    }

    public void testParseReviewDataWithoutDueDate() throws JDOMException, IOException, ParseException {
        final Document doc = loadDocument("reviewDataWithoutDueDate.xml");
        final BasicReview review = CrucibleRestXmlHelper.parseBasicReview(TEST_URL, doc.getRootElement(), false);
        assertNull(review.getDueDate());
    }

    public void testParseReviewDataWithoutCorruptedDueDate() throws JDOMException, IOException, ParseException {
        final Document doc = loadDocument("reviewDataWithCorruptedDueDate.xml");
        TestUtil.assertThrows(ParseException.class, new IAction() {
            public void run() throws Throwable {
                CrucibleRestXmlHelper.parseBasicReview(TEST_URL, doc.getRootElement(), false);
            }
        });
    }

    public void testParseOpenSnippet() throws ParseException, JDOMException, IOException {
        final Document doc = loadDocument("open-snippet-details.xml");
        final Review review = CrucibleRestXmlHelper.parseFullReview(TEST_URL, "wseliga", doc.getRootElement(),
                false);
        assertEquals(State.OPEN_SNIPPET, review.getState());
        assertEquals(ReviewType.SNIPPET, review.getType());
    }

    public void testParseClosedSnippet() throws ParseException, JDOMException, IOException {
        final Document doc = loadDocument("closed-snippet-details.xml");
        final Review review = CrucibleRestXmlHelper.parseFullReview(TEST_URL, "wseliga", doc.getRootElement(),
                false);
        assertEquals(State.CLOSED_SNIPPET, review.getState());
        assertEquals(ReviewType.SNIPPET, review.getType());
    }

    public void testParseRegularReviewType() throws ParseException, JDOMException, IOException {
        final Document doc = loadDocument("reviewDetailsResponse-withReviewType.xml");
        final Review review = CrucibleRestXmlHelper.parseFullReview(TEST_URL, "wseliga", doc.getRootElement(),
                false);
        assertEquals(State.DRAFT, review.getState());
        assertEquals(ReviewType.REVIEW, review.getType());
    }

    private Document loadDocument(String resourceName) throws JDOMException, IOException {
        final SAXBuilder builder = new SAXBuilder();
        final Document doc = builder.build(new CrucibleMockUtil().getResource(resourceName));
        return doc;
    }

    public void testParseReviewDataWithUnkownAndIncompleteAction() throws JDOMException, IOException, ParseException {
        final Document doc = loadDocument("reviewDataWithUnknownAction.xml");
        try {
            final Review review = CrucibleRestXmlHelper.parseFullReview(TEST_URL, "pniewiadomski", doc.getRootElement(),
                    false);
            // assertTrue(a.contains(CrucibleAction.CREATE));
            TestUtil.assertHasOnlyElements(review.getActions(), CrucibleAction.CLOSE,
                    CrucibleAction.SUMMARIZE, CrucibleAction.COMPLETE,
                    CrucibleAction.VIEW, CrucibleAction.MODIFY_FILES, CrucibleAction.COMMENT,
                    CrucibleAction.UNCOMPLETE, CrucibleAction.RECOVER, CrucibleAction.REOPEN,
                    CrucibleAction.CREATE_SNIPPET, CrucibleAction.REOPEN_SNIPPET, CrucibleAction.CLOSE_SNIPPET);
        } catch (IllegalArgumentException e) {
            fail("Should not throw this excaption outside");
        }
    }

    public void testParseActionsWithUnknownAction() throws JDOMException, IOException {
        final Document doc = loadDocument("actions-with-some-new-action.xml");
        final List<Element> actionElements = XmlUtil.getChildElements(doc.getRootElement(), "actionData");
        final List<CrucibleAction> actions = CrucibleRestXmlHelper.parseActions(actionElements);
        TestUtil.assertHasOnlyElements(actions, CrucibleAction.REJECT, CrucibleAction.APPROVE,
                CrucibleAction.VIEW, CrucibleAction.CREATE, CrucibleAction.REOPEN, CrucibleAction.COMMENT,
                new CrucibleAction("My strange action", "action:myStrangeAction"));

    }

    public void testParseReviewDataWithAvatars() throws JDOMException, IOException, ParseException {
        final Document doc = loadDocument("reviewDataWithAvatars.xml");
        final Review review = CrucibleRestXmlHelper.parseFullReview(TEST_URL, "pniewiadomski", doc.getRootElement(),
                false);
        assertNotNull(review);
        assertNotNull(review.getAuthor());
        assertEquals(
                "https://secure.gravatar.com/avatar/4ba9395f438bc01f336c48adedd24532?s=48&d=https%3A//extranet.atlassian.com/crucible/avatar/j_doe%3Fs%3D48",
                review.getAuthor().getAvatarUrl());
    }


    public void testDate() {
        //2010-10-26T09:12:48-07:00
        //"2010-11-05T15:20:54.856Z'
        final DateTimeFormatter COMMENT_TIME_FORMATS[] = {DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")};


        Date d = COMMENT_TIME_FORMATS[0].parseDateTime("2010-10-26T09:12:48.000-07:00").toDate();
        assertEquals(d, CrucibleRestXmlHelper.parseDateTime("2010-10-26T09:12:48.000-07:00"));
        try {
            CrucibleRestXmlHelper.parseDateTime("2010-10-26T09:12:48-0700");
            fail();
        } catch (IllegalArgumentException e) {

        }


        d = COMMENT_TIME_FORMATS[2].parseDateTime("2010-10-26T09:12:48.000Z").toDate();
        assertEquals(d, CrucibleRestXmlHelper.parseDateTime("2010-10-26T09:12:48.000Z"));

        assertEquals(d, CrucibleRestXmlHelper.parseDateTime("2010-10-26T09:12:48Z"));
        assertEquals(d, CrucibleRestXmlHelper.parseDateTime("2010-10-26T09:12:48"));


    }

}
