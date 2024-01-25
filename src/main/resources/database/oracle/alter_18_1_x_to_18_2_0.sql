
-- To-dos
alter table o_todo_task add t_origin_sub_title varchar(500);
alter table o_todo_task add column fk_collection number(20);

alter table o_todo_task add constraint todo_task_coll_idx foreign key (fk_collection) references o_todo_task (id);
create index idx_todo_task_coll_idx on o_todo_task(fk_collection);


-- Media
create table o_media_version_metadata (
   id number(20) generated always as identity,
   creationdate date not null,
   p_url varchar(1024) default null,
   p_width number(20) default null,
   p_height number(20) default null,
   p_length varchar(32) default null,
   p_format varchar(32) default null,
   primary key (id)
);

alter table o_media_version add column fk_version_metadata number(20);

alter table o_media_version add constraint media_version_version_metadata_idx foreign key (fk_version_metadata) references o_media_version_metadata(id);
create index idx_media_version_version_metadata_idx on o_media_version (fk_version_metadata);


-- Weighted score
alter table o_as_entry add a_weighted_score decimal default null;
alter table o_as_entry add a_score_scale decimal default null;
alter table o_as_entry add a_weighted_max_score decimal default null;

alter table o_as_eff_statement add weighted_score decimal default null;


-- Quality management
create table o_qual_generator_override (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   q_identifier varchar(512) not null,
   q_start date,
   q_generator_provider_key number(20),
   fk_generator number(20) not null,
   fk_data_collection number(20),
   primary key (id)
);

alter table o_qual_generator_override add constraint qual_override_to_gen_idx foreign key (fk_generator) references o_qual_generator (id);
create index idx_override_to_gen_idx on o_qual_generator_override(fk_generator);
alter table o_qual_generator_override add constraint qual_override_to_dc_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create index idx_override_to_dc_idx on o_qual_generator_override(fk_data_collection);
create index idx_override_ident_idx on o_qual_generator_override(q_identifier);


-- Gui Preferences
create table o_gui_prefs (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   fk_identity number(20) not null,
   g_pref_attributed_class varchar(512) not null,
   g_pref_key varchar(512) not null,
   g_pref_value clob not null,
   primary key (id)
);

alter table o_gui_prefs add constraint o_gui_prefs_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_o_gui_prefs_identity_idx on o_gui_prefs (fk_identity);
create index idx_o_gui_prefs_attrclass_idx on o_gui_prefs (g_pref_attributed_class);
create index idx_o_gui_prefs_key_idx on o_gui_prefs (g_pref_key);

-- Repository
alter table o_repositoryentry add runtime_type varchar(16);

-- Assessment inspection
create table o_as_inspection_configuration (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   a_name varchar(255),
   a_duration number(20) not null,
   a_overview_options varchar(1000),
   a_restrictaccessips number default 0 not null,
   a_ips varchar(32000),
   a_safeexambrowser number default 0 not null,
   a_safeexambrowserkey varchar(32000),
   a_safeexambrowserconfig_xml CLOB,
   a_safeexambrowserconfig_plist CLOB,
   a_safeexambrowserconfig_pkey varchar(255),
   a_safeexambrowserconfig_dload number default 1 not null,
   a_safeexambrowserhint CLOB,
   fk_entry number(20) not null,
   primary key (id)
);

alter table o_as_inspection_configuration add constraint as_insp_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_insp_to_repo_entry_idx on o_as_inspection_configuration (fk_entry);

create table o_as_inspection (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   a_subident varchar(512),
   a_from date not null,
   a_to date not null,
   a_extra_time number(20),
   a_access_code varchar(128),
   a_start_time date,
   a_end_time date,
   a_end_by varchar(16),
   a_effective_duration number(20),
   a_comment varchar(32000),
   a_status varchar(16) default 'published' not null,
   fk_identity number(20) not null,
   fk_configuration number(20) not null,
   primary key (id)
);

alter table o_as_inspection add constraint as_insp_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_as_insp_to_ident_idx on o_as_inspection (fk_identity);
alter table o_as_inspection add constraint as_insp_to_config_idx foreign key (fk_configuration) references o_as_inspection_configuration (id);
create index idx_as_insp_to_config_idx on o_as_inspection (fk_configuration);
create index idx_as_insp_subident_idx on o_as_inspection (a_subident);
create index idx_as_insp_endtime_idx on o_as_inspection (a_end_time);
create index idx_as_insp_fromto_idx on o_as_inspection (a_from,a_to);

create table o_as_inspection_log (
   id number(20) generated always as identity,
   creationdate date not null,
   a_action varchar(32) not null,
   a_before CLOB,
   a_after CLOB,
   fk_doer number(20),
   fk_inspection number(20) not null,
   primary key (id)
);

alter table o_as_inspection_log add constraint as_insp_log_to_ident_idx foreign key (fk_doer) references o_bs_identity (id);
create index idx_as_insp_log_to_ident_idx on o_as_inspection_log (fk_doer);
alter table o_as_inspection_log add constraint as_log_to_insp_idx foreign key (fk_inspection) references o_as_inspection (id);
create index idx_as_log_to_insp_idx on o_as_inspection_log (fk_inspection);
