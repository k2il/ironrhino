<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
response.setHeader("Cache-Control", "max-age=86400");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>region</title>
<link href="<c:url value="/components/tree/styles/xtree.css"/>"
	media="screen" rel="stylesheet" type="text/css" />
<script type="text/javascript"
	src="<c:url value="/components/tree/scripts/xtree.js"/>"></script>
<script type="text/javascript"
	src="<c:url value="/components/tree/scripts/jsonloadtree.js"/>"></script>
<c:if test="${param['type']=='satellite'}">
	<script src="http://maps.google.com/maps?file=api&amp;v=2"
		type="text/javascript"></script>
</c:if>
<c:if test="${param['type']!='satellite'}">
	<script src="http://ditu.google.com/maps?file=api&amp;v=2"
		type="text/javascript"></script>
</c:if>
<script type="text/javascript">
var lat = ${not empty param['lat']?param['lat']:22.5162};
var lng = ${not empty param['lng']?param['lng']:114.050128};
var zoom = ${not empty param['zoom']?param['zoom']:8};

var map;
var mgr;
$(window).unload(GUnload);

Initialization.GLoad=function(){
		map = new GMap2($('map_container'));
		map.setCenter(new GLatLng(lat, lng), zoom);
		map.addControl(new GLargeMapControl());
		map.addControl(new GOverviewMapControl());
		<c:if test="${param['type']=='satellite'}">
		map.addControl(new GMapTypeControl());
		map.setMapType(G_SATELLITE_MAP);
		</c:if>
		//map.enableDoubleClickZoom();
		//map.enableScrollWheelZoom();
		mgr = new GMarkerManager(map);
		mark();
		GEvent.addListener(map, 'moveend', mark);
		GEvent.addListener(map, 'singlerightclick', function(point){map.panTo(gpoint2glatlng(point))});
}

function gpoint2glatlng(point){
var lat1=map.getBounds().getNorthEast().lat();
var lng1=map.getBounds().getSouthWest().lng();
var lat2=map.getBounds().getSouthWest().lat();
var lng2=map.getBounds().getNorthEast().lng();
var height=map.getSize().height;
lng1=lng1+(point.x/map.getSize().width)*(lng2-lng1);
lat1=lat1+(point.y/map.getSize().height)*(lat2-lat1);
return new GLatLng(lat1,lng1);
}

function mark(){
		var bounds = map.getBounds();
		var southWest = bounds.getSouthWest();
		var northEast = bounds.getNorthEast();
		GDownloadUrl('<c:url value="/backend/common/region/mark"/>?southWest='+southWest.lat()+','+southWest.lng()+'&northEast='+northEast.lat()+','+northEast.lng()+'&zoom='+map.getZoom(), function(data, responseCode) {
        var markers = eval('('+data+')');
  		for (var i = 0; i < markers.length; i++) 
  			addMarker(markers[i]);
		});
}

function addMarker(region){
		var point = new GLatLng(region.latitude,region.longitude);
    	var marker = new GMarker(point);
    	var level = region.level;
    	if(level<=1)
    		mgr.addMarker(marker);
    	else if(level<=2)
    		mgr.addMarker(marker,5);
    	else
    		mgr.addMarker(marker,7);
    	mgr.refresh();
    	GEvent.addListener(marker, "click", function() {
    		marker.openInfoWindow(document.createTextNode(region.name));
  		});
}

function moveTo(region){
if(region.latitude)
	map.panTo(new GLatLng(region.latitude,region.longitude));
}

function saveLatLng(region){
if(!confirm('map center to '+region.name+'?'))
return;
region.latitude=map.getCenter().lat();
region.longitude=map.getCenter().lng();
var url='<c:url value="/backend/common/region/save"/>?region.id='+region.id+'&region.latitude='+region.latitude+'&region.longitude='+region.longitude;
new $.ajax({url:url,dataType:true,success:function(){addMarker(region)}});
}

function switchTo(type){
url='<c:url value="/backend/common/region/map?"/>'+(type?'type='+type:'')+'&lat='+map.getCenter().lat()+'&lng='+map.getCenter().lng()+'&zoom='+map.getZoom();
window.location.href=url;
}
</script>
</head>
<body>
<div style="float: left; width: 20%;">
<div><a class="link" onclick="webFXTreeConfig.action=moveTo">move
mode</a> <a class="link" onclick="webFXTreeConfig.action=saveLatLng">mark
mode</a></div>
<div id="tree_container"><script>
webFXTreeConfig.src='<c:url value="/region/children/"/>';
webFXTreeConfig.action=moveTo;
var tree = new WebFXLoadTree('please select',webFXTreeConfig.src,'','explorer');
document.write(tree);
tree.expand();
</script></div>
</div>
<div style="float: left; width: 80%;">
<div><c:if test="${param['type']=='satellite'}">
	<a class="link" onclick="switchTo('map')">map </a>
</c:if> <c:if test="${param['type']!='satellite'}">
	<a class="link" onclick="switchTo('satellite')">satellite</a>
</c:if></div>
<div id="map_container" style="height: 600px;"></div>
</div>
</body>
</html>
