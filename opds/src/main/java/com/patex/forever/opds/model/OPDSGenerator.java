/*
 * Copyright 2004 Sun Microsystems, Inc.
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
 *
 */
package com.patex.forever.opds.model;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.atom.Category;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Generator;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.impl.BaseWireFeedGenerator;
import com.rometools.rome.io.impl.DateParser;
import com.rometools.utils.Lists;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Feed Generator for Atom
 * <p/>
 */
public class OPDSGenerator extends BaseWireFeedGenerator {

    private static final String ATOM_10_URI = "http://www.w3.org/2005/Atom";
    private static final Namespace ATOM_NS = Namespace.getNamespace(ATOM_10_URI);

    private static final Namespace DC_NS = Namespace.getNamespace("dc", "http://purl.org/dc/terms/");
    private static final Namespace OS_NS = Namespace.getNamespace("os", "http://a9.com/-/spec/opensearch/1.1/");
    private static final Namespace OPDS_NS = Namespace.getNamespace("opds", "http://opds-spec.org/2010/catalog");


    private final String version;

    @SuppressWarnings("unused")
    public OPDSGenerator() {
        super("opds.atom_1.0");
        this.version = "1.0";
    }

    @SuppressWarnings("unused")
    public OPDSGenerator(final String type, final String version) {
        super(type);
        this.version = version;
    }

    protected String getVersion() {
        return version;
    }

    private Namespace getFeedNamespace() {
        return ATOM_NS;
    }

    @Override
    public Document generate(final WireFeed wFeed) throws FeedException {
        final Feed feed = (Feed) wFeed;
        final Element root = createRootElement(feed);
        populateFeed(feed, root);
//        purgeUnusedNamespaceDeclarations(root);
        return createDocument(root);
    }

    private Document createDocument(final Element root) {
        return new Document(root);
    }

    private Element createRootElement(final Feed feed) {

        final Element root = new Element("feed", getFeedNamespace());

        root.addNamespaceDeclaration(getFeedNamespace());
        root.addNamespaceDeclaration(DC_NS);
        root.addNamespaceDeclaration(OS_NS);
        root.addNamespaceDeclaration(OPDS_NS);

        // Attribute version = new Attribute("version", getVersion());
        // root.setAttribute(version);

        final String xmlBase = feed.getXmlBase();
        if (xmlBase != null) {
            root.setAttribute("base", xmlBase, Namespace.XML_NAMESPACE);
        }

        generateModuleNamespaceDefs(root);

        return root;

    }

    private void populateFeed(final Feed feed, final Element parent) throws FeedException {
        addFeed(feed, parent);
        addEntries(feed, parent);
    }

    private void addFeed(final Feed feed, final Element parent) throws FeedException {
        populateFeedHeader(feed, parent);
        generateForeignMarkup(parent, feed.getForeignMarkup());
        generateFeedModules(feed.getModules(), parent);
    }

    private void addEntries(final Feed feed, final Element parent) throws FeedException {
        final List<Entry> items = feed.getEntries();
        for (final Entry entry : items) {
            addEntry(entry, parent);
        }
    }

    private void addEntry(final Entry entry, final Element parent) throws FeedException {

        final Element eEntry = new Element("entry", getFeedNamespace());

        final String xmlBase = entry.getXmlBase();
        if (xmlBase != null) {
            eEntry.setAttribute("base", xmlBase, Namespace.XML_NAMESPACE);
        }

        populateEntry(entry, eEntry);
        generateForeignMarkup(eEntry, entry.getForeignMarkup());
        generateItemModules(entry.getModules(), eEntry);
        parent.addContent(eEntry);

    }

