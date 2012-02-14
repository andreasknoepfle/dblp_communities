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
    <#if key="top_z" || key="top_p">
    <tr><td>${key}</td><td> <#assign num=1><#list node.properties[key] as item>${num}) ${item}<br><#assign num=num+1></#list></td></tr>
    <#else>
    <tr><td>${key} </td><td> ${node.properties[key]}</td></tr>
    </#if>
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
    <#if node.properties.top_level_community??>
      <h2>Neighbours</h2>
      <#assign in_community=0>
      <#assign not_in_community=0>
      <table>
      <tr><th>Name</th><th>Neo4j-ID</th><th># shared publications</th><th>Top-Level-Community</th></tr>
      <#list author.neighbours as neighbour>
        <tr bgcolor=<#if neighbour.node.properties.top_level_community=node.properties.top_level_community><#assign in_community=in_community+1>"#d4f3c8"<#else><#assign not_in_community=not_in_community+1>"#f3b3b9"</#if>><td><a href="${neighbour.node.id?c}.html">${neighbour.node.name}</a></td><td>${neighbour.node.id?c}</td><td>${neighbour.count}</td><td>${neighbour.node.properties.top_level_community?c}</td></tr>
      </#list>
      <tr><td># Neighbours in same top-level Community  (<font color="green">green</font>) </td><td colspan=3>${in_community} </td></tr>
      <tr><td># Neighbours in diffrent top-level Community (<font color="red">red</font>) </td><td colspan=3>${not_in_community} </td></tr>
      </table>
      </#if>
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
