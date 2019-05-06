create or replace view drive.person_staging as
select 
    row__::int as person_id,
    email_address,
    preferred_first_name,
    last_name,
    role_on_project,
    institution,
    github_handle_url
from drive.person;

create or replace view drive.role_staging as
    select 1 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__institutional_informatics__research_it__a as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 2 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__ctsa_data_sharing_governance_pathways_ as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 3 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__architecting_attribution_  as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 4 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__educational_resource_and_competency_harmo as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 5 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__health_open_terminology_fhir_server_ as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 6 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__personas_for_clinical_and_translational_s as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 7 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__reusable_data_best_practice_portal_ as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 8 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__the_biodata_club_kit__supporting_data_sci as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 9 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__data_quality_methods_and_tools_to_support as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 10 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__data_harmonization_ as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 11 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__loinc2hpo_semantic_phenotyping_tool_ as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 12 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__menrva__an_interdisciplinary_open_researc as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 13 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__patient_mortality_prediction_dream_challe as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 14 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__science_of_translational_science_research as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 15 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__sparc_in_the_cloud_for_ctsa_hubs_ as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 16 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__competitions_tool_for_ctsa_community_peer as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 17 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__open_source_clinical_enterprise_data_ware as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 18 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__secure_cloud_based_infrastructure_for_cts as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
union
    select 19 as project_id, person_id,role
    from (select row__::int as person_id, email_address,phase_ii_projects__nlp_systematic_review_ as role from drive.person) as foo
    where role is not null and role != '' and role != 'Not involved'
;

truncate google.person cascade;
truncate google.role cascade;

insert into google.person select person_id,email_address,preferred_first_name,last_name,role_on_project,institution,github_handle_url from drive.person_staging;
insert into google.role select project_id,person_id,role from drive.role_staging;
update google.role set role='Contributor' where role = 'Lead' and person_id not in (select person_id from google.person natural join google.cd2h_institution);
