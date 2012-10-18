create table common_region_new select * from ironrhino.common_region;

create table common_region_temp_a select a.id,a.name,a.parentId,a.name as parentName from common_region a where parentId is null;
insert into common_region_temp_a select a.id,a.name,a.parentId,b.name as parentName from common_region a join common_region b on a.parentId=b.id;

create table common_region_temp_b select a.id,a.name,a.parentId,a.name as parentName from common_region_new a where parentId is null;
insert into common_region_temp_b select a.id,a.name,a.parentId,b.name as parentName from common_region_new a join common_region_new b on a.parentId=b.id;

create table common_region_mapping select a.id as oldId,a.name as oldName,b.id as newId,b.name as newName from common_region_temp_a a join common_region_temp_b b on a.name=b.name and substring(a.parentName,1,2)=substring(b.parentName,1,2);

create table common_region_mapping_missing select a.id,a.name,a.parentId,a.parentName,a.parentId as newParentId,a.parentName as newParentName from common_region_temp_a a where not exists(select * from common_region_mapping where oldId=a.id);
update common_region_mapping_missing set newParentId = null,newParentName = null;
update common_region_mapping_missing a,common_region_mapping b set a.newParentId=b.newId where a.parentId = b.oldId;
update common_region_mapping_missing a,common_region_new b set a.newParentName=b.name where a.newParentId = b.id;

select a.id,a.name,a.parentName,b.id as newId,b.fullname from common_region_mapping_missing a,common_region_new b where a.name=b.name;
select * from common_region_mapping_missing;

--resolve missing mapping from common_region_mapping_missing and insert into common_region_mapping;
select count(*) from common_region where id not in (select oldId from common_region_mapping); --make sure 0 : 

									
update xxx a set a.regionId = (select b.newId from common_region_mapping b where b.oldId=a.regionId);


drop table common_region_temp_a;

drop table common_region_temp_b;

drop table common_region_mapping_missing;

drop table common_region_mapping;

truncate table common_region;

insert into common_region select * from common_region_new;

drop table common_region_new;

--rebuild search index;

