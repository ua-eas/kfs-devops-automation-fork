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
package org.kuali.kfs.module.ar.document;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomerAddress;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomerInvoice;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomerInvoiceRecurrenceDetails;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.CustomerAddress;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceRecurrenceDetails;
import org.kuali.kfs.module.ar.businessobject.CustomerProcessingType;
import org.kuali.kfs.module.ar.businessobject.InvoiceRecurrence;
import org.kuali.kfs.module.ar.businessobject.PrintInvoiceOptions;
import org.kuali.kfs.module.ar.businessobject.ReceivableCustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.SalesTaxCustomerInvoiceDetail;
import org.kuali.kfs.module.ar.document.service.AccountsReceivablePendingEntryService;
import org.kuali.kfs.module.ar.document.service.AccountsReceivableTaxService;
import org.kuali.kfs.module.ar.document.service.CustomerAddressService;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceDetailService;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceDocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.TaxDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.Correctable;
import org.kuali.kfs.sys.document.dataaccess.FinancialSystemDocumentHeaderDao;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.TaxService;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.document.Copyable;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

public class CustomerInvoiceDocument extends AccountingDocumentBase implements AmountTotaling, Copyable, Correctable, Comparable<CustomerInvoiceDocument>, AccountsReceivableCustomerInvoice {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CustomerInvoiceDocument.class);

    protected static final String HAS_RECCURENCE_NODE = "HasReccurence";
    protected static final String BATCH_GENERATED_NODE = "BatchGenerated";

    protected String invoiceHeaderText;
    protected String invoiceAttentionLineText;
    protected Date invoiceDueDate;
    protected Date billingDate;
    protected Date closedDate;
    protected Date billingDateForDisplay;
    protected String invoiceTermsText;
    protected String organizationInvoiceNumber;
    protected String customerPurchaseOrderNumber;
    protected String printInvoiceIndicator;
    protected Date customerPurchaseOrderDate;
    protected String billByChartOfAccountCode;
    protected String billedByOrganizationCode;
    protected Integer customerShipToAddressIdentifier;
    protected Integer customerBillToAddressIdentifier;
    protected String customerSpecialProcessingCode;
    protected boolean customerRecordAttachmentIndicator;
    protected boolean openInvoiceIndicator;
    protected Date printDate;
    protected Integer age;
    protected String customerName;
    protected String billingAddressName;
    protected String billingCityName;
    protected String billingStateCode;
    protected String billingZipCode;
    protected String billingCountryCode;
    protected String billingAddressInternationalProvinceName;
    protected String billingInternationalMailCode;
    protected String billingEmailAddress;
    protected String billingAddressTypeCode;
    protected String billingLine1StreetAddress;
    protected String billingLine2StreetAddress;
    protected String shippingLine1StreetAddress;
    protected String shippingLine2StreetAddress;
    protected String shippingAddressName;
    protected String shippingCityName;
    protected String shippingStateCode;
    protected String shippingZipCode;
    protected String shippingCountryCode;
    protected String shippingAddressInternationalProvinceName;
    protected String shippingInternationalMailCode;
    protected String shippingEmailAddress;
    protected String shippingAddressTypeCode;
    protected boolean recurredInvoiceIndicator;
    protected Date reportedDate;

    protected AccountsReceivableDocumentHeader accountsReceivableDocumentHeader;
    protected Chart billByChartOfAccount;
    protected Organization billedByOrganization;
    protected CustomerProcessingType customerSpecialProcessing;
    protected PrintInvoiceOptions printInvoiceOption;
    protected CustomerAddress customerShipToAddress;
    protected CustomerAddress customerBillToAddress;
    protected CustomerInvoiceRecurrenceDetails customerInvoiceRecurrenceDetails;

    /**
     * Default constructor.
     */
    public CustomerInvoiceDocument() {
        super();
    }

    /**
     * This method calculates the outstanding balance on an invoice.
     *
     * @return the outstanding balance on this invoice
     */
    @Override
    public KualiDecimal getOpenAmount() {
        return SpringContext.getBean(CustomerInvoiceDocumentService.class).getOpenAmountForCustomerInvoiceDocument(this);
    }

    /**
     * Gets the documentNumber attribute.
     *
     * @return Returns the documentNumber
     */
    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Sets the documentNumber attribute.
     *
     * @param documentNumber The documentNumber to set.
     */
    @Override
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }


    /**
     * Gets the invoiceHeaderText attribute.
     *
     * @return Returns the invoiceHeaderText
     */
    public String getInvoiceHeaderText() {
        return invoiceHeaderText;
    }

    /**
     * Sets the invoiceHeaderText attribute.
     *
     * @param invoiceHeaderText The invoiceHeaderText to set.
     */
    public void setInvoiceHeaderText(String invoiceHeaderText) {
        this.invoiceHeaderText = invoiceHeaderText;
    }


    /**
     * Gets the invoiceAttentionLineText attribute.
     *
     * @return Returns the invoiceAttentionLineText
     */
    @Override
    public String getInvoiceAttentionLineText() {
        return invoiceAttentionLineText;
    }

    /**
     * Sets the invoiceAttentionLineText attribute.
     *
     * @param invoiceAttentionLineText The invoiceAttentionLineText to set.
     */
    public void setInvoiceAttentionLineText(String invoiceAttentionLineText) {
        this.invoiceAttentionLineText = invoiceAttentionLineText;
    }


    /**
     * Gets the invoiceDueDate attribute.
     *
     * @return Returns the invoiceDueDate
     */
    @Override
    public Date getInvoiceDueDate() {
        return invoiceDueDate;
    }

    /**
     * Sets the invoiceDueDate attribute.
     *
     * @param invoiceDueDate The invoiceDueDate to set.
     */
    @Override
    public void setInvoiceDueDate(Date invoiceDueDate) {
        this.invoiceDueDate = invoiceDueDate;
    }


    /**
     * Gets the billingDate attribute.
     *
     * @return Returns the billingDate
     */
    @Override
    public Date getBillingDate() {
        return billingDate;
    }

    /**
     * This method returns the age of an invoice (i.e. current date - billing date)
     *
     * @return
     */
    @Override
    public Integer getAge() {
        if (ObjectUtils.isNotNull(billingDate)) {
            return (int) KfsDateUtils.getDifferenceInDays(new Timestamp(billingDate.getTime()), SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());
        }
        // TODO should I be throwing an exception or throwing a null?
        return null;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * Sets the billingDate attribute.
     *
     * @param billingDate The billingDate to set.
     */
    @Override
    public void setBillingDate(Date billingDate) {
        this.billingDate = billingDate;
    }


    /**
     * Gets the invoiceTermsText attribute.
     *
     * @return Returns the invoiceTermsText
     */
    @Override
    public String getInvoiceTermsText() {
        return invoiceTermsText;
    }

    /**
     * Sets the invoiceTermsText attribute.
     *
     * @param invoiceTermsText The invoiceTermsText to set.
     */
    @Override
    public void setInvoiceTermsText(String invoiceTermsText) {
        this.invoiceTermsText = invoiceTermsText;
    }


    /**
     * Gets the organizationInvoiceNumber attribute.
     *
     * @return Returns the organizationInvoiceNumber
     */
    public String getOrganizationInvoiceNumber() {
        return organizationInvoiceNumber;
    }

    /**
     * Sets the organizationInvoiceNumber attribute.
     *
     * @param organizationInvoiceNumber The organizationInvoiceNumber to set.
     */
    @Override
    public void setOrganizationInvoiceNumber(String organizationInvoiceNumber) {
        this.organizationInvoiceNumber = organizationInvoiceNumber;
    }

    /**
     * Gets the customerPurchaseOrderNumber attribute.
     *
     * @return Returns the customerPurchaseOrderNumber
     */
    public String getCustomerPurchaseOrderNumber() {
        return customerPurchaseOrderNumber;
    }

    /**
     * Sets the customerPurchaseOrderNumber attribute.
     *
     * @param customerPurchaseOrderNumber The customerPurchaseOrderNumber to set.
     */
    public void setCustomerPurchaseOrderNumber(String customerPurchaseOrderNumber) {
        this.customerPurchaseOrderNumber = customerPurchaseOrderNumber;
    }

    /**
     * Gets the printInvoiceIndicator attribute.
     *
     * @return Returns the printInvoiceIndicator.
     */
    public String getPrintInvoiceIndicator() {
        return printInvoiceIndicator;
    }

    /**
     * Sets the printInvoiceIndicator attribute value.
     *
     * @param printInvoiceIndicator The printInvoiceIndicator to set.
     */
    @Override
    public void setPrintInvoiceIndicator(String printInvoiceIndicator) {
        this.printInvoiceIndicator = printInvoiceIndicator;
    }

    /**
     * Gets the customerPurchaseOrderDate attribute.
     *
     * @return Returns the customerPurchaseOrderDate
     */
    public Date getCustomerPurchaseOrderDate() {
        return customerPurchaseOrderDate;
    }

    /**
     * Sets the customerPurchaseOrderDate attribute.
     *
     * @param customerPurchaseOrderDate The customerPurchaseOrderDate to set.
     */
    public void setCustomerPurchaseOrderDate(Date customerPurchaseOrderDate) {
        this.customerPurchaseOrderDate = customerPurchaseOrderDate;
    }


    /**
     * Gets the billByChartOfAccountCode attribute.
     *
     * @return Returns the billByChartOfAccountCode
     */
    @Override
    public String getBillByChartOfAccountCode() {
        return billByChartOfAccountCode;
    }

    /**
     * Sets the billByChartOfAccountCode attribute.
     *
     * @param billByChartOfAccountCode The billByChartOfAccountCode to set.
     */
    @Override
    public void setBillByChartOfAccountCode(String billByChartOfAccountCode) {
        this.billByChartOfAccountCode = billByChartOfAccountCode;
    }

    /**
     * Gets the billedByOrganizationCode attribute.
     *
     * @return Returns the billedByOrganizationCode
     */
    @Override
    public String getBilledByOrganizationCode() {
        return billedByOrganizationCode;
    }

    /**
     * Sets the billedByOrganizationCode attribute.
     *
     * @param billedByOrganizationCode The billedByOrganizationCode to set.
     */
    @Override
    public void setBilledByOrganizationCode(String billedByOrganizationCode) {
        this.billedByOrganizationCode = billedByOrganizationCode;
    }

    /**
     * Gets the customerShipToAddressIdentifier attribute.
     *
     * @return Returns the customerShipToAddressIdentifier
     */
    public Integer getCustomerShipToAddressIdentifier() {
        return customerShipToAddressIdentifier;
    }

    /**
     * Sets the customerShipToAddressIdentifier attribute.
     *
     * @param customerShipToAddressIdentifier The customerShipToAddressIdentifier to set.
     */
    public void setCustomerShipToAddressIdentifier(Integer customerShipToAddressIdentifier) {
        this.customerShipToAddressIdentifier = customerShipToAddressIdentifier;
    }


    /**
     * Gets the customerBillToAddressIdentifier attribute.
     *
     * @return Returns the customerBillToAddressIdentifier
     */
    public Integer getCustomerBillToAddressIdentifier() {
        return customerBillToAddressIdentifier;
    }

    /**
     * Sets the customerBillToAddressIdentifier attribute.
     *
     * @param customerBillToAddressIdentifier The customerBillToAddressIdentifier to set.
     */
    @Override
    public void setCustomerBillToAddressIdentifier(Integer customerBillToAddressIdentifier) {
        this.customerBillToAddressIdentifier = customerBillToAddressIdentifier;
    }


    /**
     * Gets the customerSpecialProcessingCode attribute.
     *
     * @return Returns the customerSpecialProcessingCode
     */
    public String getCustomerSpecialProcessingCode() {
        return customerSpecialProcessingCode;
    }

    /**
     * Sets the customerSpecialProcessingCode attribute.
     *
     * @param customerSpecialProcessingCode The customerSpecialProcessingCode to set.
     */
    public void setCustomerSpecialProcessingCode(String customerSpecialProcessingCode) {
        this.customerSpecialProcessingCode = customerSpecialProcessingCode;
    }


    /**
     * Gets the customerRecordAttachmentIndicator attribute.
     *
     * @return Returns the customerRecordAttachmentIndicator
     */
    public boolean isCustomerRecordAttachmentIndicator() {
        return customerRecordAttachmentIndicator;
    }

    /**
     * Sets the customerRecordAttachmentIndicator attribute.
     *
     * @param customerRecordAttachmentIndicator The customerRecordAttachmentIndicator to set.
     */
    public void setCustomerRecordAttachmentIndicator(boolean customerRecordAttachmentIndicator) {
        this.customerRecordAttachmentIndicator = customerRecordAttachmentIndicator;
    }


    /**
     * Gets the openInvoiceIndicator attribute.
     *
     * @return Returns the openInvoiceIndicator
     */
    @Override
    public boolean isOpenInvoiceIndicator() {
        return openInvoiceIndicator;
    }

    /**
     * Sets the openInvoiceIndicator attribute.
     *
     * @param openInvoiceIndicator The openInvoiceIndicator to set.
     */
    @Override
    public void setOpenInvoiceIndicator(boolean openInvoiceIndicator) {
        this.openInvoiceIndicator = openInvoiceIndicator;
    }

    /**
     * Gets the printDate attribute.
     *
     * @return Returns the printDate
     */
    public Date getPrintDate() {
        return printDate;
    }

    /**
     * Sets the printDate attribute.
     *
     * @param printDate The printDate to set.
     */
    public void setPrintDate(Date printDate) {
        this.printDate = printDate;
    }

    /**
     * Gets the accountsReceivableDocumentHeader attribute.
     *
     * @return Returns the accountsReceivableDocumentHeader
     */
    @Override
    public AccountsReceivableDocumentHeader getAccountsReceivableDocumentHeader() {
        return accountsReceivableDocumentHeader;
    }

    /**
     * Sets the accountsReceivableDocumentHeader attribute.
     *
     * @param accountsReceivableDocumentHeader The accountsReceivableDocumentHeader to set.
     */
    public void setAccountsReceivableDocumentHeader(AccountsReceivableDocumentHeader accountsReceivableDocumentHeader) {
        this.accountsReceivableDocumentHeader = accountsReceivableDocumentHeader;
    }

    /**
     *
     * This method...
     * @return
     */
    public String getParentInvoiceNumber() {
        return getAccountsReceivableDocumentHeader().getDocumentHeader().getDocumentTemplateNumber();
    }

    /**
     * Gets the billByChartOfAccount attribute.
     *
     * @return Returns the billByChartOfAccount
     */
    public Chart getBillByChartOfAccount() {
        return billByChartOfAccount;
    }

    /**
     * Sets the billByChartOfAccount attribute.
     *
     * @param billByChartOfAccount The billByChartOfAccount to set.
     * @deprecated
     */
    @Deprecated
    public void setBillByChartOfAccount(Chart billByChartOfAccount) {
        this.billByChartOfAccount = billByChartOfAccount;
    }

    /**
     * Gets the billedByOrganization attribute.
     *
     * @return Returns the billedByOrganization
     */
    public Organization getBilledByOrganization() {
        return billedByOrganization;
    }

    /**
     * Sets the billedByOrganization attribute.
     *
     * @param billedByOrganization The billedByOrganization to set.
     * @deprecated
     */
    @Deprecated
    public void setBilledByOrganization(Organization billedByOrganization) {
        this.billedByOrganization = billedByOrganization;
    }

    /**
     * Gets the customerSpecialProcessing attribute.
     *
     * @return Returns the customerSpecialProcessing
     */
    public CustomerProcessingType getCustomerSpecialProcessing() {
        return customerSpecialProcessing;
    }

    /**
     * Sets the customerSpecialProcessing attribute.
     *
     * @param customerSpecialProcessing The customerSpecialProcessing to set.
     * @deprecated
     */
    @Deprecated
    public void setCustomerSpecialProcessing(CustomerProcessingType customerSpecialProcessing) {
        this.customerSpecialProcessing = customerSpecialProcessing;
    }

    /**
     * This method returns the billing date for display. If billing date hasn't been set yet, just display current date
     *
     * @return
     */
    public Date getBillingDateForDisplay() {
        if (ObjectUtils.isNotNull(getBillingDate())) {
            return getBillingDate();
        }
        else {
            return SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
        }
    }

    /**
     * This method...
     *
     * @param date
     */
    public void setBillingDateForDisplay(Date date) {
        // do nothing
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Date closedDate) {
        this.closedDate = closedDate;
    }

    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    @SuppressWarnings("unchecked")
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("documentNumber", this.documentNumber);
        return m;
    }

    /**
     * This method returns true if this document has been corrected
     *
     * @return
     */
    public boolean hasInvoiceBeenCorrected() {
        DocumentHeader documentHeader = SpringContext.getBean(FinancialSystemDocumentHeaderDao.class).getCorrectingDocumentHeader(documentNumber);

        if (ObjectUtils.isNotNull(documentHeader) && StringUtils.isNotBlank(documentHeader.getDocumentNumber())) {
            return true;
        }

        return false;
    }

    /**
     * This method returns true if this document is a reversal for another document
     *
     * @return
     */
    public boolean isInvoiceReversal() {
        return ObjectUtils.isNotNull(getFinancialSystemDocumentHeader().getFinancialDocumentInErrorNumber());
    }

    /**
     * @see org.kuali.kfs.sys.document.AccountingDocumentBase#isDebit(org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail)
     */
    @Override
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        return ((CustomerInvoiceDetail) postable).isDebit();
    }

    /**
     * @see org.kuali.kfs.sys.document.AccountingDocumentBase#getSourceAccountingLineClass()
     */
    @Override
    public Class<CustomerInvoiceDetail> getSourceAccountingLineClass() {
        return CustomerInvoiceDetail.class;
    }

    /**
     * Ensures that all the accounts receivable object codes are correctly updated
     */
    public void updateAccountReceivableObjectCodes() {
        for (Iterator e = getSourceAccountingLines().iterator(); e.hasNext();) {
            SpringContext.getBean(CustomerInvoiceDetailService.class).updateAccountsReceivableObjectCode(((CustomerInvoiceDetail) e.next()));
        }
    }

    /**
     * This method creates the following GLPE's for the invoice 1. Debit to receivable for total line amount ( including sales tax
     * if it exists ). 2. Credit to income based on item price * quantity. 3. Credit to state sales tax account/object code if state
     * sales tax exists. 4. Credit to district sales tax account/object code if district sales tax exists.
     *
     * @see org.kuali.kfs.service.impl.GenericGeneralLedgerPendingEntryGenerationProcessImpl#processGenerateGeneralLedgerPendingEntries(org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource,
     *      org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail,
     *      org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper)
     */
    @Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        addReceivableGLPEs(sequenceHelper, glpeSourceDetail);
        sequenceHelper.increment();
        addIncomeGLPEs(sequenceHelper, glpeSourceDetail);

        // if sales tax is enabled generate GLPEs
        if (SpringContext.getBean(AccountsReceivableTaxService.class).isCustomerInvoiceDetailTaxable(this, (CustomerInvoiceDetail) glpeSourceDetail)) {
            addSalesTaxGLPEs(sequenceHelper, glpeSourceDetail);
        }


        return true;
    }


    /**
     * This method creates the receivable GLPEs for each invoice detail line.
     *
     * @param poster
     * @param sequenceHelper
     * @param postable
     * @param explicitEntry
     */
    protected void addReceivableGLPEs(GeneralLedgerPendingEntrySequenceHelper sequenceHelper, GeneralLedgerPendingEntrySourceDetail glpeSourceDetail) {
        CustomerInvoiceDetail customerInvoiceDetail = (CustomerInvoiceDetail) glpeSourceDetail;
        ReceivableCustomerInvoiceDetail receivableCustomerInvoiceDetail = new ReceivableCustomerInvoiceDetail(customerInvoiceDetail, this);

        AccountsReceivablePendingEntryService service = SpringContext.getBean(AccountsReceivablePendingEntryService.class);
        service.createAndAddGenericInvoiceRelatedGLPEs(this, receivableCustomerInvoiceDetail, sequenceHelper, isInvoiceDetailReceivableDebit(customerInvoiceDetail), false, customerInvoiceDetail.getInvoiceItemPreTaxAmount());
    }

    /**
     * Determines if the given customer invoice detail should be a debit on the receivable glpe, or a credit
     * @param customerInvoiceDetail the customer invoice detail to determine debitness of
     * @return true if the detail represents a debit, false if it represents a credit
     */
    protected boolean isInvoiceDetailReceivableDebit(CustomerInvoiceDetail customerInvoiceDetail) {
        final boolean isDebit = (!isInvoiceReversal() && !customerInvoiceDetail.isDiscountLine()) || (isInvoiceReversal() && customerInvoiceDetail.isDiscountLine());
        return isDebit;
    }

    /**
     * This method adds pending entry with transaction ledger entry amount set to item price * quantity
     *
     * @param poster
     * @param sequenceHelper
     * @param postable
     * @param explicitEntry
     */
    protected void addIncomeGLPEs(GeneralLedgerPendingEntrySequenceHelper sequenceHelper, GeneralLedgerPendingEntrySourceDetail glpeSourceDetail) {

        CustomerInvoiceDetail customerInvoiceDetail = (CustomerInvoiceDetail) glpeSourceDetail;

        AccountsReceivablePendingEntryService service = SpringContext.getBean(AccountsReceivablePendingEntryService.class);
        service.createAndAddGenericInvoiceRelatedGLPEs(this, customerInvoiceDetail, sequenceHelper, isInvoiceDetailIncomeDebit(customerInvoiceDetail), false, customerInvoiceDetail.getInvoiceItemPreTaxAmount());
    }

    /**
     * Determines if the given customer invoice detail should be a debit on the income glpe, or a credit
     * @param customerInvoiceDetail the customer invoice detail to determine debitness of
     * @return true if the detail represents a debit, false if it represents a credit
     */
    protected boolean isInvoiceDetailIncomeDebit(CustomerInvoiceDetail customerInvoiceDetail) {
        final boolean isDebit = (!isInvoiceReversal() && customerInvoiceDetail.isDiscountLine()) || (isInvoiceReversal() && !customerInvoiceDetail.isDiscountLine());
        return isDebit;
    }

    /**
     * This method add pending entries for every tax detail that exists for a particular postal code
     *
     * @param sequenceHelper
     * @param glpeSourceDetail
     * @param hasClaimOnCashOffset
     */
    protected void addSalesTaxGLPEs(GeneralLedgerPendingEntrySequenceHelper sequenceHelper, GeneralLedgerPendingEntrySourceDetail glpeSourceDetail) {

        CustomerInvoiceDetail customerInvoiceDetail = (CustomerInvoiceDetail) glpeSourceDetail;
        boolean isDebit = (!isInvoiceReversal() && customerInvoiceDetail.isDiscountLine()) || (isInvoiceReversal() && !customerInvoiceDetail.isDiscountLine());

        String postalCode = SpringContext.getBean(AccountsReceivableTaxService.class).getPostalCodeForTaxation(this);
        Date dateOfTransaction = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();

        List<TaxDetail> salesTaxDetails = SpringContext.getBean(TaxService.class).getSalesTaxDetails(dateOfTransaction, postalCode, customerInvoiceDetail.getInvoiceItemPreTaxAmount());

        AccountsReceivablePendingEntryService service = SpringContext.getBean(AccountsReceivablePendingEntryService.class);
        SalesTaxCustomerInvoiceDetail salesTaxCustomerInvoiceDetail;
        ReceivableCustomerInvoiceDetail receivableCustomerInvoiceDetail;
        for (TaxDetail salesTaxDetail : salesTaxDetails) {

            salesTaxCustomerInvoiceDetail = new SalesTaxCustomerInvoiceDetail(salesTaxDetail, customerInvoiceDetail);
            receivableCustomerInvoiceDetail = new ReceivableCustomerInvoiceDetail(salesTaxCustomerInvoiceDetail, this);

            sequenceHelper.increment();
            service.createAndAddGenericInvoiceRelatedGLPEs(this, receivableCustomerInvoiceDetail, sequenceHelper, !isDebit, false, salesTaxDetail.getTaxAmount());

            sequenceHelper.increment();
            service.createAndAddGenericInvoiceRelatedGLPEs(this, salesTaxCustomerInvoiceDetail, sequenceHelper, isDebit, false, salesTaxDetail.getTaxAmount());
        }
    }

    /**
     * Returns an implementation of the GeneralLedgerPendingEntryService
     *
     * @return an implementation of the GeneralLedgerPendingEntryService
     */
    public GeneralLedgerPendingEntryService getGeneralLedgerPendingEntryService() {
        return SpringContext.getBean(GeneralLedgerPendingEntryService.class);
    }

    @Override
    public List<String> getWorkflowEngineDocumentIdsToLock() {
        //  add the invoice number of the Error Corrected doc, if this is an error correction
        if (this.isInvoiceReversal()) {
            if (StringUtils.isNotBlank(getFinancialSystemDocumentHeader().getFinancialDocumentInErrorNumber())) {
                List<String> documentIds = new ArrayList<String>();
                documentIds.add(getFinancialSystemDocumentHeader().getFinancialDocumentInErrorNumber());
                return documentIds;
            }
        }
        return null;
    }

    /**
     * When document is processed do the following: 1) Set the billingDate to today's date if not already set 2) If there are
     * discounts, create corresponding invoice paid applied rows 3) If the document is a reversal, in addition to reversing paid
     * applied rows, update the open paid applied indicator
     *
     * @see org.kuali.kfs.sys.document.GeneralLedgerPostingDocumentBase#doRouteStatusChange()
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        //  fast-exit if status != P
        if (!getDocumentHeader().getWorkflowDocument().isProcessed()) {
            return;
        }

        //  wire up the billing date
        if (ObjectUtils.isNull(getBillingDate())) {
            setBillingDate(SpringContext.getBean(DateTimeService.class).getCurrentSqlDateMidnight());
        }

        // apply discounts
        CustomerInvoiceDocumentService invoiceService = SpringContext.getBean(CustomerInvoiceDocumentService.class);
        invoiceService.convertDiscountsToPaidApplieds(this);

        //  handle a Correction/Reversal document
        if (this.isInvoiceReversal()) {
            CustomerInvoiceDocument correctedCustomerInvoiceDocument;
            try {
                correctedCustomerInvoiceDocument = (CustomerInvoiceDocument) SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(this.getFinancialSystemDocumentHeader().getFinancialDocumentInErrorNumber());
            }
            catch (WorkflowException e) {
                throw new RuntimeException("Cannot find customer invoice document with id " + this.getFinancialSystemDocumentHeader().getFinancialDocumentInErrorNumber());
            }

            // if reversal, close both this reversal invoice and the original invoice
            SpringContext.getBean(CustomerInvoiceDocumentService.class).closeCustomerInvoiceDocument(correctedCustomerInvoiceDocument);
            SpringContext.getBean(CustomerInvoiceDocumentService.class).closeCustomerInvoiceDocument(this);
        }

        //  handle Recurrence
        if (ObjectUtils.isNull(this.getCustomerInvoiceRecurrenceDetails()) ||
                (ObjectUtils.isNull(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceBeginDate())
                && ObjectUtils.isNull(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceEndDate())
                && ObjectUtils.isNull(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceIntervalCode())
                && ObjectUtils.isNull(this.getCustomerInvoiceRecurrenceDetails().getDocumentTotalRecurrenceNumber())
                && ObjectUtils.isNull(this.getCustomerInvoiceRecurrenceDetails().getDocumentInitiatorUserIdentifier()))) {
        }
        else {
            // set new user session to recurrence initiator
            UserSession currentSession = GlobalVariables.getUserSession();
            GlobalVariables.setUserSession(new UserSession(KFSConstants.SYSTEM_USER));

            // populate InvoiceRecurrence business object
            InvoiceRecurrence newInvoiceRecurrence = new InvoiceRecurrence();
            newInvoiceRecurrence.setInvoiceNumber(this.getCustomerInvoiceRecurrenceDetails().getInvoiceNumber());
            newInvoiceRecurrence.setCustomerNumber(this.getCustomerInvoiceRecurrenceDetails().getCustomerNumber());
            newInvoiceRecurrence.setDocumentRecurrenceBeginDate(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceBeginDate());
            newInvoiceRecurrence.setDocumentRecurrenceEndDate(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceEndDate());
            newInvoiceRecurrence.setDocumentRecurrenceIntervalCode(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceIntervalCode());
            newInvoiceRecurrence.setDocumentTotalRecurrenceNumber(this.getCustomerInvoiceRecurrenceDetails().getDocumentTotalRecurrenceNumber());
            newInvoiceRecurrence.setDocumentInitiatorUserIdentifier(this.getCustomerInvoiceRecurrenceDetails().getDocumentInitiatorUserIdentifier());
            newInvoiceRecurrence.setActive(this.getCustomerInvoiceRecurrenceDetails().isActive());

            // create a new InvoiceRecurrenceMaintenanceDocument
            MaintenanceDocument invoiceRecurrenceMaintDoc = null;
            try {
                invoiceRecurrenceMaintDoc = (MaintenanceDocument) SpringContext.getBean(DocumentService.class).getNewDocument(getInvoiceRecurrenceMaintenanceDocumentTypeName());
            }
            catch (WorkflowException e1) {
                throw new RuntimeException("Cannot create new Invoice Recurrence Maintenance Document.");
            }
            invoiceRecurrenceMaintDoc.getDocumentHeader().setDocumentDescription("Automatically created from Invoice");
            invoiceRecurrenceMaintDoc.getNewMaintainableObject().setBusinessObject(newInvoiceRecurrence);

            try {
                // blanket approve the INVR, bypassing everything
                //invoiceRecurrenceMaintDoc.getDocumentHeader().getWorkflowDocument().blanketApprove("Blanket Approved by the creating Invoice Document #" + getDocumentNumber());
                //TODO temporarily just do regular route until we can get blanket approve perms setup for KFS
                SpringContext.getBean(DocumentService.class).saveDocument(invoiceRecurrenceMaintDoc);
                invoiceRecurrenceMaintDoc.getDocumentHeader().getWorkflowDocument().route("Automatically created and routed by CustomerInvoiceDocument #" + getDocumentNumber() + ".");
            }
            catch (WorkflowException e) {
                throw new RuntimeException("Cannot route Invoice Recurrence Maintenance Document with id " + invoiceRecurrenceMaintDoc.getDocumentNumber() + ".");
            }

            // return the session to the original initiator
            GlobalVariables.setUserSession(currentSession);

        }
    }

    protected String getInvoiceRecurrenceMaintenanceDocumentTypeName() {
        return "INVR";
    }

    /**
     * If this invoice is a reversal, set the open indicator to false
     *
     * @see org.kuali.kfs.sys.document.FinancialSystemTransactionalDocumentBase#prepareForSave()
     */
    @Override
    public void prepareForSave() {
        if (this.isInvoiceReversal()) {
            setOpenInvoiceIndicator(false);
        }

        //  make sure the docHeader gets its doc total right.  This is here because there's an ordering
        // bug in the struts classes for invoice that is preventing this from being set right.  There is
        // probably a better way to fix this that can be pursued later.
        getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(getTotalDollarAmount());

        captureWorkflowHeaderInformation();

        //  invoice recurrence stuff, if there is a recurrence object
        if (ObjectUtils.isNotNull(this.getCustomerInvoiceRecurrenceDetails()) && getProcessRecurrenceFlag()) {

            //  wire up the recurrence customer number if one exists
            if (ObjectUtils.isNull(this.getCustomerInvoiceRecurrenceDetails().getCustomerNumber())) {
                this.getCustomerInvoiceRecurrenceDetails().setCustomerNumber(this.getAccountsReceivableDocumentHeader().getCustomerNumber());
            }

            customerInvoiceRecurrenceDetails.setInvoiceNumber(getDocumentNumber());

            // calc recurrence number if only end-date specified
            if (ObjectUtils.isNull(this.getCustomerInvoiceRecurrenceDetails().getDocumentTotalRecurrenceNumber()) && ObjectUtils.isNotNull(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceEndDate())) {

                Calendar beginCalendar = Calendar.getInstance();
                beginCalendar.setTime(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceBeginDate());
                Date beginDate = this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceBeginDate();
                Calendar endCalendar = Calendar.getInstance();

                endCalendar.setTime(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceEndDate());
                Date endDate = this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceEndDate();
                Calendar nextCalendar = Calendar.getInstance();
                Date nextDate = beginDate;

                int totalRecurrences = 0;
                int addCounter = 0;
                String intervalCode = this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceIntervalCode();
                if (intervalCode.equals("M")) {
                    addCounter = 1;
                }
                if (intervalCode.equals("Q")) {
                    addCounter = 3;
                }
                /* perform this loop while begin_date is less than or equal to end_date */
                while (!(beginDate.after(endDate))) {
                    beginCalendar.setTime(beginDate);
                    beginCalendar.add(Calendar.MONTH, addCounter);
                    beginDate = KfsDateUtils.convertToSqlDate(beginCalendar.getTime());
                    totalRecurrences++;

                    nextDate = beginDate;
                    nextCalendar.setTime(nextDate);
                    nextCalendar.add(Calendar.MONTH, addCounter);
                    nextDate = KfsDateUtils.convertToSqlDate(nextCalendar.getTime());
                    if (endDate.after(beginDate) && endDate.before(nextDate)) {
                        totalRecurrences++;
                        break;
                    }
                }
                if (totalRecurrences > 0) {
                    this.getCustomerInvoiceRecurrenceDetails().setDocumentTotalRecurrenceNumber(totalRecurrences);
                }
            }

            //  calc end-date if only recurrence-number is specified
            if (ObjectUtils.isNotNull(this.getCustomerInvoiceRecurrenceDetails().getDocumentTotalRecurrenceNumber()) && ObjectUtils.isNull(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceEndDate())) {

                Calendar beginCalendar = Calendar.getInstance();
                beginCalendar.setTime(new Timestamp(this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceBeginDate().getTime()));
                Calendar endCalendar = Calendar.getInstance();
                endCalendar = beginCalendar;

                int addCounter = 0;
                Integer documentTotalRecurrenceNumber = this.getCustomerInvoiceRecurrenceDetails().getDocumentTotalRecurrenceNumber();
                String intervalCode = this.getCustomerInvoiceRecurrenceDetails().getDocumentRecurrenceIntervalCode();
                if (intervalCode.equals("M")) {
                    addCounter = -1;
                    addCounter += documentTotalRecurrenceNumber * 1;
                }
                if (intervalCode.equals("Q")) {
                    addCounter = -3;
                    addCounter += documentTotalRecurrenceNumber * 3;
                }
                endCalendar.add(Calendar.MONTH, addCounter);
                this.getCustomerInvoiceRecurrenceDetails().setDocumentRecurrenceEndDate(KfsDateUtils.convertToSqlDate(endCalendar.getTime()));
            }
        }

        // Force upper case
        //TODO Force to upper case here since DD forceUpperCase doesn't work. Revert this temp fix after Rice fix.
        setBilledByOrganizationCode(StringUtils.upperCase(billedByOrganizationCode));
        accountsReceivableDocumentHeader.setProcessingOrganizationCode(StringUtils.upperCase(accountsReceivableDocumentHeader.getProcessingOrganizationCode()));
        accountsReceivableDocumentHeader.setCustomerNumber(StringUtils.upperCase(accountsReceivableDocumentHeader.getCustomerNumber()));

        if (ObjectUtils.isNull(getCustomerShipToAddressIdentifier())) {
            setCustomerShipToAddress(null);
            setCustomerShipToAddressOnInvoice(null);
        }

    }

    // returns true only when there is all the required recurrence info
    public boolean getProcessRecurrenceFlag() {
            CustomerInvoiceRecurrenceDetails rec = this.getCustomerInvoiceRecurrenceDetails();

        boolean processRecurrenceFlag = (null != rec.getDocumentRecurrenceIntervalCode());
        processRecurrenceFlag &= (null != rec.getDocumentRecurrenceBeginDate());
        processRecurrenceFlag &= ( (null != rec.getDocumentRecurrenceEndDate()) || (null != rec.getDocumentTotalRecurrenceNumber()));
        processRecurrenceFlag &= (rec.isActive());
        processRecurrenceFlag &= (null != rec.getDocumentInitiatorUserIdentifier());

        return processRecurrenceFlag;
            }

    // returns true only if there is no recurrence data at all in recurrence tab
    public boolean getNoRecurrenceDataFlag() {
        CustomerInvoiceRecurrenceDetails rec = this.getCustomerInvoiceRecurrenceDetails();

        boolean noRecurrenceDataFlag = ObjectUtils.isNull(rec.getDocumentRecurrenceIntervalCode());
        noRecurrenceDataFlag &= ObjectUtils.isNull(rec.getDocumentRecurrenceBeginDate());
        noRecurrenceDataFlag &= ObjectUtils.isNull(rec.getDocumentRecurrenceEndDate());
        noRecurrenceDataFlag &= !rec.isActive();
        noRecurrenceDataFlag &= ObjectUtils.isNull(rec.getDocumentTotalRecurrenceNumber());
        noRecurrenceDataFlag &= ObjectUtils.isNull(rec.getDocumentInitiatorUserIdentifier());

        return noRecurrenceDataFlag;
    }

    /**
     * @see org.kuali.kfs.sys.document.AccountingDocumentBase#toCopy()
     */
    @Override
    public void toCopy() throws WorkflowException {
        super.toCopy();
        CustomerInvoiceDocumentService customerInvoiceDocumentService = SpringContext.getBean(CustomerInvoiceDocumentService.class);
        customerInvoiceDocumentService.setupDefaultValuesForCopiedCustomerInvoiceDocument(this);
        this.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(getTotalDollarAmount());
    }

    /**
     * @see org.kuali.kfs.sys.document.GeneralLedgerPostingDocumentBase#toErrorCorrection()
     */
    @Override
    public void toErrorCorrection() throws WorkflowException {
        super.toErrorCorrection();
        negateCustomerInvoiceDetailUnitPrices();
        this.setOpenInvoiceIndicator(false);
        this.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(getTotalDollarAmount());

        //  if we dont force this on the error correction, the recurrence will
        // have the old doc number, and will revert the main doc due to OJB fun,
        // which will cause PK unique index failure.
        if (ObjectUtils.isNotNull(customerInvoiceRecurrenceDetails)) {
            customerInvoiceRecurrenceDetails.setInvoiceNumber(this.documentNumber);
        }
    }


    /**
     * This method...
     */
    @SuppressWarnings("unchecked")
    public void negateCustomerInvoiceDetailUnitPrices() {
        CustomerInvoiceDetail customerInvoiceDetail;
        for (Iterator i = getSourceAccountingLines().iterator(); i.hasNext();) {
            customerInvoiceDetail = (CustomerInvoiceDetail) i.next();
            customerInvoiceDetail.setInvoiceItemUnitPrice(customerInvoiceDetail.getInvoiceItemUnitPrice().negate());

            //clear the old CustomerInvoiceDocument
            customerInvoiceDetail.setCustomerInvoiceDocument(null);

            // revert changes for custom invoice error correction
            //SpringContext.getBean(CustomerInvoiceDetailService.class).prepareCustomerInvoiceDetailForErrorCorrection(customerInvoiceDetail, this);
        }
    }

    /**
     * This method returns true if invoice document has at least one discount line
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean hasAtLeastOneDiscount() {

        CustomerInvoiceDetail customerInvoiceDetail;
        for (Iterator i = getSourceAccountingLines().iterator(); i.hasNext();) {
            customerInvoiceDetail = (CustomerInvoiceDetail) i.next();
            if (customerInvoiceDetail.isDiscountLineParent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns true if line number is discount line number based on sequence number
     *
     * @param sequenceNumber
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean isDiscountLineBasedOnSequenceNumber(Integer sequenceNumber) {
        if (ObjectUtils.isNull(sequenceNumber)) {
            return false;
        }

        CustomerInvoiceDetail customerInvoiceDetail;
        for (Iterator i = getSourceAccountingLines().iterator(); i.hasNext();) {
            customerInvoiceDetail = (CustomerInvoiceDetail) i.next();
            Integer discLineNum = customerInvoiceDetail.getInvoiceItemDiscountLineNumber();

            // check if sequence number is referenced as a discount line for another customer invoice detail (i.e. the parent line)
            if (ObjectUtils.isNotNull(discLineNum) && sequenceNumber.equals(customerInvoiceDetail.getInvoiceItemDiscountLineNumber())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns parent customer invoice detail based on child discount sequence number
     *
     * @param sequenceNumber
     * @return
     */
    @SuppressWarnings("unchecked")
    public CustomerInvoiceDetail getParentLineBasedOnDiscountSequenceNumber(Integer discountSequenceNumber) {

        if (ObjectUtils.isNull(discountSequenceNumber)) {
            return null;
        }

        CustomerInvoiceDetail customerInvoiceDetail;
        for (Iterator i = getSourceAccountingLines().iterator(); i.hasNext();) {
            customerInvoiceDetail = (CustomerInvoiceDetail) i.next();
            Integer discLineNum = customerInvoiceDetail.getInvoiceItemDiscountLineNumber();
            if (ObjectUtils.isNotNull(discLineNum) && discountSequenceNumber.equals(customerInvoiceDetail.getInvoiceItemDiscountLineNumber())) {
                return customerInvoiceDetail;
            }
        }
        return null;
    }


    /**
     * This method is called on CustomerInvoiceDocumentAction.execute() to set isDiscount to true if it truly is a discount line
     */
    @SuppressWarnings("unchecked")
    public void updateDiscountAndParentLineReferences() {

        CustomerInvoiceDetail discount;
        for (Iterator i = getSourceAccountingLines().iterator(); i.hasNext();) {
            discount = (CustomerInvoiceDetail) i.next();

            // get sequence number and check if theres a corresponding parent line for that discount line
            CustomerInvoiceDetail parent = getParentLineBasedOnDiscountSequenceNumber(discount.getSequenceNumber());
            if (ObjectUtils.isNotNull(parent)) {
                discount.setParentDiscountCustomerInvoiceDetail(parent);
                parent.setDiscountCustomerInvoiceDetail(discount);
            }
            else {
                discount.setParentDiscountCustomerInvoiceDetail(null);
            }
        }
    }

    /**
     * This method removes the corresponding discount line based on the index of the parent line index. This assumes that the
     * discount line is ALWAYS after the index of the parent line.
     *
     * @param deleteIndex
     */
    public void removeDiscountLineBasedOnParentLineIndex(int parentLineIndex) {
        // get parent line line
        CustomerInvoiceDetail parentLine = (CustomerInvoiceDetail) getSourceAccountingLines().get(parentLineIndex);

        // get index for discount line
        int discountLineIndex = -1; // this should ALWAYS get set
        for (int i = 0; i < getSourceAccountingLines().size(); i++) {
            if (parentLine.getInvoiceItemDiscountLineNumber().equals(((CustomerInvoiceDetail) getSourceAccountingLines().get(i)).getSequenceNumber())) {
                discountLineIndex = i;
            }
        }
        // remove discount line
        getSourceAccountingLines().remove(discountLineIndex);
    }

    public CustomerInvoiceRecurrenceDetails getCustomerInvoiceRecurrenceDetails() {
        return customerInvoiceRecurrenceDetails;
    }

    public void setCustomerInvoiceRecurrenceDetails(CustomerInvoiceRecurrenceDetails customerInvoiceRecurrenceDetails) {
        this.customerInvoiceRecurrenceDetails = customerInvoiceRecurrenceDetails;
    }

    public CustomerAddress getCustomerShipToAddress() {
        return customerShipToAddress;
    }

    public void setCustomerShipToAddress(CustomerAddress customerShipToAddress) {
        this.customerShipToAddress = customerShipToAddress;
    }

    public CustomerAddress getCustomerBillToAddress() {
        return customerBillToAddress;
    }

    public void setCustomerBillToAddress(CustomerAddress customerBillToAddress) {
        this.customerBillToAddress = customerBillToAddress;
    }

    public PrintInvoiceOptions getPrintInvoiceOption() {
        if (ObjectUtils.isNull(printInvoiceOption) && StringUtils.isNotEmpty(printInvoiceIndicator)){
            refreshReferenceObject("printInvoiceOption");
        }
        return printInvoiceOption;
    }

    public void setPrintInvoiceOption(PrintInvoiceOptions printInvoiceOption) {
        this.printInvoiceOption = printInvoiceOption;
    }

    /**
     * This method returns the total of all pre tax amounts for all customer invoice detail lines
     *
     * @return
     */
    public KualiDecimal getInvoiceItemPreTaxAmountTotal() {

        KualiDecimal invoiceItemPreTaxAmountTotal = new KualiDecimal(0);
        for (Iterator i = getSourceAccountingLines().iterator(); i.hasNext();) {
            invoiceItemPreTaxAmountTotal = invoiceItemPreTaxAmountTotal.add(((CustomerInvoiceDetail) i.next()).getInvoiceItemPreTaxAmount());
        }
        return invoiceItemPreTaxAmountTotal;
    }

    /**
     * This method returns the total of all tax amounts for all customer invoice detail lines
     *
     * @return
     */
    public KualiDecimal getInvoiceItemTaxAmountTotal() {

        KualiDecimal invoiceItemTaxAmountTotal = new KualiDecimal(0);
        for (Iterator i = getSourceAccountingLines().iterator(); i.hasNext();) {
            invoiceItemTaxAmountTotal = invoiceItemTaxAmountTotal.add(((CustomerInvoiceDetail) i.next()).getInvoiceItemTaxAmount());
        }
        return invoiceItemTaxAmountTotal;
    }

    /**
     * This method returns the primary customer address for the customer number provided.
     *
     * @return
     */
    public CustomerAddress getPrimaryAddressForCustomerNumber() {
        if (ObjectUtils.isNotNull(accountsReceivableDocumentHeader) && StringUtils.isNotEmpty(accountsReceivableDocumentHeader.getCustomerNumber())) {
            return SpringContext.getBean(CustomerAddressService.class).getPrimaryAddress(accountsReceivableDocumentHeader.getCustomerNumber());
        }
        return null;
    }

    /**
     * This method returns the customer object for the invoice
     *
     * @return
     */
    public Customer getCustomer() {
        if (ObjectUtils.isNotNull(accountsReceivableDocumentHeader)) {
            return accountsReceivableDocumentHeader.getCustomer();
        }
        return null;
    }

    /**
     * This method will return all the customer invoice details excluding discount invoice detail lines.
     *
     * @return
     */
    public List<CustomerInvoiceDetail> getCustomerInvoiceDetailsWithoutDiscounts() {
        List<CustomerInvoiceDetail> customerInvoiceDetailsWithoutDiscounts = new ArrayList<CustomerInvoiceDetail>();

        updateDiscountAndParentLineReferences();

        List<CustomerInvoiceDetail> customerInvoiceDetailsWithDiscounts = getSourceAccountingLines();
        for (CustomerInvoiceDetail customerInvoiceDetail : customerInvoiceDetailsWithDiscounts) {
            if (!customerInvoiceDetail.isDiscountLine()) {
                customerInvoiceDetail.setDocumentNumber(getDocumentNumber());
                customerInvoiceDetailsWithoutDiscounts.add(customerInvoiceDetail);
            }
        }

        return customerInvoiceDetailsWithoutDiscounts;
    }

    //TODO Andrew
