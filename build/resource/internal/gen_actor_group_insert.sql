SELECT
        'INSERT INTO ifx_projects.actor_group ( actgrp_id, actgrp_create_date, actgrp_modify_date, actgrp_actor_id, actgrp_group_id ) VALUES ( ',
        actgrp_id,  ",",
        CONCAT('\"',actgrp_create_date,'\"') , ",",
        CONCAT('\"',actgrp_modify_date,'\"') , ",",
        actgrp_actor_id,  ",",
        actgrp_group_id,
        ' ) ;'
FROM ifx_projects.actor_group
;