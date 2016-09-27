-- LOOKUP_VALUE
-- select * from ifx_projects.lookup_value;
SELECT
        'INSERT INTO lookup_value ( lkuvlu_id, lkuvlu_name, lkuvlu_type, lkuvlu_data_type, lkuvlu_create_date, lkuvlu_modify_date ) VALUES ( ',
                lkuvlu_id,  ",",
                CONCAT('\"',lkuvlu_name,'\"') , ",",
                CONCAT('\"',lkuvlu_type,'\"'), ",",
                CONCAT('\"',lkuvlu_data_type,'\"'), ",",
                CONCAT('\"',lkuvlu_create_date,'\"'), ",",
                CONCAT('\"',lkuvlu_modify_date,'\"'),
                ' ) ON DUPLICATE KEY UPDATE ',
                'lkuvlu_name=',CONCAT('\"',lkuvlu_name,'\"') , ',lkuvlu_type=',CONCAT('\"',lkuvlu_type,'\"'),
                ',lkuvlu_data_type=',CONCAT('\"',lkuvlu_data_type,'\"'), ',lkuvlu_create_date=',CONCAT('\"',lkuvlu_create_date,'\"'),
                ',lkuvlu_modify_date=',CONCAT('\"',lkuvlu_modify_date,'\"'),';'
FROM ifx_projects.lookup_value
;
