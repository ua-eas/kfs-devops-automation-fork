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
package org.kuali.module.budget.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.core.service.BusinessObjectService;
import org.kuali.core.service.KualiConfigurationService;
import org.kuali.kfs.KFSPropertyConstants;
import org.kuali.module.budget.bo.BudgetConstructionOrgSalaryStatisticsReport;
import org.kuali.module.budget.bo.BudgetConstructionSalaryTotal;
import org.kuali.module.budget.dao.BudgetConstructionSalaryStatisticsReportDao;
import org.kuali.module.budget.service.BudgetConstructionOrganizationReportsService;
import org.kuali.module.budget.service.BudgetConstructionSalaryStatisticsReportService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation of BudgetConstructionAccountSummaryReportService.
 */
@Transactional
public class BudgetConstructionSalaryStatisticsReportServiceImpl implements BudgetConstructionSalaryStatisticsReportService {

    BudgetConstructionSalaryStatisticsReportDao budgetConstructionSalaryStatisticsReportDao;
    BudgetConstructionOrganizationReportsService budgetConstructionOrganizationReportsService;
    KualiConfigurationService kualiConfigurationService;
    BusinessObjectService businessObjectService;


    

    public void updateSalaryStatisticsReport(String personUserIdentifier, Integer universityFiscalYear) {
        budgetConstructionSalaryStatisticsReportDao.updateReportsSalaryStatisticsTable(personUserIdentifier, universityFiscalYear); 
            
    }

    public Collection<BudgetConstructionOrgSalaryStatisticsReport> buildReports(Integer universityFiscalYear, String personUserIdentifier) {
        Collection<BudgetConstructionOrgSalaryStatisticsReport> reportSet = new ArrayList();

        
        BudgetConstructionOrgSalaryStatisticsReport orgSalaryStatisticsReportEntry;
        // build searchCriteria
        Map searchCriteria = new HashMap();
        searchCriteria.put(KFSPropertyConstants.KUALI_USER_PERSON_UNIVERSAL_IDENTIFIER, personUserIdentifier);

        // build order list
        List<String> orderList = buildOrderByList();
        Collection<BudgetConstructionSalaryTotal> salarySummaryList = budgetConstructionOrganizationReportsService.getBySearchCriteriaOrderByList(BudgetConstructionSalaryTotal.class, searchCriteria, orderList);
        
        
        String test = "";
        
        
        return reportSet;
    }

    
    
    
    /**
     * builds orderByList for sort order.
     * 
     * @return returnList
     */
    public List<String> buildOrderByList() {
        List<String> returnList = new ArrayList();
        /*returnList.add(KFSPropertyConstants.ORGANIZATION_CHART_OF_ACCOUNTS_CODE);
        returnList.add(KFSPropertyConstants.ORGANIZATION_CODE);
        returnList.add(KFSPropertyConstants.SUB_FUND_GROUP_CODE);
        returnList.add(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
        returnList.add(KFSPropertyConstants.INCOME_EXPENSE_CODE);
        returnList.add(KFSPropertyConstants.FINANCIAL_CONSOLIDATION_SORT_CODE);
        returnList.add(KFSPropertyConstants.FINANCIAL_LEVEL_SORT_CODE);
        returnList.add(KFSPropertyConstants.EMPLID);
        */
        return returnList;
    }
  
    
    public void setBudgetConstructionSalaryStatisticsReportDao(BudgetConstructionSalaryStatisticsReportDao budgetConstructionSalaryStatisticsReportDao) {
        this.budgetConstructionSalaryStatisticsReportDao = budgetConstructionSalaryStatisticsReportDao;
    }

    public void setBudgetConstructionOrganizationReportsService(BudgetConstructionOrganizationReportsService budgetConstructionOrganizationReportsService) {
        this.budgetConstructionOrganizationReportsService = budgetConstructionOrganizationReportsService;
    }


    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setKualiConfigurationService(KualiConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

}
