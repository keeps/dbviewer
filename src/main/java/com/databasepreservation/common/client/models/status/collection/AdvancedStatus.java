/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"fixed", "picker"})
public class AdvancedStatus implements Serializable {

  private boolean fixed;
  private String picker;

  public AdvancedStatus() {
    this.fixed = true;
  }

  public boolean isFixed() {
    return fixed;
  }

  public void setFixed(boolean fixed) {
    this.fixed = fixed;
  }

  public String getPicker() {
    return picker;
  }

  public void setPicker(String picker) {
    this.picker = picker;
  }
}
