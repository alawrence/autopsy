/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.keywordsearch;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.sleuthkit.autopsy.keywordsearch.Server.Core;
import org.sleuthkit.datamodel.Content;

/**
 * Gets extracted content from Solr with the parts that match the query
 * highlighted
 */
class HighlightedMatchesSource implements MarkupSource {

    private static final Logger logger = Logger.getLogger(HighlightedMatchesSource.class.getName());
    private static final String HIGHLIGHT_PRE = "<span style=\"background:yellow\">";
    private static final String HIGHLIGHT_POST = "</span>";
    private static final String ANCHOR_PREFIX = HighlightedMatchesSource.class.getName() + "_";
    private Content content;
    private String solrQuery;
    private Core solrCore;
    private int numberHits;

    HighlightedMatchesSource(Content content, String solrQuery) {
        this(content, solrQuery, KeywordSearch.getServer().getCore());
    }

    HighlightedMatchesSource(Content content, String solrQuery, Core solrCore) {
        this.content = content;
        this.solrQuery = solrQuery;
        this.solrCore = solrCore;
    }

    @Override
    public String getMarkup() {

        SolrQuery q = new SolrQuery();
        final String queryEscaped = KeywordSearchUtil.escapeLuceneQuery(solrQuery, true, false);

        q.setQuery(queryEscaped);
        q.addFilterQuery("id:" + content.getId());
        q.addHighlightField("content"); //for exact highlighting, try content_ws field (with stored="true" in Solr schema)
        q.setHighlightSimplePre(HIGHLIGHT_PRE);
        q.setHighlightSimplePost(HIGHLIGHT_POST);
        q.setHighlightFragsize(0); // don't fragment the highlight

        try {
            QueryResponse response = solrCore.query(q, METHOD.POST);
            Map<String, Map<String, List<String>>> responseHighlight = response.getHighlighting();
            long contentID = content.getId();
            Map<String, List<String>> responseHighlightID = responseHighlight.get(Long.toString(contentID));
            final String NO_MATCHES = "<span style=\"background:red\">No matches in content.</span>";
            if (responseHighlightID == null) {
                return NO_MATCHES;
            }
            List<String> contentHighlights = responseHighlightID.get("content");
            if (contentHighlights == null) {
                return NO_MATCHES;
            } else {
                // extracted content (minus highlight tags) is HTML-escaped
                String highlightedContent = contentHighlights.get(0).trim();
                highlightedContent = insertAnchors(highlightedContent);
                return "<pre>" + highlightedContent + "</pre>";
            }
        } catch (SolrServerException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return "Search Matches";
    }

    @Override
    public boolean isSearchable() {
        return true;
    }

    @Override
    public String getAnchorPrefix() {
        return ANCHOR_PREFIX;
    }

    @Override
    public int getNumberHits() {
        return numberHits;
    }

    private String insertAnchors(String searchableContent) {
        int searchOffset = 0;
        int index = -1;

        StringBuilder buf = new StringBuilder(searchableContent);

        final String searchToken = HIGHLIGHT_PRE;
        final int indexSearchTokLen = searchToken.length();
        final String insertPre = "<a name=\"" + ANCHOR_PREFIX;
        final String insertPost = "\"></a>";
        int count = 0;
        while ((index = buf.indexOf(searchToken, searchOffset)) >= 0) {
            String insertString = insertPre + Integer.toString(count) + insertPost;
            int insertStringLen = insertString.length();
            buf.insert(index, insertString);
            searchOffset = index + indexSearchTokLen + insertStringLen; //next offset past this anchor
            ++count;
        }

        this.numberHits = count;
        return buf.toString();
    }
}
