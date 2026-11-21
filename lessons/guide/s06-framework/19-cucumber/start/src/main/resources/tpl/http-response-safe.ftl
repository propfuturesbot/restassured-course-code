<#ftl output_format="HTML">
<#-- Same as Allure's own http-response.ftl, with the secret headers masked and the tokens in a body replaced by stars. -->
<#function secret name>
    <#local n = name?lower_case>
    <#return ["x-api-key", "authorization", "cookie", "set-cookie"]?seq_contains(n) || n?contains("secret") || n?contains("password")>
</#function>
<#function show name value>
    <#local v = value!"">
    <#if secret(name)>
        <#return (v?length > 4)?then(v?substring(0, 4), "") + "****">
    </#if>
    <#return value!"null">
</#function>
<div>Status code <#if data.responseCode??>${data.responseCode} <#else>Unknown</#if></div>
<#if data.url??><div>${data.url}</div></#if>

<#if data.body??>
<h4>Body</h4>
<div>
    <pre class="preformated-text">
    <#t>${data.body?replace(r'"(access_token|accessToken|refresh_token|client_secret|password)"\s*:\s*"[^"]*"', r'"$1":"****"', "r")}
    </pre>
</div>
</#if>

<#if (data.headers)?has_content>
<h4>Headers</h4>
<div>
    <#list data.headers as name, value>
        <div>${name}: ${show(name, value)}</div>
    </#list>
</div>
</#if>

<#if (data.cookies)?has_content>
<h4>Cookies</h4>
<div>
    <#list data.cookies as name, value>
        <div>${name}: ${show(name, value)}</div>
    </#list>
</div>
</#if>
