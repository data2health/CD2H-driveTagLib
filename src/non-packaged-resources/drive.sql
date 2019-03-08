--
-- PostgreSQL database dump
--

-- Dumped from database version 11.1
-- Dumped by pg_dump version 11.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: drive; Type: SCHEMA; Schema: -; Owner: eichmann
--

CREATE SCHEMA drive;


ALTER SCHEMA drive OWNER TO eichmann;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: cd2h_people; Type: TABLE; Schema: drive; Owner: eichmann
--

CREATE TABLE drive.cd2h_people (
    "timestamp" text,
    email_address text,
    currently_a_contributor_to_cd2h_ boolean,
    preferred_first_name text,
    last_name text,
    url_of_ctsa__or_home_institution_if_not_a_ctsa_ text,
    role_on_project text,
    what_is_your_expertise_or_interest_relevant_to_cd2h text,
    assistant_email text,
    what_working_groups_are_you_interested_in__data_ text,
    what_working_groups_are_you_interested_in__sta_ text,
    what_working_groups_are_you_interested_in__pea_ text,
    what_working_groups_are_you_interested_in__edu_ text,
    phase_ii_projects__ctsa_data_sharing_governance_pathways_ text,
    phase_ii_projects__data_quality_methods_and_tools_to_support text,
    phase_ii_projects__harmonizing_clinical_data_models_and_buil text,
    phase_ii_projects__secure_cloud_based_infrastructure_for_cts text,
    phase_ii_projects__institutional_informatics__research_it__a text,
    phase_ii_projects__science_of_translational_science_research text,
    phase_ii_projects__health_open_terminology_fhir_server_ text,
    phase_ii_projects__open_source_clinical_enterprise_data_ware text,
    phase_ii_projects__reusable_data_best_practice_portal_ text,
    phase_ii_projects__competitions_tool_for_ctsa_community_peer text,
    phase_ii_projects__sparc_in_the_cloud_for_ctsa_hubs_ text,
    phase_ii_projects__loinc2hpo_semantic_phenotyping_tool_ text,
    phase_ii_projects__a_computable_representation_of_contributi text,
    phase_ii_projects__menrva__an_interdisciplinary_open_researc text,
    phase_ii_projects__personas_for_clinical_and_translational_s text,
    phase_ii_projects__patient_mortality_prediction_dream_challe text,
    phase_ii_projects__educational_resource_and_competency_harmo text,
    phase_ii_projects__the_biodata_club_kit__supporting_data_sci text,
    cd2h_operations_committee boolean,
    primary_correspondence_email text,
    valid_email_aliases text,
    github_handle_url text,
    author_first_name text,
    author_middle_name text,
    author_last_name text,
    google_docs_email text,
    orcid_id text,
    nih_era_commons_id text,
    comments text,
    profile_picture_or_avatar text,
    calendar_system text
);


ALTER TABLE drive.cd2h_people OWNER TO eichmann;

--
-- Name: document; Type: TABLE; Schema: drive; Owner: eichmann
--

CREATE TABLE drive.document (
    folder text,
    id text,
    name text,
    mime_type text,
    owner text,
    last_modifying_user text,
    created text,
    modified text
);


ALTER TABLE drive.document OWNER TO eichmann;

--
-- Name: hierarchy; Type: TABLE; Schema: drive; Owner: eichmann
--

CREATE TABLE drive.hierarchy (
    parent text,
    id text,
    name text,
    owner text,
    last_modifying_user text,
    created text,
    modified text
);


ALTER TABLE drive.hierarchy OWNER TO eichmann;

--
-- Name: master; Type: TABLE; Schema: drive; Owner: eichmann
--

