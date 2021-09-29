create table n3c_groups.group_raw(raw jsonb);

create view n3c_groups.google_group as
select
	raw->>'id' as id,
	raw->>'name' as name,
	raw->>'description' as description,
	raw->>'email' as email,
	(raw->>'directMembersCount')::int as directMembersCount
from n3c_groups.group_raw
;

create table n3c_groups.user_raw(raw jsonb);

create view n3c_groups.google_user as
select
	raw->>'id' as id,
	(raw->>'name')::jsonb->>'fullName' as full_name,
	(raw->>'name')::jsonb->>'familyName' as family_name,
	(raw->>'name')::jsonb->>'givenName' as given_name,
	raw->>'primaryEmail' as primary_email,
	(raw->>'creationTime')::timestamp as creation_time,
	(raw->>'lastLoginTime')::timestamp as last_login
from n3c_groups.google_user_raw
;

create view n3c_groups.google_user_email as
select
    id,
    rank,
    email->>'address' as email,
    email->>'type' as type,
    email->>'primary' as primary
from (
    select raw->>'id' as id,t.*
    from
        n3c_groups.google_user_raw
    cross join lateral
        jsonb_array_elements(((raw->>'emails')::jsonb)::jsonb) with ordinality as t(email,rank)
    ) as foo
;

create view n3c_groups.google_user_organization as
select
    id,
    rank,
    organization->>'department' as department,
    organization->>'domain' as domain,
    organization->>'name' as name,
    organization->>'customType' as type,
    organization->>'primary' as primary
from (
    select raw->>'id' as id,t.*
    from
        n3c_groups.google_user_raw
    cross join lateral
        jsonb_array_elements(((raw->>'organizations')::jsonb)::jsonb) with ordinality as t(organization,rank)
    ) as foo
;

create table n3c_groups.google_member_raw(id text, raw jsonb);

create view n3c_groups.google_member as
select
	id as gid,
	raw->>'id' as uid,
	raw->>'email' as email,
	raw->>'status' as status
from n3c_groups.google_member_raw
;

create table n3c_groups.member_error(email text);

create view n3c_groups.new_member as
select
	coalesce(case when gsuite_email = '' then null else gsuite_email end,email) as email,
	last_name||', '||first_name as name,
	created
from n3c_admin.registration
where lower(email) not in (select lower(email) from n3c_groups.google_member where gid='040ew0vw1p0o54r')
  and lower(gsuite_email) not in (select lower(email) from n3c_groups.google_member where gid='040ew0vw1p0o54r')
  and lower(email) not in (select lower(email) from n3c_groups.member_error)
  and lower(gsuite_email) not in (select lower(email) from n3c_groups.member_error)
  and email != ''
order by created
;
