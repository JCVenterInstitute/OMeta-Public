-- PROJECT
-- select * from ifx_projects. project where projet_is_public = 1;
SELECT
        'INSERT INTO project ( projet_id, projet_name, projet_projet_parent_id, projet_create_date, projet_actor_created_by, projet_actor_modified_by, projet_modified_date, projet_level, projet_is_public, projet_view_group_id, projet_edit_group_id, projet_is_secure ) VALUES ( ',
                projet_id,  ",",
                CONCAT('\"',projet_name,'\"') , ",",
                projet_projet_parent_id, ", ",
                CONCAT('\"',projet_create_date,'\"'), ",",
                projet_actor_created_by, ", ",
                projet_actor_modified_by, ", ",
                CONCAT('\"',projet_modified_date,'\"'), ",",
                projet_level, ", ",
                projet_is_public,", ",
                projet_view_group_id,", ",
                projet_edit_group_id,", ",
                projet_is_secure,
                ' ) ON DUPLICATE KEY UPDATE ',
                'projet_name=',CONCAT('\"',projet_name,'\"'), ',projet_projet_parent_id=',projet_projet_parent_id,
                ',projet_create_date=',CONCAT('\"',projet_create_date,'\"'), ',projet_actor_created_by=',projet_actor_created_by,
                ',projet_actor_modified_by=',projet_actor_modified_by, ',projet_modified_date=',CONCAT('\"',projet_modified_date,'\"'),
                ',projet_level=',projet_level, ',projet_is_public=',projet_is_public,
                ',projet_view_group_id=',projet_view_group_id, ',projet_edit_group_id=',projet_edit_group_id,
                ',projet_is_secure=',projet_is_secure,';'
FROM ifx_projects.project

; 
