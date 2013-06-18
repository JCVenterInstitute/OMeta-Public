-- EVENT
-- select e.* from ifx_projects. project p, ifx_projects. event e where p.projet_is_public = 1 and p.projet_id = e.event_projet_id; 
SELECT
        'INSERT INTO ifx_projects.event ( event_id, event_projet_id, event_type_lkuvl_id, event_actor_created_by, event_create_date, event_actor_modified_by, event_modified_date, event_status_lkuvl_id, event_sampl_id ) VALUES ( ',
        event_id,  ",",
        event_projet_id,  ",",
        event_type_lkuvl_id,  ",",
        event_actor_created_by,  ",",
        CONCAT('\"',event_create_date,'\"') , ",",
        event_actor_modified_by, ", ",
        CONCAT('\"',event_modified_date,'\"') , ",",
        event_status_lkuvl_id, ", ",
        event_sampl_id,
        ' ) ;'
FROM ifx_projects.project p, ifx_projects.event e
WHERE p.projet_id = e.event_projet_id
;

