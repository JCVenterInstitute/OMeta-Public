-- EVENT_ATTRIBUTE
-- select ea.* from dod_ometa. project p, dod_ometa. event e, dod_ometa. event_attribute ea where p.projet_is_public = 1 and p.projet_id = e.event_projet_id and e.event_id = ea.eventa_event_id;
SELECT
        'INSERT INTO event_attribute ( eventa_id, eventa_lkuvlu_attribute_id, eventa_event_id, eventa_attribute_date, eventa_attribute_float, eventa_attribute_str, eventa_attribute_int, eventa_actor_created_by, eventa_actor_modified_by, eventa_create_date, eventa_modified_date ) VALUES ( ',
                eventa_id,  ",",
                eventa_lkuvlu_attribute_id,  ",",
                eventa_event_id,  ",",
                CONCAT('\"',eventa_attribute_date,'\"') , ",",
                eventa_attribute_float, ", ",
                CONCAT('\"',eventa_attribute_str,'\"') , ",",
                eventa_attribute_int, ", ",
                eventa_actor_created_by,  ",",
                eventa_actor_modified_by,  ",",
                CONCAT('\"',eventa_create_date,'\"') , ",",
                CONCAT('\"',eventa_modified_date,'\"'),
                ' ) ON DUPLICATE KEY UPDATE ',
                'eventa_lkuvlu_attribute_id=',eventa_lkuvlu_attribute_id, ',eventa_event_id=',eventa_event_id,
                ',eventa_attribute_date=',CONCAT('\"',eventa_attribute_date,'\"') , ',eventa_attribute_float=',eventa_attribute_float,
                ',eventa_attribute_str=',CONCAT('\"',eventa_attribute_str,'\"') , ',eventa_attribute_int=',eventa_attribute_int,
                ',eventa_actor_created_by=',eventa_actor_created_by, ',eventa_actor_modified_by=',eventa_actor_modified_by,
                ',eventa_create_date=',CONCAT('\"',eventa_create_date,'\"') , ',eventa_modified_date=',CONCAT('\"',eventa_modified_date,'\"'),';'
FROM dod_ometa.project p, dod_ometa.event e, dod_ometa.event_attribute ea
WHERE p.projet_id = e.event_projet_id and e.event_id = ea.eventa_event_id
;

