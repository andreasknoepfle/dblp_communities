<html>
<head>
  <title>${node.name}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <LINK href="../css/style.css" type=text/css rel=stylesheet>  
</head>
<body>
  <h1>${node.name}</h1>
  <h2>ID: ${node.id?c}</h2>
  
  <table>
  <tr><th>Property</th><th>Value</th></tr>
  <#list node.properties?keys as key>
    <tr><td>${key} </td><td> ${node.properties[key]}</td></tr>
  </#list>
  </table>
  
  <h2>Community-Structure</h2>
  <#assign level = 0>
  <table>
  <tr><th>Level</th><th>Community</th></tr>
  <#list node.communities as community>
    <tr>
    <td>${level}</td><td><b><#if level != 0><#list 1..level as i>-</#list>></#if></b> <a href="${community.id?c}.html">${community.name}</a></td>
    </tr>
    <#assign level = level + 1>
  </#list>
  </table>
  <#if author??>
      <h2>Neighbours</h2>
      <table>
      <tr><th>Name</th><th>Neo4j-ID</th><th># shared publications</th></tr>
      <#list author.neighbours as neighbour>
        <tr><td><a href="${neighbour.node.id?c}.html">${neighbour.node.name}</a></td><td>${neighbour.node.id?c}</td><td>${neighbour.count}</td></tr>
      </#list>
      </table>
  </#if>
   <#if community??>
      <h2>Children</h2>
      <table>
       <#list community.children as child>
          <tr><td><a href="${child.id?c}.html">${child.name}</a></td></tr>
       </#list>
      </table>
  </#if>
</body>
</html>  
