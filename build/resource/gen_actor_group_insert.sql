SELECT
        'INSERT INTO actor_group ( actgrp_id, actgrp_create_date, actgrp_modify_date, actgrp_actor_id, actgrp_group_id ) VALUES ( ',
        actgrp_id,  ",",
        CONCAT('\"',actgrp_create_date,'\"') , ",",
        CONCAT('\"',actgrp_modify_date,'\"') , ",",
        actgrp_actor_id,  ",",
        actgrp_group_id,
        ' ) ON DUPLICATE KEY UPDATE ',
        'actgrp_create_date=',CONCAT('\"',actgrp_create_date,'\"') , ',actgrp_modify_date=',CONCAT('\"',actgrp_modify_date,'\"') ,
        ',actgrp_actor_id=',actgrp_actor_id,  ',actgrp_group_id=',actgrp_group_id,';'
FROM actor_group
WHERE (date(actgrp_create_date)=CURRENT_DATE()-INTERVAL 1 DAY or date(actgrp_modify_date)=CURRENT_DATE()-INTERVAL 1 DAY or
  date(actgrp_create_date)=CURRENT_DATE() or date(actgrp_modify_date)=CURRENT_DATE())
;