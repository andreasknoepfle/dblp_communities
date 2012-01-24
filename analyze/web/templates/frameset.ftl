<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
</head>
<body>
<#list chars as char>
    <a href="index/${char}.html" target="content">${char}</a>&nbsp;
</#list><br><br>
 <#list links?keys as key>
    <a href="${key}.html" target="content">${links[key]}</a>&nbsp;
  </#list>
<br><br>

<iframe id="content" name="content" src="nodes/${node.id}.html" width="100%" height="90%" marginheight="0" marginwidth="0" frameborder="0" />
</body>
</html>
