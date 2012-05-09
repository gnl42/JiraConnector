create or replace temporary view optinsvsoptouts as 
select a.month, optins * 100 / (optins + optouts) as optins_per, optouts * 100 / (optins + optouts) as optouts_per from (
select date_trunc('month', week) as month, count(distinct uid) as optins from atlassian_by_weeks where optin = true group by month order by month
) a
join (
select date_trunc('month', week) as month, count(distinct uid) as optouts from atlassian_by_weeks where optout = true group by month order by month
) b
on a.month = b.month;

CREATE OR REPLACE TEMPORARY VIEW eclipse_fue_view as       
SELECT
    TO_CHAR(uaf.month, 'YYYY-MM')                                          AS "Period",
    users_reported_before                                                  AS "Existing Users (Logged ever)",
--    (used_atlassian_feature + not_used_atlassian_feature) * 100 / optins_per AS
--    "Existing Users (Total) ",
    used_atlassian_feature + not_used_atlassian_feature AS "Existing Users (Logged this month)",
    used_atlassian_feature * 100 / optins_per           AS "# Used Any Feature (Total) - estimated",
    used_atlassian_feature                              AS "# Used Any Feature (Logged)" ,
    -- this stat does not make sense with out approximation of total users as "% Used Any Feature (Total)",
    to_char(used_atlassian_feature::float * 100 / (used_atlassian_feature + not_used_atlassian_feature), '990D9%') AS "% Used Any Feature (Logged or Total)",
    to_char(used_jira_feature::float * 100 / (used_atlassian_feature + not_used_atlassian_feature), '990D9%') AS "% Used JIRA Feature (Logged or Total)",
    to_char(used_crucible_feature::float * 100 / (used_atlassian_feature + not_used_atlassian_feature), '990D9%') AS "% Used Crucible Feature (Logged or Total)",
    to_char(used_bamboo_feature::float * 100 / (used_atlassian_feature + not_used_atlassian_feature), '990D9%') AS "% Used Bamboo Feature (Logged or Total)",
    to_char(used_fisheye_feature::float * 100 / (used_atlassian_feature + not_used_atlassian_feature), '990D9%') AS "% Used FishEye Feature (Logged or Total)"
FROM
    (
        SELECT
            month,
            SUM(used_atlassian::integer) AS used_atlassian_feature,
            count(*) - sum(used_atlassian::integer) AS not_used_atlassian_feature,
            SUM(used_jira::integer) AS used_jira_feature,
            SUM(used_crucible::integer) AS used_crucible_feature,
            SUM(used_bamboo::integer) AS used_bamboo_feature,
            SUM(used_fisheye::integer) AS used_fisheye_feature
        FROM
            (
                SELECT
                    month,
                    uid,
                    bool_or(atlassian) AS used_atlassian,
                    bool_or(jira) AS used_jira,
                    bool_or(crucible) AS used_crucible,
                    bool_or(bamboo) AS used_bamboo,
                    bool_or(fisheye) AS used_fisheye
                FROM
                    (
                        SELECT
                            date_trunc('month', week) AS month,
                            uid,
                            atlassian,
                            jira,
                            crucible,
                            bamboo,
                            fisheye
                        FROM
                            atlassian_by_weeks
                        GROUP BY
                            month,
                            uid,
                            atlassian,
                            jira,
                            crucible,
                            bamboo,
                            fisheye
                        ORDER BY
                            month,
                            uid
                    )
                    a
                GROUP BY
                    month,
                    uid
                ORDER BY
                    month,
                    uid
            )
            b
        --WHERE
     --       used_atlassian = true
        GROUP BY
            month
    )
    uaf
JOIN optinsvsoptouts opt
ON
    opt.month = uaf.month
JOIN
    (
        SELECT
            month,
            COUNT (DISTINCT uid) AS users_reported_before
        FROM
            (
                SELECT
                    a.month AS month,
                    uid
                FROM
                    (
                        SELECT DISTINCT
                            date_trunc('month', week) AS month
                        FROM
                            atlassian_by_weeks
                        WHERE
                            week <= now()
                    )
                    a
                JOIN
                    (
                        SELECT
                            uid,
                            date_trunc('month', week) AS month
                        FROM
                            atlassian_by_weeks
                    )
                    b
                ON
                    b.month <= a.month
                ORDER BY
                    month,
                    uid
            )
            c
        GROUP BY
            month
        ORDER BY
            month
    )
    users_running_stats
ON
    opt.month = users_running_stats.month; 


drop table if exists eclipse_feature_usage_existing;
select * into eclipse_feature_usage_existing from eclipse_fue_view  