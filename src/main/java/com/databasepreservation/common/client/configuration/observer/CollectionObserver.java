/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.configuration.observer;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CollectionObserver {
  private final List<ICollectionStatusObserver> observerList = new ArrayList<>();

  public void addObserver(ICollectionStatusObserver observer) {
    if (!this.observerList.contains(observer)) {
      this.observerList.add(observer);
    }
  }

  public void removeObserver(ICollectionStatusObserver observer) {
    this.observerList.remove(observer);
  }

  public void setCollectionStatus(CollectionStatus collection) {
    for (ICollectionStatusObserver observer : this.observerList) {
      observer.updateCollection(collection);
    }
  }
}
