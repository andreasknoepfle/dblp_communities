<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <LINK href="../css/style.css" type=text/css rel=stylesheet>  
  <script src="../static/jquery.min.js" type="text/javascript"></script>
  <script src="../static/highcharts.js" type="text/javascript"></script>
 <script type="text/javascript">

		var chart;
		jQuery(document).ready(function() {
			chart = new Highcharts.Chart({
				chart: {
					renderTo: 'container',
					plotBackgroundColor: null,
					plotBorderWidth: null,
					plotShadow: false
				},
				title: {
					text: 'Distribution of Authors on Communities'
				},
				tooltip: {
					formatter: function() {
						return '<b>'+ this.point.name +'</b>: '+ this.percentage +' %';
					}
				},
				plotOptions: {
					pie: {
						allowPointSelect: true,
						cursor: 'pointer',
						dataLabels: {
							enabled: true,
							color:  '#000000',
							connectorColor:  '#000000',
							formatter: function() {
								return '<b>'+ this.point.name +'</b>: '+ this.point.y ;
							}
						}
					}
				},
			    series: [{
					type: 'pie',
					name: 'Distribution',
					data: [
             <#assign othercount=0 othernum=0>
             <#list community.children as child>
             <#if (child.properties["count"] > 100)>
						['ID: ${child.name}', ${child.properties["count"]?c}],
             <#else>
                <#assign
                      othercount=othercount + child.properties["count"]
                      othernum=othernum + 1
                >
             </#if>
					   </#list>
            ['Others with less than 100 authors (${othernum})', ${othercount}]
					]
				}]
			});
		});
	</script>
</head>
<body>
   <#if root??>
      <h2>Years:</h2>
      <table>
      <tr><th>ID</th><th>From</th><th>To</th><th>Number of Authors</th></tr>
       <#list community.children as child>
          <tr><td><a href="${child.id?c}.html">${child.name}</a></td><td>${child.properties["fromYear"]?c}</td><td>${child.properties["toYear"]?c}</td><td>${child.properties["count"]?c}</td>
          </tr>
       </#list>
      </table>
  </#if>
  <#if year??>
      <h2>Communities from ${year.properties["fromYear"]?c} to ${year.properties["toYear"]?c}</h2>
      <table>
           <tr><td>Property</td></td><td>Value</td></tr>
           <tr><td># top-level-communities</td><td>${community.children?size}</td></tr>
           <tr><td># authors in communities</td><td>${year.properties["count"]}</td></tr>
           <tr><td colspan=2>
      <div id="container" style="width: 600px; height: 400px"></div>
           </td></tr><table>
      <br>
      <tr><th>Community-ID</th><th>Properties</th></tr>
       <#list community.children as child>
          <tr><td><a href="${child.id?c}.html">${child.name}</a></td><td>
               <#list child.properties?keys as key>
                  <b>${key}:</b> ${child.properties[key]}<br>
               </#list>
          </td>
          </tr>
       </#list>
      </table>
  </#if>

</body>
</html>  
