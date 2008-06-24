<div class="fieldset">
<#assign labelPositionLeft = parameters.labelposition?default("") == 'left'/>
<div>
<#if labelPositionLeft><div class="label"></#if>
<#if parameters.label?exists>
<label <#if parameters.id?exists>for="${parameters.id?html}"</#if>>${parameters.label?html}</label>
</#if>
<#if labelPositionLeft></div></#if>
</div>