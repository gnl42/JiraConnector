CREATE OR REPLACE TEMPORARY VIEW monthly_usage_stats
AS
    SELECT
        first_month,
        uid,
        bool_or(atlassian) AS used_atlassian,
        bool_or(jira)      AS used_jira,
        bool_or(crucible)  AS used_crucible,
        bool_or(bamboo)    AS used_bamboo,
        bool_or(fisheye)   AS used_fisheye
    FROM
        (
            SELECT
                a.uid,
                first_month,
                atlassian,
                jira,
                crucible,
                bamboo,
                fisheye,
                first_week,
                week
            FROM
                (
                    SELECT
                        uid,
                        date_trunc('month', MIN(week)) AS first_month,
                        MIN(week)                      AS first_week
                    FROM
                        atlassian_by_weeks
                    GROUP BY
                        uid
                    ORDER BY
                        uid
                )
                a
            JOIN
                (
                    SELECT
                        uid,
                        atlassian,
                        jira,
                        crucible,
                        fisheye,
                        bamboo,
                        week
                    FROM
                        atlassian_by_weeks
                )
                b
            ON
                a.uid = b.uid
            AND date_trunc('month', b.week) = first_month
            ORDER BY
                a.uid
        )
        first_users_with_any_atlassian
    GROUP BY
        first_month,
        uid;

CREATE OR REPLACE TEMPORARY VIEW optinsvsoptouts
AS
    SELECT
        a.month,
        optins * 100 / (optins + optouts)  AS optins_per,
        optouts * 100 / (optins + optouts) AS optouts_per
    FROM
        (
            SELECT
                date_trunc('month', week) AS month,
                COUNT(DISTINCT uid)       AS optins
            FROM
                atlassian_by_weeks
            WHERE
                optin = true
            GROUP BY
                month
            ORDER BY
                month
        )
        a
    JOIN
        (
            SELECT
                date_trunc('month', week) AS month,
                COUNT(DISTINCT uid)       AS optouts
            FROM
                atlassian_by_weeks
            WHERE
                optout = true
            GROUP BY
                month
            ORDER BY
                month
        )
        b
    ON
        a.month = b.month;

CREATE OR REPLACE TEMPORARY VIEW eclipse_fun_view as       
SELECT
    TO_CHAR(a.first_month, 'YYYY-MM')                                          AS "Period",
--    (a.used_atlassian_feature + not_used_atlassian_feature) * 100 / optins_per AS
--    "New Users (Total)",
    a.used_atlassian_feature + not_used_atlassian_feature AS "New Users (Logged)",
    a.used_atlassian_feature * 100 / optins_per           AS "Used Any Feature (Total) - estimated",
    a.used_atlassian_feature                              AS "# Used Any Feature (Logged)",
    -- no point in showing this figure as it's exactly the same as for logged users
    --a.atl_users * 100 / optins_per * 100 / ((a.atl_users + b.non_atl_users) * 100 / optins_per)
    -- as "% Used Any Feature (Total or Logged)",
    TO_CHAR(used_atlassian_feature::FLOAT * 100 / (used_atlassian_feature +
    not_used_atlassian_feature), '990D9%') AS "% Used Any Feature (Logged or Total)",
    TO_CHAR(used_jira_feature::FLOAT * 100 / (used_atlassian_feature + not_used_atlassian_feature),
    '990D9%') AS "% Used JIRA Feature (Logged or Total)",
    TO_CHAR(used_crucible_feature::FLOAT * 100 / (used_atlassian_feature +
    not_used_atlassian_feature), '990D9%') AS "% Used Crucible Feature (Logged or Total)",
    TO_CHAR(used_bamboo_feature::FLOAT * 100 / (used_atlassian_feature + not_used_atlassian_feature
    ), '990D9%') AS "% Used Bamboo Feature (Logged or Total)",
    TO_CHAR(used_fisheye_feature::FLOAT * 100 / (used_atlassian_feature +
    not_used_atlassian_feature), '990D9%') AS "% Used FishEye Feature (Logged or Total)"
FROM
    (
        SELECT
            first_month,
            SUM(used_atlassian::INTEGER)            AS used_atlassian_feature,
            COUNT(*) - SUM(used_atlassian::INTEGER) AS not_used_atlassian_feature,
            SUM(used_jira::INTEGER)                 AS used_jira_feature,
            SUM(used_crucible::INTEGER)             AS used_crucible_feature,
            SUM(used_bamboo::INTEGER)               AS used_bamboo_feature,
            SUM(used_fisheye::INTEGER)              AS used_fisheye_feature
            --COUNT(*) AS atl_users
        FROM
            monthly_usage_stats
        GROUP BY
            first_month
    )
    a
JOIN optinsvsoptouts c
ON
    c.month = a.first_month
ORDER BY
    "Period";

drop table if exists eclipse_feature_usage_new;
select * into eclipse_feature_usage_new from eclipse_fun_view   
    
