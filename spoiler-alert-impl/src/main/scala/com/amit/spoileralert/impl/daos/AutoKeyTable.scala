package com.amit.spoileralert.impl.daos

object AutoKeyTable {

  private val tableName = ColumnFamilies.AutoKey

  val tableScript  = s"""
     CREATE TABLE IF NOT EXISTS ${tableName} (
        	model_context text,
        	model_name text,
        	key_number counter,
        	PRIMARY KEY (model_context, model_name)
         )
      """.stripMargin

}
