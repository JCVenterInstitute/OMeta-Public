--  ============================== BACKING OUT THE Lorem Ipsum (BS) PROJECT
-- Here's how I clean up the user-junkpile project.  This should be identical to the cleanup
-- for the other project below, except for the project name values used in all the SQL.

-- NOTE: you really have to have the TEMPTEMP table in there to beat
-- "you can't specify target table" error 1093 in MySQL
delete from event_attribute where eventa_id in (
select * from
(select DISTINCT EAI.eventa_id from event_attribute EAI, project, event
where event_projet_id = projet_id and projet_name='Pellentesque loremipsumii' and eventa_event_id=event_id
) AS TEMPTEMP);

delete from sample_attribute where sampla_id in (
select * from
(select DISTINCT sampla_id from sample_attribute, project, sample
where sample_projet_id = projet_id and projet_name='Pellentesque loremipsumii' and sampla_projet_id=projet_id
) AS TEMPTEMP)
;

delete from project_attribute where projea_projet_id in (
select * from
(select DISTINCT projet_id from project
where projet_name='Pellentesque loremipsumii'
) AS TEMPTEMP)
;

-- Then the meta attributes.
delete from project_meta_attribute where projma_id in (
select * from
(select projma_id from project_meta_attribute, project
where projma_projet_id=projet_id and projet_name='Pellentesque loremipsumii') AS TEMPTEMP
);


delete from sample_meta_attribute where sampma_id in (
select * from
(select sampma_id from sample_meta_attribute, project
where sampma_projet_id=projet_id and projet_name='Pellentesque loremipsumii') AS TEMPTEMP
);


delete from event_meta_attribute where evenma_id in (
select * from
(select evenma_id from event_meta_attribute, project
where evenma_projet_id=projet_id and projet_name='Pellentesque loremipsumii') AS TEMPTEMP
);


-- Next eliminate the events relevant to the project.
delete from event where event_id in (
select * from
(select event_id from event, project where event_projet_id=projet_id and projet_name='Pellentesque loremipsumii') AS TEMPTEMP
);

-- Now the samples
delete from sample where sample_id in (
select sample_id from
(select * from sample, project where sample_projet_id=projet_id and projet_name='Pellentesque loremipsumii') AS TEMPTEMP
);

-- The project itself.
delete from project where projet_name='Pellentesque loremipsumii';

-- END


--  --  --  ============================== BACKING OUT THE Cellulosome PROJECT
-- Here's how I clean up a unit-test project to reload as a test.

-- NOTE: you really have to have the TEMPTEMP table in there to beat
-- "you can't specify target table" error 1093 in MySQL
delete from event_attribute where eventa_id in (
select * from
(select DISTINCT EAI.eventa_id from event_attribute EAI, project, event
where event_projet_id = projet_id and projet_name='Cellulosome' and eventa_event_id=event_id
) AS TEMPTEMP);

delete from sample_attribute where sampla_id in (
select * from
(select DISTINCT sampla_id from sample_attribute, project, sample
where sample_projet_id = projet_id and projet_name='Cellulosome' and sampla_projet_id=projet_id
) AS TEMPTEMP)
;

delete from project_attribute where projea_projet_id in (
select * from
(select DISTINCT projet_id from project
where projet_name='Cellulosome'
) AS TEMPTEMP)
;

-- Then the meta attributes.
delete from project_meta_attribute where projma_id in (
select * from
(select projma_id from project_meta_attribute, project
where projma_projet_id=projet_id and projet_name='Cellulosome') AS TEMPTEMP
);


delete from sample_meta_attribute where sampma_id in (
select * from
(select sampma_id from sample_meta_attribute, project
where sampma_projet_id=projet_id and projet_name='Cellulosome') AS TEMPTEMP
);


delete from event_meta_attribute where evenma_id in (
select * from
(select evenma_id from event_meta_attribute, project
where evenma_projet_id=projet_id and projet_name='Cellulosome') AS TEMPTEMP
);


-- Next eliminate the events relevant to the project.
delete from event where event_id in (
select * from
(select event_id from event, project where event_projet_id=projet_id and projet_name='Cellulosome') AS TEMPTEMP
);

-- Now the samples
delete from sample where sample_id in (
select sample_id from
(select * from sample, project where sample_projet_id=projet_id and projet_name='Cellulosome') AS TEMPTEMP
);

-- The project itself.
delete from project where projet_name='Cellulosome';

-- END

--  UPGRADING VAL REQUIREMENTS
--  This is required to be run against the database, prior to use of the new Attribute instead of sub-types.
update lookup_value set lkuvlu_type='Attribute'
  where lkuvlu_type like '% Attribute';
