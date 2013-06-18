-- GROUP
-- select * from ifx_projects.group;
SELECT
        'INSERT INTO ifx_projects.groups ( group_id, group_name_lkuvl_id ) VALUES ( ',
        group_id,  ",",
        group_name_lkuvl_id,
        ' ) ;'
FROM ifx_projects.groups
;