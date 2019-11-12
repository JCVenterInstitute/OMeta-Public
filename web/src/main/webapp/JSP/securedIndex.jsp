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
  <jsp:include page="header.jsp"/>
  <style>
    polyline {
      opacity: .3;
      stroke: black;
      stroke-width: 2px;
      fill: none;
    }
  </style>

</head>

<body class="smart-style-2">
<div id="container">
  <jsp:include page="top.jsp"/>
  <!-- Modal -->
  <div class="modal fade" id="project-details" role="dialog">
    <div class="modal-dialog">

      <!-- Modal content-->
      <div class="modal-content">
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
      <s:if test="#status.index == 0 or #status.index % 2 == 0">
  <s:if test="#status.index != 0"></div>
</s:if>
<div class="row mt">
  </s:if>
  <div class="showback col-lg-6 col-md-6">
    <h3><i class="fa fa-angle-right"></i> <strong>Project: </strong><s:property value="#projectMap.ProjectName"/>
      <span data-toggle="modal" data-target="#project-details"><i class="fa fa-info-circle" title="Click for project details." data-toggle="tooltip" data-placement="right"
                                                                  onclick="getProjectInfo(<s:property value="#projectMap.ProjectId"/>);"></i></span>
    </h3>

    <div class="project-panel pn-lg">
      <div class="project-panel-header">
        <h5>SPECIMEN STATUS</h5>
      </div>

      <div id="sample-donut-<s:property value="#status.index" />" class="graph"></div>
      <s:iterator value="#projectMap.sampleInfo" var="sample">
        <s:hidden class="sample-info-%{#status.index}" name="%{#sample[1]}" value="%{#sample[0]}"/>
      </s:iterator>
    </div>
  </div>
  <s:if test="#status.last"></div>
</s:if>
</s:iterator>
</section>
<! --/wrapper -->
</section>

</div>
<jsp:include page="../html/footer.html"/>
<script src="https://d3js.org/d3.v3.min.js"></script>
<script>
  $(document).ready(function () {
    $('.navbar-nav li').removeClass('active');
    $('.navbar-nav > li:first-child').addClass('active');

    $('[data-toggle="tooltip"]').tooltip({
      container: 'body'
    });

    $(".graph").each(function (index) {
      var id = $(this).attr('id');

      var dataset = [], domainSource = [], colorSource = [];
      $('.sample-info-' + index).each(function () {
        var name = $(this).attr('name');
        var val = $(this).val()
        var color = (name == 'Hospital - collection') ? '#98abc5' : (name == 'Hospital - shipped to ISR') ? '#8a89a6' : (name == 'ISR - received') ? '#7b6888'
            : (name == 'ISR - extraction') ? '#6b486b' : (name == 'ISR - shipped to JCVI') ? '#a05d56' : (name == 'JCVI - received') ? '#d0743c' : (name == 'JCVI - library') ? '#ff8c00'
                : (name == 'JCVI - sequencing') ? '#ff9842' : (name == 'JCVI - analysis') ? '#ff571c' : '#ff8c00';

        name += ' (' + val + ')';
        dataset.push({label: name, value: val, percent: ''});
        domainSource.push(name);
        colorSource.push(color);
      });

      var total = d3.sum(dataset, function (d) {
        return d.value;
      });

      for (i = 0; i < dataset.length; i++) {
        dataset[i].percent = parseInt((dataset[i].value / total).toFixed(2) * 100);
      }

      var width = 700,
          height = 250,
          radius = Math.min(width, height) / 2;

      var svg = d3.select('#' + id)
          .append("svg")
          .attr({
            width:width,
            height:height,
            class:'shadow'
          })
          .append("g");

      svg.append("g")
          .attr("class", "slices");
      svg.append("g")
          .attr("class", "labels");
      svg.append("g")
          .attr("class", "lines");

      var pie = d3.layout.pie()
          .value(function (d) {
            return d.percent
          })
          .sort(null);

      var arc = d3.svg.arc()
          .outerRadius(radius * 0.8)
          .innerRadius(radius * 0.4);

      var outerArc = d3.svg.arc()
          .innerRadius(radius * 0.9)
          .outerRadius(radius * 0.9);

      svg.attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

      var key = function (d) {
        return d.data.label;
      };

      var color = d3.scale.ordinal()
          .domain(domainSource)
          .range(colorSource);

      var slice = svg.select(".slices").selectAll("path.slice")
          .data(pie(dataset), key);

      slice.enter()
          .insert("path")
          .style("fill", function (d) {
            return color(d.data.label);
          })
          .attr("class", "slice");

      slice.transition()
          .duration(1000)
          .attrTween('d', function (d) {
            this._current = this._current || d;
            var interpolate = d3.interpolate(this._current, d);
            this._current = interpolate(0);
            return function (t) {
              return arc(interpolate(t));
            };
          });

      slice.exit().remove();


      // Append percent info
      svg.selectAll('text')
          .data(pie(dataset), key)
          .enter()
          .append("text")
          .transition()
          .duration(1000)
          .attr("transform", function (d) {
            return "translate(" + arc.centroid(d) + ")";
          })
          .attr("dy", ".4em")
          .attr("text-anchor", "middle")
          .text(function (d) {
            return d.data.percent + "%";
          })
          .style({
            fill: '#fff',
            'font-size': '10px'
          });

      //Append text for each slice
      var text = svg.select(".labels").selectAll("text")
          .data(pie(dataset), key);

      text.enter()
          .append("text")
          .attr("dy", ".35em")
          .text(function (d) {
            return d.data.label;
          });

      function midAngle(d) {
        return d.startAngle + (d.endAngle - d.startAngle) / 2;
      }

      text.transition().duration(1000)
          .attrTween("transform", function (d) {
            this._current = this._current || d;
            var interpolate = d3.interpolate(this._current, d);
            this._current = interpolate(0);
            return function (t) {
              var d2 = interpolate(t);
              var pos = outerArc.centroid(d2);
              pos[0] = radius * (midAngle(d2) < Math.PI ? 1 : -1);
              return "translate(" + pos + ")";
            };
          })
          .styleTween("text-anchor", function (d) {
            this._current = this._current || d;
            var interpolate = d3.interpolate(this._current, d);
            this._current = interpolate(0);
            return function (t) {
              var d2 = interpolate(t);
              return midAngle(d2) < Math.PI ? "start" : "end";
            };
          });

      text.exit().remove();

      var polyline = svg.select(".lines").selectAll("polyline")
          .data(pie(dataset), key);

      polyline.enter()
          .append("polyline");

      polyline.transition()
          .duration(1000)
          .attrTween("points", function (d) {
            this._current = this._current || d;
            var interpolate = d3.interpolate(this._current, d);
            this._current = interpolate(0);
            return function (t) {
              var d2 = interpolate(t);
              var pos = outerArc.centroid(d2);
              pos[0] = radius * 0.95 * (midAngle(d2) < Math.PI ? 1 : -1);
              return [arc.centroid(d2), outerArc.centroid(d2), pos];
            };
          });

      polyline.exit().remove();
    });
  });

  function getProjectInfo(projectId) {
    utils.getProjectDetailsIntoModal(projectId, "#project-detail-table");
  }

</script>
</body>
</html>