<%@ taglib prefix="c" uri="/tlds/c.tld" %>
<%@ taglib prefix="html" uri="/tlds/struts-html.tld" %>
<%@ attribute name="columnCount" required="true"
    description="The total number of columns of this row." %>
<%@ attribute name="isSource" required="true"
    description="Boolean whether this group is of source or target lines." %>
<%@ attribute name="editingMode" required="true" type="java.util.Map"%>
<%@ attribute name="sectionTitle" required="true"
    description="The document specific name of this group of accounting lines, e.g., 'To', 'Disencumbrance', etc." %>

<c:set var="sourceOrTarget" value="${isSource ? 'source' : 'target'}"/>
<c:set var="capitalSourceOrTarget" value="${isSource ? 'Source' : 'Target'}"/>
<c:set var="showLink" value="${sourceOrTarget}ShowLink"/>
<c:set var="uploadDiv" value="upload${capitalSourceOrTarget}Div"/>
<c:set var="hideImport" value="hide${capitalSourceOrTarget}Import"/>
<c:set var="showImport" value="show${capitalSourceOrTarget}Import"/>
<c:set var="file" value="${sourceOrTarget}File"/>
<c:set var="uploadLines" value="upload${capitalSourceOrTarget}Lines"/>
<c:set var="canUpload" value="${!empty editingMode['fullEntry']}"/>
<c:set var="formatUrl" value="${KualiForm.accountingLineImportInstructionsUrl}"/>

<c:set var="titleColSpan" value="4" />
<c:if test="${empty editingMode['fullEntry']}" >
    <c:set var="titleColSpan" value="${titleColSpan + rightColumnCount}" />
</c:if>

<tr>
    <td colspan="${canUpload ? 4 : columnCount}" class="tab-subhead" style="border-right: none;">${sectionTitle}</td>

    <c:if test="${canUpload}">
        <td colspan="${columnCount - 4}" class="tab-subhead-import" align="right" nowrap="nowrap" style="border-left: none;">
            <SCRIPT type="text/javascript">
                <!--
                  function ${hideImport}() {
                      document.getElementById("${showLink}").style.display="inline";
                      document.getElementById("${uploadDiv}").style.display="none";
                  }
                  function ${showImport}() {
                      document.getElementById("${showLink}").style.display="none";
                      document.getElementById("${uploadDiv}").style.display="inline";
                  }
                  document.write(
                    '<a id="${showLink}" href="#" onclick="${showImport}();return false;">' +
                      '<img src="images/tinybutton-importlines.gif" alt="import file"' +
                      '     width=72 height=15 border=0 align="middle" class="det-button">' +
                    '<\/a>' +
                    '<div id="${uploadDiv}" style="display:none;" >' +
                      '<a href="html/help/AccountingLineImport.html" target="helpWindow"><img src="images/my_cp_inf.gif" alt="${altText}" hspace=5 border=0  align="middle"><\/a>&nbsp;' +
                      '<html:file size="30" property="${file}" />' +
                      '<html:image property="methodToCall.${uploadLines}" src="images/tinybutton-add1.gif"
                                    styleClass="tinybutton" alt="insert ${sourceOrTarget} accounting lines" />' +
                      '<html:image property="methodToCall.cancel" src="images/tinybutton-cancelimport.gif"
                                    styleClass="tinybutton" alt="cancel insert" onclick="${hideImport}();return false;" />' +
                    '<\/div>');
                //-->
            </SCRIPT>
            <NOSCRIPT>
                Import lines <a href="html/help/AccountingLineImport.html" target="helpWindow"><img src="images/my_cp_inf.gif" alt="${altText}" hspace=5 border=0  align="middle"></a>&nbsp;
                <html:file size="30" property="${file}" style="font:10px;height:16px;"/>
                <html:image property="methodToCall.${uploadLines}" src="images/tinybutton-add1.gif"
                            alt="insert ${sourceOrTarget} accounting lines"/>
            </NOSCRIPT>
        </td>
    </c:if>
</tr>