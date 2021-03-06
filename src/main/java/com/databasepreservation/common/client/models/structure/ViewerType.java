/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerType implements Serializable {
  public enum dbTypes {
    BINARY, CLOB, BOOLEAN, DATETIME, DATETIME_JUST_DATE, DATETIME_JUST_TIME, ENUMERATION, TIME_INTERVAL,
    NUMERIC_FLOATING_POINT, NUMERIC_INTEGER, STRING, COMPOSED_STRUCTURE, COMPOSED_ARRAY, NESTED
  }

  private String originalTypeName;
  private String description;
  private String typeName;
  private dbTypes dbType;

  public ViewerType() {
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public String getOriginalTypeName() {
    return originalTypeName;
  }

  public void setOriginalTypeName(String originalTypeName) {
    this.originalTypeName = originalTypeName;
  }

  public dbTypes getDbType() {
    return dbType;
  }

  public void setDbType(dbTypes dbType) {
    this.dbType = dbType;
  }

  @Override
  public String toString() {
    return "ViewerType{" + "dbType=" + dbType + ", originalTypeName='" + originalTypeName + '\'' + ", description='"
      + description + '\'' + ", typeName='" + typeName + '\'' + '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(originalTypeName, description, typeName, dbType);
  }
}