    private void populateFeedHeader(final Feed feed, final Element eFeed) throws FeedException {

        final Content titleEx = feed.getTitleEx();
        if (titleEx != null) {
            final Element titleElement = new Element("title", getFeedNamespace());
            fillContentElement(titleElement, titleEx);
            eFeed.addContent(titleElement);
        }

        final List<Link> alternateLinks = feed.getAlternateLinks();
        if (alternateLinks != null) {
            for (final Link link : alternateLinks) {
                eFeed.addContent(generateLinkElement(link));
            }
        }

        final List<Link> otherLinks = feed.getOtherLinks();
        if (otherLinks != null) {
            for (final Link link : otherLinks) {
                eFeed.addContent(generateLinkElement(link));
            }
        }

        final List<Category> cats = feed.getCategories();
        if (cats != null) {
            for (final Category category : cats) {
                eFeed.addContent(generateCategoryElement(category));
            }
        }

        addPersons(eFeed, feed.getAuthors(), "author");
        addPersons(eFeed, feed.getContributors(), "contributor");

        final Content subtitle = feed.getSubtitle();
        if (subtitle != null) {
            final Element subtitleElement = new Element("subtitle", getFeedNamespace());
            fillContentElement(subtitleElement, subtitle);
            eFeed.addContent(subtitleElement);
        }

        final String id = feed.getId();
        if (id != null) {
            eFeed.addContent(generateSimpleElement("id", id));
        }

        final Generator generator = feed.getGenerator();
        if (generator != null) {
            eFeed.addContent(generateGeneratorElement(generator));
        }

        final String rights = feed.getRights();
        if (rights != null) {
            eFeed.addContent(generateSimpleElement("rights", rights));
        }

        final String icon = feed.getIcon();
        if (icon != null) {
            eFeed.addContent(generateSimpleElement("icon", icon));
        }

        final String logo = feed.getLogo();
        if (logo != null) {
            eFeed.addContent(generateSimpleElement("logo", logo));
        }

        final Date updated = feed.getUpdated();
        if (updated != null) {
            final Element updatedElement = new Element("updated", getFeedNamespace());
            updatedElement.addContent(DateParser.formatW3CDateTime(updated, Locale.US));
            eFeed.addContent(updatedElement);
        }

    }

    private void addPersons(Element feed, List<SyndPerson> persons, String elementName) {
        if (Lists.isNotEmpty(persons)) {
            for (final SyndPerson author : persons) {
                final Element authorElement = new Element(elementName, getFeedNamespace());
                fillPersonElement(authorElement, author);
                feed.addContent(authorElement);
            }
        }
    }

    private void populateEntry(final Entry entry, final Element eEntry) throws FeedException {

        final Content titleEx = entry.getTitleEx();
        if (titleEx != null) {
            final Element titleElement = new Element("title", getFeedNamespace());
            fillContentElement(titleElement, titleEx);
            eEntry.addContent(titleElement);
        }

        final List<Link> alternateLinks = entry.getAlternateLinks();
        if (alternateLinks != null) {
            for (final Link link : alternateLinks) {
                eEntry.addContent(generateLinkElement(link));
            }
        }

        final List<Link> otherLinks = entry.getOtherLinks();
        if (otherLinks != null) {
            for (final Link link : otherLinks) {
                eEntry.addContent(generateLinkElement(link));
            }
        }

        final List<Category> cats = entry.getCategories();
        if (cats != null) {
            for (final Category category : cats) {
                eEntry.addContent(generateCategoryElement(category));
            }
        }

        addPersons(eEntry, entry.getAuthors(), "author");
        addPersons(eEntry, entry.getContributors(), "contributor");

        final String id = entry.getId();
        if (id != null) {
            eEntry.addContent(generateSimpleElement("id", id));
        }

        final Date updated = entry.getUpdated();
        if (updated != null) {
            final Element updatedElement = new Element("updated", getFeedNamespace());
            updatedElement.addContent(DateParser.formatW3CDateTime(updated, Locale.US));
            eEntry.addContent(updatedElement);
        }

        final Date published = entry.getPublished();
        if (published != null) {
            final Element publishedElement = new Element("published", getFeedNamespace());
            publishedElement.addContent(DateParser.formatW3CDateTime(published, Locale.US));
            eEntry.addContent(publishedElement);
        }

        final List<Content> contents = entry.getContents();
        if (Lists.isNotEmpty(contents)) {
            for (Content content : contents) {
                final Element contentElement = new Element("content", getFeedNamespace());
                fillContentElement(contentElement, content);
                eEntry.addContent(contentElement);
            }
        }

        final Content summary = entry.getSummary();
        if (summary != null) {
            final Element summaryElement = new Element("summary", getFeedNamespace());
            fillContentElement(summaryElement, summary);
            eEntry.addContent(summaryElement);
        }

        final Feed source = entry.getSource();
        if (source != null) {
            final Element sourceElement = new Element("source", getFeedNamespace());
            populateFeedHeader(source, sourceElement);
            eEntry.addContent(sourceElement);
        }

        final String rights = entry.getRights();
        if (rights != null) {
            eEntry.addContent(generateSimpleElement("rights", rights));
        }

    }

    private Element generateCategoryElement(final Category cat) {

        final Namespace namespace = getFeedNamespace();
        final Element catElement = new Element("category", namespace);

        final String term = cat.getTerm();
        if (term != null) {
            final Attribute termAttribute = new Attribute("term", term);
            catElement.setAttribute(termAttribute);
        }

        final String label = cat.getLabel();
        if (label != null) {
            final Attribute labelAttribute = new Attribute("label", label);
            catElement.setAttribute(labelAttribute);
        }

        final String scheme = cat.getScheme();
        if (scheme != null) {
            final Attribute schemeAttribute = new Attribute("scheme", scheme);
            catElement.setAttribute(schemeAttribute);
        }

        return catElement;

    }