CREATE TABLE drive.master (
    "timestamp" text,
    email_address text,
    preferred_first_name text,
    last_name text,
    url_of_ctsa__or_home_institution_if_not_a_ctsa_ text,
    role_on_project text,
    what_is_your_expertise_or_interest_relevant_to_cd2h text,
    percent_effort_on_the_project text,
    what_working_groups_are_you_interested_in__data_ text,
    what_working_groups_are_you_interested_in__sta_ text,
    what_working_groups_are_you_interested_in__pea_ text,
    what_working_groups_are_you_interested_in__edu_ text,
    phase_ii_projects__ctsa_data_sharing_governance_pathways_ text,
    phase_ii_projects__data_quality_methods_and_tools_to_support text,
    phase_ii_projects__harmonizing_clinical_data_models_and_buil text,
    phase_ii_projects__secure_cloud_based_infrastructure_for_cts text,
    phase_ii_projects__institutional_informatics__research_it__a text,
    phase_ii_projects__science_of_translational_science_research text,
    phase_ii_projects__health_open_terminology_fhir_server_ text,
    phase_ii_projects__open_source_clinical_enterprise_data_ware text,
    phase_ii_projects__reusable_data_best_practice_portal_ text,
    phase_ii_projects__competitions_tool_for_ctsa_community_peer text,
    phase_ii_projects__sparc_in_the_cloud_for_ctsa_hubs_ text,
    phase_ii_projects__loinc2hpo_semantic_phenotyping_tool_ text,
    phase_ii_projects__architecting_attribution_ text,
    phase_ii_projects__menrva__an_interdisciplinary_open_researc text,
    phase_ii_projects__personas_for_clinical_and_translational_s text,
    phase_ii_projects__patient_mortality_prediction_dream_challe text,
    phase_ii_projects__educational_resource_and_competency_harmo text,
    phase_ii_projects__the_biodata_club_kit__supporting_data_sci text,
    phase_ii_projects__nlp_systematic_review_ text,
    cd2h_operations_committee boolean,
    primary_correspondence_email text,
    google_docs_email text,
    valid_email_aliases text,
    github_handle_url text,
    orcid_id text,
    author_first_name text,
    author_middle_name text,
    author_last_name text,
    nih_era_commons_id text,
    comments text,
    profile_picture_or_avatar text,
    calendar_system text,
    contributor_type text,
    assistant_email text,
    all_aliases text,
    row__ integer,
    homepage text,
    institution__ text,
    github_handle_harmonized__ text,
    has_upper_case text,
    nonstandard_case text,
    proper_first text,
    proper_last text,
    proper_name_concat text,
    author_concat text,
    aliases__ text,
    same_for_both text,
    primary_communication_block_to_add text,
    gdocs_to_add text
);


ALTER TABLE drive.master OWNER TO eichmann;

--
-- Name: person; Type: TABLE; Schema: drive; Owner: eichmann
--

CREATE TABLE drive.person (
    "timestamp" text,
    email_address text,
    preferred_first_name text,
    last_name text,
    url_of_ctsa__or_home_institution_if_not_a_ctsa_ text,
    role_on_project text,
    what_is_your_expertise_or_interest_relevant_to_cd2h text,
    percent_effort_on_the_project text,
    what_working_groups_are_you_interested_in__data_ text,
    what_working_groups_are_you_interested_in__sta_ text,
    what_working_groups_are_you_interested_in__pea_ text,
    what_working_groups_are_you_interested_in__edu_ text,
    phase_ii_projects__ctsa_data_sharing_governance_pathways_ text,
    phase_ii_projects__data_quality_methods_and_tools_to_support text,
    phase_ii_projects__harmonizing_clinical_data_models_and_buil text,
    phase_ii_projects__secure_cloud_based_infrastructure_for_cts text,
    phase_ii_projects__institutional_informatics__research_it__a text,
    phase_ii_projects__science_of_translational_science_research text,
    phase_ii_projects__health_open_terminology_fhir_server_ text,
    phase_ii_projects__open_source_clinical_enterprise_data_ware text,
    phase_ii_projects__reusable_data_best_practice_portal_ text,
    phase_ii_projects__competitions_tool_for_ctsa_community_peer text,
    phase_ii_projects__sparc_in_the_cloud_for_ctsa_hubs_ text,
    phase_ii_projects__loinc2hpo_semantic_phenotyping_tool_ text,
    phase_ii_projects__architecting_attribution_ text,
    phase_ii_projects__menrva__an_interdisciplinary_open_researc text,
    phase_ii_projects__personas_for_clinical_and_translational_s text,
    phase_ii_projects__patient_mortality_prediction_dream_challe text,
    phase_ii_projects__educational_resource_and_competency_harmo text,
    phase_ii_projects__the_biodata_club_kit__supporting_data_sci text,
    phase_ii_projects__nlp_systematic_review_ text,
    cd2h_operations_committee boolean,
    primary_correspondence_email text,
    google_docs_email text,
    valid_email_aliases text,
    github_handle_url text,
    orcid_id text,
    author_first_name text,
    author_middle_name text,
    author_last_name text,
    nih_era_commons_id text,
    comments text,
    profile_picture_or_avatar text,
    calendar_system text,
    contributor_type text,
    assistant_email text,
    all_aliases text,
    row__ integer,
    homepage text,
    institution__ text,
    github_handle_harmonized__ text,
    has_upper_case text,
    nonstandard_case text,
    proper_first text,
    proper_last text,
    proper_name_concat text,
    author_concat text,
    aliases__ text,
    same_for_both text,
    primary_communication_block_to_add text,
    gdocs_to_add text
);


