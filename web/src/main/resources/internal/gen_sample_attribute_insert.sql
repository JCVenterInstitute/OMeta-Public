-- SAMPLE_ATTRIBUTE
-- select sa.* from dod_ometa. project p, dod_ometa. sample_attribute sa where p.projet_is_public = 1 and p.projet_id = sa.sampla_projet_id;
SELECT
        'INSERT INTO sample_attribute ( sampla_id, sampla_projet_id, sampla_lkuvlu_attribute_id, sampla_sample_id, sampla_attribute_date, sampla_attribute_float, sampla_attribute_str, sampla_attribute_int, sampla_actor_created_by, sampla_actor_modified_by, sampla_create_date, sampla_modified_date ) VALUES ( ',
                sampla_id, ",",
                sampla_projet_id,  ",",
                sampla_lkuvlu_attribute_id,  ",",
                sampla_sample_id,  ",",
                CONCAT('\"',sampla_attribute_date,'\"') , ",",
                sampla_attribute_float, ", ",
                CONCAT('\"',sampla_attribute_str,'\"') , ",",
                sampla_attribute_int, ", ",
                sampla_actor_created_by,  ",",
                sampla_actor_modified_by,  ",",
                CONCAT('\"',sampla_create_date,'\"') , ",",
                CONCAT('\"',sampla_modified_date,'\"'),
                ' ) ON DUPLICATE KEY UPDATE ',
                'sampla_projet_id=',sampla_projet_id,
                ',sampla_lkuvlu_attribute_id=',sampla_lkuvlu_attribute_id, ',sampla_sample_id=',sampla_sample_id,
                ',sampla_attribute_date=',CONCAT('\"',sampla_attribute_date,'\"'), ',sampla_attribute_float=',sampla_attribute_float,
                ',sampla_attribute_str=',CONCAT('\"',sampla_attribute_str,'\"'), ',sampla_attribute_int=',sampla_attribute_int,
                ',sampla_actor_created_by=',sampla_actor_created_by, ',sampla_actor_modified_by=',sampla_actor_modified_by,
                ',sampla_create_date=',CONCAT('\"',sampla_create_date,'\"'), ',sampla_modified_date=',CONCAT('\"',sampla_modified_date,'\"'),';'
FROM dod_ometa.project p, dod_ometa.sample_attribute sa
WHERE p.projet_id = sa.sampla_projet_id
;

