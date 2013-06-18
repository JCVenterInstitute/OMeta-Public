-- SAMPLE
-- select s.* from  project p,  sample s where p.projet_is_public = 1 and p.projet_id = s.sample_projet_id;
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
FROM project p, sample s
WHERE p.projet_is_public = 1 and p.projet_id = s.sample_projet_id and s.sample_is_public = 1
and (date(s.sample_create_date)=CURRENT_DATE()-INTERVAL 1 DAY or date(s.sample_modified_date)=CURRENT_DATE()-INTERVAL 1 DAY or
  date(s.sample_create_date)=CURRENT_DATE() or date(s.sample_modified_date)=CURRENT_DATE())
;

