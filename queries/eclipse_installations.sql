CREATE OR REPLACE TEMPORARY VIEW eclipse_installs_view as
select to_char(a1.first_month, 'YYYY-MM') as "Period", a1.num_new_users as "New Installations in Period", sum(a2.num_new_users) as "Total Installations" from
(
select first_month, count(distinct uid) as num_new_users from (
select uid, date_trunc('month', min(week)) as first_month from user_by_weeks group by uid) first_reports
group by first_month
) 
a1
join
(
select first_month, count(distinct uid) as num_new_users from (
select uid, date_trunc('month', min(week)) as first_month from user_by_weeks group by uid) first_reports
group by first_month
) 
a2
on a2.first_month <= a1.first_month
where a1.first_month >= '2009.08.01'
group by a1.first_month, a1.num_new_users
order by a1.first_month;


drop table if exists eclipse_installations;
select * into eclipse_installations from eclipse_installs_view   
