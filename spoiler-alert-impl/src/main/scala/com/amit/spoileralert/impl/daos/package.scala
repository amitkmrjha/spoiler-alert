package com.amit.spoileralert.impl

import java.util

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.utils.UUIDs

package object daos {

  /*private[impl]*/ object ColumnFamilies {

    val SpoilerAlerts: String = "spoileralerts"
    val SpoilerAlertsByKey: String = "spoileralerts_by_key"
    val SpoilerAlertByUser: String = "spoileralerts_by_users_series"
    val SpoilerAlertBySeriesPercentage: String = "spoileralerts_by_series_percentage"
  }
}

/*


select * from spoiler_alert.messages;
select * from spoiler_alert.tag_write_progress;
select * from spoiler_alert.tag_views;
select * from spoiler_alert.spoileralerts;
select * from spoiler_alert.spoileralerts_by_key;
select * from spoiler_alert.spoileralerts_by_users_series;
select * from spoiler_alert.spoileralerts_by_series_percentage;

truncate spoiler_alert.auto_key;
truncate spoiler_alert.messages;
truncate spoiler_alert.metadata;
truncate spoiler_alert.offsetstore;
truncate spoiler_alert.snapshots;
truncate spoiler_alert.spoileralerts;
truncate spoiler_alert.spoileralerts_by_key;
truncate spoiler_alert.spoileralerts_by_users_series;
truncate spoiler_alert.spoileralerts_by_series_percentage;
truncate spoiler_alert.tag_scanning;
truncate spoiler_alert.tag_views;
truncate spoiler_alert.tag_write_progress;
 */
