/*
Created: 5/5/2010
Modified: 10/6/2010
Model: Sybase ASE 15
Database: Sybase ASE 15
*/

-- Create tables section -------------------------------------------------

-- Table Project

CREATE TABLE [Project]
(
   [proje_id] bigint NOT NULL,
   [proje_name] varchar(255) NULL,
   [parent_project_id] bigint NULL,
   [proje_create_date] date NOT NULL,
   [proje_created_by] bigint NOT NULL,
   [proje_last_modified_by] bigint NULL,
   [proje_last_modified_date] date NULL,
   [proje_level] int NULL
)
go

CREATE INDEX [proje information] ON [Project] ([proje_id],[proje_name],[parent_project_id],[proje_create_date],[proje_created_by],[proje_level])
go

ALTER TABLE [Project] ADD CONSTRAINT [project_pk] PRIMARY KEY ([proje_id])
go

ALTER TABLE [Project] ADD CONSTRAINT [project_id] UNIQUE CLUSTERED ([proje_id])
go

-- Table Project_meta_attributes

CREATE TABLE [Project_meta_attributes]
(
   [projma_id] bigint NOT NULL,
   [projma_project_id] bigint NOT NULL,
   [projma_attribute_name] varchar(40) NOT NULL,
   [projma_attribute_options] varchar(4000) NULL,
   [projma_attribute_is_required] int NULL,
   [projma_attribute_data_type] varchar(10) NULL,
   [projma_attribute_desc] varchar(1000) NULL,
   [projma_attribute_created_by] bigint NOT NULL,
   [projma_attribute_create_date] date NOT NULL,
   [projma_attribute_modified_date] date NULL,
   [projma_attribute_modified_by] bigint NULL
)
 WITH IDENTITY_GAP = projma
go

CREATE INDEX [IDs] ON [Project_meta_attributes] ([projma_id],[projma_project_id],[projma_attribute_name],[projma_attribute_created_by],[projma_attribute_options])
go

ALTER TABLE [Project_meta_attributes] ADD CONSTRAINT [projma_pk] PRIMARY KEY NONCLUSTERED ([projma_id])
go

ALTER TABLE [Project_meta_attributes] ADD CONSTRAINT [Key1] UNIQUE ([projma_project_id])
go

ALTER TABLE [Project_meta_attributes] ADD CONSTRAINT [projma_attribute_name] UNIQUE CLUSTERED ([projma_attribute_name])
go

ALTER TABLE [Project_meta_attributes] ADD CONSTRAINT [projma_id] UNIQUE CLUSTERED ([projma_id])
go

-- Table Sample_meta_attributes

CREATE TABLE [Sample_meta_attributes]
(
   [sampma_id] bigint NOT NULL,
   [sampma_project_id] bigint NOT NULL,
   [sampma_attribute_name] varchar(40) NOT NULL,
   [sampma_attribue_options] varchar(4000) NULL,
   [sampma_attribute_is_required] int NULL,
   [sampma_attribute_data_type] varchar(10) NULL,
   [sampma_attribute_desc] varchar(1000) NULL,
   [sampma_created_by] bigint NULL,
   [sampma_modified_by] bigint NULL,
   [sampma_create_date] date NULL,
   [sampma_modified_date] date NULL
)
go

CREATE INDEX [eventma IDs] ON [Sample_meta_attributes] ([sampma_id],[sampma_project_id],[sampma_attribute_name],[sampma_attribute_data_type],[sampma_created_by],[sampma_create_date],[sampma_attribue_options])
go

ALTER TABLE [Sample_meta_attributes] ADD CONSTRAINT [Key2] PRIMARY KEY ([sampma_id])
go

ALTER TABLE [Sample_meta_attributes] ADD CONSTRAINT [sampma_attribute_name] UNIQUE CLUSTERED ([sampma_attribute_name])
go

ALTER TABLE [Sample_meta_attributes] ADD CONSTRAINT [sampma_id] UNIQUE CLUSTERED ([sampma_id])
go

-- Table Event_meta_attributes

CREATE TABLE [Event_meta_attributes]
(
   [evenma_id] bigint NOT NULL,
   [evenma_project_id] bigint NOT NULL,
   [evenma_event_type_lkuvl_id] bigint NULL,
   [evenma_attribute_type] bigint NULL,
   [evenma_attribute_name] varchar(50) NULL,
   [evenma_attribute_desc] varchar(1000) NULL,
   [evenma_attribute_options] varchar(1000) NULL,
   [evenma_attribute_create_date] date NOT NULL,
   [evenma_attribute_created_by] bigint NOT NULL,
   [evenma_attribute_modified_by] bigint NULL,
   [evenma_attribute_modified_date] date NULL,
   [evenma_attribute_is_required] int NULL
)
go

CREATE INDEX [IDs] ON [Event_meta_attributes] ([evenma_id],[evenma_attribute_name])
go

ALTER TABLE [Event_meta_attributes] ADD CONSTRAINT [Event_pk] PRIMARY KEY ([evenma_id])
go

ALTER TABLE [Event_meta_attributes] ADD CONSTRAINT [evenma_id] UNIQUE CLUSTERED ([evenma_id])
go

