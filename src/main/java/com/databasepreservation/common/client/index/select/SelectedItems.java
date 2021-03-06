/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index.select;

import java.io.Serializable;

import com.databasepreservation.common.client.index.IsIndexed;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@Type(value = SelectedItemsAll.class, name = "all"),
  @Type(value = SelectedItemsNone.class, name = "none"), @Type(value = SelectedItemsList.class, name = "list"),
  @Type(value = SelectedItemsFilter.class, name = "simpleFilterParameter")})
@FunctionalInterface
public interface SelectedItems<T extends IsIndexed> extends Serializable {
  String getSelectedClass();
}
