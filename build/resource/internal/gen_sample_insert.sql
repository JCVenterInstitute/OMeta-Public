-- SAMPLE
-- select s.* from ifx_projects. project p, ifx_projects. sample s where p.projet_is_public = 1 and p.projet_id = s.sample_projet_id;
SELECT
        'INSERT INTO ifx_projects.sample ( sample_id, sample_projet_id, sample_name, sample_created_by, sample_create_date, sample_modified_by, sample_modified_date, sample_is_public, sample_sample_parent_id, sample_level ) VALUES ( ',
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
        ' ) ;'
FROM ifx_projects.project p, ifx_projects.sample s
WHERE p.projet_id = s.sample_projet_id
;

