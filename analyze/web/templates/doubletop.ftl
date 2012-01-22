<html>
<head>
  <title>${title}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <LINK href="css/style.css" type=text/css rel=stylesheet>  
</head>
<body>
  <h1>${title}</h1>
  <table>
  <tr><th>Position</th><th>Author</th><th>Value</th></tr>
  <#assign num = 1>
  <#list top as author>
    <tr><td>${num} </td><td><a href="nodes/${author.id?c}.html">${author.name}</a></td><td>${author.value}</td></tr>
    <#assign num =num +1>
  </#list>
  </table>
</body>
</html>  
