var ECSideMessage={
	ENCODING	:	"UTF-8",
	WAITTING_MSG	:	"正在提交...",
	ERR_PAGENO :	"跳转页数只能是 1 至 #{1} 的整数。",
	OVER_MAXEXPORT : "数据总数超过了所允许的最大值( #{1} 条 )。"
};

(function(){
	window.undefined = window.undefined;
	window.isIE=!!(window.attachEvent && !window.opera);
	if (window.isIE) {
	  window.attachEvent('onunload', $_IEGC);
	  /* window.attachEvent('onload', $_IEGC); */
	}
	if ($_E){
		$_E_original=$_E;
	}

})();

function $_IEGC(){
	CollectGarbage();
}


var ECSideConstants={
	EMPTY_FUNCTION : function(){},
	EC_ID : "ec",
	ETI_ID : "eti",
	PAGEFIELD_SUFFIX : "_p",
	SORT_PREFIX : "s_",
	EXPORT_IFRAME_SUFFIX : "_ecs_export_iframe",
	SHADOW_ROW : "_shadowRow",
	HIDE_HEADER_ROW : "_hideListRow",
	AJAX_ZONE_BEGIN : "_begin_ ",
	AJAX_ZONE_END : " _end_",
	AJAX_ZONE_PREFIX : "<!-- ECS_AJAX_ZONE_PREFIX_",
	AJAX_ZONE_SUFFIX : "_ECS_AJAX_ZONE_SUFFIX -->",
	MIN_COL_WIDTH : 10,
	SCROLLBAR_WIDTH :18,
	SCROLL_SPEED : 50,
	MIN_COLWIDTH : "30",
	AJAX_HEADER :['useAjaxPrep','true'],
	ROW_HIGHLIGHT_CLASS : "highlight",
	ROW_SELECTLIGHT_CLASS : "selectlight",
	DRAG_BUTTON_COLOR : "#3366ff",
	LIST_HEIGHT_FIXED : window.isIE?0:1 ,
	LIST_WIDTH_FIXED : window.isIE?0:1 ,
	IE_WIDTH_FIX_A : 1,
	IE_WIDTH_FIX_B : 2,
	FF_WIDTH_FIX_A : -3,
	FF_WIDTH_FIX_B : -6,
	OFFSET_A		: 2
};



var $_E=function(){

  var elements = [];

  for (var i = 0; i < arguments.length; i++) {
    var element = arguments[i];
    if (typeof(element) == 'string') {
		var elemId=element;
		element = document.getElementById(elemId);
		if (element==null){
			element = document.getElementsByName(elemId);
			if (element.length>0){	
				element=element[0];	
			}else{
				element=null;
			}
		}
	}

    if (arguments.length == 1) {return element;}
    elements.push(element);
  }

  return elements;
};