-- Table Event_attributes

CREATE TABLE [Event_attributes]
(
   [eventa_id] bigint NOT NULL,
   [eventa_event_type] bigint NULL,
   [eventa_attribute_name] varchar(50) NULL,
   [eventa_event_id] bigint NOT NULL,
   [eventa_attribute_date] date NULL,
   [eventa_attribute_float] float(4) NULL,
   [eventa_attribute_str] varchar(4000) NULL,
   [eventa_created_by] bigint NULL,
   [eventa_modified_by] bigint NULL,
   [eventa_create_date] date NULL,
   [eventa_modified_date] date NULL
)
 WITH IDENTITY_GAP = Eventa
go

CREATE INDEX [IDs] ON [Event_attributes] ([eventa_id],[eventa_attribute_name],[eventa_event_type],[eventa_event_id],[eventa_created_by])
go

ALTER TABLE [Event_attributes] ADD CONSTRAINT [Event_pk] PRIMARY KEY ([eventa_id])
go

ALTER TABLE [Event_attributes] ADD CONSTRAINT [eventa_id] UNIQUE CLUSTERED ([eventa_id])
go

-- Table Sample_attribute

CREATE TABLE [Sample_attributes]
(
   [sampla_id] bigint NOT NULL,
   [sampla_project_id] bigint NOT NULL,
   [sampla_attribute_name] varchar(40) NULL,
   [sampla_attribute_is_required] int NULL,
   [sampla_sample_id] bigint NOT NULL,
   [sampla_attribute_date] date NULL,
   [sampla_attribue_float] float(4) NULL,
   [sampla_attribue_str] varchar(4000) NULL,
   [sampla_created_by] bigint NULL,
   [sampla_modified_by] bigint NULL,
   [sampla_create_date] date NULL,
   [sampla_modified_date] date NULL
)
 WITH IDENTITY_GAP = sampla
go

CREATE INDEX [sampla information] ON [Sample_attributes] ([sampla_project_id],[sampla_id],[sampla_attribute_name],[sampla_sample_id],[sampla_attribute_date],[sampla_attribue_float],[sampla_attribue_str],[sampla_created_by],[sampla_create_date])
go

ALTER TABLE [Sample_attributes] ADD CONSTRAINT [Key2] PRIMARY KEY ([sampla_id])
go

ALTER TABLE [Sample_attributes] ADD CONSTRAINT [sampla_id] UNIQUE CLUSTERED ([sampla_id])
go

-- Table Project_attributes

CREATE TABLE [Project_attributes]
(
   [proja_id] bigint NOT NULL,
   [proja_project_id] bigint NOT NULL,
   [proja_attribute_name] varchar(40) NULL,
   [proja_attribute_data_date] date NULL,
   [proja_attribute_is_required] int NULL,
   [proja_attribute_data_str] varchar(4000) NULL,
   [proja_attribute_data_float] float(4) NULL,
   [proja_attribute_created_by] bigint NOT NULL,
   [proja_attribute_create_date] date NOT NULL,
   [proja_attribute_modified_by] bigint NULL,
   [proja_attribute_modified_date] date NULL
)
go

CREATE INDEX [IDs] ON [Project_attributes] ([proja_project_id],[proja_id],[proja_attribute_name])
go

ALTER TABLE [Project_attributes] ADD CONSTRAINT [project_attribute_pk] PRIMARY KEY ([proja_id])
go

ALTER TABLE [Project_attributes] ADD CONSTRAINT [proja_id] UNIQUE CLUSTERED ([proja_id])
go

-- Table Event

CREATE TABLE [Event]
(
   [event_id] bigint NOT NULL,
   [event_project_id] bigint NOT NULL,
   [event_type_lkuvl_id] bigint NULL,
   [event_created_by] bigint NOT NULL,
   [event_create_date] date NOT NULL,
   [event_modified_by] bigint NULL,
   [event_modified_date] date NULL,
   [event_status_lkuvl_id] bigint NULL
)
 WITH IDENTITY_GAP = Event
go

CREATE INDEX [IDs] ON [Event] ([event_id],[event_project_id],[event_created_by],[event_modified_by],[event_type_lkuvl_id],[event_status_lkuvl_id])
go

ALTER TABLE [Event] ADD CONSTRAINT [Event_pk] PRIMARY KEY ([event_id])
go

ALTER TABLE [Event] ADD CONSTRAINT [event_id] UNIQUE CLUSTERED ([event_id])
go

-- Table Sample

CREATE TABLE [Sample]
(
   [sample_id] bigint NOT NULL,
   [sample_project_id] bigint NOT NULL,
   [sample_name] varchar(255) NOT NULL,
   [sample_created_by] bigint NOT NULL,
   [sample_create_date] date NOT NULL,
   [sample_modified_by] bigint NULL,
   [sample_modified_date] date NULL
)
 WITH IDENTITY_GAP = sampla
go

CREATE INDEX [sample information] ON [Sample] ([sample_project_id],[sample_id],[sample_name],[sample_created_by],[sample_create_date])
go

