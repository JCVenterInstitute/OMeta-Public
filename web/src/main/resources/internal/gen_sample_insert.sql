-- SAMPLE
-- select s.* from dod_ometa. project p, dod_ometa. sample s where p.projet_is_public = 1 and p.projet_id = s.sample_projet_id;
SELECT
        'INSERT INTO sample ( sample_id, sample_projet_id, sample_name, sample_created_by, sample_create_date, sample_modified_by, sample_modified_date, sample_is_public, sample_sample_parent_id, sample_level ) VALUES ( ',
                sample_id,  ",",
                sample_projet_id,  ",",
                CONCAT('\"',sample_name,'\"') , ",",
                sample_created_by, ", ",
                CONCAT('\"',sample_create_date,'\"') , ",",
                sample_modified_by, ", ",
                CONCAT('\"',sample_modified_date,'\"') , ",",
                sample_is_public, ",",
                sample_sample_parent_id, ",",
                sample_level,
                ' ) ON DUPLICATE KEY UPDATE ',
                'sample_projet_id=',sample_projet_id,',sample_projet_id=',sample_projet_id,
                ',sample_name=',CONCAT('\"',sample_name,'\"'),',sample_created_by=',sample_created_by,
                ',sample_create_date=',CONCAT('\"',sample_create_date,'\"'),',sample_modified_by=',sample_modified_by,
                ',sample_modified_date=',CONCAT('\"',sample_modified_date,'\"'),',sample_is_public=',sample_is_public,
                ',sample_sample_parent_id=',sample_sample_parent_id,',sample_level=',sample_level,';'
FROM dod_ometa.project p, dod_ometa.sample s
WHERE p.projet_id = s.sample_projet_id
;

