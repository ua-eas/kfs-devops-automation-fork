package edu.arizona.kfs.gl.web.struts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.fp.document.GeneralErrorCorrectionDocument;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.impl.KfsModuleServiceImpl;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kns.lookup.HtmlData;
import org.kuali.rice.kns.web.struts.action.KualiMultipleValueLookupAction;
import org.kuali.rice.kns.web.struts.form.MultipleValueLookupForm;
import org.kuali.rice.kns.web.ui.Column;
import org.kuali.rice.kns.web.ui.ResultRow;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.service.LookupService;
import org.kuali.rice.krad.service.impl.ModuleServiceBase;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.UrlFactory;

import edu.arizona.kfs.fp.document.validation.impl.GeneralErrorCorrectionDocumentRuleConstants;
import edu.arizona.kfs.gl.businessobject.EntryBo;
import edu.arizona.kfs.gl.businessobject.lookup.EntryBoHelperServiceImpl;
import edu.arizona.kfs.sys.KFSPropertyConstants;

/**
 * This class serves as the struts action for implementing GEC Entry lookups
 *
 * @author Adam Kost <kosta@email.arizona.edu> with some code adapted from UCI
 */

@SuppressWarnings("deprecation")
public class GecEntryLookupAction extends KualiMultipleValueLookupAction {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GecEntryLookupAction.class);

    private static transient volatile ObjectCodeService objectCodeService;
    private static transient volatile ParameterEvaluatorService parameterEvaluatorService;
    private static transient volatile LookupService lookupService;
    private static transient volatile SystemOptions systemOptions;
    private static transient volatile BusinessObjectService businessObjectService;
    private static transient volatile EntryBoHelperServiceImpl entryBoHelperService;

    private static ParameterEvaluatorService getParameterEvaluatorService() {
        if (parameterEvaluatorService == null) {
            parameterEvaluatorService = SpringContext.getBean(ParameterEvaluatorService.class);
        }
        return parameterEvaluatorService;
    }

    private static ObjectCodeService getObjectCodeService() {
        if (objectCodeService == null) {
            objectCodeService = SpringContext.getBean(ObjectCodeService.class);
        }
        return objectCodeService;
    }

    private static LookupService getLookupService() {
        if (lookupService == null) {
            lookupService = SpringContext.getBean(LookupService.class);
        }
        return lookupService;
    }

    private static SystemOptions getSystemOptions() {
        if (systemOptions == null) {
            systemOptions = SpringContext.getBean(OptionsService.class).getCurrentYearOptions();
        }
        return systemOptions;
    }

    private static BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }

    private static EntryBoHelperServiceImpl getEntryBoHelperService() {
        if (entryBoHelperService == null) {
            entryBoHelperService = new EntryBoHelperServiceImpl();
        }
        return entryBoHelperService;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<EntryBo> performMultipleValueLookup(MultipleValueLookupForm multipleValueLookupForm, List<ResultRow> resultTable, int maxRowsPerPage, boolean bounded) {
        Collection<EntryBo> c = super.performMultipleValueLookup(multipleValueLookupForm, resultTable, maxRowsPerPage, bounded);
        if (c == null || c.isEmpty()) {
            LOG.debug("No results found.");
            return new ArrayList<EntryBo>();
        }

        List<EntryBo> entries = new ArrayList<EntryBo>(c);
        List<EntryBo> entriesToRemove = new ArrayList<EntryBo>();
        List<EntryBo> entriesToDisable = new ArrayList<EntryBo>();

        for (EntryBo e : entries) {
            boolean removeEntry = removeEntry(e);
            boolean disableEntry = disableEntry(e);
            if (removeEntry) {
                entriesToRemove.add(e);
            }
            if (disableEntry) {
                entriesToDisable.add(e);
            }
        }

        removeRecords(entriesToRemove, entries, resultTable, multipleValueLookupForm);
        disableRecords(entriesToDisable, resultTable, multipleValueLookupForm);
        addObjectLinksToRecords(resultTable);
        multipleValueLookupForm.jumpToFirstPage(resultTable.size(), maxRowsPerPage);

        Map<String, String> displayedEntryIds = generateEntryIdMap(entries);
        multipleValueLookupForm.setCompositeObjectIdMap(displayedEntryIds);
        return entries;
    }

    private Map<String, String> generateEntryIdMap(List<EntryBo> entries) {
        Map<String, String> retval = new HashMap<String, String>();
        for (EntryBo entry : entries) {
            retval.put(entry.getEntryId(), entry.getEntryId());
        }
        return retval;
    }

    @Override
    public ActionForward prepareToReturnSelectedResults(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        MultipleValueLookupForm multipleValueLookupForm = (MultipleValueLookupForm) form;
        if (StringUtils.isBlank(multipleValueLookupForm.getLookupResultsSequenceNumber())) {
            // no search was executed
            return prepareToReturnNone(mapping, form, request, response);
        }

        prepareToReturnSelectedResultBOs(multipleValueLookupForm);

        // build the parameters for the refresh url
        Properties parameters = new Properties();
        parameters.put(KRADConstants.LOOKUP_RESULTS_BO_CLASS_NAME, multipleValueLookupForm.getBusinessObjectClassName());
        parameters.put(KRADConstants.LOOKUP_RESULTS_SEQUENCE_NUMBER, multipleValueLookupForm.getLookupResultsSequenceNumber());
        parameters.put(KRADConstants.DOC_FORM_KEY, multipleValueLookupForm.getFormKey());
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.RETURN_METHOD_TO_CALL);
        parameters.put(KRADConstants.REFRESH_CALLER, KRADConstants.MULTIPLE_VALUE);
        parameters.put(KRADConstants.ANCHOR, multipleValueLookupForm.getLookupAnchor());
        if (multipleValueLookupForm.getDocNum() != null) {
            parameters.put(KRADConstants.DOC_NUM, multipleValueLookupForm.getDocNum());
        }

        String backUrl = UrlFactory.parameterizeUrl(multipleValueLookupForm.getBackLocation(), parameters);
        return new ActionForward(backUrl, true);
    }

    /**
     * Exclude the entry selection base on custom logic.
     *
     * @param entry
     * @return
     */
    private boolean removeEntry(EntryBo entry) {
        LOG.debug("Determining if entry should be removed: " + entry.toString());
        ObjectCode code = getObjectCodeService().getByPrimaryId(entry.getUniversityFiscalYear(), entry.getChartOfAccountsCode(), entry.getFinancialObjectCode());

        // GEC Validation
        boolean objectTypeValid = getParameterEvaluatorService().getParameterEvaluator(GeneralErrorCorrectionDocument.class, GeneralErrorCorrectionDocumentRuleConstants.RESTRICTED_OBJECT_TYPE_CODES, entry.getFinancialObjectTypeCode()).evaluationSucceeds();
        boolean objectTypeValidBySubType = getParameterEvaluatorService().getParameterEvaluator(GeneralErrorCorrectionDocument.class, GeneralErrorCorrectionDocumentRuleConstants.VALID_OBJECT_SUB_TYPES_BY_OBJECT_TYPE, GeneralErrorCorrectionDocumentRuleConstants.INVALID_OBJECT_SUB_TYPES_BY_OBJECT_TYPE, code.getFinancialObjectTypeCode(), code.getFinancialObjectSubTypeCode()).evaluationSucceeds();
        boolean objectSubTypeValid = getParameterEvaluatorService().getParameterEvaluator(GeneralErrorCorrectionDocument.class, GeneralErrorCorrectionDocumentRuleConstants.RESTRICTED_OBJECT_SUB_TYPE_CODES, code.getFinancialObjectSubTypeCode()).evaluationSucceeds();
        boolean documentTypeValid = getParameterEvaluatorService().getParameterEvaluator(GeneralErrorCorrectionDocument.class, GeneralErrorCorrectionDocumentRuleConstants.DOCUMENT_TYPES, entry.getFinancialDocumentTypeCode()).evaluationSucceeds();
        boolean originationCodeValid = getParameterEvaluatorService().getParameterEvaluator(GeneralErrorCorrectionDocument.class, GeneralErrorCorrectionDocumentRuleConstants.ORIGIN_CODES, entry.getFinancialSystemOriginationCode()).evaluationSucceeds();

        if (!objectTypeValid || !objectTypeValidBySubType || !objectSubTypeValid || !documentTypeValid || !originationCodeValid) {
            LOG.debug("objectTypeValid=" + objectTypeValid + "; objectTypeValidBySubType=" + objectTypeValidBySubType + "; objectSubTypeValid=" + objectSubTypeValid + "; documentTypeValid=" + documentTypeValid + "; originationCodeValid=" + originationCodeValid);
            return true;
        }

        // Exclude offset entries.
        List<Bank> bankAccountList = (List<Bank>) getLookupService().findCollectionBySearchUnbounded(Bank.class, new HashMap<String, String>());
        boolean isOffsetDescription = KFSConstants.GL_PE_OFFSET_STRING.equalsIgnoreCase(entry.getTransactionLedgerEntryDescription());
        boolean isOffsetAccountEntry = isOffsetAccountEntry(bankAccountList, entry);
        if (isOffsetDescription || isOffsetAccountEntry) {
            LOG.debug("offsetDescription=[" + isOffsetDescription + ", " + entry.getTransactionLedgerEntryDescription() + "]; isOffsetAccountEntry=[" + isOffsetAccountEntry + ", " + entry.getAccountNumber() + "]");
            return true;
        }

        // Entry lookup criteria
        boolean balanceTypeValid = getSystemOptions().getActualFinancialBalanceTypeCd().equals(entry.getFinancialBalanceTypeCode());
        boolean entryAmoutIsZero = entry.getTransactionLedgerEntryAmount().isZero();
        if (!balanceTypeValid || entryAmoutIsZero) {
            LOG.debug("balanceTypeValid=" + balanceTypeValid + "; entryAmoutIsZero=" + entryAmoutIsZero);
            return true;
        }

        return false;
    }

    private boolean isOffsetAccountEntry(List<Bank> bankAccountList, EntryBo entry) {
        for (Bank bankAccount : bankAccountList) {
            if (bankAccount.getBankAccountNumber().equals(entry.getAccountNumber())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Disable the entry selection based on custom logic
     *
     * @param entry
     * @return
     */
    private boolean disableEntry(EntryBo entry) {
        LOG.debug("Determining if entry should be disabled: " + entry.toString());
        String gecDocumentNumber = entry.getGecDocumentNumber();
        if (StringUtils.isBlank(gecDocumentNumber)) {
            return false;
        }

        String docStatusCode = getDocumentStatusCode(gecDocumentNumber);
        if (docStatusCode.equals(KewApiConstants.ROUTE_HEADER_FINAL_CD)) {
            return true;
        }
        if (docStatusCode.equals(KewApiConstants.ROUTE_HEADER_ENROUTE_CD)) {
            return true;
        }
        if (docStatusCode.equals(KewApiConstants.ROUTE_HEADER_INITIATED_CD)) {
            return true;
        }
        if (docStatusCode.equals(KewApiConstants.ROUTE_HEADER_PROCESSED_CD)) {
            return true;
        }
        if (docStatusCode.equals(KewApiConstants.ROUTE_HEADER_SAVED_CD)) {
            return true;
        }
        if (docStatusCode.equals(KewApiConstants.ROUTE_HEADER_INITIATED_CD)) {
            return true;
        }
        return false;
    }

    private String getDocumentStatusCode(String documentNumber) {
        DocumentRouteHeaderValue docHeader = getBusinessObjectService().findBySinglePrimaryKey(DocumentRouteHeaderValue.class, documentNumber);
        if (docHeader == null) {
            return KFSConstants.EMPTY_STRING;
        }
        return docHeader.getDocRouteStatus();
    }

    private void removeRecords(List<EntryBo> entriesToRemove, List<EntryBo> entries, List<ResultRow> resultTable, MultipleValueLookupForm multipleValueLookupForm) {
        for (EntryBo entryToRemove : entriesToRemove) {
            entries.remove(entryToRemove);
            Iterator<ResultRow> iter = resultTable.iterator();
            while (iter.hasNext()) {
                ResultRow currentRow = iter.next();
                boolean isSameEntry = compareEntryToRow(entryToRemove, currentRow);
                if (isSameEntry) {
                    iter.remove();
                }
            }
        }
    }

    private void disableRecords(List<EntryBo> entriesToDisable, List<ResultRow> resultTable, MultipleValueLookupForm multipleValueLookupForm) {
        if (entriesToDisable.isEmpty()) {
            LOG.debug("entriesToRemove is Empty");
            return;
        }
        for (EntryBo entryToDisable : entriesToDisable) {
            for (ResultRow row : resultTable) {
                boolean isSameEntry = compareEntryToRow(entryToDisable, row);
                if (isSameEntry) {
                    row.setReturnUrl(StringUtils.EMPTY);
                    row.setRowReturnable(false);
                    setFieldObjectValue(row, KFSPropertyConstants.GEC_DOCUMENT_NUMBER, entryToDisable.getGecDocumentNumber());
                    LOG.debug("gecDocumentNumber=" + entryToDisable.getGecDocumentNumber());
                }
            }
        }
    }

    private void addObjectLinksToRecords(List<ResultRow> resultTable) {
        for (ResultRow row : resultTable) {
            setFieldObjectURL(row, KFSPropertyConstants.GEC_DOCUMENT_NUMBER);
            setFieldObjectURL(row, KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR);
            setFieldObjectURL(row, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
            setFieldObjectURL(row, KFSPropertyConstants.ACCOUNT_NUMBER);
            setFieldObjectURL(row, KFSPropertyConstants.SUB_ACCOUNT_NUMBER);
            setFieldObjectURL(row, KFSPropertyConstants.FINANCIAL_OBJECT_CODE);
            setFieldObjectURL(row, KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE);
            setFieldObjectURL(row, KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE);
            setFieldObjectURL(row, KFSPropertyConstants.FINANCIAL_OBJECT_TYPE_CODE);
            setFieldObjectURL(row, KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE);
            setFieldObjectURL(row, KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE);
            setFieldObjectURL(row, KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE);
            setFieldObjectURL(row, KFSPropertyConstants.DOCUMENT_NUMBER);
            setFieldObjectURL(row, KFSPropertyConstants.REFERENCE_FINANCIAL_DOCUMENT_TYPE_CODE);
            setFieldObjectURL(row, KFSPropertyConstants.REFERENCE_FINANCIAL_SYSTEM_ORIGINATION_CODE);
        }
    }

    private void setFieldObjectValue(ResultRow row, String field, String value) {
        int col = getColumnIndexByProperty(row.getColumns(), field);
        if (col != 1) {
            Column column = row.getColumns().get(col);
            if (value != null) {
                column.setPropertyValue(value);
            }
        }
    }

    private void setFieldObjectURL(ResultRow row, String field) {
        List<Column> columnList = row.getColumns();
        int col = getColumnIndexByProperty(columnList, field);
        if (col != 1) {
          Column column = columnList.get(col);
          if(StringUtils.isNotBlank(column.getPropertyValue())){
              Map<String,String[]> keys = getPrimaryKeysForInquiryURL(row, field);
//            HtmlData columnAnchor = new HtmlData.AnchorHtmlData(KewApiConstants.Namespaces.MODULE_NAME + KRADConstants.DOCHANDLER_DO_URL + entryToDisable.getGecDocumentNumber() + KRADConstants.DOCHANDLER_URL_CHUNK, KRADConstants.EMPTY_STRING);
//            columnList.get(col).setColumnAnchor(columnAnchor);
            String title = "show Inquiry for " + field + "=" + column.getPropertyValue();
            String inquiryURL = getInquiryURL(row, field, keys);
            HtmlData columnAnchor = new HtmlData.AnchorHtmlData(inquiryURL, title);
            column.setColumnAnchor(columnAnchor);
          }
        }
    }

    private Map<String, String[]> getPrimaryKeysForInquiryURL(ResultRow row, String field) {
        Map<String, String[]> retval = new HashMap<String, String[]>();
        int colField = getColumnIndexByProperty(row.getColumns(), field);
        Column column = row.getColumns().get(colField);

        switch (field) {
            case KFSPropertyConstants.GEC_DOCUMENT_NUMBER:
            case KFSPropertyConstants.DOCUMENT_NUMBER:
                //Class: DocHandler
                retval.put(KFSPropertyConstants.DOCUMENT_NUMBER, new String[] {column.getPropertyValue()});
                return retval;

            case KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR://businessObjectClassName=org.kuali.kfs.sys.businessobject.SystemOptions
            case KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE://businessObjectClassName=org.kuali.kfs.coa.businessobject.Chart
            case KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE://businessObjectClassName=org.kuali.kfs.sys.businessobject.OriginationCode  financialSystemOriginationCode=value
                retval.put(field,                                           new String[] {column.getPropertyValue()});
                return retval;

            case KFSPropertyConstants.ACCOUNT_NUMBER://businessObjectClassName=org.kuali.kfs.coa.businessobject.Account
                retval.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,     new String[] {getFieldValue(row, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE)});
                retval.put(field,                                           new String[] {column.getPropertyValue()});
                return retval;

            case KFSPropertyConstants.SUB_ACCOUNT_NUMBER:
                if (StringUtils.equals(column.getPropertyValue(), PdpPropertyConstants.CustomerProfile.CUSTOMER_DEFAULT_SUB_ACCOUNT_NUMBER)) {
                    return null;
                }
                retval.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,     new String[] {getFieldValue(row, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE)});
                retval.put(KFSPropertyConstants.ACCOUNT_NUMBER,             new String[] {getFieldValue(row, KFSPropertyConstants.ACCOUNT_NUMBER)});
                retval.put(field,                                           new String[] {column.getPropertyValue()});
                return retval;

            case KFSPropertyConstants.FINANCIAL_OBJECT_CODE://businessObjectClassName=org.kuali.kfs.coa.businessobject.ObjectCode   chartOfAccountsCode=UA  universityFiscalYear=2016   financialObjectCode=value
                retval.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR,     new String[] {getFieldValue(row, KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)});
                retval.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,     new String[] {getFieldValue(row, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE)});
                retval.put(field,                                           new String[] {column.getPropertyValue()});
                return retval;
            case KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE:
                if (StringUtils.equalsIgnoreCase(PdpPropertyConstants.CustomerProfile.CUSTOMER_DEFAULT_SUB_OBJECT_CODE, column.getPropertyValue())) {
                    return null;
                }
                retval.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR,     new String[] {getFieldValue(row, KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)});
                retval.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,     new String[] {getFieldValue(row, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE)});
                retval.put(KFSPropertyConstants.ACCOUNT_NUMBER,             new String[] {getFieldValue(row, KFSPropertyConstants.ACCOUNT_NUMBER)});
                retval.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE,      new String[] {getFieldValue(row, KFSPropertyConstants.FINANCIAL_OBJECT_CODE)});
                retval.put(field,                                           new String[] {column.getPropertyValue()});
                return retval;
                
            case KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE://businessObjectClassName=org.kuali.kfs.coa.businessobject.BalanceType
            case KFSPropertyConstants.FINANCIAL_OBJECT_TYPE_CODE://businessObjectClassName=org.kuali.kfs.coa.businessobject.ObjectType  code=value
            case KFSPropertyConstants.PROJECT_CODE://org.kuali.kfs.coa.businessobject.ProjectCode code=value if ----------  = null
                retval.put(KFSPropertyConstants.CODE,                       new String[] {column.getPropertyValue()});
                return retval;
            case KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE://businessObjectClassName=org.kuali.kfs.coa.businessobject.AccountingPeriod universityFiscalYear=2016  universityFiscalPeriodCode=value;
                retval.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR,     new String[] {getFieldValue(row, KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)});
                retval.put(field,                                           new String[] {column.getPropertyValue()});
                return retval;

            case KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE://businessObjectClassName=org.kuali.rice.kew.doctype.bo.DocumentTypeEBO  documentTypeId=4975623  docFormKey=88888888
            case KFSPropertyConstants.REFERENCE_FINANCIAL_DOCUMENT_TYPE_CODE://org.kuali.rice.kew.doctype.bo.DocumentTypeEBO
                retval.put(KFSPropertyConstants.DOCUMENT_TYPE_ID, new String[] {column.getPropertyValue()});//TODO Check the value, it's inconsistent.
                return retval;

            case KFSPropertyConstants.REFERENCE_FINANCIAL_SYSTEM_ORIGINATION_CODE:
                retval.put(KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE, new String[] {column.getPropertyValue()});
                return retval;
        }
        return retval;
    }

    private String getFieldValue(ResultRow row, String field) {
        int col = getColumnIndexByProperty(row.getColumns(), field);
        Column column = row.getColumns().get(col);
        String retval = column.getPropertyValue();
        return retval;
    }

    private String getInquiryURL(ResultRow row, String field, Map<String, String[]> keys) {
        String url = KFSConstants.EMPTY_STRING;
        Class clazz = null;
        switch (field) {
            case KFSPropertyConstants.GEC_DOCUMENT_NUMBER:                          url = KewApiConstants.Namespaces.MODULE_NAME + KRADConstants.DOCHANDLER_DO_URL + keys.get(KFSPropertyConstants.DOCUMENT_NUMBER) + KRADConstants.DOCHANDLER_URL_CHUNK;       break;
            case KFSPropertyConstants.DOCUMENT_NUMBER:                              url = KewApiConstants.Namespaces.MODULE_NAME + KRADConstants.DOCHANDLER_DO_URL + keys.get(KFSPropertyConstants.DOCUMENT_NUMBER) + KRADConstants.DOCHANDLER_URL_CHUNK;       break;
            case KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR:                       clazz=org.kuali.kfs.sys.businessobject.SystemOptions.class;             break;
            case KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE:                       clazz=org.kuali.kfs.coa.businessobject.Chart.class;                     break;
            case KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE:            clazz=org.kuali.kfs.sys.businessobject.OriginationCode.class;           break;
            case KFSPropertyConstants.REFERENCE_FINANCIAL_SYSTEM_ORIGINATION_CODE:  clazz=org.kuali.kfs.sys.businessobject.OriginationCode.class;           break;
            case KFSPropertyConstants.ACCOUNT_NUMBER:                               clazz=org.kuali.kfs.coa.businessobject.Account.class;                   break;
            case KFSPropertyConstants.SUB_ACCOUNT_NUMBER:                           clazz=org.kuali.kfs.coa.businessobject.SubAccount.class;                break;
            case KFSPropertyConstants.FINANCIAL_OBJECT_CODE:                        clazz=org.kuali.kfs.coa.businessobject.ObjectCode.class;                break;
            case KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE:                    clazz=org.kuali.kfs.coa.businessobject.SubObjectCode.class;             break;
            case KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE:                  clazz=org.kuali.kfs.coa.businessobject.BalanceType.class;               break;
            case KFSPropertyConstants.FINANCIAL_OBJECT_TYPE_CODE:                   clazz=org.kuali.kfs.coa.businessobject.ObjectType.class;                break;
            case KFSPropertyConstants.PROJECT_CODE:                                 clazz=org.kuali.kfs.coa.businessobject.ProjectCode.class;               break;
            case KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE:                clazz=org.kuali.kfs.coa.businessobject.AccountingPeriod.class;          break;
            case KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE:                 break;//businessObjectClassName=org.kuali.rice.kew.doctype.bo.DocumentTypeEBO  documentTypeId=4975623  docFormKey=88888888
            case KFSPropertyConstants.REFERENCE_FINANCIAL_DOCUMENT_TYPE_CODE:       break;//org.kuali.rice.kew.doctype.bo.DocumentTypeEBO
                

        }
        
        if (clazz != null && url.equals(KFSConstants.EMPTY_STRING))
            url = UrlFactory.parameterizeUrl(getInquiryUrl(clazz), getUrlParameters(field, keys));
        
        
        return url;
    }
    
    private String getInquiryUrl(Class inquiryBusinessObjectClass) {
        String inquiryUrl = KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsString(KRADConstants.APPLICATION_URL_KEY);
        String potentialUrlAddition = "kr/";
        if (!inquiryUrl.endsWith("/")) {
            inquiryUrl = inquiryUrl + "/";
        }
        return inquiryUrl + potentialUrlAddition + KRADConstants.INQUIRY_ACTION;
    }

    private Properties getUrlParameters(String businessObjectClassAttribute, Map<String, String[]> parameters) {
        Properties urlParameters = new Properties();
        for (String paramName : parameters.keySet()) {
            String[] parameterValues = parameters.get(paramName);
            if (parameterValues.length > 0) {
                urlParameters.put(paramName, parameterValues[0]);
            }
        }
        urlParameters.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, businessObjectClassAttribute);
        urlParameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.START_METHOD);
        return urlParameters;
    }

    private boolean compareEntryToRow(EntryBo entry, ResultRow row) {
        List<Column> columnList = row.getColumns();
        boolean isSameEntry = true;

        isSameEntry &= isPropertyEqual(entry.getUniversityFiscalYear().toString(), KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, columnList);
        isSameEntry &= isPropertyEqual(entry.getChartOfAccountsCode(), KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, columnList);
        isSameEntry &= isPropertyEqual(entry.getAccountNumber(), KFSPropertyConstants.ACCOUNT_NUMBER, columnList);
        isSameEntry &= isPropertyEqual(entry.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER, columnList);
        isSameEntry &= isPropertyEqual(entry.getFinancialObjectCode(), KFSPropertyConstants.FINANCIAL_OBJECT_CODE, columnList);
        isSameEntry &= isPropertyEqual(entry.getFinancialSubObjectCode(), KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE, columnList);
        isSameEntry &= isPropertyEqual(entry.getFinancialBalanceTypeCode(), KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, columnList);
        isSameEntry &= isPropertyEqual(entry.getFinancialObjectTypeCode(), KFSPropertyConstants.FINANCIAL_OBJECT_TYPE_CODE, columnList);
        isSameEntry &= isPropertyEqual(entry.getUniversityFiscalPeriodCode(), KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE, columnList);
        isSameEntry &= isPropertyEqual(entry.getFinancialDocumentTypeCode(), KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE, columnList);
        isSameEntry &= isPropertyEqual(entry.getFinancialSystemOriginationCode(), KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE, columnList);
        isSameEntry &= isPropertyEqual(entry.getDocumentNumber(), KFSPropertyConstants.DOCUMENT_NUMBER, columnList);
        isSameEntry &= isPropertyEqual(entry.getTransactionLedgerEntrySequenceNumber().toString(), KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER, columnList);

        return isSameEntry;
    }

    private boolean isPropertyEqual(String entryPropertyValue, String propertyName, List<Column> columnList) {
        int col = getColumnIndexByProperty(columnList, propertyName);
        if (col == -1) {
            return true;
        }
        String columnValue = columnList.get(col).getPropertyValue();
        boolean isEqual = StringUtils.equals(entryPropertyValue, columnValue);
        return isEqual;
    }

    private int getColumnIndexByProperty(List<Column> columnList, String propertyName) {
        for (int i = 0; i < columnList.size(); i++) {
            if (StringUtils.equals(propertyName, columnList.get(i).getPropertyName())) {
                return i;
            }
        }
        return -1;// not in the list
    }

}
