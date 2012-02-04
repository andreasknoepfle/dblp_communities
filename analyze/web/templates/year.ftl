<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <LINK href="../css/style.css" type=text/css rel=stylesheet>  
 <#if year??>
  <script src="../static/jquery.min.js" type="text/javascript"></script>
  <script src="../static/highcharts.js" type="text/javascript"></script>
 <script type="text/javascript">

		var chart,chart2,chart3;
		jQuery(document).ready(function() {
			chart = new Highcharts.Chart({
				chart: {
					renderTo: 'distribution',
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

      chart2 = new Highcharts.Chart({
        chart: {
           renderTo: 'roles',
           defaultSeriesType: 'column'
        },
        title: {
           text: 'Distribution of Roles (exact)'
        },
        subtitle: {
           text: 'R1 = ultraperipheral, R2 = peripheral, R3 = connectors, R4 = kinless vertices,R5 = provincial hubs, R6 = connector hubs, R7 = kinless hubs'
        },
         xAxis: {
         categories: [
            'R1', 
            'R2',
            'R3',
            'R4', 
            'R5',
            'R6',
            'R7',
         ]
      },
        yAxis: {
           min: 0,
           title: {
              text: 'Number of Authors'
           }
        },
        legend: {
         enabled: false
        },
        plotOptions: {
           column: {
              pointPadding: 0.2,
              borderWidth: 0,
              colorByPoint: true
           }
        },
        series: [{
           name: 'Number of Authors',
           data: [ <#if year.properties.R1_count??>${year.properties.R1_count}<#else>0</#if>,<#if year.properties.R2_count??>${year.properties.R2_count}<#else>0</#if>,<#if year.properties.R3_count??>${year.properties.R3_count}<#else>0</#if>,<#if year.properties.R4_count??>${year.properties.R4_count}<#else>0</#if>,<#if year.properties.R5_count??>${year.properties.R5_count}<#else>0</#if>,<#if year.properties.R6_count??>${year.properties.R6_count}<#else>0</#if>,<#if year.properties.R7_count??>${year.properties.R7_count}<#else>0</#if>]
        }]
     });


     chart3 = new Highcharts.Chart({
        chart: {
           renderTo: 'roles-hc',
           defaultSeriesType: 'column'
        },
        title: {
           text: 'Distribution of Roles (Hub/Connectors)'
        },
    
         xAxis: {
         categories: [
            'Hubs', 
            'Non-Hubs',
         ]
      },
        yAxis: {
           min: 0,
           title: {
              text: 'Number of Authors'
           }
        },
        legend: {
         enabled: false
        },
        plotOptions: {
           column: {
              pointPadding: 0.2,
              borderWidth: 0,
              colorByPoint: true
           }
        },
        series: [{
           name: 'Number of Authors',
           data: [ <#if year.properties.HUBS_count??>${year.properties.HUBS_count}<#else>0</#if>,<#if year.properties.NON_HUBS_count??>${year.properties.NON_HUBS_count}<#else>0</#if>]
        }]
     });


		});


    
	</script>
</#if>
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
           <tr><td># top-level-communities</td><td>${community.children?size}</td></tr>
           <tr><td># authors in communities</td><td>${year.properties["count"]}</td></tr>
           <tr><td colspan=2>
      <div id="distribution" style="width: 800px; height: 400px"></div>
           </td></tr>
           <tr><td colspan=2>
      <div id="roles-hc" style="width: 400px; height: 400px"></div>
           </td></tr>
          <tr><td colspan=2>
      <div id="roles" style="width: 800px; height: 400px"></div>
           </td></tr>
      </table>
      <br>
      <table>
      
      <tr><th>Community-ID</th><th>Properties</th></tr>
       <#list community.children as child>
          <tr><td><a href="${child.id?c}.html">${child.name}</a></td><td>
               <#list child.properties?keys as key>
                 <#if key!="top_z" && key!="top_p">
                     <b>${key}: </b>${child.properties[key]}<br>
                 </#if>
               </#list>
          </td>
          </tr>
       </#list>
      </table>
  </#if>

</body>
</html>  
