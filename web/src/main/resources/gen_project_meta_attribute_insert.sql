-- PROJECT_META_ATTRIBUTE
-- select pma.* from  project p,  project_meta_attribute pma where p.projet_is_public = 1 and p.projet_id = pma.projma_projet_id;
SELECT
        'INSERT INTO project_meta_attribute ( projma_id, projma_projet_id, projma_lkuvlu_attribute_id, projma_is_required, projma_options, projma_attribute_desc, projma_actor_created_by, projma_create_date, projma_modified_date, projma_actor_modified_by, projma_is_active, projma_ontology ) VALUES ( ',
        projma_id,  ",",
        projma_projet_id,  ",",
        projma_lkuvlu_attribute_id,  ",",
        projma_is_required,  ",",
        CONCAT('\"',projma_options,'\"') , ",",
        CONCAT('\"',projma_attribute_desc,'\"') , ",",
        projma_actor_created_by, ", ",
        CONCAT('\"',projma_create_date,'\"') , ",",
        CONCAT('\"',projma_modified_date,'\"') , ",",
        projma_actor_modified_by, ", ",
        projma_is_active, ", ",
        CONCAT('\"',projma_ontology,'\"'),
        ' ) ON DUPLICATE KEY UPDATE ',
        'projma_projet_id=',projma_projet_id,
        ',projma_lkuvlu_attribute_id=',projma_lkuvlu_attribute_id, ',projma_is_required=',projma_is_required,
        ',projma_options=',CONCAT('\"',projma_options,'\"'), ',projma_attribute_desc=',CONCAT('\"',projma_attribute_desc,'\"'),
        ',projma_actor_created_by=',projma_actor_created_by, ',projma_create_date=',CONCAT('\"',projma_create_date,'\"'),
        ',projma_modified_date=',CONCAT('\"',projma_modified_date,'\"'), ',projma_actor_modified_by=',projma_actor_modified_by,
        ',projma_is_active=',projma_is_active, ',projma_ontology=',CONCAT('\"',projma_ontology,'\"'),';'
FROM project p, project_meta_attribute pma
WHERE p.projet_is_public = 1 and p.projet_id = pma.projma_projet_id
and (date(pma.projma_create_date)=CURRENT_DATE()-INTERVAL 1 DAY or date(pma.projma_modified_date)=CURRENT_DATE()-INTERVAL 1 DAY or
  date(pma.projma_create_date)=CURRENT_DATE() or date(pma.projma_modified_date)=CURRENT_DATE())
; 

