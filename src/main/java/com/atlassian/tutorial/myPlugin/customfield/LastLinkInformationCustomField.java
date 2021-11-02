package com.atlassian.tutorial.myPlugin.customfield;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Scanned
public class LastLinkInformationCustomField extends GenericTextCFType {
    private static final String DEFAULT_VALUE = "There are no links";

    public LastLinkInformationCustomField(
            @JiraImport CustomFieldValuePersister customFieldValuePersister,
            @JiraImport GenericConfigManager genericConfigManager,
            @JiraImport TextFieldCharacterLengthValidator textFieldCharacterLengthValidator,
            @JiraImport JiraAuthenticationContext jiraAuthenticationContext) {
        super(customFieldValuePersister, genericConfigManager, textFieldCharacterLengthValidator, jiraAuthenticationContext);
    }

    private Optional<IssueLink> getIssueLink(final Issue issue) {
        List<IssueLink> inwardLinks = ComponentAccessor.getIssueLinkManager().getInwardLinks(issue.getId());
        List<IssueLink> outwardLinks = ComponentAccessor.getIssueLinkManager().getOutwardLinks(issue.getId());
        List<IssueLink> links = new ArrayList<>();
        links.addAll(outwardLinks);
        links.addAll(inwardLinks);

        return links.stream().sorted(Comparator.comparing(IssueLink::getId, Collections.reverseOrder())).findFirst();
    }

    @Nullable
    @Override
    public String getValueFromIssue(CustomField field, Issue issue) {
        Optional<IssueLink> link = getIssueLink(issue);
        if (link.isPresent()) {
            if (link.get().getSourceObject().getKey().equals(issue.getKey())) {
                return link.get().getDestinationObject().getKey();
            } else {
                return link.get().getSourceObject().getKey();
            }
        } else {
            return DEFAULT_VALUE;
        }
    }

    @Override
    public void createValue(CustomField field, Issue issue, @Nonnull String value) {
        super.createValue(field, issue, DEFAULT_VALUE);
    }

    @Override
    public void updateValue(CustomField customField, Issue issue, String value) {
        Optional<IssueLink> link = getIssueLink(issue);
        if (link.isPresent()) {
            if (link.get().getSourceObject().getKey().equals(issue.getKey())) {
                super.updateValue(customField, issue, link.get().getDestinationObject().getKey());
            } else {
                super.updateValue(customField, issue, link.get().getSourceObject().getKey());
            }
        } else {
            super.updateValue(customField, issue, DEFAULT_VALUE);
        }
    }

    @Override
    public String getDefaultValue(FieldConfig fieldConfig) {
        return DEFAULT_VALUE;
    }
}