//    /**
//     * This method could be a bit dangerous. It's meant to be used only on the payment application document, where the modified
//     * invoice is never saved.
//     *
//     * @param customerInvoiceDetails
//     */
//    public void setCustomerInvoiceDetailsWithoutDiscounts(List<CustomerInvoiceDetail> customerInvoiceDetails) {
//        List<CustomerInvoiceDetail> customerInvoiceDetailsWithoutDiscounts = getSourceAccountingLines();
//        int sequenceCounter = 0;
//        for (CustomerInvoiceDetail customerInvoiceDetail : customerInvoiceDetailsWithoutDiscounts) {
//            for (CustomerInvoiceDetail revisedCustomerInvoiceDetail : customerInvoiceDetails) {
//                if (!customerInvoiceDetail.isDiscountLine() && customerInvoiceDetail.getSequenceNumber().equals(revisedCustomerInvoiceDetail.getSequenceNumber())) {
//                    customerInvoiceDetailsWithoutDiscounts.remove(sequenceCounter);
//                    customerInvoiceDetailsWithoutDiscounts.add(sequenceCounter, revisedCustomerInvoiceDetail);
//                }
//            }
//            sequenceCounter += 1;
//        }
//        setSourceAccountingLines(customerInvoiceDetailsWithoutDiscounts);
//    }

    /**
     * This method will return all the customer invoice details that are discounts
     *
     * @return
     */
    public List<CustomerInvoiceDetail> getDiscounts() {
        List<CustomerInvoiceDetail> discounts = new ArrayList<CustomerInvoiceDetail>();

        updateDiscountAndParentLineReferences();

        List<CustomerInvoiceDetail> customerInvoiceDetailsWithDiscounts = getSourceAccountingLines();
        for (CustomerInvoiceDetail customerInvoiceDetail : customerInvoiceDetailsWithDiscounts) {
            if (customerInvoiceDetail.isDiscountLine()) {
                customerInvoiceDetail.setDocumentNumber(getDocumentNumber());
                discounts.add(customerInvoiceDetail);
            }
        }

        return discounts;
    }

    @Override
    public int compareTo(CustomerInvoiceDocument customerInvoiceDocument) {
        if (this.getBillByChartOfAccountCode().equals(customerInvoiceDocument.getBillByChartOfAccountCode())) {
            if (this.getBilledByOrganizationCode().equals(customerInvoiceDocument.getBilledByOrganizationCode())) {
                return 0;
            }
        }
        return -1;
    }

    /**
     *
     * Returns whether or not the Invoice would be paid off by applying the additional amount, passed in
     * by the parameter.
     *
     * @param additionalAmountToApply The additional applied amount to test against.
     * @return True if applying the additionalAmountToApply parameter amount would bring the OpenAmount to zero.
     */
    public boolean wouldPayOff(KualiDecimal additionalAmountToApply) {
        KualiDecimal openAmount = getOpenAmount();
        return KualiDecimal.ZERO.isGreaterEqual(openAmount.subtract(additionalAmountToApply));
    }

    @Override
    public KualiDecimal getTotalDollarAmount() {
        return getSourceTotal();
    }

    public String getBillingAddressInternationalProvinceName() {
        return billingAddressInternationalProvinceName;
    }

    @Override
    public void setBillingAddressInternationalProvinceName(String billingAddressInternationalProvinceName) {
        this.billingAddressInternationalProvinceName = billingAddressInternationalProvinceName;
    }

    public String getBillingAddressName() {
        return billingAddressName;
    }

    @Override
    public void setBillingAddressName(String billingAddressName) {
        this.billingAddressName = billingAddressName;
    }

    public String getBillingAddressTypeCode() {
        return billingAddressTypeCode;
    }

    public void setBillingAddressTypeCode(String billingAddressTypeCode) {
        this.billingAddressTypeCode = billingAddressTypeCode;
    }

    public String getBillingCityName() {
        return billingCityName;
    }

    @Override
    public void setBillingCityName(String billingCityName) {
        this.billingCityName = billingCityName;
    }

    public String getBillingCountryCode() {
        return billingCountryCode;
    }

    @Override
    public void setBillingCountryCode(String billingCountryCode) {
        this.billingCountryCode = billingCountryCode;
    }

    public String getBillingEmailAddress() {
        return billingEmailAddress;
    }

    @Override
    public void setBillingEmailAddress(String billingEmailAddress) {
        this.billingEmailAddress = billingEmailAddress;
    }

    public String getBillingInternationalMailCode() {
        return billingInternationalMailCode;
    }

    @Override
    public void setBillingInternationalMailCode(String billingInternationalMailCode) {
        this.billingInternationalMailCode = billingInternationalMailCode;
    }

    public String getBillingStateCode() {
        return billingStateCode;
    }

    @Override
    public void setBillingStateCode(String billingStateCode) {
        this.billingStateCode = billingStateCode;
    }

    public String getBillingZipCode() {
        return billingZipCode;
    }

    @Override
    public void setBillingZipCode(String billingZipCode) {
        this.billingZipCode = billingZipCode;
    }

    @Override
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getShippingAddressInternationalProvinceName() {
        return shippingAddressInternationalProvinceName;
    }

    public void setShippingAddressInternationalProvinceName(String shippingAddressInternationalProvinceName) {
        this.shippingAddressInternationalProvinceName = shippingAddressInternationalProvinceName;
    }

    public String getShippingAddressName() {
        return shippingAddressName;
    }

    public void setShippingAddressName(String shippingAddressName) {
        this.shippingAddressName = shippingAddressName;
    }

    public String getShippingAddressTypeCode() {
        return shippingAddressTypeCode;
    }

    public void setShippingAddressTypeCode(String shippingAddressTypeCode) {
        this.shippingAddressTypeCode = shippingAddressTypeCode;
    }

    public String getShippingCityName() {
        return shippingCityName;
    }

    public void setShippingCityName(String shippingCityName) {
        this.shippingCityName = shippingCityName;
    }

    public String getShippingCountryCode() {
        return shippingCountryCode;
    }

    public void setShippingCountryCode(String shippingCountryCode) {
        this.shippingCountryCode = shippingCountryCode;
    }

    public String getShippingEmailAddress() {
        return shippingEmailAddress;
    }

    public void setShippingEmailAddress(String shippingEmailAddress) {
        this.shippingEmailAddress = shippingEmailAddress;
    }

    public String getShippingInternationalMailCode() {
        return shippingInternationalMailCode;
    }

    public void setShippingInternationalMailCode(String shippingInternationalMailCode) {
        this.shippingInternationalMailCode = shippingInternationalMailCode;
    }

    public String getShippingStateCode() {
        return shippingStateCode;
    }

    public void setShippingStateCode(String shippingStateCode) {
        this.shippingStateCode = shippingStateCode;
    }

    public String getShippingZipCode() {
        return shippingZipCode;
    }

    public void setShippingZipCode(String shippingZipCode) {
        this.shippingZipCode = shippingZipCode;
    }

    public String getBillingLine1StreetAddress() {
        return billingLine1StreetAddress;
    }

    @Override
    public void setBillingLine1StreetAddress(String billingLine1StreetAddress) {
        this.billingLine1StreetAddress = billingLine1StreetAddress;
    }

    public String getBillingLine2StreetAddress() {
        return billingLine2StreetAddress;
    }

    @Override
    public void setBillingLine2StreetAddress(String billingLine2StreetAddress) {
        this.billingLine2StreetAddress = billingLine2StreetAddress;
    }

    public String getShippingLine1StreetAddress() {
        return shippingLine1StreetAddress;
    }

    public void setShippingLine1StreetAddress(String shippingLine1StreetAddress) {
        this.shippingLine1StreetAddress = shippingLine1StreetAddress;
    }

    public String getShippingLine2StreetAddress() {
        return shippingLine2StreetAddress;
    }

    public void setShippingLine2StreetAddress(String shippingLine2StreetAddress) {
        this.shippingLine2StreetAddress = shippingLine2StreetAddress;
    }

    public boolean getRecurredInvoiceIndicator() {
        return recurredInvoiceIndicator;
    }

    public void setRecurredInvoiceIndicator(boolean recurredInvoiceIndicator) {
        this.recurredInvoiceIndicator = recurredInvoiceIndicator;
    }

    public Date getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(Date reportedDate) {
        this.reportedDate = reportedDate;
    }

    /**
     * Get a string representation for billing chart/organization
     *
     * @return
     */
    public String getBilledByChartOfAccCodeAndOrgCode() {
        String returnVal = getBillByChartOfAccountCode() + "/" + getBilledByOrganizationCode();

        return returnVal;
    }

    /**
     * Populate Customer Billing Address fields on Customer Invoice.
     *
     * @return
     */
    public void setCustomerBillToAddressOnInvoice(CustomerAddress customerBillToAddress) {
        accountsReceivableDocumentHeader.refreshReferenceObject("customer");
        if (ObjectUtils.isNotNull(accountsReceivableDocumentHeader.getCustomer())) {
            this.setCustomerName(accountsReceivableDocumentHeader.getCustomer().getCustomerName());
        }

        if (ObjectUtils.isNotNull(customerBillToAddress)) {
            this.setBillingAddressTypeCode(customerBillToAddress.getCustomerAddressTypeCode());
            this.setBillingAddressName(customerBillToAddress.getCustomerAddressName());
            this.setBillingLine1StreetAddress(customerBillToAddress.getCustomerLine1StreetAddress());
            this.setBillingLine2StreetAddress(customerBillToAddress.getCustomerLine2StreetAddress());
            this.setBillingCityName(customerBillToAddress.getCustomerCityName());
            this.setBillingStateCode(customerBillToAddress.getCustomerStateCode());
            this.setBillingZipCode(customerBillToAddress.getCustomerZipCode());
            this.setBillingCountryCode(customerBillToAddress.getCustomerCountryCode());
            this.setBillingAddressInternationalProvinceName(customerBillToAddress.getCustomerAddressInternationalProvinceName());
            this.setBillingInternationalMailCode(customerBillToAddress.getCustomerInternationalMailCode());
            this.setBillingEmailAddress(customerBillToAddress.getCustomerEmailAddress());
        }
    }

    /**
     * Populate Customer Shipping Address fields on Customer Invoice.
     *
     * @return
     */
    public void setCustomerShipToAddressOnInvoice(CustomerAddress customerShipToAddress) {

        accountsReceivableDocumentHeader.refreshReferenceObject("customer");
        Customer customer = accountsReceivableDocumentHeader.getCustomer();
        if (ObjectUtils.isNotNull(customer)) {
            this.setCustomerName(customer.getCustomerName());
        }

        if (ObjectUtils.isNotNull(customerShipToAddress)) {
            this.setShippingAddressTypeCode(customerShipToAddress.getCustomerAddressTypeCode());
            this.setShippingAddressName(customerShipToAddress.getCustomerAddressName());
            this.setShippingLine1StreetAddress(customerShipToAddress.getCustomerLine1StreetAddress());
            this.setShippingLine2StreetAddress(customerShipToAddress.getCustomerLine2StreetAddress());
            this.setShippingCityName(customerShipToAddress.getCustomerCityName());
            this.setShippingStateCode(customerShipToAddress.getCustomerStateCode());
            this.setShippingZipCode(customerShipToAddress.getCustomerZipCode());
            this.setShippingCountryCode(customerShipToAddress.getCustomerCountryCode());
            this.setShippingAddressInternationalProvinceName(customerShipToAddress.getCustomerAddressInternationalProvinceName());
            this.setShippingInternationalMailCode(customerShipToAddress.getCustomerInternationalMailCode());
            this.setShippingEmailAddress(customerShipToAddress.getCustomerEmailAddress());
        }
        else {
            this.setShippingAddressTypeCode(null);
            this.setShippingAddressName(null);
            this.setShippingLine1StreetAddress(null);
            this.setShippingLine2StreetAddress(null);
            this.setShippingCityName(null);
            this.setShippingStateCode(null);
            this.setShippingZipCode(null);
            this.setShippingCountryCode(null);
            this.setShippingAddressInternationalProvinceName(null);
            this.setShippingInternationalMailCode(null);
            this.setShippingEmailAddress(null);
        }
    }

    /**
     * Gets the quickApply attribute.
     *
     * @return Returns the quickApply.
     */
    //TODO Andrew - this is payapp specific stuff and needs to go
