<%--
  ~ Copyright J. Craig Venter Institute, 2013
  ~
  ~ The creation of this program was supported by J. Craig Venter Institute
  ~ and National Institute for Allergy and Infectious Diseases (NIAID),
  ~ Contract number HHSN272200900007C.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<!doctype html>
<%@ taglib uri="/struts-tags" prefix="s" %>
<head>
  <jsp:include page="header.jsp" />

</head>

<body class="smart-style-2">
<div id="container">
  <jsp:include page="top.jsp" />
  <!-- Modal -->
  <div class="modal fade" id="project-details" role="dialog">
    <div class="modal-dialog">

      <!-- Modal content-->
      <div class="modal-content" >
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal">&times;</button>
          <h4 class="modal-title"><i class="fa fa-angle-right"></i> <strong></strong></h4>
        </div>
        <div class="modal-body">
          <table id="project-detail-table" class="table table-bordered table-striped table-responsive table-hover table-condensed"></table>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        </div>
      </div>

    </div>
  </div>

  <section id="main-content">
    <section class="wrapper">
      <s:iterator value="projectMapList" var="projectMap" status="status">
        <s:if test="#status.index == 0 or #status.index % 4 == 0">
          <s:if test="#status.index != 0"></div></s:if><div class="row mt">
        </s:if>
          <div class="showback col-lg-3 col-md-3 col-sm-6">
            <h3><i class="fa fa-angle-right"></i> <strong>Project: </strong><s:property value="#projectMap.ProjectName" />
              <span data-toggle="modal" data-target="#project-details"><i class="fa fa-info-circle" title="Click for project details." data-toggle="tooltip" data-placement="right"
                 onclick="getProjectInfo(<s:property value="#projectMap.ProjectId" />);"></i></span>
            </h3>

            <div class="project-panel pn-lg">
              <div class="project-panel-header">
                <h5>SAMPLE STATUS</h5>
              </div>

              <div id="sample-donut-<s:property value="#status.index" />" class="graph"></div>
              <s:iterator value="#projectMap.sampleInfo" var="sample">
                <s:hidden class="sample-info-%{#status.index}" name="%{#sample[1]}" value="%{#sample[0]}" />
              </s:iterator>
            </div>
          </div>
        <s:if test="#status.last"></div></s:if>
      </s:iterator>
    </section>
    <! --/wrapper -->
  </section>

</div>
<jsp:include page="../html/footer.html" />
<script src="https://d3js.org/d3.v3.min.js"></script>
<script>
  $(document).ready(function() {
    $('.navbar-nav li').removeClass('active');
    $('.navbar-nav > li:first-child').addClass('active');

    $('[data-toggle="tooltip"]').tooltip({
      container: 'body'
    });

    $( ".graph" ).each(function(index) {
      var id = $(this).attr('id');

      var dataset = [], domainSource = [], colorSource = [];
      $('.sample-info-' + index).each(function () {
        var name = $(this).attr('name');
        var val = $(this).val()
        var color = (name == 'Completed') ? 'green' : (name == 'Pending') ? 'orange' : (name == 'Submitted') ? 'blue' : (name == 'Deprecated') ? 'red' : 'brown';

        name += ' - ' + val;
        dataset.push({name: name, value: val, percent: '' });
        domainSource.push(name);
        colorSource.push(color);
      });

      var total = d3.sum(dataset, function (d) {
        return d.value;
      });

      for(i = 0; i < dataset.length; i++) {
        dataset[i].percent = parseInt((dataset[i].value / total).toFixed(2) * 100);
      }

      var pie=d3.layout.pie()
          .value(function(d){return d.percent})
          .sort(null)
          .padAngle(.03);

      var w=250,h=250,outerRadius=w/2,innerRadius=100;

      var color = d3.scale.ordinal()
          .domain(domainSource)
          .range(colorSource);

      var arc=d3.svg.arc()
          .outerRadius(outerRadius)
          .innerRadius(innerRadius);

      var svg=d3.select('#'+id)
          .append("svg")
          .attr({
            width:w,
            height:h,
            class:'shadow'
          }).append('g')
          .attr({
            transform:'translate('+w/2+','+h/2+')'
          });
      var path=svg.selectAll('path')
          .data(pie(dataset))
          .enter()
          .append('path')
          .attr({
            d:arc,
            fill:function(d,i){
              return color(d.data.name);
            }
          });

      path.transition()
          .duration(1000)
          .attrTween('d', function(d) {
            var interpolate = d3.interpolate({startAngle: 0, endAngle: 0}, d);
            return function(t) {
              return arc(interpolate(t));
            };
          });


      var restOfTheData=function(){
        var text=svg.selectAll('text')
            .data(pie(dataset))
            .enter()
            .append("text")
            .transition()
            .duration(200)
            .attr("transform", function (d) {
              return "translate(" + arc.centroid(d) + ")";
            })
            .attr("dy", ".4em")
            .attr("text-anchor", "middle")
            .text(function(d){
              return d.data.percent+"%";
            })
            .style({
              fill:'#fff',
              'font-size':'10px'
            });

        var legendRectSize=20;
        var legendSpacing=7;
        var legendHeight=legendRectSize+legendSpacing;

        var legend=svg.selectAll('.legend')
            .data(color.domain())
            .enter()
            .append('g')
            .attr({
              class:'legend',
              transform:function(d,i){
                //Just a calculation for x & y position
                return 'translate(-60,' + ((i*legendHeight)-65) + ')';
              }
            });
        legend.append('rect')
            .attr({
              width:legendRectSize,
              height:legendRectSize,
              rx:20,
              ry:20
            })
            .style({fill:color,stroke:color});

        legend.append('text')
            .attr({
              x:30,
              y:15
            })
            .text(function(d){
              return d;
            }).style({fill:'#929DAF', 'font-size':'14px'});
      };

      setTimeout(restOfTheData,1000);
    });
  });

  function getProjectInfo(projectId) {
    utils.getProjectDetailsIntoModal(projectId, "#project-detail-table");
  }

</script>
</body>
</html>