var ECSideList={};
var ECSide=function(formid){
	
	var Me=this;

	Me.ETI_ID=ECSideConstants.ETI_ID;

	
	Me.MIN_COL_WIDTH=80;

	Me.onLoad=null;

	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	
	Me.EXPORT_IFRAME_ID=formid+ ECSideConstants.EXPORT_IFRAME_SUFFIX;

	Me.SHADOWROW_ID=formid+ECSideConstants.SHADOW_ROW;

	Me.ECForm=null;

	Me.ECMainContent=null;

	Me.selectedRow=null;

	/* TODO: ... */
	Me.columns=null;

	Me.sortedColumn=null;
	Me.sortType="default";
	Me.sortedColumnHearderId=null;


	Me.afterFillForm=null;

	ECSideList[formid]=Me;
	
	Me.id=formid;
	this.useAjax=true;
	this.doPrep=false;
	this.isDebug=false;
	this.doPrepPrev=false;
	this.findAjaxZoneAtClient=true;

	this.prepState={next : 0 ,prev : 0};
	this.prepPage={next : 0 ,prev : 0};
	this.prepareaName={};
	this.pageFieldName=this.id + ECSideConstants.PAGEFIELD_SUFFIX;

	this.totalPagesFieldName=this.id+"_totalpages";
	this.totalRowsFieldName=this.id+"_totalrows";


	this.prepareaName['next']=this.id+"_ec_preparea_n";

	this.prepareaName['prev']=this.id+"_ec_preparea_p";

	Me.scrollList=false;
	Me.orgListHeight=0;
	Me.listHeight=0;
	
 	Me.init=function(){

		Me.ECForm=document.getElementById(Me.id);
		
		if (!Me.ECForm)	{
			/* alert("ERR: tableId=\""+Me.id+"\" not exist!"); */
			return;
		}
		Me.DEFAULT_ACTION=Me.ECForm.getAttribute("action");

		Me.ECMainContent=document.getElementById(Me.id+"_main_content");
		
		if (window.isIE){
			var hideHeader=document.getElementById(Me.id+ECSideConstants.HIDE_HEADER_ROW);
			if (hideHeader){
				hideHeader.style.display="none";
			}
		}
		if (window.frameElement && window.frameElement.name==Me.EXPORT_IFRAME_ID){
			Me.ECForm.style.visibility ="visible";
			ECSideUtil.printFrame(window.frameElement.contentWindow);
			return;
		}
		
		if (Me.sortedColumnHearderId){
			var sortedHeader=document.getElementById(Me.sortedColumnHearderId);
			if (sortedHeader && Me.sortType && Me.sortType!='' && Me.sortType!='default' ){
				var newHtml=ECSideUtil.trimString(sortedHeader.innerHTML,-1)+"&#160;<div class=\"sort"+Me.sortType.toUpperCase()+"\"></div>";
				sortedHeader.innerHTML=newHtml;
			}
		}




		if (typeof(Me.ajaxSubmit)!="function"){
			Me.useAjax=false;
			Me.ajaxSubmit=function(){
				Me.ECForm.submit();
			};
		}
		if (!Me.useAjax){
			Me.doPrep=false;
		}
		Me.buildPrepArea();
		Me.ajaxPrepSubmit();
		
		Me.ECForm.style.visibility ="visible";

		Me.initList();

	};
	
	Me.initList=function(){


			Me.MIN_COL_WIDTH=Me.ECForm.getAttribute("minColWidth");
			Me.ECListHead=document.getElementById(Me.id+"_table_head");
			Me.ECListBody=document.getElementById(Me.id+"_table_body");

			var canResizeColWidth=Me.ECForm.getAttribute("canResizeColWidth");
			if (canResizeColWidth=="true" || canResizeColWidth===true){
				Me.ECListHead.parentNode.style.tableLayout="fixed";
				Me.ECListBody.parentNode.style.tableLayout="fixed";
				ECSideUtil.resizeInit();
			}else if (Me.ECListHead.parentNode!=Me.ECListBody.parentNode && Me.ECListBody.rows && Me.ECListBody.rows.length>0){
				var headTDs=Me.ECListHead.rows[0].cells;
				var bodyTDs=Me.ECListBody.rows[0].cells;
				for (var i=0;i<headTDs.length;i++ )	{
					
					if (window.isIE){
						headTDs[i].style.width=bodyTDs[i].offsetWidth+"px";
					}else{
						var tt=bodyTDs[i].offsetWidth-headTDs[i].offsetWidth;
						
						tt=parseInt(headTDs[i].width)+tt;
						if (isNaN(tt))	{
							continue;
						}
						if (i==0){
							tt-=1;
						}
						headTDs[i].style.width=tt  +"px";
					}

				}
				
			}



		Me.bodyScrollY=$_E(Me.id+"_bodyScrollY");

		if (Me.bodyScrollY)	{
			Me.scrollList=true;
		}

		if (Me.scrollList){

			Me.headerScrollX=$_E(Me.id+"_headerScrollX");
			Me.bodyScrollX=$_E(Me.id+"_bodyScrollX");
			Me.bodyScrollXspan=$_E(Me.id+"_bodyScrollXspan");

			Me.ECListHead.parentNode.style.tableLayout="fixed";

			Me.bodyScrollX.style.height=ECSideConstants.SCROLLBAR_WIDTH+"px";

			var twidth=Me.headerScrollX.scrollWidth;
			Me.bodyScrollXspan.style.width=twidth+"px";
			Me.bodyScrollXspan.style.paddingLeft=twidth+"px";


			if (window.isIE){
				Me.initScrollXBarSize();
				Me.bodyScrollY.onresize=Me.initScrollXBarSize;
			}
			Me.initScrollBarSize();


			window.onresize=ECSideUtil.resizeListSize;

			Me.orgListHeight=ECSideUtil.parseIntOrZero(Me.bodyScrollY.style.height);

		}else{
			Me.orgListHeight=Me.ECListBody.scrollHeight;
		}

		Me.listHeight=Me.orgListHeight;


	};

Me.initScrollXBarSize=function(){

	if (!Me.scrollList) { return; }
		var listClientWidth=Me.bodyScrollY.clientWidth;
		if (listClientWidth>0){
			Me.headerScrollX.style.width=listClientWidth +"px";
			Me.bodyScrollX.style.width=listClientWidth+"px";
		}else{
			var wid=Me.bodyScrollY.style.width;
			Me.headerScrollX.style.width=wid;
			Me.bodyScrollX.style.width=wid;
		}
};

	Me.initScrollBarSize=function(){

			if (!Me.scrollList) { return; }

			if (!window.isIE){
				Me.initScrollXBarSize();
			}

			if (Me.bodyScrollY.clientHeight>=Me.ECListBody.scrollHeight){
				Me.bodyScrollY.style.height=(Me.ECListBody.scrollHeight+ECSideConstants.LIST_HEIGHT_FIXED)+"px";
			}
			
			var headerScrollXOffsetWidth=Me.headerScrollX.offsetWidth;
			var headerScrollXScrollWidth=Me.headerScrollX.scrollWidth;
			
			Me.bodyScrollY.scrollLeft=1;
			if (headerScrollXOffsetWidth >= headerScrollXScrollWidth || Me.bodyScrollY.scrollLeft<1){
				Me.bodyScrollX.style.display="none";
			}else{
				Me.bodyScrollX.style.display="block";
			}
			Me.headerScrollX.scrollLeft=0;
			Me.bodyScrollY.scrollLeft=0;
			Me.bodyScrollX.scrollLeft=0;
	};



	Me.buildPrepArea=function(){
		if (!Me.doPrep){
			return;
		}

		var hasPrepareaNext=document.getElementById(this.prepareaName['next']);
		if (!hasPrepareaNext){
			var ta=document.createElement("textarea");
			ta.id=this.prepareaName['next'];
			ta.disabled=true;
			ta.style.display="none";
			document.body.appendChild(ta);
		}


		var hasPrepareaPrev=document.getElementById(this.prepareaName['prev']);
		if (!hasPrepareaPrev){
			var tb=document.createElement("textarea");
			tb.id=this.prepareaName['prev'];
			tb.disabled=true;
			tb.style.display="none";
			document.body.appendChild(tb);
		}

		/* for Debug */
		if(Me.isDebug){
			ta=document.getElementById(this.prepareaName['next']);
			tb=document.getElementById(this.prepareaName['prev']);
			ta.disabled=false;
			ta.style.display="inline";
			ta.rows=10;
			ta.cols=50;
			tb.disabled=false;
			tb.style.display="inline";
			tb.rows=10;
			tb.cols=50;
		}

	};

	Me.goPage=function(){
    	var newPageNO = $_E(Me.pageFieldName).value;
		
		var key=null;

		if(newPageNO==Me.prepPage['next'] && Me.prepState['next']==2){
			key='next';
		}else if(newPageNO== Me.prepPage['prev'] && Me.prepState['prev']==2 && Me.doPrepPrev){
			key='prev';
		}
		
    	if (key!==null){
			try{
				var newhtml=$_E(Me.prepareaName[key]).value;
				if (newhtml==''){	$_E(Me.id).submit(); return;	}
				Me.ECMainContent.innerHTML=newhtml;
				Me.prepState[key]=0;
				Me.init();
				var originalRequest={};
				originalRequest.responseText=newhtml;
				if (Me.afterFillForm && typeof(Me.afterFillForm)=="function"){
					Me.afterFillForm(originalRequest);
				}
				window.setTimeout(Me.ajaxPrepSubmit,10);
				if (typeof(Me.onLoad)=="function"){
					Me.onLoad();
				}
			}catch(ex){
				$_E(Me.pageFieldName).value=newPageNO;
				Me.ajaxSubmit();
				/* $_E(Me.id).submit(); */
			}
    	}else{
	    	/* $_E(Me.id).submit(); */
			Me.ajaxSubmit();
    	}

 	};

	Me.dealResponse={
		'next'	: function(data){
			$_E(Me.prepareaName['next']).value =ECSideUtil.cutText(data,Me.id);
			Me.prepState['next']=2;
			Me.doingAjaxSubmit=false;

		},
		'prev'	: function(data){
			$_E(Me.prepareaName['prev']).value =ECSideUtil.cutText(data,Me.id);
			Me.prepState['prev']=2;
			Me.doingAjaxSubmit=false;
		}
	};
    

	Me.ajaxPrepSubmit=function(){
		if (!Me.doPrep){
			return;
		}
		Me.ajaxPrep(1);
		Me.ajaxPrep(-1);
	};

    Me.ajaxPrep=function(which){

		var key;

		if (which==1){
			key='next';
		}else if (which==-1 && Me.doPrepPrev){
			key='prev';
		}else{
			return;
		}
		Me.prepState[key]=1;
		Me.prepPage[key]=$_E(Me.pageFieldName).value/1+which;
		if (Me.prepPage[key]<1 || Me.prepPage[key]>($_E(Me.totalPagesFieldName).value/1)) {
			 return;
		}
		$_E(Me.pageFieldName).value=Me.prepPage[key];


		Me.ajaxSubmit(Me.dealResponse[key],true);

		$_E(Me.pageFieldName).value=Me.prepPage[key]-which;
	};
	
 	

	Me.doingAjaxSubmit=false;
	Me.ajaxSubmit=function(resfunc,asy,parameter){
		if (!Me.useAjax){
			Me.ECForm.submit();
			return;
		}

		if (!asy){
			asy=false;
		}
		if (!resfunc){
			resfunc=Me.fillForm;
		}
		if(!asy && Me.doingAjaxSubmit){
			/*
			alert("the last ajax request is not complete. try later.");
			return;
			*/
		}
		Me.doingAjaxSubmit=true;

		ECSideUtil.formSubmit(Me.id,resfunc,"post",asy,parameter);

	};


	Me.fillForm=function(data){
		var newhtml=ECSideUtil.cutText(data,Me.id);
		if (newhtml==''){
			return;	
		}
		Me.ECMainContent.innerHTML=newhtml;
		Me.init();

		if((typeof _observe)!='undefined')
			_observe(Me.ECMainContent);
		
		if (Me.afterFillForm && typeof(Me.afterFillForm )=="function"){
			Me.afterFillForm(originalRequest);
		}
		
		Me.doingAjaxSubmit=false;

		/*
		Me.initList();
		if (typeof(Me.onLoad)=="function"){
			Me.onLoad();
		}
		*/
	};
	
	Me.currentShadowRowParentId=null;
	Me.currentShadowEventSrcId=null;
	Me.autoCloseOtherShadowRow=true;

	Me.showShadowRowCallBack=function(formid,crow,shadowRow,eventSrc){
	/* todo */
	};
	Me.hideShadowRowCallBack=function(formid,crow,shadowRow,eventSrc){
	/* todo */
	};
	Me.firstShowShadowRowCallBack=function(formid,crow,shadowRow,eventSrc){
	/* todo */
	};
	
};
    
