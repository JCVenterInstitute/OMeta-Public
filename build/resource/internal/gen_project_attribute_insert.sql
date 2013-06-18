-- PROJECT_ATTRIBUTE
-- select pa.* from ifx_projects. project p, ifx_projects. project_attribute pa where p.projet_is_public = 1 and p.projet_id = pa.projea_projet_id; 
SELECT
        'INSERT INTO ifx_projects.project_attribute ( projea_id, projea_projet_id, projea_lkuvlu_attribute_id, projea_attribute_date, projea_attribute_str, projea_attribute_float, projea_attribute_int, projea_actor_created_by, projea_create_date, projea_actor_modified_by, projea_modified_date ) VALUES ( ',
        projea_id,  ",",
        projea_projet_id, ",",
        projea_lkuvlu_attribute_id, ",",
        CONCAT('\"',projea_attribute_date,'\"') , ",",
        CONCAT('\"',projea_attribute_str,'\"'), ",",
        projea_attribute_float, ", ",
        projea_attribute_int, ", ",
        projea_actor_created_by, ", ",
        CONCAT('\"',projea_create_date,'\"'), ",",
        projea_actor_modified_by, ", ",
        CONCAT('\"',projea_modified_date,'\"'),
        ' ) ;'
FROM ifx_projects.project p, ifx_projects.project_attribute pa
WHERE p.projet_id = pa.projea_projet_id
; 
