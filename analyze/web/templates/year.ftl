 
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <LINK href="../css/style.css" type=text/css rel=stylesheet>  
 <#if year??>
  <script src="../static/jquery.min.js" type="text/javascript"></script>
  <script src="../static/highcharts.js" type="text/javascript"></script>
 <script type="text/javascript">

		var chart,chart2,chart3,chart4,chart5;
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
             <#assign othercount=0>
             <#assign othernum=0>
             <#list community.children as child>
             <#if (child.properties["count"] > 5000)>
						['ID: ${child.name}', ${child.properties["count"]?c}],
             <#else>
                <#assign othercount=othercount + child.properties["count"]>
                <#assign othernum=othernum + 1>
             </#if>
					   </#list>
            ['Others with less than 5000 authors (${othernum?c})', ${othercount?c}]
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
           data: [ <#if year.properties.R1_count??>${year.properties.R1_count?c}<#else>0</#if>,<#if year.properties.R2_count??>${year.properties.R2_count?c}<#else>0</#if>,<#if year.properties.R3_count??>${year.properties.R3_count?c}<#else>0</#if>,<#if year.properties.R4_count??>${year.properties.R4_count?c}<#else>0</#if>,<#if year.properties.R5_count??>${year.properties.R5_count?c}<#else>0</#if>,<#if year.properties.R6_count??>${year.properties.R6_count?c}<#else>0</#if>,<#if year.properties.R7_count??>${year.properties.R7_count?c}<#else>0</#if>]
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
           data: [ <#if year.properties["HUBS_count"]??>${year.properties["HUBS_count"]?c}<#else>0</#if>,<#if year.properties["NON_HUBS_count"]??>${year.properties["NON_HUBS_count"]?c}<#else>0</#if>]
        }]
     });

     chart4 = new Highcharts.Chart({
        chart: {
           renderTo: 'conductance',
           defaultSeriesType: 'column'
        },
        title: {
           text: 'Distribution of on Communities Conductance '
        },
    
         xAxis: {
         categories: [
           <#list conductance?keys as key >
              <#if key!="0.00">
              "${key}",
              </#if>
           </#list>
            
         ]
      },
        yAxis: {
           min: 0,
           title: {
              text: 'Number of Communities'
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
           name: '',
           data: [
            <#list conductance?keys as key >
              <#if key!="0.00">
                  ${conductance[key]?c},
              <#else>
                  <#assign zeroconductance=conductance[key]>
              </#if>
            </#list>
            ]
        }]
     });

   chart5 = new Highcharts.Chart({
      chart: {
         renderTo: 'conductance-count', 
         defaultSeriesType: 'scatter',
         zoomType: 'xy'
      },
      title: {
         text: 'Conductance vs number of authors of toplevel communities'
      },
      xAxis: {
         min: 0,
         title: {
            enabled: true,
            text: 'Number of Authors'
         },
         startOnTick: true,
         endOnTick: true,
         showLastLabel: true
      },
      yAxis: {
         min: 0,
         title: {
            text: 'Conductance'
         }
      },
      legend: {
         enabled: false
      },
      plotOptions: {
         scatter: {
            marker: {
               radius: 2,
               states: {
                  hover: {
                     enabled: true,
                     lineColor: 'rgb(100,100,100)'
                  }
               }
            },
            states: {
               hover: {
                  marker: {
                     enabled: false
                  }
               }
            }
         }
      },
      series: [{
         name: '',
         data: [
                <#list community.children as child>
                [${child.properties.count?c}, ${child.properties.conductance?c}],
                </#list>
          ]

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
           <tr><td colspan=2>
      <div id="conductance" style="width: 800px; height: 400px"></div>
            <br>Number of Communities with 0 Conductance: ${zeroconductance}
           </td></tr>
            <tr><td colspan=2>
      <div id="conductance-count" style="width: 800px; height: 400px"></div>
           </td></tr>
      </table>
      <br>
      <table>
      
     
       <#assign num=0>
       <#list community.children as child>
          <#if num%25==0>
             <tr><th>Community-ID</th><th>count</th><th>edge_count</th><th>cutsize</th><th>conductance</th></tr>
          </#if>
          <tr><td><a href="${child.id?c}.html">${child.name}</a></td><td>${child.properties.count}</td><td>${child.properties.edge_count}</td><td>${child.properties.cutsize}</td><td>${child.properties.conductance}</td>
          </tr>
          <#assign num=num+1>
       </#list>
      </table>
  </#if>

</body>
</html>  
