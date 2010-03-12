/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.sys.batch;

import java.util.Date;

import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.batch.service.AutoDisapproveEDocsService;

/**
 * Runs the batch job that gathers all EDocs that are in ENROUTE status and cancels them.
 */
public class AutoDisapproveEDocsStep extends AbstractStep {
    private AutoDisapproveEDocsService autoDisapproveEDocsService;
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AutoDisapproveEDocsStep.class);
    
    /**
     * This step will auto disapprove the EDocs that are in ENROUTE status.
     * 
     * @return true if the job completed successfully, false if otherwise
     * @see org.kuali.kfs.sys.batch.Step#execute(String, Date)
     */
    public boolean execute(String jobName, Date jobRunDate) {
        if (systemParametersForAutoDisapproveEDocsJobExist()) {
            if (canAutoDisapproveJobRun()) {
                autoDisapproveEDocsService.autoDisapproveEDocsInEnrouteStatus();
            }
        }
        
        return true;            
    }

    /**
     * This method checks if the System parameters have been set up for this batch job.
     * @result return true if the system parameters exist, else false
     */
    public boolean systemParametersForAutoDisapproveEDocsJobExist() {
        boolean systemParametersExists = true;
        
        // check to make sure the system parameter for run date check has already been setup...
        if (!getParameterService().parameterExists(AutoDisapproveEDocsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_EDOCS_STEP_RUN_DATE)) {
          LOG.warn("YEAR_END_AUTO_DISAPPROVE_EDOCS_STEP_RUN_DATE System parameter does not exist in the parameters list.  The job can not continue without this parameter");
          return false;
        }
        
        // check to make sure the system parameter for Parent Document Type = FP has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveEDocsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE)) {
          LOG.warn("YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE System parameter does not exist in the parameters list.  The job can not continue without this parameter");
          return false;
        }
        
        // check to make sure the system parameter for Parent Document Type = FP has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveEDocsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE)) {
          LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE System parameter does not exist in the parameters list.  The job can not continue without this parameter");
          return false;
        }
        
        // check to make sure the system parameter for Parent Document Type = FP has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveEDocsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES)) {
          LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter does not exist in the parameters list.  The job can not continue without this parameter");
          return false;
        }
        
        // check to make sure the system parameter for Parent Document Type = FP has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveEDocsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_ANNOTATION)) {
          LOG.warn("YEAR_END_AUTO_DISAPPROVE_ANNOTATION System parameter does not exist in the parameters list.  The job can not continue without this parameter");
          return false;
        }        
        
        return systemParametersExists;
    }
    
    /**
     * This method will compare today's date to the system parameter for year end auto disapproval run date
     * @return true if today's date equals to the system parameter run date
     */
    protected boolean canAutoDisapproveJobRun() {
      boolean autoDisapproveCanRun = true;
      
      // IF trunc(SYSDATE - 14/24) = v_yec_cncl_doc_run_dt THEN...FIS CODE equivalent here...
      String yearEndAutoDisapproveRunDate = getParameterService().getParameterValue(AutoDisapproveEDocsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_EDOCS_STEP_RUN_DATE);
      
      String today = getDateTimeService().toDateString(getDateTimeService().getCurrentDate());
      
      if (!yearEndAutoDisapproveRunDate.equals(today)) {
          LOG.warn("Automatic disapproval bypassed - date test failed. The date on which the auto disapproval step should run: " + yearEndAutoDisapproveRunDate + " does not equal to today's date: " + today);
          autoDisapproveCanRun = false;
      }
      
      return autoDisapproveCanRun;
    }
        
    /**
     * Sets the autoDisapproveEDocsService attribute value.
     * 
     * @param autoDisapproveEDocsService The autoDisapproveEDocsService to set.
     * @see org.kuali.kfs.sys.service.AutoDisapproveEDocsService
     */
    public void setAutoDisapproveEDocsService(AutoDisapproveEDocsService autoDisapproveEDocsService) {
        this.autoDisapproveEDocsService = autoDisapproveEDocsService;
    }
}
