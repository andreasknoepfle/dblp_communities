<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
</head>
<body>
  <h1>${char}</h1>
  <#list index as author>
    <a href="../nodes/${author.id?c}.html">${author.name}</a><br>
  </#list>
</body>
</html>  
