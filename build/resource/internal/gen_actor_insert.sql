SELECT
        'INSERT INTO actor ( actor_username, actor_first_name, actor_last_name, actor_middle_name, actor_email_address, actor_id, actor_create_date, actor_modify_date ) VALUES ( ',
                CONCAT('\"',actor_username,'\"') , ",",
                CONCAT('\"',actor_first_name,'\"') , ",",
                CONCAT('\"',actor_last_name,'\"') , ",",
                CONCAT('\"',actor_middle_name ,'\"') , ",",
                CONCAT('\"',actor_email_address ,'\"') , ",",
                actor_id,  ",",
                CONCAT('\"',actor_create_date,'\"') , ",",
                CONCAT('\"',actor_modify_date,'\"') ,
                ' ) ON DUPLICATE KEY UPDATE ',
                'actor_username=',CONCAT('\"',actor_username,'\"') , ',actor_first_name=',CONCAT('\"',actor_first_name,'\"') ,
                ',actor_last_name=',CONCAT('\"',actor_last_name,'\"') , ',actor_middle_name=',CONCAT('\"',actor_middle_name ,'\"') ,
                ',actor_email_address=',CONCAT('\"',actor_email_address ,'\"') ,
                ',actor_create_date=',CONCAT('\"',actor_create_date,'\"') , ',actor_modify_date=',CONCAT('\"',actor_modify_date,'\"') ,';'
FROM ifx_projects.actor
;