var ECSideUtil={};

ECSideUtil.getMessage=function(name, msgs){
var msgTemplate=ECSideMessage[name];
	for (var i=1;i<arguments.length ;i++ ){
		msgTemplate=ECSideUtil.replaceAll(msgTemplate,"#{"+i+"}",arguments[i]);
	}
	return msgTemplate;
};

ECSideUtil.getTotalPages=function(formid){

	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	try{
		var form=ECSideList[formid].ECForm;
		return form[formid+"_totalpages"].value;
	}catch(e){
		return -1;
	}
};

ECSideUtil.getTotalRows=function(formid){
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	try{
		var form=ECSideList[formid].ECForm;
		return form[formid+"_totalrows"].value;
	}catch(e){
		return -1;
	}
};

ECSideUtil.cutText=function(text,formid){
		var ecsideObj=ECSideList[formid];
		if (text.responseText){
			text=text.responseText;
		}
		if (ecsideObj && !ecsideObj.findAjaxZoneAtClient) {
			return text;
		}

		var begin=ECSideConstants.AJAX_ZONE_PREFIX+ECSideConstants.AJAX_ZONE_BEGIN+formid +ECSideConstants.AJAX_ZONE_SUFFIX;
		var end=ECSideConstants.AJAX_ZONE_PREFIX+ECSideConstants.AJAX_ZONE_END+formid +ECSideConstants.AJAX_ZONE_SUFFIX;

        var p1 = text.indexOf(begin);
        if (p1 != -1) {
            p1+=begin.length;
            var p2 = text.indexOf(end, p1);
            if (p2!=-1){
                return text.substring(p1, p2);
            }
        }
		return text;
	};

ECSideUtil.noExport=function(formid){
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	var ecsideObj=ECSideList[formid];
	var etiid;
	var form;
	if (!ecsideObj)	{
		etiid=ECSideConstants.ETI_ID;
		form=document.getElementById(formid);
	}else{
		etiid=ecsideObj.ETI_ID;
		form=ecsideObj.ECForm;
	}

	try{
		form[etiid].value="";
	}catch(e){
	}
	
};


ECSideUtil.refresh=function(formid){
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	var ecsideObj=ECSideList[formid];
	var form;
	if (!ecsideObj)	{
		form=document.getElementById(formid);
	}else{
		form=ecsideObj.ECForm;
	}
	try{
    	form[formid+"_totalrows"].value="";
	}catch(e){
	}
};

ECSideUtil.reload=function(formid,pageno){
	ECSideUtil.noExport(formid);
	ECSideUtil.refresh(formid);
	var ecsideObj=ECSideList[formid];
	var pageid=ecsideObj.pageFieldName;
	if (!pageno){
		pageno=ecsideObj.ECForm[pageid].value;
	}
	ECSideUtil.gotoPage(formid,pageno);

	ECSideUtil.showShadowRow(ecsideObj.currentShadowRowParentId,ecsideObj.currentShadowEventSrcId,formid);
};


ECSideUtil.gotoPage=function(formid,pageno){

var ecsideObj=ECSideList[formid];

	var pageid=ecsideObj.pageFieldName;
	var form=ecsideObj.ECForm;

	form[pageid].value=pageno;
	ECSideUtil.noExport(formid);
	form.action=ecsideObj.DEFAULT_ACTION;

	try {
		if (ecsideObj.doPrep){
			ecsideObj.goPage();
		}else {
			ecsideObj.ajaxSubmit();
		}
	}catch (e){
		try {
			ecsideObj.ajaxSubmit();
		}catch (e2){
			form.submit();
		}
	}
};


ECSideUtil.gotoPageByInput=function(formid,inputNoObj){

	var form=ECSideList[formid].ECForm;
	var tempInput=null;
	if (inputNoObj.type!="text"){
		tempInput=inputNoObj.nextSibling;
		if (tempInput.type!="text"){
			tempInput=inputNoObj.previousSibling;
		}
		inputNoObj=tempInput;
	}

	var pageno=inputNoObj.value/1;

	
	var totalpages=form[formid+"_totalpages"].value/1;
	if (!isFinite(pageno) || (pageno+"").indexOf(".")!=-1 || pageno<1 || pageno>totalpages){
		alert(ECSideUtil.getMessage("ERR_PAGENO",totalpages));
		inputNoObj.focus();
		inputNoObj.select();
		return;
	}
	if (pageno<1){
		pageno=1;
	}
	ECSideUtil.gotoPage(formid,pageno);
};




