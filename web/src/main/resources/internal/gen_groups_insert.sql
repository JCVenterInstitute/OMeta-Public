-- GROUP
-- select * from dod_ometa.group;
SELECT
        'INSERT INTO groups ( group_id, group_name_lkuvl_id ) VALUES ( ',
                group_id,  ",",
                group_name_lkuvl_id,
                ' ) ON DUPLICATE KEY UPDATE ',
                'group_name_lkuvl_id=',group_name_lkuvl_id,';'
FROM dod_ometa.groups
;