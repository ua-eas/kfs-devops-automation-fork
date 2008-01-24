/*
 * Copyright 2008 The Kuali Foundation.
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
package org.kuali.module.effort.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kuali.core.service.BusinessObjectService;
import org.kuali.core.service.PersistenceService;
import org.kuali.kfs.context.KualiTestBase;
import org.kuali.kfs.context.SpringContext;
import org.kuali.kfs.util.ObjectUtil;
import org.kuali.module.effort.EffortConstants.SystemParameters;
import org.kuali.module.effort.bo.EffortCertificationDetailBuild;
import org.kuali.module.effort.bo.EffortCertificationReportDefinition;
import org.kuali.module.gl.web.TestDataGenerator;
import org.kuali.module.labor.bo.LedgerBalance;
import org.kuali.module.labor.util.TestDataPreparator;
import org.kuali.test.ConfigureContext;

@ConfigureContext
public class EffortCertificationDetailBuildServiceTest extends KualiTestBase {

    private Properties properties, message;
    private String balanceFieldNames, detailFieldNames;
    private String deliminator;
    Integer postingYear;

    private EffortCertificationReportDefinition reportDefinition;

    private BusinessObjectService businessObjectService;
    private PersistenceService persistenceService;
    private EffortCertificationDetailBuildService effortCertificationDetailBuildService;

    /**
     * Constructs a EffortCertificationDetailBuildServiceTest.java.
     */
    public EffortCertificationDetailBuildServiceTest() {
        super();
        String messageFileName = "test/src/org/kuali/module/effort/testdata/message.properties";
        String propertiesFileName = "test/src/org/kuali/module/effort/testdata/effortCertificationDetailBuildService.properties";

        TestDataGenerator generator = new TestDataGenerator(propertiesFileName, messageFileName);
        properties = generator.getProperties();
        message = generator.getMessage();

        deliminator = properties.getProperty("deliminator");

        balanceFieldNames = properties.getProperty("balanceFieldNames");
        detailFieldNames = properties.getProperty("detailFieldNames");
        postingYear = Integer.valueOf(properties.getProperty("postingYear")); 
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        persistenceService = SpringContext.getBean(PersistenceService.class);
        effortCertificationDetailBuildService = SpringContext.getBean(EffortCertificationDetailBuildService.class);

        this.doCleanUp();
    }

    /**
     * test if a build detail line can be generated from the specified ledger balance whose sub account is not in sub account table
     */
    public void testGenerateDetailBuild_NullSubAccount() throws Exception {
        String testTarget = "generateDetailBuild.nullSubAccount.";        
        reportDefinition = this.buildReportDefinition("");
        this.assertDetailEquals(testTarget);
    }

    /**
     * test if a build detail line can be generated from the specified ledger balance whose sub account is of expense type.
     */
    public void testGenerateDetailBuild_ExpenseSubAccount() throws Exception {
        String testTarget = "generateDetailBuild.expenseSubAccount.";        
        reportDefinition = this.buildReportDefinition("");
        this.assertDetailEquals(testTarget);
    }

    /**
     * test if a build detail line can be generated from the specified ledger balance whose sub account is of cost share type.
     */
    public void testGenerateDetailBuild_CostShareSubAccount() throws Exception {
        String testTarget = "generateDetailBuild.costShareSubAccount.";
        reportDefinition = this.buildReportDefinition("");
        this.assertDetailEquals(testTarget);
    }

    /**
     * test if a build detail line can be generated from the specified ledger balance with the sub account whose type is not null
     * and the specified as the system parameters
     */
    public void testGenerateDetailBuild_UnspecifiedTypeSubAccount() throws Exception {
        String testTarget = "generateDetailBuild.unspecifiedTypeSubAccount.";        
        reportDefinition = this.buildReportDefinition("");        
        this.assertDetailEquals(testTarget);
    }

    /**
     * test if a build detail line can be generated from the specified ledger balance approperitely for the customized long period
     */
    public void testGenerateDetailBuild_LongReportPeriod() throws Exception {
        String testTarget = "generateDetailBuild.longReportPeriod.";
        reportDefinition = this.buildReportDefinition(testTarget);
        this.assertDetailEquals(testTarget);
    }

    /**
     * compare the resulting detail line with the expected
     * 
     * @param testTarget the given test target that specifies the test data being used
     */
    private void assertDetailEquals(String testTarget) {
        List<String> keyFields = ObjectUtil.split(detailFieldNames, deliminator);
        Map<String, List<String>> systemParameters = this.buildSystemParameterMap(testTarget);

        LedgerBalance ledgerBalance = this.buildLedgerBalance(testTarget);

        EffortCertificationDetailBuild detailBuild = effortCertificationDetailBuildService.generateDetailBuild(postingYear, ledgerBalance, reportDefinition, systemParameters);

        EffortCertificationDetailBuild expectedDetailBuild = TestDataPreparator.buildTestDataObject(EffortCertificationDetailBuild.class, properties, testTarget + "expectedDetail", detailFieldNames, deliminator);

        String errorMessage = message.getProperty("error.detailBuildService.unexpectedDetailLineGenerated");
        assertTrue(errorMessage, ObjectUtil.compareObject(detailBuild, expectedDetailBuild, keyFields));
    }

    /**
     * construct a ledger balance and persist it
     * 
     * @param testTarget the given test target that specifies the test data being used
     * @return a ledger balance
     */
    private LedgerBalance buildLedgerBalance(String testTarget) {
        LedgerBalance ledgerBalance = TestDataPreparator.buildTestDataObject(LedgerBalance.class, properties, testTarget + "inputBalance", balanceFieldNames, deliminator);
        businessObjectService.save(ledgerBalance);
        persistenceService.retrieveNonKeyFields(ledgerBalance);

        return ledgerBalance;
    }

    /**
     * construct a system parameter map
     * 
     * @param testTarget the given test target that specifies the test data being used
     * @return a system parameter map
     */
    private Map<String, List<String>> buildSystemParameterMap(String testTarget) {
        List<String> expSubAccountType = ObjectUtil.split(properties.getProperty(testTarget + "systemParameter.EXPENSE_SUB_ACCOUNT_TYPE_CODE"), deliminator);
        List<String> csSubAccountType = ObjectUtil.split(properties.getProperty(testTarget + "systemParameter.COST_SHARE_SUB_ACCOUNT_TYPE_CODE"), deliminator);

        Map<String, List<String>> systemParameters = new HashMap<String, List<String>>();
        systemParameters.put(SystemParameters.EXPENSE_SUB_ACCOUNT_TYPE_CODE, expSubAccountType);
        systemParameters.put(SystemParameters.COST_SHARE_SUB_ACCOUNT_TYPE_CODE, csSubAccountType);

        return systemParameters;
    }

    /**
     * build a report defintion object from the given test target
     * 
     * @param testTarget the given test target that specifies the test data being used
     * @return a report defintion object
     */
    private EffortCertificationReportDefinition buildReportDefinition(String testTarget) {
        EffortCertificationReportDefinition reportDefinition = new EffortCertificationReportDefinition();
        String reprtDefinitionFieldNames = properties.getProperty("reportDefinitionFieldNames");
        ObjectUtil.populateBusinessObject(reportDefinition, properties, testTarget + "reportDefinitionFieldValues", reprtDefinitionFieldNames, deliminator);
        
        return reportDefinition;
    }
    
    /**
     * remove the existing data from the database so that they cannot affact the test results 
     */
    private void doCleanUp() throws Exception {
        LedgerBalance cleanup = new LedgerBalance();
        ObjectUtil.populateBusinessObject(cleanup, properties, "dataCleanup", balanceFieldNames, deliminator);
        Map<String, Object> fieldValues = ObjectUtil.buildPropertyMap(cleanup, Arrays.asList(StringUtils.split(balanceFieldNames, deliminator)));
        businessObjectService.deleteMatching(LedgerBalance.class, fieldValues);
    }
}