select * from lookup_value where lkuvlu_type='Attribute';

-- Shows all the event attributes for project given.
select * from event_attribute EA, project P, event E
where EA.eventa_event_id=E.event_id and E.event_projet_id=P.projet_id and P.projet_name='Cellulosome';

-- Shows all event meta attributes for project given.
select * from event_meta_attribute EMA, lookup_value LV, project P
where EMA.evenma_lkuvlu_attribute_id = LV.lkuvlu_id
 and P.projet_name='Cellulosome' and EMA.evenma_projet_id = P.projet_id;

-- This changes the wgs accession for the given sample to the value given.
update event_attribute set eventa_attribute_str='AEUZ00000000'
where eventa_id IN
(
select * from
(select ea.eventa_id
from event_attribute ea, lookup_value lv
where
lv.lkuvlu_id = ea.eventa_lkuvlu_attribute_id
and
lkuvlu_name = 'wgs accession'
and
ea.eventa_event_id IN
(
select e.event_id from sample s, event e
where s.sample_id=e.event_sampl_id
   and s.sample_name='sur'
)
) AS TEMPTEMP
)


-- This query will find the wgs accession, which happens to be an event attribute, for sample "sur".
select ea.eventa_attribute_str, lv.lkuvlu_name, 'sur' from event_attribute ea, lookup_value lv
where
lv.lkuvlu_id = ea.eventa_lkuvlu_attribute_id
and
lkuvlu_name = 'wgs accession'
and
ea.eventa_event_id IN
(
select e.event_id from sample s, event e
where s.sample_id=e.event_sampl_id
   and s.sample_name='sur'
)
;


-- This query will find all the attributes named "run date".  There is a link required back to the lookup value, in order
-- to query by name.
select * from ifx_projects.lookup_value LV, ifx_projects.event_attribute EA
where LV.lkuvlu_id=EA.eventa_lkuvlu_attribute_id and LV.lkuvlu_name='run date';

-- Find full lookup value info on event meta attributes for the project whose name is given.
select * from event_meta_attribute EMA, project P
where EMA.evenma_projet_id=P.projet_id   AND P.projet_name='Cellulosome';

-- Find out the names associated with all event meta attributes setup for the project whose name is given.
select * from lookup_value,event_meta_attribute,project where lkuvlu_type='Event Attribute'
and evenma_projet_id=project.projet_id and evenma_lkuvlu_attribute_id=lkuvlu_id
and projet_name='Yersinia pestis';

-- Find out what event meta attributes are NOT associated with the project whose names are given.
select * from lookup_value where lkuvlu_type='Event Attribute' AND NOT lkuvlu_name in (
select lkuvlu_name from lookup_value,event_meta_attribute,project where lkuvlu_type='Event Attribute'
and evenma_projet_id=project.projet_id and evenma_lkuvlu_attribute_id=lkuvlu_id
and projet_name='Yersinia pestis' );

-- This tells all attributes for a sample:
select * from ifx_projects.sample, ifx_projects.sample_attribute, ifx_projects.lookup_value
  where sample_name='gstec01'
  AND ifx_projects.sample_attribute.sampla_sample_id=sample.sample_id
  AND sampla_lkuvlu_attribute_id=lkuvlu_id;

-- ====================================== SECURING A PROJECT
--  Here, set the non-public project Werewolf to secured, gave it both an Edit and a View group.  However,
--  The Edit group is not being used.  Instead, using a universal access group.  If you are in for view,
--  you may also edit.
select * from project where projet_name='Werewolf';

update project set projet_is_secure=1 where projet_name='Werewolf';

-- These IDs were generated using a web browser and this URL http://guid/guid/GuidClientServer?Request=GET&Size=1
insert into lookup_value values( 1130106189942, 'Werewolf', 'Access Group', 'string', '2011-06-08 13:57', null );
insert into lookup_value values( 1130121174632, 'Werewolf-Edit', 'Edit Group', 'string', '2011-06-08 13:59', null );

select * from lookup_value where lkuvlu_type like '%Group%'

select * from project where projet_name='Werewolf';

-- The group ID generated using a web browser and this URL http://guid/guid/GuidClientServer?Request=GET&Size=1
insert into ifx_projects.group values (1130098580025, 1130106189942);

-- NOTE: same group ID for edit or view.
update project set view_group=1130098580025, edit_group=1130098580025 where projet_name='Werewolf';


--  ============================== SECURING A PROJECT AND ADDING MYSELF AS USER
--    I had to generate serveral GUIDs.  One for each new lookup value, one for each new group.
select * from ifx_projects.project where projet_name='Cellulosome';

update ifx_projects.project set projet_is_secure=1 where projet_name='Cellulosome';

