/* GrowPlot crop-planning domain module. No network calls or credentials. */
(function(root,factory){const api=factory();if(typeof module==="object"&&module.exports)module.exports=api;else root.GrowPlotCropPlanning=api;})(typeof globalThis!=="undefined"?globalThis:this,function(){
"use strict";
const STAGES=["planned","seedling","vegetative","flowering","fruiting","harvesting","completed","cancelled"];
const CROPS=[
{id:"chilli",name:"Chilli",malay:"Cili",scientific:"Capsicum annuum"},
{id:"cucumber",name:"Cucumber",malay:"Timun",scientific:"Cucumis sativus"},
{id:"eggplant",name:"Eggplant",malay:"Terung",scientific:"Solanum melongena"},
{id:"banana",name:"Banana",malay:"Pisang",scientific:"Musa spp."},
{id:"durian",name:"Durian",malay:"Durian",scientific:"Durio zibethinus"}
];
function requiredString(value,label){if(typeof value!=="string"||!value.trim())throw Error(label+" is required");return value.trim();}
function isoDate(value,label){const v=requiredString(value,label);if(!/^\d{4}-\d{2}-\d{2}$/.test(v)||Number.isNaN(Date.parse(v+"T00:00:00Z")))throw Error(label+" must be YYYY-MM-DD");return v;}
function createPlan(data){
if(!data||typeof data!=="object")throw Error("Plan is required");
const scope={organizationId:requiredString(data.organizationId,"organizationId"),farmId:requiredString(data.farmId,"farmId"),plotId:requiredString(data.plotId,"plotId"),greenhouseId:data.greenhouseId||null};
const sectionIds=[...new Set((data.sectionIds||[]).map(v=>requiredString(v,"sectionId")))];
if(!data.allSections&&!sectionIds.length)throw Error("Select sections or allSections");
const count=Number(data.plantCount);if(!Number.isSafeInteger(count)||count<=0)throw Error("plantCount must be a positive integer");
const plantingDate=isoDate(data.plantingDate,"plantingDate");
const harvestDate=data.expectedHarvestDate?isoDate(data.expectedHarvestDate,"expectedHarvestDate"):null;
if(harvestDate&&harvestDate<plantingDate)throw Error("Harvest date cannot precede planting date");
const stage=data.stage||"planned";if(!STAGES.includes(stage))throw Error("Invalid growth stage");
return {id:requiredString(data.id,"id"),...scope,sectionIds,allSections:!!data.allSections,cropId:requiredString(data.cropId,"cropId"),variety:data.variety||"",plantCount:count,plantingDate,expectedHarvestDate:harvestDate,stage,assignedUserIds:[...new Set(data.assignedUserIds||[])],notes:data.notes||""};
}
function isInSection(plan,sectionId){return !!plan.allSections||plan.sectionIds.includes(sectionId);}
return {STAGES,CROPS,createPlan,isInSection};
});
