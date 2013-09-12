-- EVENT_META_ATTRIBUTE
-- select ema.* from i project p, i event_meta_attribute ema where p.projet_is_public = 1 and p.projet_id = ema.evenma_projet_id;
SELECT
        'INSERT INTO event_meta_attribute ( evenma_id, evenma_projet_id, evenma_event_type_lkuvl_id, evenma_lkuvlu_attribute_id, evenma_is_required, evenma_desc, evenma_options, evenma_create_date, evenma_actor_created_by, evenma_actor_modified_by, evenma_is_active, evenma_modified_date, evenma_is_sample_required, evenma_ontology, evenma_order ) VALUES ( ',
        evenma_id,  ",",
        evenma_projet_id,  ",",
        evenma_event_type_lkuvl_id,  ",",
        evenma_lkuvlu_attribute_id,  ",",
        evenma_is_required,  ",",
        CONCAT('\"',evenma_desc,'\"') , ",",
        CONCAT('\"',evenma_options,'\"') , ",",
        CONCAT('\"',evenma_create_date,'\"') , ",",
        evenma_actor_created_by, ", ",
        evenma_actor_modified_by, ", ",
        evenma_is_active , ",",
        CONCAT('\"',evenma_modified_date,'\"') ,",",
        evenma_is_sample_required ,",",
        CONCAT('\"',evenma_ontology,'\"') ,",",
        evenma_order,
        ' ) ON DUPLICATE KEY UPDATE ',
        'evenma_projet_id=',evenma_projet_id, ',evenma_event_type_lkuvl_id=',evenma_event_type_lkuvl_id,
        ',evenma_lkuvlu_attribute_id=',evenma_lkuvlu_attribute_id, ',evenma_is_required=',evenma_is_required,
        ',evenma_desc=',CONCAT('\"',evenma_desc,'\"') , ',evenma_options=',CONCAT('\"',evenma_options,'\"') ,
        ',evenma_create_date=',CONCAT('\"',evenma_create_date,'\"') , ',evenma_actor_created_by=',evenma_actor_created_by,
        ',evenma_actor_modified_by=',evenma_actor_modified_by, ',evenma_is_active=',evenma_is_active ,
        ',evenma_modified_date=',CONCAT('\"',evenma_modified_date,'\"') , ',evenma_is_sample_required=',evenma_is_sample_required,
        ',evenma_order=',evenma_order,';'
FROM project p, event_meta_attribute ema
WHERE p.projet_is_public = 1 and p.projet_id = ema.evenma_projet_id
and (date(ema.evenma_create_date)=CURRENT_DATE()-INTERVAL 1 DAY or date(ema.evenma_modified_date)=CURRENT_DATE()-INTERVAL 1 DAY or
  date(ema.evenma_create_date)=CURRENT_DATE() or date(ema.evenma_modified_date)=CURRENT_DATE())
;