select * from ifx_projects.group;

insert into ifx_projects.lookup_value values( 1130175784000, 'Cellulosome', 'Access Group', 'string', '2011-06-11 12:37', null );
insert into ifx_projects.lookup_value values( 1130175784001, 'Cellulosome-Edit', 'Edit Group', 'string', '2011-06-11 12:37', null );

insert into ifx_projects.group values(1130116680436,1130175784000);
insert into ifx_projects.group values(1130116680437,1130175784001);


update ifx_projects.project set projet_edit_group_id=1130116680436, projet_view_group_id=1130116680437
 where projet_name='Cellulosome';


select * from ifx_projects.actor_group;

select * from ifx_projects.actor where actor_username='lfoster';

insert into ifx_projects.actor_group values( 1130122071742, '2011-06-11 12:43', null, 4461009, 1130116680436);
insert into ifx_projects.actor_group values( 1130116680438, '2011-06-11 12:43', null, 4461009, 1130116680437);

--  ============================== BACKING OUT A PROJECT
-- Here's how I cleaned up a mis-loaded project to completely reload it.


-- NOTE: you really have to have the TEMPTEMP table in there to beat
-- "you can't specify target table" error 1093 in MySQL
delete from event_attribute where eventa_id in (
select * from
(select DISTINCT EAI.eventa_id from event_attribute EAI, project, event
where event_projet_id = projet_id and projet_name='Yersinia pestis' and eventa_event_id=event_id
) AS TEMPTEMP);

delete from sample_attribute where sampla_id in (
select * from
(select DISTINCT sampla_id from sample_attribute, project, sample
where sample_projet_id = projet_id and projet_name='Yersinia pestis' and sampla_projet_id=projet_id
) AS TEMPTEMP)
;

delete from project_attribute where projea_projet_id in (
select * from
(select DISTINCT projet_id from project
where projet_name='Yersinia pestis'
) AS TEMPTEMP)
;

-- Then the meta attributes.
delete from project_meta_attribute where projma_id in (
select * from
(select projma_id from project_meta_attribute, project
where projma_projet_id=projet_id and projet_name='Yersinia pestis') AS TEMPTEMP
);


delete from sample_meta_attribute where sampma_id in (
select * from
(select sampma_id from sample_meta_attribute, project
where sampma_projet_id=projet_id and projet_name='Yersinia pestis') AS TEMPTEMP
);


delete from event_meta_attribute where evenma_id in (
select * from
(select evenma_id from event_meta_attribute, project
where evenma_projet_id=projet_id and projet_name='Yersinia pestis') AS TEMPTEMP
);


-- Next eliminate the events relevant to the project.
delete from event where event_id in (
select * from
(select event_id from event, project where event_projet_id=projet_id and projet_name='Yersinia pestis') AS TEMPTEMP
);

-- Now the samples
delete from sample where sample_id in (
select sample_id from
(select * from sample, project where sample_projet_id=projet_id and projet_name='Yersinia pestis') AS TEMPTEMP
);

-- The project itself.
delete from project where projet_name='Yersinia pestis';

-- Lookup values?
delete from lookup_value where lkuvlu_name IN (
'Project Group','Project Leader','Project Name','Project Species','Strain/isolate/breed');

delete from lookup_value where lkuvlu_name IN (
'Complete','Grant','Organism'
);

delete from lookup_value where lkuvlu_name IN (
'Taxonomy ID');

delete from lookup_value where lkuvlu_name IN ('Object Type','End Accession');

delete from lookup_value where lkuvlu_name IN ('Start Accession','run date');

-- Some things needed for exploration and implementation of re-discovered requirement: synch of EMA with PMA/SMA.
select * from lookup_value;

select * from event_meta_attribute ema,lookup_value lv
where ema.evenma_lkuvlu_attribute_id=lv.lkuvlu_id
 and lv.lkuvlu_type="Event Type";


select * from lookup_value where lkuvlu_name='ProjectStatus';



select * from project_meta_attribute PMA, project P
where P.projet_name='Cellulosome'
  AND P.projet_id=PMA.projma_projet_id;

select * from project where projet_name='Cellulosome';

select * from event_meta_attribute EMA, project P
where  P.projet_id=EMA.evenma_projet_id AND P.projet_name='Cellulosome' order by P.projet_name;



select * from event_meta_attribute EMA, lookup_value LV, event_attribute EA
where LV.lkuvlu_name='ProjectDescription'
 AND EA.eventa_lkuvlu_attribute_id=LV.lkuvlu_id
  AND EMA.evenma_lkuvlu_attribute_id=LV.lkuvlu_id;




