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
package org.kuali.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.BulkReceivingDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.BulkReceivingService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.document.validation.ContinuePurapRule;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.rules.DocumentRuleBase;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.document.TransactionalDocument;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

public class BulkReceivingDocumentRule extends DocumentRuleBase implements ContinuePurapRule {

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(Document document) {       
        
        boolean valid = true;
        
        BulkReceivingDocument bulkReceivingDocument = (BulkReceivingDocument)document;
        
        GlobalVariables.getMessageMap().clearErrorPath();
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.DOCUMENT);
        
        valid &= super.processCustomRouteDocumentBusinessRules(document);
        valid &= canCreateBulkReceivingDocument(bulkReceivingDocument);
        
        return valid;
    }
    
    public boolean processContinuePurapBusinessRules(TransactionalDocument document) {
        
        boolean valid = true;
        BulkReceivingDocument bulkReceivingDocument = (BulkReceivingDocument)document;
        
        GlobalVariables.getMessageMap().clearErrorPath();
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.DOCUMENT);
        
        valid = hasRequiredFieldsForContinue(bulkReceivingDocument) &&
                canCreateBulkReceivingDocument(bulkReceivingDocument);
        
        return valid;
    }
    
    /**
     * Make sure the required fields on the init screen are filled in.
     * 
     * @param bulkReceivingDocument
     * @return
     */
    protected boolean hasRequiredFieldsForContinue(BulkReceivingDocument bulkReceivingDocument){
        
        boolean valid = true;
        
        if (ObjectUtils.isNull(bulkReceivingDocument.getShipmentReceivedDate())) {
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.SHIPMENT_RECEIVED_DATE, KFSKeyConstants.ERROR_REQUIRED, PurapConstants.BulkReceivingDocumentStrings.VENDOR_DATE);
            valid = false;
        }

        return valid;
    }
    
    /**
     * Determines if it is valid to create a bulk receiving document.  
     * 
     * @param bulkReceivingDocument
     * @return
     */
    protected boolean canCreateBulkReceivingDocument(BulkReceivingDocument bulkReceivingDocument){
        
        boolean valid = true;
        
        if (bulkReceivingDocument.getPurchaseOrderIdentifier() != null){
            PurchaseOrderDocument po = SpringContext.getBean(PurchaseOrderService.class).getCurrentPurchaseOrder(bulkReceivingDocument.getPurchaseOrderIdentifier());
            
            if (ObjectUtils.isNull(po)){
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER, PurapKeyConstants.ERROR_BULK_RECEIVING_DOCUMENT_INVALID_PO, bulkReceivingDocument.getDocumentNumber(), bulkReceivingDocument.getPurchaseOrderIdentifier().toString());
                valid = false;
            }else{
                if (!(po.getApplicationDocumentStatus().equals(PurapConstants.PurchaseOrderStatuses.APPDOC_OPEN) || 
                    po.getApplicationDocumentStatus().equals(PurapConstants.PurchaseOrderStatuses.APPDOC_CLOSED))){
                    valid &= false;
                    GlobalVariables.getMessageMap().putError(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER, PurapKeyConstants.ERROR_BULK_RECEIVING_PO_NOT_OPEN, bulkReceivingDocument.getDocumentNumber(), bulkReceivingDocument.getPurchaseOrderIdentifier().toString());
                }else{
                    String docNumberInProcess = SpringContext.getBean(BulkReceivingService.class).getBulkReceivingDocumentNumberInProcessForPurchaseOrder(po.getPurapDocumentIdentifier(), bulkReceivingDocument.getDocumentNumber());
                    if (StringUtils.isNotEmpty(docNumberInProcess)){
                        valid &= false;
                        GlobalVariables.getMessageMap().putError(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER, PurapKeyConstants.ERROR_BULK_RECEIVING_DOCUMENT_ACTIVE_FOR_PO, docNumberInProcess, bulkReceivingDocument.getPurchaseOrderIdentifier().toString());
                    }
                }
            }
        }
         
        return valid;
    }

}
