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
package org.kuali.kfs.module.cam.document.validation.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.gl.businessobject.UniversityDate;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsKeyConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPaymentDetail;
import org.kuali.kfs.module.cam.document.gl.AssetGlobalGeneralLedgerPendingEntrySource;
import org.kuali.kfs.module.cam.document.service.AssetAcquisitionTypeService;
import org.kuali.kfs.module.cam.document.service.AssetGlobalService;
import org.kuali.kfs.module.cam.document.service.AssetLocationService;
import org.kuali.kfs.module.cam.document.service.AssetService;
import org.kuali.kfs.module.cam.document.service.AssetLocationService.LocationField;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.ParameterService;
import org.kuali.rice.kns.bo.PersistableBusinessObject;
import org.kuali.rice.kns.bo.user.UniversalUser;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

/**
 * Rule implementation for Asset Global document.
 */
public class AssetGlobalRule extends MaintenanceDocumentRuleBase {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AssetGlobalRule.class);
    
    private static final Map<LocationField, String> LOCATION_FIELD_MAP = new HashMap<LocationField, String>();
    static {
        LOCATION_FIELD_MAP.put(LocationField.CAMPUS_CODE, CamsPropertyConstants.AssetGlobalDetail.CAMPUS_CODE);
        LOCATION_FIELD_MAP.put(LocationField.BUILDING_CODE, CamsPropertyConstants.AssetGlobalDetail.BUILDING_CODE);
        LOCATION_FIELD_MAP.put(LocationField.ROOM_NUMBER, CamsPropertyConstants.AssetGlobalDetail.BUILDING_ROOM_NUMBER);
        LOCATION_FIELD_MAP.put(LocationField.SUB_ROOM_NUMBER, CamsPropertyConstants.AssetGlobalDetail.BUILDING_SUB_ROOM_NUMBER);
        LOCATION_FIELD_MAP.put(LocationField.CONTACT_NAME, CamsPropertyConstants.AssetGlobalDetail.OFF_CAMPUS_NAME);
        LOCATION_FIELD_MAP.put(LocationField.STREET_ADDRESS, CamsPropertyConstants.AssetGlobalDetail.OFF_CAMPUS_ADDRESS);
        LOCATION_FIELD_MAP.put(LocationField.CITY_NAME, CamsPropertyConstants.AssetGlobalDetail.OFF_CAMPUS_CITY_NAME);
        LOCATION_FIELD_MAP.put(LocationField.STATE_CODE, CamsPropertyConstants.AssetGlobalDetail.OFF_CAMPUS_STATE_CODE);
        LOCATION_FIELD_MAP.put(LocationField.ZIP_CODE, CamsPropertyConstants.AssetGlobalDetail.OFF_CAMPUS_ZIP_CODE);
        LOCATION_FIELD_MAP.put(LocationField.COUNTRY_CODE, CamsPropertyConstants.AssetGlobalDetail.OFF_CAMPUS_COUNTRY_CODE);
        LOCATION_FIELD_MAP.put(LocationField.LOCATION_TAB_KEY, CamsPropertyConstants.AssetGlobalDetail.VERSION_NUM);
    }
    private static ParameterService parameterService = SpringContext.getBean(ParameterService.class);
    private static AssetService assetService = SpringContext.getBean(AssetService.class);
    private static AssetAcquisitionTypeService assetAcquisitionTypeService = SpringContext.getBean(AssetAcquisitionTypeService.class);
    private static AssetGlobalService assetGlobalService = SpringContext.getBean(AssetGlobalService.class);
    private static BusinessObjectService boService = SpringContext.getBean(BusinessObjectService.class);
    private static DataDictionaryService dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);

    /**
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#checkAuthorizationRestrictions(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean checkAuthorizationRestrictions(MaintenanceDocument document) {
        
        boolean success = true;
        
        AssetGlobal assetGlobal = (AssetGlobal) document.getNewMaintainableObject().getBusinessObject();
        if (assetGlobalService.isAssetSeparateDocument(assetGlobal)) {
            return success;
        }
        return success;
    }   
    
    
    private boolean checkReferenceExists(AssetGlobal assetGlobal, AssetPaymentDetail assetPaymentDetail) {
        boolean valid = true;

        if (StringUtils.isBlank(assetGlobal.getAcquisitionTypeCode()) || ObjectUtils.isNull(assetGlobal.getAcquisitionType())) {
            putFieldError(CamsPropertyConstants.AssetGlobal.ACQUISITION_TYPE_CODE, CamsKeyConstants.AssetGlobal.ERROR_ACQUISITION_TYPE_CODE_REQUIRED);
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.VERSION_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_ASSET_LOCATION_DEPENDENCY);
            valid = false;
        }

        if (StringUtils.isBlank(assetGlobal.getInventoryStatusCode())) {
            putFieldError(CamsPropertyConstants.AssetGlobal.INVENTORY_STATUS_CODE, CamsKeyConstants.AssetGlobal.ERROR_INVENTORY_STATUS_REQUIRED);
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.VERSION_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_ASSET_LOCATION_DEPENDENCY);
            valid = false;
        }

        // Check for account
        assetPaymentDetail.refreshReferenceObject(CamsPropertyConstants.AssetPaymentDetail.ACCOUNT);
        if (StringUtils.isBlank(assetPaymentDetail.getAccountNumber()) || isAccountInvalid(assetPaymentDetail.getAccount())) {
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.ACCOUNT_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_OWNER_ACCT_NOT_ACTIVE, new String[] { assetPaymentDetail.getChartOfAccountsCode(), assetPaymentDetail.getAccountNumber() });
            valid = false;
        }

        // Check for ObjectCode
        if (StringUtils.isNotBlank(assetPaymentDetail.getFinancialObjectCode())) {
            if (assetPaymentDetail.getPostingYear() == null) {
                GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_FISCAL_YEAR, CamsKeyConstants.AssetGlobal.ERROR_FINANCIAL_DOCUMENT_POSTING_YEAR_REQUIRED);
                valid = false;
            }
            else {
                assetPaymentDetail.refreshReferenceObject(CamsPropertyConstants.AssetPaymentDetail.OBJECT_CODE);
                if (ObjectUtils.isNull(assetPaymentDetail.getObjectCode())) {
                    GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.FINANCIAL_OBJECT_CODE, CamsKeyConstants.AssetGlobal.ERROR_CAPITAL_OBJECT_CODE_INVALID, assetPaymentDetail.getFinancialObjectCode());
                    valid = false;
                }
            }
        }

        return valid;
    }


    private boolean checkReferenceExists(AssetGlobalDetail assetGlobalDetail) {
        boolean valid = true;
        if (StringUtils.isNotBlank(assetGlobalDetail.getCampusCode())) {
            assetGlobalDetail.refreshReferenceObject(CamsPropertyConstants.AssetGlobalDetail.CAMPUS);
            if (ObjectUtils.isNull(assetGlobalDetail.getCampus())) {
                GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetGlobalDetail.CAMPUS_CODE, CamsKeyConstants.AssetLocation.ERROR_INVALID_CAMPUS_CODE, assetGlobalDetail.getCampusCode());
                valid &= false;
            }
        }
        if (StringUtils.isNotBlank(assetGlobalDetail.getBuildingCode())) {
            assetGlobalDetail.refreshReferenceObject(CamsPropertyConstants.AssetGlobalDetail.BUILDING);
            if (ObjectUtils.isNull(assetGlobalDetail.getBuilding()) || !assetGlobalDetail.getBuilding().isActive()) {
                GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetGlobalDetail.BUILDING_CODE, CamsKeyConstants.AssetLocation.ERROR_INVALID_BUILDING_CODE, assetGlobalDetail.getBuildingCode(), assetGlobalDetail.getCampusCode());
                valid &= false;
            }
        }
        if (StringUtils.isNotBlank(assetGlobalDetail.getBuildingRoomNumber())) {
            assetGlobalDetail.refreshReferenceObject(CamsPropertyConstants.AssetGlobalDetail.BUILDING_ROOM);
            if (ObjectUtils.isNull(assetGlobalDetail.getBuildingRoom()) || !assetGlobalDetail.getBuildingRoom().isActive()) {
                GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetGlobalDetail.BUILDING_ROOM_NUMBER, CamsKeyConstants.AssetLocation.ERROR_INVALID_ROOM_NUMBER, assetGlobalDetail.getBuildingCode(), assetGlobalDetail.getBuildingRoomNumber(), assetGlobalDetail.getCampusCode());
                valid &= false;
            }
        }
        if (StringUtils.isNotBlank(assetGlobalDetail.getOffCampusStateCode())) {
            assetGlobalDetail.refreshReferenceObject(CamsPropertyConstants.AssetGlobalDetail.OFF_CAMPUS_STATE);
            if (ObjectUtils.isNull(assetGlobalDetail.getOffCampusState())) {
                GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetGlobalDetail.OFF_CAMPUS_STATE_CODE, CamsKeyConstants.AssetLocation.ERROR_INVALID_OFF_CAMPUS_STATE, assetGlobalDetail.getOffCampusStateCode());
                valid &= false;
            }
        }
        return valid;
    }

    /**
     * This explicit check needs to be done, else DB constraint will fail when "SAVE" button is clicked with invalid chart and
     * account values
     * 
     * @param assetGlobal Asset Global
     * @return true if values are valid
     */
    private boolean checkReferenceExists(AssetGlobal assetGlobal) {
        boolean valid = true;
        if (StringUtils.isNotBlank(assetGlobal.getOrganizationOwnerChartOfAccountsCode())) {
            assetGlobal.refreshReferenceObject(CamsPropertyConstants.AssetGlobal.ORGANIZATION_OWNER_CHART);
            if (ObjectUtils.isNull(assetGlobal.getOrganizationOwnerChartOfAccounts())) {
                putFieldError(CamsPropertyConstants.AssetGlobal.ORGANIZATION_OWNER_CHART_OF_ACCOUNTS, CamsKeyConstants.AssetGlobal.ERROR_OWNER_CHART_INVALID, new String[] { assetGlobal.getOrganizationOwnerChartOfAccountsCode() });
                valid &= false;
            }
        }
        if (StringUtils.isNotBlank(assetGlobal.getOrganizationOwnerAccountNumber())) {
            assetGlobal.refreshReferenceObject(CamsPropertyConstants.AssetGlobal.ORGANIZATION_OWNER_ACCOUNT);
            assetGlobal.refreshReferenceObject(CamsPropertyConstants.AssetGlobal.ORGANIZATION_OWNER_ACCOUNT);
            if (ObjectUtils.isNull(assetGlobal.getOrganizationOwnerAccount())) {
                putFieldError(CamsPropertyConstants.AssetGlobal.ORGANIZATION_OWNER_ACCT_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_OWNER_ACCT_NUMBER_INVALID, new String[] { assetGlobal.getOrganizationOwnerChartOfAccountsCode(), assetGlobal.getOrganizationOwnerAccountNumber() });
                valid &= false;
            }
        }

        return valid;
    }

    private boolean isCapitalStatus(AssetGlobal assetGlobal) {
        return parameterService.getParameterValues(Asset.class, CamsConstants.Parameters.CAPITAL_ASSET_STATUS_CODES).contains(assetGlobal.getInventoryStatusCode());
    }

    private boolean isStatusCodeRetired(String statusCode) {
        return parameterService.getParameterValues(Asset.class, CamsConstants.Parameters.RETIRED_STATUS_CODES).contains(statusCode);
    }

    @Override
    public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
        boolean success = super.processCustomAddCollectionLineBusinessRules(document, collectionName, line);
        AssetGlobal assetGlobal = (AssetGlobal) document.getNewMaintainableObject().getBusinessObject();
        List<AssetGlobalDetail> assetSharedDetails = assetGlobal.getAssetSharedDetails();
        int pos = assetSharedDetails.size() - 1;
        if (CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS.equals(collectionName)) {
            // handle location information
            AssetGlobalDetail assetGlobalDetail = (AssetGlobalDetail) line;
            success &= checkReferenceExists(assetGlobalDetail);
            success &= validateLocation(assetGlobal, assetGlobalDetail);
        }
        else if ((CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + pos + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS).equals(collectionName)) {
            // handle unique information
            AssetGlobalDetail assetUniqueDetail = (AssetGlobalDetail) line;
            String campusTagNumber = assetUniqueDetail.getCampusTagNumber();
            if (StringUtils.isNotBlank(campusTagNumber)) {
                success = validateTagDuplication(assetSharedDetails, campusTagNumber);
            }
        }
        else if (CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS.equals(collectionName)) {
            AssetPaymentDetail assetPaymentDetail = (AssetPaymentDetail) line;
            if (success &= checkReferenceExists(assetGlobal, assetPaymentDetail)) {
                success &= validatePaymentLine(assetGlobal, assetPaymentDetail);
            }

        }
        return success;
    }


    private boolean validateTagDuplication(List<AssetGlobalDetail> assetSharedDetails, String campusTagNumber) {
        boolean success = true;
        for (AssetGlobalDetail assetSharedDetail : assetSharedDetails) {
            List<AssetGlobalDetail> assetGlobalUniqueDetails = assetSharedDetail.getAssetGlobalUniqueDetails();
            for (AssetGlobalDetail assetSharedUniqueDetail : assetGlobalUniqueDetails) {
                if (campusTagNumber.equalsIgnoreCase(assetSharedUniqueDetail.getCampusTagNumber())) {
                    success &= false;
                    GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetGlobalDetail.CAMPUS_TAG_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_CAMPUS_TAG_NUMBER_DUPLICATE, campusTagNumber);
                }
            }
        }
        if (success) {
            List<Asset> tagMatches = assetService.findActiveAssetsMatchingTagNumber(campusTagNumber);
            if (!tagMatches.isEmpty()) {
                GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetGlobalDetail.CAMPUS_TAG_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_CAMPUS_TAG_NUMBER_DUPLICATE, campusTagNumber);
                success &= false;
            }
        }
        return success;
    }

    private boolean validateTagDuplication(List<AssetGlobalDetail> assetSharedDetails) {
        HashSet<String> assetTags = new HashSet<String>();
        boolean success = true;
        int parentIndex = -1;
        int childIndex = -1;
        for (AssetGlobalDetail assetSharedDetail : assetSharedDetails) {
            parentIndex++;
            List<AssetGlobalDetail> assetGlobalUniqueDetails = assetSharedDetail.getAssetGlobalUniqueDetails();
            for (AssetGlobalDetail assetSharedUniqueDetail : assetGlobalUniqueDetails) {
                childIndex++;
                String campusTagNumber = assetSharedUniqueDetail.getCampusTagNumber();
                if (StringUtils.isNotBlank(campusTagNumber) && !assetTags.add(campusTagNumber)) {
                    success &= false;
                    String errorPath = MAINTAINABLE_ERROR_PREFIX + CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + parentIndex + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "[" + childIndex + "]";
                    GlobalVariables.getErrorMap().addToErrorPath(errorPath);
                    GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetGlobalDetail.CAMPUS_TAG_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_CAMPUS_TAG_NUMBER_DUPLICATE, campusTagNumber);
                    GlobalVariables.getErrorMap().removeFromErrorPath(errorPath);

                }
            }
            childIndex = -1;
            for (AssetGlobalDetail assetSharedUniqueDetail : assetGlobalUniqueDetails) {
                childIndex++;
                String campusTagNumber = assetSharedUniqueDetail.getCampusTagNumber();
                if (StringUtils.isNotBlank(campusTagNumber)) {
                    List<Asset> tagMatches = assetService.findActiveAssetsMatchingTagNumber(campusTagNumber);
                    if (!tagMatches.isEmpty()) {
                        success &= false;
                        String errorPath = MAINTAINABLE_ERROR_PREFIX + CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + parentIndex + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "[" + childIndex + "]";
                        GlobalVariables.getErrorMap().addToErrorPath(errorPath);
                        GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetGlobalDetail.CAMPUS_TAG_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_CAMPUS_TAG_NUMBER_DUPLICATE, campusTagNumber);
                        GlobalVariables.getErrorMap().removeFromErrorPath(errorPath);
                    }
                }
            }
        }
        return success;
    }

    private boolean validatePaymentLine(AssetGlobal assetGlobal, AssetPaymentDetail assetPaymentDetail) {
        boolean success = true;

        if (assetGlobalService.existsInGroup(CamsConstants.AssetGlobal.NEW_ACQUISITION_TYPE_CODE, assetGlobal.getAcquisitionTypeCode())) {
            success &= checkRequiredFieldsForNew(assetPaymentDetail);
        }
        else {
            // Validate Financial Document Type Code
            success &= validateDocumentTypeForNonNew(assetPaymentDetail.getExpenditureFinancialDocumentTypeCode());
        }

        // Validate Financial Posted date
        if (assetPaymentDetail.getExpenditureFinancialDocumentPostedDate() != null) {
            success &= validatePostedDate(assetPaymentDetail);
        }


        // handle payment information amount should be positive
        if (assetPaymentDetail.getAmount() != null && !assetPaymentDetail.getAmount().isPositive()) {
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.AMOUNT, CamsKeyConstants.AssetGlobal.ERROR_INVALID_PAYMENT_AMOUNT);
            success = false;
        }

        assetPaymentDetail.refreshReferenceObject(CamsPropertyConstants.AssetPaymentDetail.OBJECT_CODE);

        success &= validateObjectCode(assetPaymentDetail.getObjectCode(), assetGlobal);

        success &= validateObjectSubTypeCode(assetGlobal, assetPaymentDetail);

        return success;
    }

    /**
     * This method check the required fields for acquisition type New .
     * 
     * @param assetPaymentDetail
     * @return
     */
    private boolean checkRequiredFieldsForNew(AssetPaymentDetail assetPaymentDetail) {
        boolean valid = true;

        if (StringUtils.isBlank(assetPaymentDetail.getExpenditureFinancialDocumentNumber())) {
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_EXPENDITURE_FINANCIAL_DOCUMENT_NUMBER_REQUIRED);
            valid = false;
        }
        if (assetPaymentDetail.getExpenditureFinancialDocumentPostedDate() == null) {
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_DATE, CamsKeyConstants.AssetGlobal.ERROR_DOCUMENT_POSTING_DATE_REQUIRED);
            valid = false;
        }
        if (StringUtils.isBlank(assetPaymentDetail.getExpenditureFinancialDocumentTypeCode())) {
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_TYPE, CamsKeyConstants.AssetGlobal.ERROR_EXPENDITURE_FINANCIAL_DOCUMENT_TYPE_CODE_REQUIRED);
            valid = false;
        }
        return valid;
    }


    private boolean validatePostedDate(AssetPaymentDetail assetPaymentDetail) {
        boolean valid = true;
        Map<String, Object> primaryKeys = new HashMap<String, Object>();
        primaryKeys.put(KFSPropertyConstants.UNIVERSITY_DATE, assetPaymentDetail.getExpenditureFinancialDocumentPostedDate());
        UniversityDate universityDate = (UniversityDate) boService.findByPrimaryKey(UniversityDate.class, primaryKeys);
        if (universityDate == null) {
           GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_FISCAL_YEAR, CamsKeyConstants.AssetGlobal.ERROR_UNIVERSITY_NOT_DEFINED_FOR_DATE, new String[] { assetPaymentDetail.getExpenditureFinancialDocumentPostedDate().toString() });
           valid = false;          
        } else {
            assetPaymentDetail.setPostingYear(universityDate.getUniversityFiscalYear());
            assetPaymentDetail.setPostingPeriodCode(universityDate.getUniversityFiscalAccountingPeriod());
        }
        return valid;
    }

    /**
     * Check document type code is set to the desired value when acquisition type code is non-new.
     * 
     * @param documentTypeCode
     * @return
     */
    private boolean validateDocumentTypeForNonNew(String documentTypeCode) {
        boolean valid = true;
        if (StringUtils.isNotBlank(documentTypeCode) && !CamsConstants.AssetGlobal.ADD_ASSET_DOCUMENT_TYPE_CODE.equalsIgnoreCase(documentTypeCode)) {
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_TYPE, CamsKeyConstants.AssetGlobal.ERROR_DOCUMENT_TYPE_CODE_NOT_ALLOWED, documentTypeCode);
            valid = false;
        }
        return valid;
    }

    /**
     * Check object code is set to capital only when the status is capital.
     * 
     * @param assetGlobal
     * @param assetPaymentDetail
     * @return valid
     */
    private boolean validateObjectCode(ObjectCode objectCode, AssetGlobal assetGlobal) {
        boolean valid = true;
        // Check Object Code: Capital object code shall not be used for a non-capital asset.
        if (!isCapitalStatus(assetGlobal) && assetGlobalService.isCapitablObjectCode(objectCode)) {
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.FINANCIAL_OBJECT_CODE, CamsKeyConstants.AssetGlobal.ERROR_CAPITAL_OBJECT_CODE_NOT_ALLOWED);
            valid = false;
        }

        // The acquisition type code of (C, F, G, N, P, S, T) requires a capital object code.
        if (assetGlobalService.existsInGroup(CamsConstants.AssetGlobal.CAPITAL_OBJECT_ACCQUISITION_CODE_GROUP, assetGlobal.getAcquisitionTypeCode()) && !assetGlobalService.isCapitablObjectCode(objectCode)) {
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.FINANCIAL_OBJECT_CODE, CamsKeyConstants.AssetGlobal.ERROR_CAPITAL_OBJECT_CODE_REQUIRED);
            valid = false;
        }
        return valid;
    }


    private boolean isAccountInvalid(Account account) {
        return ObjectUtils.isNull(account) || account.isActive() || account.isExpired();
    }


    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        AssetGlobal assetGlobal = (AssetGlobal) document.getNewMaintainableObject().getBusinessObject();
        List<AssetGlobalDetail> assetSharedDetails = assetGlobal.getAssetSharedDetails();
        boolean success = super.processCustomRouteDocumentBusinessRules(document);
        
        // need at least one asset location
        if (assetSharedDetails.isEmpty() || assetSharedDetails.get(0).getAssetGlobalUniqueDetails().isEmpty()) {
            putFieldError(CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS, CamsKeyConstants.AssetGlobal.MIN_ONE_ASSET_REQUIRED);
            success &= false;
        }
        
        // Capital Asset must have payment zone.
        if (isCapitalStatus(assetGlobal) && assetGlobal.getAssetPaymentDetails().isEmpty()) {
            putFieldError(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS, CamsKeyConstants.AssetGlobal.MIN_ONE_PAYMENT_REQUIRED);
            success &= false;
        }
        
        // check if amount is above threshold for capital assets for normal user
        KualiDecimal totalPaymentByAsset = assetGlobalService.totalPaymentByAsset(assetGlobal);
        UniversalUser universalUser = GlobalVariables.getUserSession().getUniversalUser();
        String capitalizationThresholdAmount = parameterService.getParameterValue(AssetGlobal.class, CamsConstants.Parameters.CAPITALIZATION_LIMIT_AMOUNT);
        if (isCapitalStatus(assetGlobal) && totalPaymentByAsset.isLessThan(new KualiDecimal(capitalizationThresholdAmount)) && !universalUser.isMember(CamsConstants.Workgroups.WORKGROUP_CM_ADMINISTRATORS)) {
            putFieldError(CamsPropertyConstants.AssetGlobal.VERSION_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_CAPITAL_ASSET_PAYMENT_AMOUNT_MIN, capitalizationThresholdAmount);
            success = false;
        }

        // check if amount is less than threshold for non-capital assets for all users
        if (!isCapitalStatus(assetGlobal) && totalPaymentByAsset.isGreaterEqual(new KualiDecimal(capitalizationThresholdAmount))) {
            putFieldError(CamsPropertyConstants.AssetGlobal.VERSION_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_NON_CAPITAL_ASSET_PAYMENT_AMOUNT_MAX);
            success = false;
        }
        
        // for Asset Separate doc - new source payments must be greater than the capital asset cost amount
        if (assetGlobalService.isAssetSeparateDocument(assetGlobal)) {
            
            // new source payments must be greater than the capital asset cost amount
            KualiDecimal totalSeparateSourceAmount = assetGlobalService.totalSeparateSourceAmount(assetGlobal);
            if (totalSeparateSourceAmount.isGreaterThan(assetGlobal.getTotalCostAmount())) {
                putFieldError(CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS, CamsKeyConstants.AssetSeparate.ERROR_TOTAL_SEPARATE_SOURCE_AMOUNT_REQUIRED, new String[] { assetGlobal.getSeparateSourceCapitalAssetNumber().toString() });
                success = false;
            }

            // only active capital assets can be separated
            assetGlobal.refreshReferenceObject(CamsPropertyConstants.AssetGlobal.SEPARATE_SOURCE_CAPITAL_ASSET);
            if (ObjectUtils.isNotNull(assetGlobal.getSeparateSourceCapitalAsset())) {
                if (!assetService.isCapitalAsset(assetGlobal.getSeparateSourceCapitalAsset())) {
                    GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetGlobal.SEPARATE_SOURCE_CAPITAL_ASSET_NUMBER, CamsKeyConstants.AssetSeparate.ERROR_NON_CAPITAL_ASSET_SEPARATE_REQUIRED, assetGlobal.getSeparateSourceCapitalAssetNumber().toString());
                    success = false;
                }
            }

        } 
        
        success = validateLocationCollection(assetGlobal, assetSharedDetails);
        success = validateTagDuplication(assetSharedDetails);
        
        return success;
    }


    private boolean validateLocationCollection(AssetGlobal assetGlobal, List<AssetGlobalDetail> assetSharedDetails) {
        boolean success = true;
        // for each shared location info, validate
        boolean isCapitalAsset = isCapitalStatus(assetGlobal);
        int index = 0;
        for (AssetGlobalDetail assetLocationDetail : assetSharedDetails) {
            String errorPath = MAINTAINABLE_ERROR_PREFIX + CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + index + "]";
            GlobalVariables.getErrorMap().addToErrorPath(errorPath);
            success = SpringContext.getBean(AssetLocationService.class).validateLocation(LOCATION_FIELD_MAP, assetLocationDetail, isCapitalAsset, assetGlobal.getCapitalAssetType());
            GlobalVariables.getErrorMap().removeFromErrorPath(errorPath);
            index++;
        }
        return success;
    }


    /**
     * Validate all object sub type codes are from the same group.
     * 
     * @param assetGlobal
     * @param newLine
     * @return
     */
    private boolean validateObjectSubTypeCode(AssetGlobal assetGlobal, AssetPaymentDetail newLine) {
        boolean valid = true;
        List<String> objectSubTypeList = new ArrayList<String>();

        // build object sub type list
        if (ObjectUtils.isNotNull(newLine.getObjectCode())) {
            objectSubTypeList.add(newLine.getObjectCode().getFinancialObjectSubTypeCode());
        }

        for (AssetPaymentDetail assetPaymentDetail : assetGlobal.getAssetPaymentDetails()) {
            assetPaymentDetail.refreshReferenceObject(CamsPropertyConstants.AssetPaymentDetail.OBJECT_CODE);
            if (ObjectUtils.isNotNull(assetPaymentDetail.getObjectCode())) {
                objectSubTypeList.add(assetPaymentDetail.getObjectCode().getFinancialObjectSubTypeCode());
            }
        }

        if (!assetService.isObjectSubTypeCompatible(objectSubTypeList)) {
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetPaymentDetail.FINANCIAL_OBJECT_CODE, CamsKeyConstants.AssetGlobal.ERROR_INVALID_FIN_OBJECT_SUB_TYPE_CODE, dataDictionaryService.getAttributeLabel(ObjectCode.class, KFSPropertyConstants.FINANCIAL_OBJECT_SUB_TYPE_CODE), CamsConstants.Parameters.OBJECT_SUB_TYPE_GROUPS);
            
            valid = false;
        }
        return valid;
    }


    @Override
    protected boolean processCustomSaveDocumentBusinessRules(MaintenanceDocument document) {
        AssetGlobal assetGlobal = (AssetGlobal) document.getNewMaintainableObject().getBusinessObject();
        boolean success = true;
        success &= super.processCustomSaveDocumentBusinessRules(document);
        success &= validateAccount(assetGlobal);

        String acquisitionTypeCode = assetGlobal.getAcquisitionTypeCode();
        String statusCode = assetGlobal.getInventoryStatusCode();

        // no need to validate non-editable fields for "Asset Separate" doc
        if (!assetGlobalService.isAssetSeparateDocument(assetGlobal)) {
            if (CamsConstants.AssetGlobal.NEW_ACQUISITION_TYPE_CODE.equals(acquisitionTypeCode)) {
                UniversalUser universalUser = GlobalVariables.getUserSession().getUniversalUser();
                if (!universalUser.isMember(CamsConstants.Workgroups.WORKGROUP_CM_SUPER_USERS)) {
                    putFieldError(CamsPropertyConstants.AssetGlobal.ACQUISITION_TYPE_CODE, CamsKeyConstants.AssetGlobal.ERROR_ACQUISITION_TYPE_CODE_NOT_ALLOWED, new String[] { CamsConstants.Workgroups.WORKGROUP_CM_SUPER_USERS, acquisitionTypeCode });
                    success &= false;
                }
            }
            if (StringUtils.isNotBlank(statusCode) && (CamsConstants.InventoryStatusCode.CAPITAL_ASSET_UNDER_CONSTRUCTION.equals(statusCode) || isStatusCodeRetired(statusCode))) {
                putFieldError(CamsPropertyConstants.AssetGlobal.INVENTORY_STATUS_CODE, CamsKeyConstants.AssetGlobal.ERROR_INVENTORY_STATUS_CODE_INVALID, new String[] { statusCode });
                success &= false;
            }
            if (StringUtils.isNotBlank(acquisitionTypeCode) && StringUtils.isNotBlank(statusCode)) {
                // check if status code and acquisition type code combination is valid
                success &= SpringContext.getBean(ParameterService.class).getParameterEvaluator(AssetGlobal.class, CamsConstants.Parameters.VALID_ASSET_ACQUISITION_TYPES_BY_ASSET_STATUS, CamsConstants.Parameters.INVALID_ASSET_ACQUISITION_TYPES_BY_ASSET_STATUS, statusCode, acquisitionTypeCode).evaluateAndAddError(AssetGlobal.class, MAINTAINABLE_ERROR_PREFIX + CamsPropertyConstants.AssetGlobal.ACQUISITION_TYPE_CODE);
            }
            success &= validateAssetType(assetGlobal);
            if (isCapitalStatus(assetGlobal)) {
                success &= validateVendorAndManufacturer(assetGlobal);
            }

            success &= validatePaymentCollection(assetGlobal);
        }
        
        // System shall only GL entries if we have an incomeAssetObjectCode for this acquisitionTypeCode and the statusCode
        // is for capital assets
        if ((success & super.processCustomSaveDocumentBusinessRules(document))
                && assetAcquisitionTypeService.hasIncomeAssetObjectCode(acquisitionTypeCode)
                && this.isCapitalStatus(assetGlobal)) {

            // create poster
            AssetGlobalGeneralLedgerPendingEntrySource assetGlobalGlPoster = new AssetGlobalGeneralLedgerPendingEntrySource((FinancialSystemDocumentHeader) document.getDocumentHeader());
            // create postables
                assetGlobalService.createGLPostables(assetGlobal, assetGlobalGlPoster);
                
            if (SpringContext.getBean(GeneralLedgerPendingEntryService.class).generateGeneralLedgerPendingEntries(assetGlobalGlPoster)) {
                assetGlobal.setGeneralLedgerPendingEntries(assetGlobalGlPoster.getPendingEntries());
            } else {
                assetGlobalGlPoster.getPendingEntries().clear();
            }
        }
        
        return success;
    }


    private boolean validatePaymentCollection(AssetGlobal assetGlobal) {
        boolean success = true;
        int index = 0;
        for (AssetPaymentDetail assetPaymentDetail : assetGlobal.getAssetPaymentDetails()) {
            String errorPath = MAINTAINABLE_ERROR_PREFIX + CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + index + "]";
            GlobalVariables.getErrorMap().addToErrorPath(errorPath);
            success &= validatePaymentLine(assetGlobal, assetPaymentDetail);
            GlobalVariables.getErrorMap().remove(errorPath);
            index++;
        }
        return success;
    }

    private boolean validateVendorAndManufacturer(AssetGlobal assetGlobal) {
        boolean success = true;
        if (StringUtils.isBlank(assetGlobal.getVendorName())) {
            putFieldError(CamsPropertyConstants.AssetGlobal.VENDOR_NAME, CamsKeyConstants.AssetGlobal.ERROR_VENDOR_NAME_REQUIRED);
            success &= false;
        }
        if (StringUtils.isBlank(assetGlobal.getManufacturerName())) {
            putFieldError(CamsPropertyConstants.AssetGlobal.MFR_NAME, CamsKeyConstants.AssetGlobal.ERROR_MFR_NAME_REQUIRED);
            success &= false;
        }
        return success;
    }

    @Override
    public boolean processSaveDocument(Document document) {
        MaintenanceDocument maintenanceDocument = (MaintenanceDocument) document;
        AssetGlobal assetGlobal = (AssetGlobal) maintenanceDocument.getNewMaintainableObject().getBusinessObject();
        if (!checkReferenceExists(assetGlobal)) {
            return false;
        }
        return super.processSaveDocument(document);
    }

    private boolean validateAccount(AssetGlobal assetGlobal) {
        boolean success = true;
        assetGlobal.refreshReferenceObject(CamsPropertyConstants.AssetGlobal.ORGANIZATION_OWNER_ACCOUNT);
        Account organizationOwnerAccount = assetGlobal.getOrganizationOwnerAccount();
        if (StringUtils.isNotBlank(assetGlobal.getOrganizationOwnerAccountNumber()) && (organizationOwnerAccount == null || organizationOwnerAccount.isActive() || organizationOwnerAccount.isExpired())) {
            putFieldError(CamsPropertyConstants.AssetGlobal.ORGANIZATION_OWNER_ACCOUNT_NUMBER, CamsKeyConstants.AssetGlobal.ERROR_OWNER_ACCT_NOT_ACTIVE, new String[] { assetGlobal.getOrganizationOwnerChartOfAccountsCode(), assetGlobal.getOrganizationOwnerAccountNumber() });
            success &= false;
        }
        return success;
    }

    private boolean validateLocation(AssetGlobal assetGlobal, AssetGlobalDetail assetGlobalDetail) {
        boolean success = true;
        if (StringUtils.isBlank(assetGlobal.getInventoryStatusCode())) {
            putFieldError(CamsPropertyConstants.AssetGlobal.INVENTORY_STATUS_CODE, CamsKeyConstants.AssetGlobal.ERROR_INVENTORY_STATUS_REQUIRED);
            success = false;
        }
        success = validateAssetType(assetGlobal);
        if (success) {
            boolean isCapitalAsset = isCapitalStatus(assetGlobal);
            success = SpringContext.getBean(AssetLocationService.class).validateLocation(LOCATION_FIELD_MAP, assetGlobalDetail, isCapitalAsset, assetGlobal.getCapitalAssetType());
        } else {
            GlobalVariables.getErrorMap().putError(CamsPropertyConstants.AssetGlobalDetail.VERSION_NUM, CamsKeyConstants.AssetGlobal.ERROR_ASSET_LOCATION_DEPENDENCY);
        }
        return success;
    }

    private boolean validateAssetType(AssetGlobal assetGlobal) {
        boolean success = true;
        assetGlobal.refreshReferenceObject(CamsPropertyConstants.AssetGlobal.CAPITAL_ASSET_TYPE);
        if (ObjectUtils.isNull(assetGlobal.getCapitalAssetType())) {
            putFieldError(CamsPropertyConstants.AssetGlobal.CAPITAL_ASSET_TYPE_CODE, CamsKeyConstants.AssetGlobal.ERROR_ASSET_TYPE_REQUIRED);
            success = false;
        }
        return success;
    }

}