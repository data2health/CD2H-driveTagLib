create table mapping as
select
    coalesce(substring(site from '(.*) \([^)]+\)$'),site) as site,
    substring(site from '.* \(([^),]+).*\)$') as city,
    substring(site from '.* \([^),]+, *(.*)\)$') as state,
    site as n3c,
    null as ncats
from site_tracking order by 1;

create table ncats as
select
    site_name,
    pi_poc_name,
    dta_sent::date,
    dta_executed::date,
    dua_sent::date,
    dua_executed::date,
    ctsa_non_ctsa
from sheet1
where site_name != 'Sent batch email to all the below'
  and site_name is not null;
  
create table n3c as
select
	site,
	substring(case_count from '[0-9]+')::int*1000 as case_count,
	data_network,
	target_reason,
	wave
from site_tracking;

create view dashboard as
select
	mapping.site,
	city,
	state,
	ncats.dta_sent,
	ncats.dta_executed,
	ncats.dua_sent,
	ncats.dua_executed,
	ctsa_non_ctsa,
	case_count,
	data_network,
	target_reason,
	wave
from mapping,ncats,n3c
where mapping.n3c=n3c.site
  and mapping.ncats=ncats.site_name;
  