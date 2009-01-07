/*
 * Copyright 2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.purap.document.searching.processor;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.kew.docsearch.DocumentSearchColumn;
import org.kuali.rice.kew.docsearch.DocumentSearchResult;

public class RequisitionDocumentSearchResultProcessor extends PurApDocumentSearchResultProcessor {

    /**
     * @see org.kuali.workflow.module.purap.docsearch.KualiPurApDocumentSearchResultProcessor#getDocumentSpecificCustomColumns()
     */
    @Override
    public List<DocumentSearchColumn> getDocumentSpecificCustomColumns() {
        List<DocumentSearchColumn> columns = new ArrayList<DocumentSearchColumn>();
        List<String> searchableAttributeFieldNames = new ArrayList<String>();
        searchableAttributeFieldNames.add("requisitionDocumentRequisitionId");
        searchableAttributeFieldNames.add("requisitionDocumentStatusDescription");
        searchableAttributeFieldNames.add("purapDocumentOrgDisplayValue");
        searchableAttributeFieldNames.add("documentHeaderDescription");
        searchableAttributeFieldNames.add("documentTotalAmount");
        searchableAttributeFieldNames.add("requisitionDocumentVendorName");
        addSearchableAttributeColumnsBasedOnFields(columns, getSearchCriteria(), searchableAttributeFieldNames);
        addColumnUsingKey(columns, DocumentSearchResult.PROPERTY_NAME_DATE_CREATED);
        return columns;
    }

}
