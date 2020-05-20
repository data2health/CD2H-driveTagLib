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
