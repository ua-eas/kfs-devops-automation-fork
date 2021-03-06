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

import org.kuali.kfs.fp.document.CashReceiptFamilyBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants.CashReceipt;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 * Validation for the Cash Receipt family of documents that checks the total amount of the document.
 */
public class CashReceiptFamilyDocumentTotalValidation extends GenericValidation {
    private CashReceiptFamilyBase cashReceiptFamilyDocumentForValidation;

    /**
     * For all CashReceiptFamilysDocument, the following rules on total amounts are required:
     *  1. The the document net total must be greater than 0.
     *  2. The document must be balanced, i.e. the document net total must be equal to the accounting line total;
     * For Cash Receipt, since there could be change request, the total amount refers to the net total after change total is taken off.
     * Following are the formula for various total amounts in Cash Receipt:
     *      net total = money in - money out
     *      money in = check + currency + coin
     *      money out = change currency + change coin
     *
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        CashReceiptFamilyBase cfd = getCashReceiptFamilyDocumentForValidation();
        boolean isValid = true;

        // For CR, we want to check the original total amount before it is confirmed; otherwise we check the confirmed total amount;
        // this has been taken care of by its getTotalAmount(), so we don't need to check the doc status here.
        // Also, since getTotalAmount() already takes care of returning the net total (i.e. with change total subtracted),
        // we don't need subtract change total here either.
        KualiDecimal totalAmount = cfd.getTotalDollarAmount();

        // make sure that net reconciliation total is greater than zero
        if (!totalAmount.isPositive()) {
            isValid = false;
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, CashReceipt.ERROR_DOCUMENT_CASH_RECEIPT_NET_TOTAL_NOT_GREATER_THAN_ZERO);
        }

        // make sure the document is in balance
        KualiDecimal accountingLineTotal = cfd.getSourceTotal();
        if (totalAmount.compareTo(accountingLineTotal) != 0) {
            isValid = false;
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, CashReceipt.ERROR_DOCUMENT_CASH_RECEIPT_BALANCE);
        }

        return isValid;
    }

    /**
     * Gets the cashReceiptFamilyDocumentForValidation attribute.
     * @return Returns the cashReceiptFamilyDocumentForValidation.
     */
    public CashReceiptFamilyBase getCashReceiptFamilyDocumentForValidation() {
        return cashReceiptFamilyDocumentForValidation;
    }

    /**
     * Sets the cashReceiptFamilyDocumentForValidation attribute value.
     * @param cashReceiptFamilyDocumentForValidation The cashReceiptFamilyDocumentForValidation to set.
     */
    public void setCashReceiptFamilyDocumentForValidation(CashReceiptFamilyBase cashReceiptFamilyDocumentForValidation) {
        this.cashReceiptFamilyDocumentForValidation = cashReceiptFamilyDocumentForValidation;
    }
}