ECSideUtil.doSort=function(formid,columnAlias,sortT1,columnHearderId){


	if ( ECSideUtil.onDragobj==true){
		ECSideUtil.onDragobj=false;
		return;
	}

	if (window.isIE && event.button>1) {
		return;
	}

	var asc="asc";
	var desc="desc";
	var defaultOrder="default";

	var ecsideObj=ECSideList[formid];

	var pageid=ecsideObj.pageFieldName;

	var form=ecsideObj.ECForm;

	form[pageid].value=1;

if ( typeof(sortT1)!='string'){
	columnHearderId=sortT1.id;
	sortT1=null;
}else if(columnHearderId && typeof(columnHearderId)!='string'){
	columnHearderId=columnHearderId.id;
}



var tOrder="default";

	if (sortT1){
		tOrder=sortT1;
	}else if (ecsideObj.sortedColumn==columnAlias){
		if (!ecsideObj.sortType || ecsideObj.sortType=="default"){
			tOrder="asc";
		}else if (ecsideObj.sortType=="asc"){
			tOrder="desc";
		}else if (ecsideObj.sortType=="desc"){
			tOrder="default";
		}else{
			tOrder="asc";
		}
	}else{
		tOrder="asc";
	}

ecsideObj.sortedColumn=columnAlias;
ecsideObj.sortType=tOrder;
ecsideObj.sortedColumnHearderId=columnHearderId;




	ECSideUtil.noExport(formid);
	var oAction=form.action;
	form.action=ecsideObj.DEFAULT_ACTION;

if (ecsideObj.sortedColumn && ecsideObj.sortedColumn!=''){
	form[formid+"_"+ECSideConstants.SORT_PREFIX+ecsideObj.sortedColumn].value="";
}
if (ecsideObj.custSort){
	ecsideObj.custSort(columnAlias,tOrder);
}else{
	form[formid+"_"+ECSideConstants.SORT_PREFIX+columnAlias].value=tOrder;
}
		try {
			ecsideObj.ajaxSubmit();
			form.action=oAction;
		}catch (e2){
			form.submit();
		}
};


ECSideUtil.doExportList=function(formid,fileName){
	var type="xls";
	ECSideUtil.doExport(formid,type,fileName);
};
ECSideUtil.doExport=function(formid,type,fileName){

/* for compatibility */
if (arguments.length>4){
	type=arguments[4];
	fileName=arguments[5];
}

	var etiid=ECSideList[formid].ETI_ID;

	var form=ECSideList[formid].ECForm;

	var maxRowsExported = form.getAttribute("maxRowsExported");
	if (maxRowsExported && ECSideUtil.parseIntOrZero(maxRowsExported)>0){
		if(ECSideUtil.parseIntOrZero(maxRowsExported)<ECSideUtil.getTotalRows(formid)){
			alert(ECSideUtil.getMessage("OVER_MAXEXPORT",ECSideUtil.parseIntOrZero(maxRowsExported)));
			return;
		}
	}

	form[formid+"_ev"].value=type;
	form[formid+"_efn"].value=fileName;
	form[etiid].value=formid;

	var otarget=form.target;

	form.action=ECSideList[formid].DEFAULT_ACTION;

	/* if (type=="print"){ */
		var targetFrame=ECSideList[formid].EXPORT_IFRAME_ID;
		targetFrame=document.getElementById(targetFrame);
		if (targetFrame){
			form.target=ECSideList[formid].EXPORT_IFRAME_ID;
		}
		
	/* } */
	form.submit();
	form.target= otarget;
	ECSideUtil.noExport(formid);

};

ECSideUtil.changeRowsDisplayed=function(formid,selectObj){

	var pageid=ECSideList[formid].pageFieldName;

	var form=ECSideList[formid].ECForm;
	form[formid+"_crd"].value=selectObj.options[selectObj.selectedIndex].value;
	form[pageid].value='1';

	ECSideUtil.noExport(formid);
	form.action=ECSideList[formid].DEFAULT_ACTION;
	try {
		ECSideList[formid].ajaxSubmit();
	}catch (e2){
		form.submit();
	}
};

ECSideUtil.checkAll=function(formid,checkboxname,checkcontrolObj){
	var form=ECSideList[formid].ECForm;
	if (!form.elements[checkboxname]){ return;}

	if (!form.elements[checkboxname].length){ 
		if (!form.elements[checkboxname].disabled){
			form.elements[checkboxname].checked = checkcontrolObj.checked;
		}
		return;
	}
	for(i = 0; i < form.elements[checkboxname].length; i++) {
		if (!form.elements[checkboxname][i].disabled){
			form.elements[checkboxname][i].checked = checkcontrolObj.checked;
		}
	}
};


ECSideUtil.selectRow=function(formid,rowObj){
	var selectlightClassName=ECSideConstants.ROW_SELECTLIGHT_CLASS;
	var ecsideObj=ECSideList[formid];
	if (!ecsideObj || rowObj==ecsideObj.selectedRow){ return;}
	ECSideUtil.addClass(rowObj,selectlightClassName);
	ECSideUtil.removeClass(ecsideObj.selectedRow,selectlightClassName);
	ecsideObj.selectedRow=rowObj;
};

ECSideUtil.lightRow=function(formid,rowObj){
	ECSideUtil.addClass(rowObj,ECSideConstants.ROW_HIGHLIGHT_CLASS);
};

ECSideUtil.unlightRow=function(formid,rowObj){
	ECSideUtil.removeClass(rowObj,ECSideConstants.ROW_HIGHLIGHT_CLASS);
};





ECSideUtil.getFirstChildElement=function(node){
	var nodeIdx=0;
	try{
		var nodeT=-1;
		while(nodeT!=1 && nodeIdx<node.childNodes.length){
			nodeT=node.childNodes[nodeIdx].nodeType;
			nodeIdx++;
		}
		nodeIdx--;
		return node.childNodes[nodeIdx];
	}catch(e){
		return node.childNodes[0];
	}
};

ECSideUtil.getNextElement=function(node){
	if (!node){
		return null;
	}
	var tnode=node.nextSibling;
	while ( tnode!=null ){
		if (tnode.nodeType==1) {
			return tnode;
		}
		tnode=tnode.nextSibling;
	}
	return null;
};


ECSideUtil.loadForm=function(formId){
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	var form=ECSideList[formId];
	if (!form){
		form=document.getElementById(formId);
		if (form){
			ECSideList[formId]=form;
		}
	}

};

ECSideUtil.getShadowRow=function(crow,formid){
		var ecsideObj=ECSideList[formid];

		var hasShadow=crow.getAttribute("hasShadow");
		var shadowRow=null;
		if (hasShadow=="true"){
			var crowIndex=crow.rowIndex;
			if (ecsideObj.scrollList){
				crowIndex++;
			}
			shadowRow=crow.parentNode.rows[crowIndex];
		}
		return shadowRow;
};