    private Element generateLinkElement(final Link link) {

        final Namespace namespace = getFeedNamespace();
        final Element linkElement = new Element("link", namespace);

        final String rel = link.getRel();
        if (rel != null) {
            final Attribute relAttribute = new Attribute("rel", rel);
            linkElement.setAttribute(relAttribute);
        }

        final String type = link.getType();
        if (type != null) {
            final Attribute typeAttribute = new Attribute("type", type);
            linkElement.setAttribute(typeAttribute);
        }

        final String href = link.getHref();
        if (href != null) {
            final Attribute hrefAttribute = new Attribute("href", href);
            linkElement.setAttribute(hrefAttribute);
        }

        final String hreflang = link.getHreflang();
        if (hreflang != null) {
            final Attribute hreflangAttribute = new Attribute("hreflang", hreflang);
            linkElement.setAttribute(hreflangAttribute);
        }

        final String linkTitle = link.getTitle();
        if (linkTitle != null) {
            final Attribute title = new Attribute("title", linkTitle);
            linkElement.setAttribute(title);
        }

        if (link.getLength() != 0) {
            final Attribute lenght = new Attribute("length", Long.toString(link.getLength()));
            linkElement.setAttribute(lenght);
        }

        return linkElement;

    }

    private void fillPersonElement(final Element element, final SyndPerson person) {

        final String name = person.getName();
        if (name != null) {
            element.addContent(generateSimpleElement("name", name));
        }

        final String uri = person.getUri();
        if (uri != null) {
            element.addContent(generateSimpleElement("uri", uri));
        }

        final String email = person.getEmail();
        if (email != null) {
            element.addContent(generateSimpleElement("email", email));
        }

        generatePersonModules(person.getModules(), element);

    }

    private void fillContentElement(final Element contentElement, final Content content) throws FeedException {

        final String type = content.getType();

        String atomType = type;

        if (type != null) {
            // Fix for issue #39 "Atom 1.0 Text Types Not Set Correctly"
            // we're not sure who set this value, so ensure Atom types are used
            if ("text/plain".equals(type)) {
                atomType = Content.TEXT;
            } else if ("text/html".equals(type)) {
                atomType = Content.HTML;
            } else if ("application/xhtml+xml".equals(type)) {
                atomType = Content.XHTML;
            }

            final Attribute typeAttribute = new Attribute("type", type);
            contentElement.setAttribute(typeAttribute);
        }

        final String href = content.getSrc();
        if (href != null) {
            final Attribute srcAttribute = new Attribute("src", href);
            contentElement.setAttribute(srcAttribute);
        }

        final String value = content.getValue();
        if (value != null) {

            if (atomType != null && (atomType.equals(Content.XHTML) || atomType.contains("/xml") || atomType.contains("+xml"))) {

                String tmpDocString = "<tmpdoc>" + value +
                        "</tmpdoc>";
                final StringReader tmpDocReader = new StringReader(tmpDocString);
                Document tmpDoc;
                try {
                    final SAXBuilder saxBuilder = new SAXBuilder();
                    tmpDoc = saxBuilder.build(tmpDocReader);
                } catch (final Exception ex) {
                    throw new FeedException("Invalid XML", ex);
                }
                final List<org.jdom2.Content> children = tmpDoc.getRootElement().removeContent();
                contentElement.addContent(children);

            } else {

                // must be type html, text or some other non-XML format
                // JDOM will escape property for XML
                contentElement.addContent(value);

            }

        }
    }

    private Element generateGeneratorElement(final Generator generator) {

        final Element generatorElement = new Element("generator", getFeedNamespace());

        final String url = generator.getUrl();
        if (url != null) {
            final Attribute urlAttribute = new Attribute("uri", url);
            generatorElement.setAttribute(urlAttribute);
        }

        final String version2 = generator.getVersion();
        if (version2 != null) {
            final Attribute versionAttribute = new Attribute("version", version2);
            generatorElement.setAttribute(versionAttribute);
        }

        final String value = generator.getValue();
        if (value != null) {
            generatorElement.addContent(value);
        }

        return generatorElement;

    }

    private Element generateSimpleElement(final String name, final String value) {
        final Element element = new Element(name, getFeedNamespace());
        element.addContent(value);
        return element;
    }
}