//    public boolean isQuickApply() {
//        return quickApply;
//    }
//
//    //TODO Andrew - this is payapp specific stuff and needs to go
//    public boolean getQuickApply() {
//        return isQuickApply();
//    }
//
//    /**
//     * Sets the quickApply attribute value.
//     *
//     * @param quickApply The quickApply to set.
//     */
//    //TODO Andrew - this is payapp specific stuff and needs to go
//    public void setQuickApply(boolean quickApply) {
//        this.quickApply = quickApply;
//    }


    /**
     * Answers true when invoice recurrence details are provided by the user
     *
     * @see org.kuali.kfs.sys.document.FinancialSystemTransactionalDocumentBase#answerSplitNodeQuestion(java.lang.String)
     */
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (HAS_RECCURENCE_NODE.equals(nodeName)) {
            return hasRecurrence();
        }
        if (BATCH_GENERATED_NODE.equals(nodeName)) {
            return isBatchGenerated();
        }
        throw new UnsupportedOperationException("answerSplitNode('" + nodeName + "') was called but no handler for nodeName specified.");
    }

    /**
     *
     * Determines whether this document was generated from a recurrence batch.  Returns true if so, false if not.
     * @return
     */
    protected boolean isBatchGenerated() {
        return recurredInvoiceIndicator;
    }

    /**
     *
     * Determines whether this document has a Recurrence filled out enough to create an INVR doc.
     * @return
     */
    protected boolean hasRecurrence() {
        return (ObjectUtils.isNotNull(getCustomerInvoiceRecurrenceDetails()) && getCustomerInvoiceRecurrenceDetails().isActive());
    }

    @Override
    public void setCustomerBillToAddress(AccountsReceivableCustomerAddress customerBillToAddress) {
        this.customerBillToAddress = (CustomerAddress) customerBillToAddress;
    }

    @Override
    public void setBillingAddressTypeCodeAsPrimary() {
        setBillingAddressTypeCode(ArKeyConstants.CustomerConstants.CUSTOMER_ADDRESS_TYPE_CODE_PRIMARY);
    }

    @Override
    public void setCustomerInvoiceRecurrenceDetails(AccountsReceivableCustomerInvoiceRecurrenceDetails customerInvoiceRecurrenceDetails) {
        this.customerInvoiceRecurrenceDetails = (CustomerInvoiceRecurrenceDetails) customerInvoiceRecurrenceDetails;
    }

    @Override
    public void setAccountsReceivableDocumentHeader(org.kuali.kfs.integration.ar.AccountsReceivableDocumentHeader accountsReceivableDocumentHeader) {
        this.accountsReceivableDocumentHeader = (org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader)accountsReceivableDocumentHeader;
    }

    private Timestamp agingReportSentTime;

    /**
     * Gets the agingReportSentTime attribute.
     * @return Returns the agingReportSentTime.
     */
    public Timestamp getAgingReportSentTime() {
        return agingReportSentTime;
    }

    /**
     * Sets the agingReportSentTime attribute value.
     * @param agingReportSentTime The agingReportSentTime to set.
     */
    public void setAgingReportSentTime(Timestamp agingReportSentTime) {
        this.agingReportSentTime = agingReportSentTime;
    }
}
