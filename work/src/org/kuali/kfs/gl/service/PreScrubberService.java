/*
 * Copyright 2009 The Kuali Foundation.
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
package org.kuali.kfs.gl.service;

import java.io.IOException;
import java.util.Iterator;

public interface PreScrubberService {
    public void preprocessOriginEntries(Iterator<String> inputOriginEntries, String outputFileName) throws IOException;
    
    /**
     * Returns whether chart of accounts codes that are spaces should be derived from the account number.  This assumes that a particular account
     * number may be associated with at most one chart of accounts code.
     */
    public boolean deriveChartOfAccountsCodeIfSpaces();
}
