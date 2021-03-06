/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.fp.document.validation.impl;

import static org.kuali.kfs.sys.KFSConstants.DOCUMENT_ERRORS;
import static org.kuali.kfs.sys.KFSKeyConstants.ERROR_DOCUMENT_ACCOUNTING_TWO_PERIODS;

import java.sql.Date;
import java.sql.Timestamp;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.fp.document.AuxiliaryVoucherDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 * Validation for Auxiliary Voucher documents that tests whether the accounting period for the document is within the defined grace period.
 */
public class AuxiliaryVoucherAccountingPeriodWithinGracePeriodValidation extends GenericValidation {
    private AuxiliaryVoucherDocument auxiliaryVoucherDocumentForValidation;
    private AccountingPeriodService accountingPeriodService;
    private UniversityDateService universityDateService;
    private ParameterService parameterService;

    /**
     * A validation to check if the given accounting period is within the "grace period" of the AV doc, defined in JIRA KULRNE-4634.
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        /*
         * Nota bene: a full summarization of these rules can be found in the comments to KULRNE-4634
         */
        // first we need to get the period itself to check these things
        boolean valid = true;
        AccountingPeriod acctPeriod = getAccountingPeriodService().getByPeriod(getAuxiliaryVoucherDocumentForValidation().getPostingPeriodCode(), getAuxiliaryVoucherDocumentForValidation().getPostingYear());

        Timestamp ts = new Timestamp(new java.util.Date().getTime());
        AccountingPeriod currPeriod = getAccountingPeriodService().getByDate(new Date(ts.getTime()));

        if (acctPeriod.getUniversityFiscalYear().equals(getUniversityDateService().getCurrentFiscalYear())) {
            if (getAccountingPeriodService().compareAccountingPeriodsByDate(acctPeriod, currPeriod) < 0) {
                // we've only got problems if the av's accounting period is earlier than now

                // are we in the grace period for this accounting period?
                if (!getAuxiliaryVoucherDocumentForValidation().calculateIfWithinGracePeriod(new Date(ts.getTime()), acctPeriod)) {
                    GlobalVariables.getMessageMap().putError(DOCUMENT_ERRORS, ERROR_DOCUMENT_ACCOUNTING_TWO_PERIODS);
                    return false;
                }
            }
        }
        else {
            // it's not the same fiscal year, so we need to test whether we are currently
            // in the grace period of the acctPeriod
            if (!getAuxiliaryVoucherDocumentForValidation().calculateIfWithinGracePeriod(new Date(ts.getTime()), acctPeriod)) {
                GlobalVariables.getMessageMap().putError(DOCUMENT_ERRORS, ERROR_DOCUMENT_ACCOUNTING_TWO_PERIODS);
                return false;
            }
        }

        return valid;
    }

    /**
     * Gets the auxiliaryVoucherDocumentForValidation attribute.
     * @return Returns the auxiliaryVoucherDocumentForValidation.
     */
    public AuxiliaryVoucherDocument getAuxiliaryVoucherDocumentForValidation() {
        return auxiliaryVoucherDocumentForValidation;
    }

    /**
     * Sets the auxiliaryVoucherDocumentForValidation attribute value.
     * @param auxiliaryVoucherDocumentForValidation The auxiliaryVoucherDocumentForValidation to set.
     */
    public void setAuxiliaryVoucherDocumentForValidation(AuxiliaryVoucherDocument accountingDocumentForValidation) {
        this.auxiliaryVoucherDocumentForValidation = accountingDocumentForValidation;
    }

    /**
     * Gets the accountingPeriodService attribute.
     * @return Returns the accountingPeriodService.
     */
    public AccountingPeriodService getAccountingPeriodService() {
        return accountingPeriodService;
    }

    /**
     * Sets the accountingPeriodService attribute value.
     * @param accountingPeriodService The accountingPeriodService to set.
     */
    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    /**
     * Gets the universityDateService attribute.
     * @return Returns the universityDateService.
     */
    public UniversityDateService getUniversityDateService() {
        return universityDateService;
    }

    /**
     * Sets the universityDateService attribute value.
     * @param universityDateService The universityDateService to set.
     */
    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    /**
     * Gets the parameterService attribute.
     * @return Returns the parameterService.
     */
    public ParameterService getParameterService() {
        return parameterService;
    }

    /**
     * Sets the parameterService attribute value.
     * @param parameterService The parameterService to set.
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
