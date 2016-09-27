/*
created: 10/18/2010
modified: 10/25/2010
model: re mysql 5.1
database: mysql 5.1
*/

-- create tables section -------------------------------------------------

-- table actor

create table actor
(
  actor_login_id varchar(10) not null,
  actor_first_name varchar(30) not null,
  actor_last_name varchar(30) not null,
  actor_middle_name varchar(30),
  actor_email_address varchar(50),
  actor_acgrp_id bigint
) engine = innodb
  row_format = compact
;

alter table actor add primary key (actor_login_id)
;

create index actor_information using btree on actor (actor_login_id,actor_first_name,actor_last_name)
;

create index actor_actor_group_fk using btree on actor (actor_acgrp_id)
;

-- table actorgroup

create table actor_group
(
  actgrp_id bigint not null,
  actgrp_create_date date not null,
  actgrp_modify_date date,
  actgrp_group_name_lkuvl_id bigint not null
) engine = innodb
  row_format = compact
;

alter table actorgroup add primary key (actgrp_id)
;

create unique index acgrp_id using btree on actorgroup (actgrp_id)
;

create index grp_actor_ids using btree on actorgroup (actgrp_id,actgrp_group_name_lkuvl_id)
;

-- table event

create table event
(
  event_id bigint not null,
  event_project_id bigint not null,
  event_type_lkuvl_id bigint,
  event_actor_created_by bigint not null,
  event_create_date date not null,
  event_actor_modified_by bigint,
  event_modified_date date,
  event_status_lkuvl_id bigint
) engine = innodb
  row_format = compact
;

alter table event add primary key (event_id)
;

create unique index event_id using btree on event (event_id)
;

create index ids using btree on event (event_id,event_project_id,event_actor_created_by,event_actor_modified_by,event_type_lkuvl_id,event_status_lkuvl_id)
;

create index event_projec_id_fk using btree on event (event_project_id)
;

-- table event_attribute

create table event_attribute
(
  eventa_id bigint not null,
  eventa_lkuvlu_attribute_id bigint not null,
  eventa_event_id bigint not null,
  eventa_attribute_date date,
  eventa_attribute_float float,
  eventa_attribute_str varchar(4000),
  eventa_actor_created_by varchar(10),
  eventa_actor_modified_by varchar(10),
  eventa_create_date date,
  eventa_modified_date date
) engine = innodb
  row_format = compact
;

alter table event_attribute add primary key (eventa_id)
;

create unique index eventa_id using btree on event_attribute (eventa_id)
;

create index ids using btree on event_attribute (eventa_id,eventa_lkuvlu_attribute_id,eventa_event_id,eventa_actor_created_by)
;

create index eventa_event_id_fk using btree on event_attribute (eventa_event_id)
;

create index eventa_lkuvl_attribute_id_fk using btree on event_attribute (eventa_lkuvlu_attribute_id)
;

-- table event_meta_attribute

create table event_meta_attribute
(
  evenma_id bigint not null,
  evenma_project_id bigint not null,
  evenma_event_type_lkuvl_id bigint not null,
  evenma_lkuvlu_attribute_id bigint not null,
  evenma_is_required int,
  evenma_desc varchar(1000),
  evenma_options varchar(1000),
  evenma_create_date date not null,
  evenma_actor_created_by varchar(10) not null,
  evenma_actor_modified_by varchar(10),
  evenma_modified_date date
) engine = innodb
  row_format = compact
;

alter table event_meta_attribute add primary key (evenma_id)
;

create unique index evenma_id using btree on event_meta_attribute (evenma_id)
;

create index ids using btree on event_meta_attribute (evenma_id,evenma_lkuvlu_attribute_id)
;

create index event_project_id_fk using btree on event_meta_attribute (evenma_project_id)
;

create index event_lkuvlu_attribute_id_fk using btree on event_meta_attribute (evenma_lkuvlu_attribute_id)
;

create index evenma_lkuvlu_event_type_fk using btree on event_meta_attribute (evenma_event_type_lkuvl_id)
;

-- table lookup_value

create table lookup_value
(
  lkuvlu_id bigint not null,
  lkuvlu_name varchar(40) not null,
  lkuvlu_value varchar(255),
  lkuvlu_type varchar(255)
) engine = innodb
  row_format = compact
;

alter table lookup_value add primary key (lkuvlu_id)
;

