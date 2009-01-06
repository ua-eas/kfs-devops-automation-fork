<%--
 Copyright 2006-2007 The Kuali Foundation.
 
 Licensed under the Educational Community License, Version 1.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl1.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/jsp/kfs/kfsTldHeader.jsp"%>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>
<%@ attribute name="deliveryReadOnly" required="false"
              description="Boolean to indicate if delivery tab fields are read only" %>              

<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="notOtherDeliveryBuilding" value="${not KualiForm.document.deliveryBuildingOtherIndicator}" />
<c:set var="amendmentEntry" value="${(not empty KualiForm.editingMode['amendmentEntry'])}" />
<c:set var="lockB2BEntry" value="${(not empty KualiForm.editingMode['lockB2BEntry'])}" />
<c:if test="${empty deliveryReadOnly}">
	<c:set var="deliveryReadOnly" value="false" />
</c:if>
<c:set var="displayReceivingAddress" value="${(not empty KualiForm.editingMode['displayReceivingAddress'])}" />
<c:set var="lockAddressToVendor" value="${(not empty KualiForm.editingMode['lockAddressToVendor'])}" />

<kul:tab tabTitle="Delivery" defaultOpen="true" tabErrorKey="${PurapConstants.DELIVERY_TAB_ERRORS}">
    <div class="tab-container" align=center>
    
    	<!---- Final Delivery ---->
            <h3>Final Delivery</h3>

        <table cellpadding="0" cellspacing="0" class="datatable" summary="Final Delivery Section">
            <tr>
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryCampusCode}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute 
                        attributeEntry="${documentAttributes.deliveryCampusCode}" 
                        property="document.deliveryCampusCode" 
                        readOnly="true"/>&nbsp;                
                    <c:if test="${(fullEntryMode or amendmentEntry) && !deliveryReadOnly}">
                        <kul:lookup boClassName="org.kuali.kfs.vnd.businessobject.CampusParameter"
                            lookupParameters="document.deliveryCampusCode:campusCode"
                            fieldConversions="campusCode:document.deliveryCampusCode"/>
                    </c:if>
                </td>               
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryToName}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryToName}" 
                    	property="document.deliveryToName" readOnly="${not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
                    <c:if test="${fullEntryMode && !deliveryReadOnly}">
                        <kul:lookup boClassName="org.kuali.rice.kim.bo.Person" 
                        	fieldConversions="name:document.deliveryToName,personEmailAddress:document.deliveryToEmailAddress,personLocalPhoneNumber:document.deliveryToPhoneNumber"/>
                    </c:if>
                </td>
            </tr>
            <tr>
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryBuildingName}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute 
                        attributeEntry="${documentAttributes.deliveryBuildingName}" 
                        property="document.deliveryBuildingName"
                        readOnly="true"/>&nbsp;
                    <c:if test="${(fullEntryMode or amendmentEntry) && !deliveryReadOnly}">
                        <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.Building"
                            lookupParameters="document.deliveryCampusCode:campusCode"
                            fieldConversions="buildingCode:document.deliveryBuildingCode,buildingName:document.deliveryBuildingName,campusCode:document.deliveryCampusCode,buildingStreetAddress:document.deliveryBuildingLine1Address,buildingAddressCityName:document.deliveryCityName,buildingAddressStateCode:document.deliveryStateCode,buildingAddressZipCode:document.deliveryPostalCode,buildingAddressCountryCode:document.deliveryCountryCode"/>&nbsp;&nbsp;
                        <html:image property="methodToCall.useOtherDeliveryBuilding" src="${ConfigProperties.externalizable.images.url}tinybutton-buildingnotfound.gif" alt="building not found" styleClass="tinybutton"/>
                    </c:if>
                </td>           
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryToPhoneNumber}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryToPhoneNumber}" 
                    	property="document.deliveryToPhoneNumber" readOnly="${not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
                </td>
            </tr>
			<tr>
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryBuildingLine1Address}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryBuildingLine1Address}" 
                        property="document.deliveryBuildingLine1Address"  readOnly="${notOtherDeliveryBuilding or not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
                </td>           
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryToEmailAddress}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryToEmailAddress}" 
                    	property="document.deliveryToEmailAddress" readOnly="${not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
                </td>
			</tr>
			<tr>
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryBuildingLine2Address}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryBuildingLine2Address}" 
                        property="document.deliveryBuildingLine2Address" readOnly="${not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
                </td>
                <c:if test="${!lockB2BEntry}">
	                <th align=right valign=middle class="bord-l-b">
	                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryRequiredDate}"/></div>
	                </th>
	                <td align=left valign=middle class="datacell">
	                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryRequiredDate}" datePicker="true" 
	                    	property="document.deliveryRequiredDate" readOnly="${not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
	                </td>
	            </c:if>
                <c:if test="${lockB2BEntry}">
                    <th align=right valign=middle class="bord-l-b" rowspan="7">&nbsp;</th>
                    <td align=left valign=middle class="datacell" rowspan="7">&nbsp;</td>
                </c:if>
			</tr>
			<tr>
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryBuildingRoomNumber}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryBuildingRoomNumber}" 
                        property="document.deliveryBuildingRoomNumber" readOnly="${not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
			        <c:if test="${(fullEntryMode or amendmentEntry) && !deliveryReadOnly && notOtherDeliveryBuilding && (not empty KualiForm.document.deliveryBuildingCode)}">
			            <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.Room" 
			                readOnlyFields="buildingCode,campusCode"
			                lookupParameters="'Y':active,document.deliveryBuildingCode:buildingCode,document.deliveryCampusCode:campusCode"
			                fieldConversions="buildingRoomNumber:document.deliveryBuildingRoomNumber"/>
			        </c:if>
                </td>           
                <c:if test="${!lockB2BEntry}">
	                <th align=right valign=middle class="bord-l-b">
	                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryRequiredDateReasonCode}"/></div>
	                </th>
	                <td align=left valign=middle class="datacell">
	                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryRequiredDateReasonCode}" 
	                        property="document.deliveryRequiredDateReasonCode"
	                        extraReadOnlyProperty="document.deliveryRequiredDateReason.deliveryRequiredDateReasonDescription" 
	                        readOnly="${not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
	                </td>
                </c:if>
			</tr>
			<tr>
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryCityName}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryCityName}" 
                        property="document.deliveryCityName" readOnly="${notOtherDeliveryBuilding or not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
                </td>
                <c:if test="${!lockB2BEntry}">
                    <th align=right valign=middle class="bord-l-b" rowspan="4">
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryInstructionText}"/></div>
                    </th>
                    <td align=left valign=middle class="datacell"  rowspan="4">
                        <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryInstructionText}" 
                            property="document.deliveryInstructionText" readOnly="${not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
                    </td>
                </c:if>
			</tr>
            <tr>			
				<th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryStateCode}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryStateCode}" 
                    	property="document.deliveryStateCode" readOnly="${notOtherDeliveryBuilding or not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
                </td>
			</tr>
			<tr>
				<th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryPostalCode}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryPostalCode}" 
                    	property="document.deliveryPostalCode" readOnly="${notOtherDeliveryBuilding or not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
                </td>
			</tr>
			<tr>
				<th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliveryCountryCode}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.deliveryCountryCode}" 
                    	property="document.deliveryCountryCode" 
                    	extraReadOnlyProperty="document.deliveryCountryName"
                    	readOnly="${notOtherDeliveryBuilding or not (fullEntryMode or amendmentEntry) or deliveryReadOnly}"/>
                </td>
			</tr>
        </table>

	   	<c:if test="${displayReceivingAddress}">    
	   	
    	<!---- Receiving Address ---->
            <h3>Receiving Address</h3>

		<table cellpadding="0" cellspacing="0" class="datatable" summary="Receiving Address Section">	 
			<tr>
                <th align=right valign=middle  class="bord-l-b">
                   <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.receivingName}" /></div>
                </th>
                <td align=left valign=middle class="datacell"> 
                	<kul:htmlControlAttribute attributeEntry="${documentAttributes.receivingName}" property="document.receivingName" readOnly="true" /><br>
                   	<kul:htmlControlAttribute attributeEntry="${documentAttributes.receivingLine1Address}" property="document.receivingLine1Address" readOnly="true" /><br>
                   	<c:if test="${! empty KualiForm.document.receivingLine2Address}">                   	
                   		<kul:htmlControlAttribute attributeEntry="${documentAttributes.receivingLine2Address}" property="document.receivingLine2Address" readOnly="true" /><br>
                   	</c:if>
                   	<c:if test="${! empty KualiForm.document.receivingCityName}">                   	
    	           		<kul:htmlControlAttribute attributeEntry="${documentAttributes.receivingCityName}" property="document.receivingCityName" readOnly="true" />,&nbsp;
                   	</c:if>
                   	<c:if test="${! empty KualiForm.document.receivingStateCode}">                   	
	                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.receivingStateCode}" property="document.receivingStateCode" readOnly="true" />&nbsp;
                  	</c:if>
                   	<c:if test="${! empty KualiForm.document.receivingPostalCode}">                   	
	                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.receivingPostalCode}" property="document.receivingPostalCode" readOnly="true" />
                   	</c:if>
                   	<c:if test="${! empty KualiForm.document.receivingCountryCode}">                   	
	            		<br><kul:htmlControlAttribute attributeEntry="${documentAttributes.receivingCountryCode}" 
	            			property="document.receivingCountryCode" 
	            			extraReadOnlyProperty="document.receivingCountryName"
	            			readOnly="true" />
                   	</c:if>
            	</td>
                <td align=left valign=middle class="datacell">
                    <c:if test="${fullEntryMode}">
                    	<kul:lookup boClassName="org.kuali.kfs.module.purap.businessobject.ReceivingAddress"
                    		lookupParameters="'Y':active,document.chartOfAccountsCode:chartOfAccountsCode,document.organizationCode:organizationCode"
                    		fieldConversions="receivingName:document.receivingName,receivingCityName:document.receivingCityName,receivingLine1Address:document.receivingLine1Address,receivingLine2Address:document.receivingLine2Address,receivingCityName:document.receivingCityName,receivingStateCode:document.receivingStateCode,receivingPostalCode:document.receivingPostalCode,receivingCountryCode:document.receivingCountryCode,useReceivingIndicator:document.addressToVendorIndicator"/>
                    </c:if>            		
            	</td>
            </tr>
        </table>
        
    	<!---- Address To Vendor ---->
            <h3>Address To Vendor</h3>
        
		<table cellpadding="0" cellspacing="0" class="datatable" summary="Address To Vendor Section">
			<tr>
				<th align=right valign=middle class="bord-l-b">
					<div align="right">
						<kul:htmlAttributeLabel attributeEntry="${documentAttributes.addressToVendorIndicator}" />
					</div>
				</th>
				<td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.addressToVendorIndicator}" property="document.addressToVendorIndicator" readOnly="${not(fullEntryMode or amendmentEntry) or lockAddressToVendor}" /><br>				
					<!--
					<c:choose>
						<c:when test="${KualiForm.document.addressToVendorIndicator == 'true'}">
							&nbsp;<input type=radio title="${documentAttributes.addressToVendorIndicator.label} - Receiving Address" name="document.addressToVendorIndicator" value="true" checked />&nbsp;Receiving Address&nbsp;
							&nbsp;<input type=radio title="${documentAttributes.addressToVendorIndicator.label} - Final Delivery Address" name="document.addressToVendorIndicator" value="false" />&nbsp;Final Delivery Address&nbsp;
						</c:when>
						<c:otherwise>
							&nbsp;<input type=radio title="${documentAttributes.addressToVendorIndicator.label} - Receiving Address" name="document.addressToVendorIndicator" value="false" />&nbsp;Receiving Address&nbsp;
							&nbsp;<input type=radio title="${documentAttributes.addressToVendorIndicator.label} - Final Delivery Address" name="document.addressToVendorIndicator" value="true" checked />&nbsp;Final Delivery Address&nbsp;
						</c:otherwise>
					</c:choose>
					-->
            	</td>
            </tr>
        </table>
					
		</c:if>            		

	</div>
</kul:tab>