ALTER TABLE [Sample] ADD CONSTRAINT [Key2] PRIMARY KEY ([sample_id])
go

ALTER TABLE [Sample] ADD CONSTRAINT [sample_id] UNIQUE CLUSTERED ([sample_id])
go

-- Table Lookup_Value

CREATE TABLE [Lookup_Value]
(
   [lkuvl_id] bigint NOT NULL,
   [lkuvl_name] varchar(40) NOT NULL,
   [lkuvl_value] varchar(255) NULL,
   [lkuvl_type] varchar(255) NULL
)
 WITH IDENTITY_GAP = lkuvl_
go

CREATE INDEX [lkuvl_information] ON [Lookup_Value] ([lkuvl_id],[lkuvl_name],[lkuvl_type])
go

ALTER TABLE [Lookup_Value] ADD CONSTRAINT [Key3] PRIMARY KEY ([lkuvl_id])
go

ALTER TABLE [Lookup_Value] ADD CONSTRAINT [klvl_name] UNIQUE CLUSTERED ([lkuvl_name])
go

ALTER TABLE [Lookup_Value] ADD CONSTRAINT [luvl_id] UNIQUE CLUSTERED ([lkuvl_id])
go

-- Table Actor

CREATE TABLE [Actor]
(
   [actor_id] char(10) NOT NULL,
   [actor_login] char(10) NULL,
   [actor_first_name] char(10) NULL,
   [actor_last_name] char(10) NULL,
   [actor_middle_name] char(10) NULL,
   [actor_email_address] char(10) NULL,
   [actor_employee_id] char(10) NULL,
   [actor_create_date] char(10) NULL,
   [actor_created_by] char(10) NULL,
   [actor_modify_date] char(10) NULL,
   [actor_modified_by] char(10) NULL,
   [actor_password] char(10) NULL,
   [actor_phone] char(10) NULL,
   [actor_dept_name] char(10) NULL
)
 WITH IDENTITY_GAP = actor_
go

CREATE INDEX [Actor Information] ON [Actor] ([actor_id],[actor_first_name],[actor_last_name],[actor_login],[actor_employee_id])
go

ALTER TABLE [Actor] ADD CONSTRAINT [Key4] PRIMARY KEY ([actor_id])
go

-- Table ActorGroup

CREATE TABLE [ActorGroup]
(
   [acgrp_id] char(10) NOT NULL,
   [acgrp_actor_id] char(10) NULL,
   [acgrp_create_date] char(10) NULL,
   [acgrp_modify_date] char(10) NULL,
   [acgrp_group_name_lkuvl_id] char(10) NULL
)
 WITH IDENTITY_GAP = acgrp_
go

CREATE INDEX [Grp_Actor_ids] ON [ActorGroup] ([acgrp_id],[acgrp_actor_id],[acgrp_group_name_lkuvl_id])
go

ALTER TABLE [ActorGroup] ADD CONSTRAINT [Key5] PRIMARY KEY ([acgrp_id])
go

-- Create relationships section ------------------------------------------------- 

ALTER TABLE [Project] ADD CONSTRAINT [project_parent_project_id_fk] FOREIGN KEY ([parent_project_id]) REFERENCES [Project] ([proje_id])
go

ALTER TABLE [Event_meta_attributes] ADD CONSTRAINT [event_project_id_fk] FOREIGN KEY ([evenma_project_id]) REFERENCES [Project] ([proje_id])
go

ALTER TABLE [Project_meta_attributes] ADD CONSTRAINT [projma_project_id_fk] FOREIGN KEY ([projma_project_id]) REFERENCES [Project] ([proje_id])
go

ALTER TABLE [Sample_meta_attributes] ADD CONSTRAINT [sampma_project_id] FOREIGN KEY ([sampma_project_id]) REFERENCES [Project] ([proje_id])
go

ALTER TABLE [Event_attributes] ADD CONSTRAINT [Eventa_event_id_fk] FOREIGN KEY ([eventa_event_id]) REFERENCES [Event] ([event_id])
go

ALTER TABLE [Project_attributes] ADD CONSTRAINT [Relationship11] FOREIGN KEY ([proja_project_id]) REFERENCES [Project] ([proje_id])
go

ALTER TABLE [Sample_attribute] ADD CONSTRAINT [sampla_projec_id_fk] FOREIGN KEY ([sampla_project_id]) REFERENCES [Project] ([proje_id])
go

ALTER TABLE [Event] ADD CONSTRAINT [event_projec_id_fk] FOREIGN KEY ([event_project_id]) REFERENCES [Project] ([proje_id])
go

ALTER TABLE [Sample] ADD CONSTRAINT [Relationship12] FOREIGN KEY ([sample_project_id]) REFERENCES [Project] ([proje_id])
go

ALTER TABLE [Sample_attribute] ADD CONSTRAINT [sampla_sample_id_fk] FOREIGN KEY ([sampla_sample_id]) REFERENCES [Sample] ([sample_id])
go

ALTER TABLE [ActorGroup] ADD CONSTRAINT [Relationship13] FOREIGN KEY ([acgrp_actor_id]) REFERENCES [Actor] ([actor_id])
go