ALTER TABLE drive.person OWNER TO eichmann;

--
-- PostgreSQL database dump complete
--

create schema google;

create table google.working_group (tag text primary key, name text);
insert into google.working_group values('PEA','People, Expertise and Attribution');
insert into google.working_group values('DATA', 'Data');
insert into google.working_group values('STA', 'Software, Tools and Algorithms');
insert into google.working_group values('EDU', 'Education');
comment on table google.working_group is 'CD2H Working Group';
comment on column google.working_group.tag is 'Working Group acronym';
comment on column google.working_group.name is 'Working Group name';

create  view google.project as
select * from cd2h_phase2.proposal;
comment on view google.project is E'@primaryKey id\nCD2H Phase 2 Project';

create view google.person as
select
    email_address,
    preferred_first_name,
    last_name,
    role_on_project
from drive.person
;
comment on view google.person is E'@primaryKey email_address\nCD2H Participant';
comment on column google.person.email_address is 'email address';

--create view google.role as
insert into google.role
    select 1 as id, email_address,role
    from (select email_address,phase_ii_projects__institutional_informatics__research_it__a as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 2 as id, email_address,role
    from (select email_address,phase_ii_projects__ctsa_data_sharing_governance_pathways_ as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 3 as id, email_address,role
    from (select email_address,phase_ii_projects__architecting_attribution_  as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 4 as id, email_address,role
    from (select email_address,phase_ii_projects__educational_resource_and_competency_harmo as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 5 as id, email_address,role
    from (select email_address,phase_ii_projects__health_open_terminology_fhir_server_ as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 6 as id, email_address,role
    from (select email_address,phase_ii_projects__personas_for_clinical_and_translational_s as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 7 as id, email_address,role
    from (select email_address,phase_ii_projects__reusable_data_best_practice_portal_ as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 8 as id, email_address,role
    from (select email_address,phase_ii_projects__the_biodata_club_kit__supporting_data_sci as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 9 as id, email_address,role
    from (select email_address,phase_ii_projects__data_quality_methods_and_tools_to_support as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 10 as id, email_address,role
    from (select email_address,phase_ii_projects__harmonizing_clinical_data_models_and_buil as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 11 as id, email_address,role
    from (select email_address,phase_ii_projects__loinc2hpo_semantic_phenotyping_tool_ as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 12 as id, email_address,role
    from (select email_address,phase_ii_projects__menrva__an_interdisciplinary_open_researc as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 13 as id, email_address,role
    from (select email_address,phase_ii_projects__patient_mortality_prediction_dream_challe as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 14 as id, email_address,role
    from (select email_address,phase_ii_projects__science_of_translational_science_research as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 15 as id, email_address,role
    from (select email_address,phase_ii_projects__sparc_in_the_cloud_for_ctsa_hubs_ as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 16 as id, email_address,role
    from (select email_address,phase_ii_projects__competitions_tool_for_ctsa_community_peer as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 17 as id, email_address,role
    from (select email_address,phase_ii_projects__open_source_clinical_enterprise_data_ware as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 18 as id, email_address,role
    from (select email_address,phase_ii_projects__secure_cloud_based_infrastructure_for_cts as role from drive.person) as foo
    where role is not null and role != 'Not involved'
union
    select 19 as id, email_address,role
    from (select email_address,phase_ii_projects__nlp_systematic_review_ as role from drive.person) as foo
    where role is not null and role != 'Not involved'
;
	comment on view role is E'@primaryKey id,email_address\nParticipation by person on project';
	comment on column role.id is E'@foreignKey (id) references project (id)\nproject id';
	comment on column role.email_address is E'@foreignKey (email_address) references person\n@name email';
	comment on column role.role is 'Form of participation';


create table google.project as
select * from drive.project;
alter table google.project primary key id;

create table google.role (
    id int references project(id),
    email_address text references person(email_address),
    role text,
    primary key (id,email_address)
);



insert into person select email_address,preferred_first_name,last_name,role_on_project,institution__,github_handle_url from drive.person;