ECSideUtil.showShadowRow=function(crow,eventSrc,formid){
		var ecsideObj=ECSideList[formid];
		

		if(!crow){ crow=eventSrc; }
		if(!crow){ return; }
		if(typeof(crow)=="string" ){
			crow=document.getElementById(crow);
		}
		if(typeof(eventSrc)=="string" ){
			eventSrc=document.getElementById(eventSrc);
		}
		if (!crow){
			return;
		}
		var crowIndex=crow.rowIndex;

		if (ecsideObj.scrollList){
			crowIndex++;
		}
		var cellnum=crow.cells.length;

		var hasShadow=crow.getAttribute("hasShadow");
		var shadowRow=null;
		var isShowed=true;

		if (hasShadow=="true"){
			shadowRow=crow.parentNode.rows[crowIndex];
			if (shadowRow.style.display=="none"){
				shadowRow.style.display="";
				if (ecsideObj.showShadowRowCallBack){
					ecsideObj.showShadowRowCallBack(formid,crow,shadowRow,eventSrc);
				}
			}else{
				shadowRow.style.display="none";
				if (ecsideObj.hideShadowRowCallBack){
					ecsideObj.hideShadowRowCallBack(formid,crow,shadowRow,eventSrc);
				}	
				isShowed=false;
			}
		
		}else{
			shadowRow=crow.parentNode.insertRow(crowIndex);
			shadowRow.className="shadowRow";
			shadowRow.style.display="";
			var newcell=document.createElement("td");
			newcell.colSpan=cellnum;
			shadowRow.appendChild(newcell);
			crow.setAttribute("hasShadow","true");
			shadowRow.setAttribute("isShadow","true");
			if (ecsideObj.firstShowShadowRowCallBack){
				ecsideObj.firstShowShadowRowCallBack(formid,crow,shadowRow,eventSrc);
			}
			
		}
		if ( isShowed && ecsideObj.autoCloseOtherShadowRow ) {
			if (ecsideObj.currentShadowRowParentId && crow.id!=ecsideObj.currentShadowRowParentId && ecsideObj.currentShadowEventSrcId )	{
				ECSideUtil.showShadowRow(ecsideObj.currentShadowRowParentId,ecsideObj.currentShadowEventSrcId,ecsideObj.id);
			}
		}
		if (isShowed){
			ecsideObj.currentShadowRowParentId=crow.id;
			ecsideObj.currentShadowEventSrcId=eventSrc.id;
		}else{
			ecsideObj.currentShadowRowParentId=null;
			ecsideObj.currentShadowEventSrcId=null;
		}

	};



ECSideUtil.operateECForm=function(actionUrl,resFunc,parameter,asy,formid){   

	var ecsideObj=ECSideList[formid];
	var pageid=ecsideObj.pageFieldName;
	var form=ecsideObj.ECForm;

	if (!asy){
		asy=true;
	}
	var appPara='';
	if (parameter){
		if (typeof(parameter)=='string'){
			appPara=parameter;
		}else{
			for (var k in parameter){
				if (!form[k]){
					appPara=appPara+k+'='+parameter[k]+'&';
				}else{
					form[k].value=parameter[k];
				}
			}
		}
	}


	ECSideUtil.refresh(formid);
	form[pageid].value=1;
	ECSideUtil.noExport(formid);
	form.action=actionUrl;
	try {
		ecsideObj.ajaxSubmit(resFunc,asy,appPara);
	}catch (e){
		form.submit();
	}
	try {
		form.action=ecsideObj.DEFAULT_ACTION;
	}catch (e){
	}
};   

ECSideUtil.queryECForm=function(formid,asy,parameter){
	var ecsideObj=ECSideList[formid];
	var resFunc=null;
	var actionUrl=ecsideObj.DEFAULT_ACTION;
	ECSideUtil.operateECForm(actionUrl,resFunc,parameter,asy,formid);
};





ECSideUtil.formSubmit=function(formid,resfunc,method,asy,parameter){
	var form=$_E(formid);

		if (!resfunc){
			resfunc=EMPTY_FUNCTION;
		}
		if (!asy){
			asy=false;
		}
		
		var appPara='';
		if (parameter){
			if (typeof(parameter)=='string'){
				appPara=parameter;
			}else{
				for (var k in parameter){
					appPara=appPara+k+'='+parameter[k]+'&';
				}
			}
		}

		if (!method){
			method=form.method;
			if (!method || method.toLowerCase()!="get" && method.toLowerCase()!="post"){
				method="post";
			}
		}

		var url=form.action;
		//change to jquery
		var pars=$(form).serialize();
		/* fix a prototype bug */
		
		var ecsideObj=ECSideList[formid];
		if (ecsideObj && ecsideObj.findAjaxZoneAtClient===false){
			pars+="&findAjaxZoneAtClient=false&";
		}

		pars=pars+'&'+appPara;
		pars=pars.replace(/(^|&)_=(&|$)/g,'$1'+'$2');
		pars=pars.replace(/&+/g,'&');
		/* end of fix a prototype bug */
		//change to jquery
		$.ajax({
			url:url,
			type:method,
			data:pars,
			dataType:'html',
			success:resfunc
		});
};


ECSideUtil.printFrame=function(frame, doctitle,onfinish) {

	if ( !frame ) { frame = window; }
	if ( !doctitle ) {
		doctitle="";
	}
		
	frame.document.title=doctitle;

	  function execOnFinish() {
		switch ( typeof(onfinish) ) {
		  case "string": execScript(onfinish); break;
		  case "function": onfinish();
		}

		if ( focused && !focused.disabled ) { focused.focus(); }
		if (frame!=window){
			frame.location="about:blank";
		}
	  }

  if ( frame.document.readyState !== "complete" &&
       !confirm("The document to print is not downloaded yet! Continue with printing?") ) {
		execOnFinish();
		return;
  }

  if ( frame.print ) { // IE5+
    var focused = document.activeElement; 
    frame.focus();
	frame.print();
    execOnFinish();
    return;
  }else{
	alert("the PRINT for IE 5.0+ Only");
  }

};


/*============ UTILS ============*/

function ECSideUtil_addEvent( obj, type, fn ) {  
  if ( obj.attachEvent ) {  
    obj['e'+type+fn] = fn;  
    obj[type+fn] = function(){obj['e'+type+fn]( window.event );};
    obj.attachEvent( 'on'+type, obj[type+fn] );  
  }else if(obj.addEventListener){
    obj.addEventListener( type, fn, false );  
  }
}

function ECSideUtil_removeEvent( obj, type, fn ) {  
  if ( obj.detachEvent ) {  
    obj.detachEvent( 'on'+type, obj[type+fn] );  
    obj[type+fn] = null;  
    obj['e'+type+fn] = null;
  }else if(obj.removeEventListener){
    obj.removeEventListener( type, fn, false ); 
  }
}

ECSideUtil.trimString=function(str, wh){
		if(!str.replace){ return str; }
		if(!str.length){ return str; }
		var re = (wh > 0) ? (/^\s+/) : (wh < 0) ? (/\s+$/) : (/^\s+|\s+$/g);
		return str.replace(re, "");
};

