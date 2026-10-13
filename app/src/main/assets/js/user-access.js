/* GrowPlot organization-scoped authorization helpers. UI checks are NOT a security boundary. */
(function(root,factory){const api=factory();if(typeof module==="object"&&module.exports)module.exports=api;else root.GrowPlotAccess=api;})(typeof globalThis!=="undefined"?globalThis:this,function(){
"use strict";
const ROLES=["owner","farm_admin","farm_manager","supervisor","agronomist","worker","viewer"];
const PERMISSIONS={
owner:["*"],
farm_admin:["farm.read","crop.read","crop.write","health.read","health.write","camera.read","tasks.read","tasks.write","users.read","users.manage"],
farm_manager:["farm.read","crop.read","crop.write","health.read","health.write","camera.read","tasks.read","tasks.write"],
supervisor:["farm.read","crop.read","crop.write","health.read","camera.read","tasks.read","tasks.write"],
agronomist:["farm.read","crop.read","health.read","health.write","camera.read","tasks.read"],
worker:["farm.read","crop.read","health.read","camera.read","tasks.read","tasks.write"],
viewer:["farm.read","crop.read","health.read","camera.read","tasks.read"]
};
function validRole(role){return ROLES.includes(role);}
function canAccess(user,permission,resource){
if(!user||!validRole(user.role)||!resource||!user.organizationId||user.organizationId!==resource.organizationId)return false;
const allowed=PERMISSIONS[user.role];
if(!allowed.includes("*")&&!allowed.includes(permission))return false;
if(user.role==="owner")return true;
if(resource.farmId&&!(user.farmIds||[]).includes(resource.farmId))return false;
if(resource.plotId&&user.plotIds?.length&&!user.plotIds.includes(resource.plotId))return false;
if(resource.sectionId&&user.sectionIds?.length&&!user.sectionIds.includes(resource.sectionId))return false;
return true;
}
function canManageUser(actor,target){
if(!actor||!target||actor.organizationId!==target.organizationId)return false;
if(actor.id===target.id)return false;
if(actor.role==="owner")return target.role!=="owner";
return actor.role==="farm_admin"&&["farm_manager","supervisor","agronomist","worker","viewer"].includes(target.role)&&(!target.farmIds||target.farmIds.every(id=>(actor.farmIds||[]).includes(id)));
}
return {ROLES,PERMISSIONS,validRole,canAccess,canManageUser};
});