create unique index klvl_name using btree on lookup_value (lkuvlu_name)
;

create unique index luvl_id using btree on lookup_value (lkuvlu_id)
;

create index lkuvl_information using btree on lookup_value (lkuvlu_id,lkuvlu_name,lkuvlu_type)
;

-- table project

create table project
(
  projet_id bigint not null,
  projet_name varchar(255),
  projet_projet_parent_id bigint,
  projet_create_date date not null,
  projet_actor_created_by varchar(10) not null,
  projet_actor_modified_by varchar(10),
  projet_modified_date date,
  projet_level int
) engine = innodb
  row_format = compact
;

alter table project add primary key (projet_id)
;

create unique index project_id using btree on project (projet_id)
;

create index proje_information using btree on project (projet_id,projet_name,projet_projet_parent_id,projet_create_date,projet_actor_created_by,projet_level)
;

create index project_parent_project_id_fk using btree on project (projet_projet_parent_id)
;

-- table project_attribute

create table project_attribute
(
  projea_id bigint not null,
  projea_projet_id bigint not null,
  projea_lkuvlu_attribute_id bigint not null,
  projea_attribute_data_date date,
  projea_attribute_data_str varchar(4000),
  projea_attribute_data_float float,
  projea_actor_created_by varchar(10) not null,
  projea_create_date date not null,
  projea_actor_modified_by varchar(10),
  projea_modified_date date
) engine = innodb
  row_format = compact
;

alter table project_attribute add primary key (projea_id)
;

create unique index proja_id using btree on project_attribute (projea_id)
;

create index ids using btree on project_attribute (projea_projet_id,projea_id,projea_lkuvlu_attribute_id)
;

create index projea_lkuvlu_attribute_id_fk using btree on project_attribute (projea_lkuvlu_attribute_id)
;

-- table project_meta_attribute

create table project_meta_attribute
(
  projma_id bigint not null,
  projma_project_id bigint not null,
  projma_lkuvlu_attribute_id bigint not null,
  projma_is_required int,
  projma_options varchar(4000),
  projma_data_type varchar(10),
  projma_attribute_desc varchar(1000),
  projma_actor_created_by varchar(10) not null,
  projma_create_date date not null,
  projma_modified_date date,
  projma_actor_modified_by varchar(10)
) engine = innodb
  row_format = compact
;

alter table project_meta_attribute add primary key (projma_id)
;

create unique index key1 using btree on project_meta_attribute (projma_project_id)
;

create unique index projma_attribute_name using btree on project_meta_attribute (projma_lkuvlu_attribute_id)
;

create unique index projma_id using btree on project_meta_attribute (projma_id)
;

create index ids using btree on project_meta_attribute (projma_id,projma_project_id,projma_lkuvlu_attribute_id,projma_actor_created_by,projma_options)
;

-- table sample

create table sample
(
  sample_id bigint not null,
  sample_project_id bigint not null,
  sample_name varchar(255) not null,
  sample_created_by varchar(10) not null,
  sample_create_date date not null,
  sample_modified_by varchar(10),
  sample_modified_date date
) engine = innodb
  row_format = compact
;

alter table sample add primary key (sample_id)
;

create unique index sample_id using btree on sample (sample_id)
;

create index sample_information using btree on sample (sample_project_id,sample_id,sample_name,sample_created_by,sample_create_date)
;

-- table sample_attribute

create table sample_attribute
(
  sampla_id bigint not null,
  sampla_project_id bigint not null,
  sampla_lkuvlu_attribute_id bigint not null,
  sampla_sample_id bigint not null,
  sampla_attribute_date date,
  sampla_attribute_float float,
  sampla_attribute_str varchar(4000),
  sampla_actor_created_by varchar(10),
  sampla_actor_modified_by varchar(10),
  sampla_create_date date,
  sampla_modified_date date
) engine = innodb
  row_format = compact
;

alter table sample_attribute add primary key (sampla_id)
;

create unique index sampla_id using btree on sample_attribute (sampla_id)
;

create index sampla_information using btree on sample_attribute (sampla_project_id,sampla_id,sampla_lkuvlu_attribute_id,sampla_sample_id,sampla_attribute_date,sampla_attribute_float,sampla_attribute_str,sampla_actor_created_by,sampla_create_date)
;

create index sampla_sample_id_fk using btree on sample_attribute (sampla_sample_id)
;