ECSideUtil.getPosLeft=function(elm) {
	var left = elm.offsetLeft;
	while((elm = elm.offsetParent) != null)	{
		left += elm.offsetLeft;
	}
	return left;
};
ECSideUtil.getPosRight=function(elm){
	return ECSideUtil.getPosLeft(elm)+elm.offsetWidth;
};


ECSideUtil.replaceAll=function(exstr,ov,value){
	var gc=ECSideUtil.escapeRegExp(ov);
	if (gc==null || gc==''){
		return exstr;
	}
	var reReplaceGene="/"+gc+"/gm";
	var r=null;
	var cmd="r=exstr.replace("+reReplaceGene+","+ECSideUtil.escapeString(value)+")";
	eval(cmd);
	return r;
};

ECSideUtil.escapeRegExp=function(str) {
	return !str?''+str:(''+str).replace(/\\/gm, "\\\\").replace(/([\f\b\n\t\r[\^$|?*+(){}])/gm, "\\$1");
};

ECSideUtil.escapeString=function(str){ 

	return !str?''+str:('"' + (''+str).replace(/(["\\])/g, '\\$1') + '"'
		).replace(/[\f]/g, "\\f"
		).replace(/[\b]/g, "\\b"
		).replace(/[\n]/g, "\\n"
		).replace(/[\t]/g, "\\t"
		).replace(/[\r]/g, "\\r");
};


ECSideUtil.hasClass=function(object, className) {
	if (!object.className) { return false;}
	return (object.className.search('(^|\\s)' + className + '(\\s|$)') != -1);
};

ECSideUtil.removeClass=function(object,className) {
	if (!object) {return;}
	object.className = object.className.replace(new RegExp('(^|\\s)'+className+'(\\s|$)'), ' ');
};

ECSideUtil.addClass=function(object,className) {
	if (!object || ECSideUtil.hasClass(object, className)){return;}
	if (object.className) {
		object.className += ' '+className;
	} else {
		object.className = className;
	}
};

ECSideUtil.insertClass=function(object,className) {
	if (object.className) {
		object.className = className+' '+object.className;
	} else {
		object.className = className;
	}
};

ECSideUtil.parseIntOrZero=function(num){
	return ECSideUtil.parseInt(num,0);
};
ECSideUtil.parseIntOrOne=function(num){
	return ECSideUtil.parseInt(num,1);
};
ECSideUtil.parseInt=function(num,defaultNum){
	var t=parseInt(num);
	return isNaN(t)?defaultNum:t;
};


/* ===========LIST SCROLL ============= */


ECSideUtil.scrollListX=function(bodyScrollXBar,formid){
	var ecsideObj=ECSideList[formid];
	ecsideObj.bodyScrollY.scrollLeft=bodyScrollXBar.scrollLeft;	
	ecsideObj.headerScrollX.scrollLeft=bodyScrollXBar.scrollLeft;	
};

ECSideUtil.resizeListSize=function(){
	for (var k in ECSideList ){
		if (ECSideList[k]){
			ECSideList[k].initScrollBarSize();
		}
		
	}
};



/* ============ CUSTOM COLUMN WIDTH ======================= */


ECSideUtil.onDragobj=false;
ECSideUtil.Dragobj=null; 
ECSideUtil.DragobjSibling=null;
ECSideUtil.MinColWidth=ECSideConstants.MIN_COLWIDTH;

ECSideUtil.DragobjBodyCell=null;
ECSideUtil.DragobjBodyCellSibling=null;
ECSideUtil.DragFormid=null;


ECSideUtil.StartResizeTest=function(event,obj,formid){
	var e = event||window.event;
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	
	obj.focus();
	document.body.style.cursor = "e-resize";

	var dx=e.screenX;

	obj.parentTdW=obj.parentNode.clientWidth;

	obj.mouseDownX=dx;

    ECSideUtil.Dragobj=obj;
	ECSideUtil.onDragobj=true;

	ECSideUtil.MinColWidth=ECSideList[formid].MIN_COL_WIDTH;

	if (!ECSideUtil.MinColWidth||ECSideUtil.MinColWidth=='' || ECSideUtil.MinColWidth<1){
		ECSideUtil.MinColWidth=ECSideConstants.MIN_COLWIDTH;
	}
	 ECSideUtil.Dragobj.style.backgroundColor=ECSideConstants.DRAG_BUTTON_COLOR;

	 ECSideUtil.Dragobj.parentTdW-=ECSideUtil.Dragobj.mouseDownX;


	 var cellIndex=ECSideUtil.Dragobj.parentNode.cellIndex;
	try{
		ECSideUtil.DragobjBodyCell=ECSideList[formid].ECListBody.rows[0].cells[cellIndex];
	}catch(e){
		ECSideUtil.DragobjBodyCell=null;
	}
	ECSideUtil.DragFormid=formid;
	 
};


ECSideUtil.DoResizeTest=function(event){
	var e = event||window.event;

	if(ECSideUtil.Dragobj==null){
        return true;
	}
    if(!ECSideUtil.Dragobj.mouseDownX){
        return false;
	}

	document.body.style.cursor = "e-resize";

	var dx=e.screenX;

	var newWidth=ECSideUtil.Dragobj.parentTdW+dx;


	/* fix different from ie to ff . but I don't know why use 2 2 -3 -6 */
	if (window.isIE){
		newWidth=newWidth+ECSideConstants.IE_WIDTH_FIX_A;			
	}else{
		newWidth= newWidth+ ECSideConstants.FF_WIDTH_FIX_A;			// 4 because paddingLeft + paddingRight=4 ???
	}
	if(newWidth>ECSideUtil.MinColWidth  ) {
        ECSideUtil.Dragobj.parentNode.style.width = newWidth+"px";
		if (document.getElementById(ECSideUtil.DragFormid+"_table_body").parentNode!=document.getElementById(ECSideUtil.DragFormid+"_table_head").parentNode) {
		document.getElementById(ECSideUtil.DragFormid+"_table_body").parentNode.style.width=document.getElementById(ECSideUtil.DragFormid+"_table_head").parentNode.clientWidth+"px";
		}
			
		try{
			ECSideUtil.DragobjBodyCell.style.width = newWidth+"px";
			ECSideUtil.DragobjBodyCell.width = newWidth+"px";
		}catch(e){}
    }
	ECSideUtil.onDragobj=true;

};

ECSideUtil.EndResizeTest=function(event)
{
	if(ECSideUtil.Dragobj==null){
        return false;
	}
	/* fix different from ie to ff  */
	/*
		var offset=ECSideUtil.getPosRight(ECSideUtil.DragobjSibling) - ECSideUtil.Dragobj.oldSiblingRight;
		var offsetWidth=ECSideUtil.parseIntOrZero(ECSideUtil.DragobjSibling.style.width)-offset;
		ECSideUtil.DragobjSibling.style.width = offsetWidth+"px" ;
		try{
			ECSideUtil.DragobjBodyCellSibling.style.width = offsetWidth+"px";
		}catch(e){}
	*/
    ECSideUtil.Dragobj.mouseDownX=0;
	document.body.style.cursor = "";
	ECSideUtil.Dragobj.style.backgroundColor="";
	ECSideUtil.Dragobj=null; 
	ECSideUtil.DragobjSibling=null;

};



ECSideUtil.StartResize=function(event,obj,formid){


	var e = event||window.event;
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	
	obj.focus();
	document.body.style.cursor = "e-resize";
	var sibling = ECSideUtil.getNextElement(obj.parentNode);

	var dx=e.screenX;

	obj.parentTdW=obj.parentNode.clientWidth;
	obj.siblingW = sibling.clientWidth;

	obj.mouseDownX=dx;
	obj.totalWidth = obj.siblingW + obj.parentTdW;

	obj.oldSiblingRight=ECSideUtil.getPosRight(sibling);

    ECSideUtil.Dragobj=obj;
	ECSideUtil.DragobjSibling=sibling;
	ECSideUtil.onDragobj=true;

	ECSideUtil.MinColWidth=ECSideList[formid].MIN_COL_WIDTH;

	if (!ECSideUtil.MinColWidth||ECSideUtil.MinColWidth=='' || ECSideUtil.MinColWidth<1){
		ECSideUtil.MinColWidth=ECSideConstants.MIN_COLWIDTH;
	}
	 ECSideUtil.Dragobj.style.backgroundColor=ECSideConstants.DRAG_BUTTON_COLOR;

	 ECSideUtil.Dragobj.parentTdW-=ECSideUtil.Dragobj.mouseDownX;


	 var cellIndex=ECSideUtil.Dragobj.parentNode.cellIndex;
	try{
	 ECSideUtil.DragobjBodyCell=ECSideList[formid].ECListBody.rows[0].cells[cellIndex];
	 ECSideUtil.DragobjBodyCellSibling=ECSideUtil.getNextElement(ECSideUtil.DragobjBodyCell);
	}catch(e){
		ECSideUtil.DragobjBodyCell=null;
	}
	 
};


ECSideUtil.DoResize=function(event){
	var e = event||window.event;

	if(ECSideUtil.Dragobj==null){
        return true;
	}
    if(!ECSideUtil.Dragobj.mouseDownX){
        return false;
	}

	document.body.style.cursor = "e-resize";

	var dx=e.screenX;

	var newWidth=ECSideUtil.Dragobj.parentTdW+dx;

	var newSiblingWidth=0; 

	/* fix different from ie to ff . but I don't know why  */
	if (window.isIE){
		newWidth=newWidth+ECSideConstants.IE_WIDTH_FIX_A;			
		newSiblingWidth=ECSideUtil.Dragobj.totalWidth-newWidth+ECSideConstants.IE_WIDTH_FIX_B; 
	}else{
		newWidth= newWidth+ ECSideConstants.FF_WIDTH_FIX_A;			
		newSiblingWidth=ECSideUtil.Dragobj.totalWidth-newWidth + ECSideConstants.FF_WIDTH_FIX_B; 
	}
	if(newWidth>ECSideUtil.MinColWidth && newSiblingWidth>ECSideUtil.MinColWidth) {
        ECSideUtil.Dragobj.parentNode.style.width = newWidth+"px";
		ECSideUtil.DragobjSibling.style.width = newSiblingWidth+"px";
		try{
			ECSideUtil.DragobjBodyCell.style.width = newWidth+"px";
			ECSideUtil.DragobjBodyCellSibling.style.width = newSiblingWidth+"px";
			ECSideUtil.DragobjBodyCell.width = newWidth+"px";
			ECSideUtil.DragobjBodyCellSibling.width= newSiblingWidth+"px";
		}catch(e){}
    }
	ECSideUtil.onDragobj=true;

};

ECSideUtil.EndResize=function(event)
{
	if(ECSideUtil.Dragobj==null){
        return false;
	}
	/* fix different from ie to ff  */
	/*
		var offset=ECSideUtil.getPosRight(ECSideUtil.DragobjSibling) - ECSideUtil.Dragobj.oldSiblingRight;
		var offsetWidth=ECSideUtil.parseIntOrZero(ECSideUtil.DragobjSibling.style.width)-offset;
		ECSideUtil.DragobjSibling.style.width = offsetWidth+"px" ;
		try{
			ECSideUtil.DragobjBodyCellSibling.style.width = offsetWidth+"px";
		}catch(e){}
	*/
    ECSideUtil.Dragobj.mouseDownX=0;
	document.body.style.cursor = "";
	ECSideUtil.Dragobj.style.backgroundColor="";
	ECSideUtil.Dragobj=null; 
	ECSideUtil.DragobjSibling=null;

};

ECSideUtil.resizeInit=function(){
	document.onmousemove = ECSideUtil.DoResize;
	document.onmouseup = ECSideUtil.EndResize;
	document.body.ondrag = function() {return false;};
    document.body.onselectstart = function() {
		return ECSideUtil.Dragobj==null;
	};
	 
};





/* ===========EDIT CELL ============= */

// editType =  input select checkbox radio
ECSideUtil.editCell=function(cellObj,editType,templateId,formid){
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	if (cellObj.getAttribute("editing")=="true"){
		return;
	}

	cellObj.setAttribute("editing","true");
	var template=document.getElementById(templateId);
	var templateText=window.isIE?template.value:template.textContent;

	var text=window.isIE?cellObj.innerText:cellObj.textContent;
	var value=cellObj.getAttribute("cellValue");
	value=value==null?text:value;
	
	value=ECSideUtil.trimString(value);
	
	var name=cellObj.getAttribute("cellName");
	if (templateText.indexOf("name=\"\"")>0){
		templateText=ECSideUtil.replaceAll(templateText,"name=\"\"","name=\""+name+"\"");
	}


	if (editType=="input"){
		cellObj.innerHTML=ECSideUtil.replaceAll(templateText,"value=\"\"","value=\""+value+"\"");	
	}else if (editType=="select"){
		cellObj.innerHTML=ECSideUtil.replaceAll(templateText,"value=\""+value+"\"","value=\""+value+"\" selected=\"selected\"");	
	}else if (editType=="checkbox" || editType=="radio"){
		cellObj.innerHTML=ECSideUtil.replaceAll(templateText,"value=\""+value+"\"","value=\""+value+"\" checked=\"checked\"");	
	}
	
	ECSideUtil.getFirstChildElement(cellObj).focus();
};


ECSideUtil.updateCell=function(cellEditObj,editType){
	var cellObj=cellEditObj.parentNode;
	var value='';
	if (editType=="input"){
		value=cellEditObj.value;
		cellObj.innerHTML=cellEditObj.value;
		
	}else if (editType=="select"){
		value=cellEditObj.options[cellEditObj.selectedIndex].value;
		cellObj.innerHTML=cellEditObj.options[cellEditObj.selectedIndex].text;
	}else if (editType=="checkbox" || editType=="radio"){
		value=cellEditObj.value;
		cellObj.innerHTML=cellEditObj.nextSibling.nodeValue;
		
	}
	cellObj.setAttribute("cellValue",ECSideUtil.trimString(value));
	cellObj.setAttribute("edited","true");
	cellObj.parentNode.setAttribute("edited","true");
	cellObj.setAttribute("editing","false");
	ECSideUtil.addClass(cellObj, "editedCell");
};

ECSideUtil.getEditedRows=function(formid){
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	var erows=[];
	var ecsideObj = ECSideList[formid];
	if (ecsideObj && ecsideObj.ECListBody){
		var rs=ecsideObj.ECListBody.rows;
		for (var i=0;i<rs.length;i++){
			if (rs[i].getAttribute("edited")=="true"){
				erows.push(rs[i]);
			}
		}
	}
	return erows;
};


ECSideUtil.getDeletedRows=function(formid){
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	var erows=[];
	var ecsideObj = ECSideList[formid];
	if (ecsideObj && ecsideObj.ECListBody){
		var rs=ecsideObj.ECListBody.rows;
		for (var i=0;i<rs.length;i++){
			if (rs[i].getAttribute("deleted")=="true"){
				erows.push(rs[i]);
			}
		}
	}
	return erows;
};

ECSideUtil.getRemoveEditedClassRows=function(listRows,recordKey){
	if (listRows && listRows.length){
		for (var i=0;i<listRows.length;i++){
			if (listRows[i].getAttribute("recordKey")==ECSideUtil.trimString(recordKey)){
				ECSideUtil.clearRowEditedFlag(listRows[i]);
			}
		}
	}
};

ECSideUtil.clearRowEditedFlag=function(rowObj){
	var cs=rowObj.cells;
	for (var i=0;i<cs.length;i++){
		ECSideUtil.removeClass(cs[i], "editedCell");
	}
};

ECSideUtil.getRemoveDeletedRows=function(listRows,recordKey){

	if (listRows && listRows.length){
		for (var i=0;i<listRows.length;i++){
			var crow=listRows[i];
			if (crow && crow.getAttribute("recordKey")==ECSideUtil.trimString(recordKey) && crow.getAttribute("deleted")=="true"){
				var crowIndex=crow.rowIndex;
				if (crow.getAttribute("hasShadow")=="true" ){
					crow.parentNode.removeChild(crow.parentNode.rows[crowIndex+1]);
				}
				crow.parentNode.removeChild(crow);
			}
		}
	}
};

ECSideUtil.getRowCellsMap=function(rowObj,formid){

	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	var cellMap={};
	var keyvalue=rowObj.getAttribute("recordKey");
	if (keyvalue){
		cellMap["recordKey"]=keyvalue;
	}
	var cells=rowObj.cells;
	for (var i=0;i<cells.length;i++ ){
		var cellObj=cells[i];
		var name=cellObj.getAttribute("cellName");
		if (!name){
			continue;
		}
		var value=cellObj.getAttribute("cellValue");
		if (!value){
			value=window.isIE?cellObj.innerText:cellObj.textContent;
		}
		cellMap[name]=value;
	}
	return cellMap;
};

ECSideUtil.getRowCellsMapSer=function(rowObj,formid){
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
	var cellMap=ECSideUtil.getRowCellsMap(rowObj,formid);
	var cellMapS="&";
	for (var k in cellMap){
		cellMapS+=k+"="+encodeURI(cellMap[k])+"&";
	}
	return cellMapS;
};



ECSideUtil.doAjaxListUpdate=function(formid,url,callBack){
	var rows=ECSideUtil.getEditedRows(formid);
	for (var i=0;i<rows.length;i++){
		ECSideUtil.doAjaxRowUpdate(rows[i],url,callBack);
	}
	return rows;
};

ECSideUtil.doAjaxListDelete=function(formid,url,callBack){
	var rows=ECSideUtil.getDeletedRows(formid);
	for (var i=0;i<rows.length;i++){
		ECSideUtil.doAjaxRowUpdate(rows[i],url,callBack);
	}
	return rows;
};

ECSideUtil.doAjaxRowUpdate=function(rowObj,url,callBack){
	var pars=ECSideUtil.getRowCellsMapSer(rowObj);

	$.ajax( {url:url,type: "POST",requestHeaders:ECSideConstants.AJAX_HEADER,data: pars, complete: callBack } );
};


// It's like Function.prototype.bind.(prorotype.js)
ECSideUtil.bindFunction=function(functionObj){
	var newArgumentsT=[];
	for (var j=1;j< arguments.length;j++ ){
		newArgumentsT.push(arguments[j]);
	}
	return function(){
		for (var i = 0; i < arguments.length; i++) {
			if ( typeof(arguments[i])!="undefined" /* i!=1 ||*/ ){
				newArgumentsT[i]=arguments[i];
			}
		}
		return functionObj.apply(this,newArgumentsT);
	};
};

ECSideUtil.ajaxRequest=function(url,callback,parameter,asy){
	if (!asy){
		asy=true;
	}

		var appPara='';
		if (parameter){
			if (typeof(parameter)=='string'){
				appPara=parameter;
			}else{
				for (var k in parameter){
					appPara=appPara+k+'='+parameter[k]+'&';
				}
			}
		}

	$.ajax( {url:url,requestHeaders:ECSideConstants.AJAX_HEADER, type: "POST", 
		async: asy , data: appPara, complete: callback } );
	return myAjax;
};

ECSideUtil.changeListHeight=function(height,formid){
	if (!formid){
		formid=ECSideConstants.EC_ID;
	}
		var ecsideObj=ECSideList[formid];
		height=height+"";

		if ( "auto"!=height){
			if (height.indexOf('+')==0){
				height=ecsideObj.listHeight+ ECSideUtil.parseIntOrZero(height.substring(1));
			}else if (height.indexOf('-')==0){
				height=ecsideObj.listHeight- ECSideUtil.parseIntOrZero(height.substring(1));
			}else if (height=="reset"){
				height=ecsideObj.orgListHeight;
			}
		}
		if (ECSideUtil.parseIntOrZero(height)>ecsideObj.ECListBody.scrollHeight-ECSideConstants.OFFSET_A || height=="auto"){
			/* divSYT.style.overflowY="hidden"; */
			height=ecsideObj.ECListBody.parentNode.scrollHeight;
		}
		height=height<=0?1:height;

		ecsideObj.bodyScrollY.style.height=height+"px";
		ecsideObj.listHeight=height;
		window.setTimeout(ecsideObj.initScrollXBarSize,35);
};


/* for compatibility */
var ECCN = ECSide ;
var EccnUtil = ECSideUtil;