select * from event_meta_attribute EMA, lookup_value LV, event_attribute EA
where EA.eventa_lkuvlu_attribute_id=LV.lkuvlu_id
  AND EMA.evenma_lkuvlu_attribute_id=LV.lkuvlu_id;


select * from project_meta_attribute PMA, lookup_value LV, project_attribute PA
where LV.lkuvlu_name='ProjectDescription'
 AND PA.projea_lkuvlu_attribute_id=LV.lkuvlu_id
  AND PMA.projma_lkuvlu_attribute_id=LV.lkuvlu_id;


select * from event_attribute EA, lookup_value LV
where LV.lkuvlu_id=EA.eventa_lkuvlu_attribute_id AND LV.lkuvlu_name='reference';

select * from project_attribute SA, lookup_value LV
where LV.lkuvlu_id=SA.projea_lkuvlu_attribute_id AND LV.lkuvlu_name='reference';

update lookup_value set lkuvlu_data_type='date' where lkuvlu_name='Expected Duration';

select * from event E, lookup_value LV
where E.event_type_lkuvl_id=LV.lkuvlu_id and LV.lkuvlu_name='ProjectRegistration';


select * from event E, lookup_value LV, event_meta_attribute EMA
where E.event_type_lkuvl_id=LV.lkuvlu_id and LV.lkuvlu_name='ProjectRegistration'
  and EMA.evenma_lkuvlu_attribute_id=LV.lkuvlu_id;


select * from event E, lookup_value LV, project_meta_attribute PMA
where E.event_type_lkuvl_id=LV.lkuvlu_id and LV.lkuvlu_name='Short Read Archive'
  and PMA.projma_lkuvlu_attribute_id=LV.lkuvlu_id;

select * from event E, lookup_value LV, event_meta_attribute EMA
where E.event_type_lkuvl_id=LV.lkuvlu_id and LV.lkuvlu_name='Short Read Archive'
  and EMA.evenma_lkuvlu_attribute_id=LV.lkuvlu_id;

select * from lookup_value where lkuvlu_type='Event Type';


-- ============================================================================================
--                       Finding all event meta attributes for project Werewolf, and for
--                       the WGS event type.
select *
from event_meta_attribute EMA, project P, lookup_value L
where L.lkuvlu_name='WGS' and P.projet_name='Werewolf'
  and EMA.evenma_event_type_lkuvl_id=L.lkuvlu_id
  and P.projet_id=EMA.evenma_projet_id;


select * from sample_meta_attribute SMA, lookup_value L
where SMA.sampma_lkuvlu_attribute_id=L.lkuvlu_id
order by SMA.sampma_projet_id;


-- For the Sample Registration event.
select P.projet_name AS "Project Name", L.lkuvlu_name AS "Event Name"
from event_meta_attribute EMA, project P, lookup_value L, sample_meta_attribute SMA
where L.lkuvlu_name ='SampleRegistration' and P.projet_name like '%'
  and EMA.evenma_event_type_lkuvl_id=L.lkuvlu_id
  and P.projet_id=EMA.evenma_projet_id
  and P.projet_id=SMA.sampma_projet_id
  and SMA.sampma_lkuvlu_attribute_id=EMA.evenma_lkuvlu_attribute_id;

-- Triplet of values
select LA.lkuvlu_name AS "Attribute Name", LE.lkuvlu_name AS "Event Name", P.projet_name AS "Project Name"
from event_meta_attribute EMA, project P, lookup_value LE, lookup_value LA,
     sample_meta_attribute SMA
where LE.lkuvlu_name ='SampleRegistration' and P.projet_name = 'MRSA'
  and EMA.evenma_event_type_lkuvl_id=LE.lkuvlu_id
  and P.projet_id=EMA.evenma_projet_id
  and P.projet_id=SMA.sampma_projet_id
  and SMA.sampma_lkuvlu_attribute_id=EMA.evenma_lkuvlu_attribute_id
  and LA.lkuvlu_id=SMA.sampma_lkuvlu_attribute_id;


-- ===================================================================================================
--  Backed out a unique constraint on a foreign key from project to group, so could point multiple
--  projects at the same group

--  First make the temporary copy of the index...
alter table ifx_projects.project
add index projet_edit_group_temp_ind USING BTREE ( projet_edit_group_id ASC);

--  Next delete the original index.
alter table ifx_projects.project
drop index projet_edit_group_ind;

--  Add back the index to retain the original name.
alter table ifx_projects.project
add index projet_edit_group_ind USING BTREE ( projet_edit_group_id ASC);

-- Get rid of the temporary copy.
alter table ifx_projects.project
drop index projet_edit_group_temp_ind;

-- This was carried out for both edit and view groups.