create index sampla_lkuvl_attribute_id_fk using btree on sample_attribute (sampla_lkuvlu_attribute_id)
;

-- table sample_meta_attribute

create table sample_meta_attribute
(
  sampma_id bigint not null,
  sampma_project_id bigint not null,
  sampma_lkuvlu_attribute_id bigint not null,
  sampma_is_required int,
  sampma_options varchar(4000),
  sampma_data_type varchar(10),
  sampma_attribute_desc varchar(1000),
  sampma_actor_created_by varchar(10),
  sampma_actor_modified_by varchar(10),
  sampma_create_date date,
  sampma_modified_date date
) engine = innodb
  row_format = compact
;

alter table sample_meta_attribute add primary key (sampma_id)
;

create unique index sampma_attribute_name using btree on sample_meta_attribute (sampma_lkuvlu_attribute_id)
;

create unique index sampma_id using btree on sample_meta_attribute (sampma_id)
;

create index eventma_ids using btree on sample_meta_attribute (sampma_id,sampma_project_id,sampma_lkuvlu_attribute_id,sampma_data_type,sampma_actor_created_by,sampma_create_date,sampma_options)
;

create index sampma_project_id using btree on sample_meta_attribute (sampma_project_id)
;

-- create relationships section ------------------------------------------------- 

alter table actor add constraint actor_actor_group_fk foreign key (actor_acgrp_id) references actorgroup (acgrp_id) on delete restrict on update restrict
;

alter table event add constraint event_projec_id_fk foreign key (event_project_id) references project (projet_id) on delete restrict on update restrict
;

alter table event_attribute add constraint eventa_lkuvl_attribute_id_fk foreign key (eventa_lkuvlu_attribute_id) references lookup_value (lkuvlu_id) on delete restrict on update restrict
;

alter table event_attribute add constraint eventa_event_id_fk foreign key (eventa_event_id) references event (event_id) on delete restrict on update restrict
;

alter table event_meta_attribute add constraint evenma_lkuvlu_event_type_fk foreign key (evenma_event_type_lkuvl_id) references lookup_value (lkuvlu_id) on delete restrict on update restrict
;

alter table event_meta_attribute add constraint event_lkuvlu_attribute_id_fk foreign key (evenma_lkuvlu_attribute_id) references lookup_value (lkuvlu_id) on delete restrict on update restrict
;

alter table event_meta_attribute add constraint event_project_id_fk foreign key (evenma_project_id) references project (projet_id) on delete restrict on update restrict
;

alter table project add constraint project_parent_project_id_fk foreign key (projet_projet_parent_id) references project (projet_id) on delete restrict on update restrict
;

alter table project_attribute add constraint projea_lkuvlu_attribute_id_fk foreign key (projea_lkuvlu_attribute_id) references lookup_value (lkuvlu_id) on delete restrict on update restrict
;

alter table project_attribute add constraint relationship11 foreign key (projea_projet_id) references project (projet_id) on delete restrict on update restrict
;

alter table project_meta_attribute add constraint proje_lkuvlu_attribute_id_fk foreign key (projma_lkuvlu_attribute_id) references lookup_value (lkuvlu_id) on delete restrict on update restrict
;

alter table project_meta_attribute add constraint projma_project_id_fk foreign key (projma_project_id) references project (projet_id) on delete restrict on update restrict
;

alter table sample add constraint relationship12 foreign key (sample_project_id) references project (projet_id) on delete restrict on update restrict
;

alter table sample_attribute add constraint sampla_lkuvl_attribute_id_fk foreign key (sampla_lkuvlu_attribute_id) references lookup_value (lkuvlu_id) on delete restrict on update restrict
;

alter table sample_attribute add constraint sampla_projec_id_fk foreign key (sampla_project_id) references project (projet_id) on delete restrict on update restrict
;

alter table sample_attribute add constraint sampla_sample_id_fk foreign key (sampla_sample_id) references sample (sample_id) on delete restrict on update restrict
;

alter table sample_meta_attribute add constraint sampma_lkuvlu_attribute_id_fk foreign key (sampma_lkuvlu_attribute_id) references lookup_value (lkuvlu_id) on delete restrict on update restrict
;

alter table sample_meta_attribute add constraint sampma_project_id foreign key (sampma_project_id) references project (projet_id) on delete restrict on update restrict
;


