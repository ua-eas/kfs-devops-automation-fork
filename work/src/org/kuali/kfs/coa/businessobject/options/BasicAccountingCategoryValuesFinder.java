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
package org.kuali.kfs.coa.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.coa.businessobject.BasicAccountingCategory;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.kns.service.KeyValuesService;
import org.kuali.rice.kns.web.ui.KeyLabelPair;

/**
 * This class creates a new finder for our forms view (creates a drop-down of {@link BasicAccountingCategory})
 */
public class BasicAccountingCategoryValuesFinder extends KeyValuesBase {

    /**
     * Creates a list of {@link BasicAccountingCategory} with their code as the key and their code and description as the display
     * value
     * 
     * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        Collection<BasicAccountingCategory> codes = SpringContext.getBean(KeyValuesService.class).findAllOrderBy(BasicAccountingCategory.class, "sortCode", true);
        List<KeyLabelPair> labels = new ArrayList<KeyLabelPair>();
        labels.add(new KeyLabelPair("", ""));
        for (BasicAccountingCategory basicAccountingCategory : codes) {
            if(basicAccountingCategory.isActive()) {
                labels.add(new KeyLabelPair(basicAccountingCategory.getCode(), basicAccountingCategory.getCodeAndDescription()));
            }
        }

        return labels;
    }

}
